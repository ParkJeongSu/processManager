package kr.co.aim.api.service;

import kr.co.aim.domain.model.PurgeConfig;
import kr.co.aim.domain.repository.PurgeConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    
}
