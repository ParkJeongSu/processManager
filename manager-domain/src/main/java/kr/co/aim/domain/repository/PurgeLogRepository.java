package kr.co.aim.domain.repository;
import kr.co.aim.common.condition.PurgeLogSearchCondition;
import kr.co.aim.domain.model.PurgeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface PurgeLogRepository {

    List<PurgeLog> findAll();

    PurgeLog save(PurgeLog purgeLog);

    Optional<PurgeLog> findByBatchIdAndTableName(String batchId, String tableName);

    Page<PurgeLog> findPurgeLogWithConditions(PurgeLogSearchCondition condition, Pageable pageable);

}
