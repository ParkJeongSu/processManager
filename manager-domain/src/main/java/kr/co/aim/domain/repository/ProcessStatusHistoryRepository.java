package kr.co.aim.domain.repository;
import kr.co.aim.common.dto.ProcessStatusHistoryConditionDto;
import kr.co.aim.common.dto.ProcessStatusHistoryResponseDto;
import kr.co.aim.domain.model.ProcessStatus;
import kr.co.aim.domain.model.ProcessStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 알람 저장소의 기능을 정의하는 인터페이스.
 * 애플리케이션의 다른 부분(서비스 계층 등)은 이 인터페이스에만 의존합니다.
 * 실제 구현 기술(JPA, JDBC 등)과는 완전히 분리됩니다.
 */
public interface ProcessStatusHistoryRepository {

    /**
     * 모든 프로세스를 찾습니다.
     * @return 모든 프로세스 info List
     */
    ProcessStatusHistory save(ProcessStatusHistory processStatus);

    Page<ProcessStatusHistoryResponseDto> findProcessStatusHistoryWithConditions(ProcessStatusHistoryConditionDto condition, Pageable pageable);
    

}
