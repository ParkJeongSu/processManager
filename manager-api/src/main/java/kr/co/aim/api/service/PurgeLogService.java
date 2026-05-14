package kr.co.aim.api.service;

import kr.co.aim.domain.command.PurgeLogCreateCommand;
import kr.co.aim.domain.model.PurgeLog;
import kr.co.aim.domain.repository.PurgeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurgeLogService {
    private final PurgeLogRepository purgeLogRepository;

    @Transactional
    public List<PurgeLog> findAll(){
        return purgeLogRepository.findAll();
    }

    @Transactional
    public PurgeLog save(PurgeLog purgeLog){
        return purgeLogRepository.save(purgeLog);
    }

    @Transactional
    public Optional<PurgeLog> findByBatchIdAndTableName(String batchId, String tableName){
        return purgeLogRepository.findByBatchIdAndTableName(batchId, tableName);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeLog(PurgeLogCreateCommand command){
        Optional<PurgeLog> optionalPurgeLog = findByBatchIdAndTableName(command.getBatchId(), command.getTableName());
        PurgeLog purgeLog = null;
        if(optionalPurgeLog.isPresent()){
            purgeLog = optionalPurgeLog.get();
            purgeLog.setEndDateTime(command.getEndDateTime());
            purgeLog.setDeleteCount(purgeLog.getDeleteCount() + command.getDeleteCount());
        }else {
            purgeLog = PurgeLog.create(command);
        }
        this.save(purgeLog);
    }

}
