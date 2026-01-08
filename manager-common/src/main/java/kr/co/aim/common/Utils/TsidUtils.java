package kr.co.aim.common.Utils;

import com.github.f4b6a3.tsid.TsidCreator;
import com.github.f4b6a3.tsid.TsidFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom; // [1] 이거 import

@Slf4j
public class TsidUtils{

    private static final TsidFactory FACTORY;

    static {
        // [2] 0 ~ 1023 사이의 숫자를 랜덤으로 뽑습니다. (비싼 SecureRandom 안 쓰고 가벼운 거 씀)
        int randomNodeId = ThreadLocalRandom.current().nextInt(0, 1024);

        FACTORY = TsidFactory.builder()
                .withNodeBits(10)      // 10비트 사용 (기본값)
                .withNode(randomNodeId) // [3] 뽑은 랜덤 숫자를 Node ID로 박아버림
                .build();

        // (선택) 로그 한 번 찍어주면 나중에 디버깅할 때 편합니다.
        log.info("TsidUtils Initialized with Node ID: " + randomNodeId);
        //System.out.println("TsidUtils Initialized with Node ID: " + randomNodeId);
    }

    public static Long nextId() {
        return FACTORY.create().toLong();
//        return TsidCreator.getTsid().toLong();
    }
}
