package kr.co.aim.api.service;

import kr.co.aim.api.dto.PurgeConfigCreateDto;
import kr.co.aim.api.dto.PurgeConfigUpdateDto;
import kr.co.aim.common.error.EntityNotFoundException;
import kr.co.aim.domain.command.PurgeConfigCreateCommand;
import kr.co.aim.domain.command.PurgeConfigUpdateCommand;
import kr.co.aim.domain.model.PurgeConfig;
import kr.co.aim.domain.repository.PurgeConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurgeConfigService {
    private final PurgeConfigRepository purgeConfigRepository;

    @Transactional
    public List<PurgeConfig> findAll(){
        return purgeConfigRepository.findAll();
    }

    @Transactional
    public PurgeConfig save(PurgeConfig purgeConfig){
        return purgeConfigRepository.save(purgeConfig);
    }

    @Transactional
    public void deleteAllByIdInBatch(List<Integer>ids){
        purgeConfigRepository.deleteAllByIdInBatch(ids);
    }

    @Transactional
    public PurgeConfig create(PurgeConfigCreateDto request){
        PurgeConfigCreateCommand command = PurgeConfigCreateDto.toPurgeConfigCreateCommand(request);
        PurgeConfig purgeConfig = PurgeConfig.create(command);
        return save(purgeConfig);
    }

    @Transactional
    public PurgeConfig update(PurgeConfigUpdateDto request){
        Optional<PurgeConfig> optionalPurgeConfig = purgeConfigRepository.findById(request.getId());
        PurgeConfig purgeConfig = null;
        if(optionalPurgeConfig.isPresent()){
            purgeConfig = optionalPurgeConfig.get();
        }else{
            throw new EntityNotFoundException("PurgeConfig not found");
        }
        PurgeConfigUpdateCommand command = PurgeConfigUpdateDto.toPurgeConfigUpdateCommand(request);
        purgeConfig.update(command);
        purgeConfig = save(purgeConfig);
        return purgeConfig;
    }


    
}
