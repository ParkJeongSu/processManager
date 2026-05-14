package kr.co.aim.api.service;

import kr.co.aim.common.enums.DataType;
import kr.co.aim.common.enums.PurgeLogStatus;
import kr.co.aim.common.enums.SqlOperator;
import kr.co.aim.common.enums.YNState;
import kr.co.aim.domain.command.PurgeLogCreateCommand;
import kr.co.aim.domain.model.PurgeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurgeService {
    private final PurgeLogService purgeLogService;
    private final PurgeConfigService purgeConfigService;
    @Qualifier("mssqlJdbcTemplate")
    private final JdbcTemplate mssqlJdbcTemplate;

    public void purge() {

        // 유니크한 batch_id 생성
        // All PurgeConfg List 조회
        // purgeConfig List를 반복문을 돌면서
        // 데이터 purge
        // 데이터 purge 후 PurgeLog 에 기록
        // 만일 반복문을 돌면서 1000개의 데이터를 지우고 또 1000개를 지우면
        // 계속 누적됨 이때, batchId 와 tableName 으로 찾으면 됨


        // 1. 원하는 패턴 정의 (20260409150200000 형태를 위한 패턴)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        LocalDateTime now = LocalDateTime.now();
        // 2. LocalDateTime을 문자열로 변환
        String batchId = now.format(formatter);

        List<PurgeConfig> purgeConfigList = purgeConfigService.findAll();

        if(CollectionUtils.isEmpty(purgeConfigList)) {
            return;
        }

        for(PurgeConfig purgeConfig : purgeConfigList){
            if(StringUtils.equals(YNState.Y.getValue(),purgeConfig.getIsActive())){
                try {
                    executePurgeForTable(purgeConfig, batchId);
                } catch (Exception e) {
                    log.error("Failed to purge table: {}", purgeConfig.getTableName(), e);
                    // 개별 테이블 실패가 전체 루프를 멈추지 않도록 예외 처리
                }
            }

        }
    }

    private void executePurgeForTable(PurgeConfig config, String batchId) {
        int totalDeleted = 0;
        int loopCount = 0;
        int maxLoop = config.getMaxLoopCount() != null ? config.getMaxLoopCount() : 100;
        int batchSize = ObjectUtils.isEmpty(config.getBatchSize()) ? 100 : config.getBatchSize();


        // 1. 기준값 계산 (DataType이 DATE인 경우 예시)
        Object criteriaValue = calculateCriteriaValue(config);

        // 전체 경로(Full Path) 생성: [DB_NAME].[SCHEMA_NAME].[TABLE_NAME]
        String fullTableName = String.format("[%s].[%s].[%s]",
                config.getDbName(),
                config.getSchemaName(),
                config.getTableName());

        // 2. 쿼리 생성 (MSSQL TOP 활용)
        // 주의: 테이블명과 컬럼명은 SQL 파라미터로 바인딩이 안 되므로 직접 조립 (화이트리스트 체크 권장)
        String operatorSymbol = getOperatorSymbol(config.getOperator());
        String deleteSql = String.format(
                "DELETE TOP (%d) FROM %s WHERE %s %s ?",
                batchSize, fullTableName, config.getTargetColumnName(), operatorSymbol
        );

        while (loopCount < maxLoop) {
            LocalDateTime startTime = LocalDateTime.now();

            // 3. 삭제 실행
            int deletedInBatch = mssqlJdbcTemplate.update(deleteSql, criteriaValue);

            log.info("Purge start for table: {}, Query: {}, Criteria: {}",
                    config.getTableName(), deleteSql, criteriaValue);

            totalDeleted += deletedInBatch;
            loopCount++;

            // 4. 로그 기록 (PurgeLogService에서 REQUIRES_NEW로 별도 커밋됨)
            writeLog(config, batchId, startTime, deletedInBatch, PurgeLogStatus.SUCCESS.getValue(), null);

            // 5. 종료 조건
            if (deletedInBatch < batchSize) break;

            // 6. 지연 시간 (커넥션 반납 후 대기)

            if (ObjectUtils.isNotEmpty(config.getDelayMs()) && config.getDelayMs() > 0) {
                try {
                    Thread.sleep(config.getDelayMs());
                } catch (InterruptedException ignored) {

                }
            }else{
                // 기본적으로 100ms 쉬기
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {

                }
            }
        }
        log.info("Purge finished for table: {}. Total deleted: {} rows, Loops: {}",
                config.getTableName(), totalDeleted, loopCount);

        // 최종 완료 후 설정 테이블에 마지막 실행 시간 업데이트
        config.setLastRunTime(LocalDateTime.now());
        purgeConfigService.save(config);
    }

    private Object calculateCriteriaValue(PurgeConfig config) {
        if (StringUtils.equalsIgnoreCase(DataType.DATE.getValue(), config.getDataType())) {
            // COMP_VALUE에 저장된 숫자(예: 180)만큼 현재 날짜에서 뺌
            int days = Integer.parseInt(config.getCompValue());
            return LocalDateTime.now().minusDays(days);
        }
        return config.getCompValue(); // 그 외 타입은 문자열 그대로 반환
    }

    private String getOperatorSymbol(String operator) {

        if (StringUtils.equals(SqlOperator.LT.name(), operator)){
            return SqlOperator.LT.getValue();
        }
        if (StringUtils.equals(SqlOperator.LE.name(), operator)){
            return SqlOperator.LE.getValue();
        }
        if (StringUtils.equals(SqlOperator.GT.name(), operator)){
            return SqlOperator.GT.getValue();
        }
        if (StringUtils.equals(SqlOperator.GE.name(), operator)){
            return SqlOperator.GE.getValue();
        }
        if (StringUtils.equals(SqlOperator.EQ.name(), operator)){
            return SqlOperator.EQ.getValue();
        }
        return SqlOperator.LT.getValue();
    }

    private void writeLog(PurgeConfig config, String batchId, LocalDateTime start, int count, String status, String error) {
        PurgeLogCreateCommand command = PurgeLogCreateCommand.builder()
                .purgeConfigId(config.getId())
                .batchId(batchId)
                .tableName(config.getTableName())
                .startDateTime(start)
                .endDateTime(LocalDateTime.now())
                .deleteCount(count)
                .status(status)
                .errorMsg(error)
                .build();
        purgeLogService.writeLog(command);
    }
}
