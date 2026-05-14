package kr.co.aim.domain.repository;
import kr.co.aim.domain.model.PurgeConfig;
import kr.co.aim.domain.model.PurgeLog;

import java.util.List;
import java.util.Optional;


public interface PurgeLogRepository {

    List<PurgeLog> findAll();

    PurgeLog save(PurgeLog purgeLog);

    Optional<PurgeLog> findByBatchIdAndTableName(String batchId, String tableName);

}
