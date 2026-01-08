package kr.co.aim.domain.repository;

import kr.co.aim.common.dto.UserResponseDto;
import kr.co.aim.common.dto.UserSearchConditionDto;
import kr.co.aim.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 저장소의 기능을 정의하는 인터페이스.
 * 애플리케이션의 다른 부분(서비스 계층 등)은 이 인터페이스에만 의존합니다.
 * 실제 구현 기술(JPA, JDBC 등)과는 완전히 분리됩니다.
 */
public interface UserRepository {
    /**
     * 사용자를 저장하거나 업데이트합니다.
     * @param user 저장할 사용자 도메인 객체
     * @return 저장된 사용자 도메인 객체 (ID 포함)
     */
    User save(User user);

    /**
     * ID로 사용자를 찾습니다.
     * @param id 사용자 ID
     * @return Optional<User>
     */
    Optional<User> findById(Long id);

    /**
     * 이메일로 사용자를 찾습니다.
     * @param email 사용자 이메일
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);

    /**
     * 모든 사용자를 찾습니다.
     * @return 모든 사용자 도메인 객체 리스트
     */
    List<User> findAll();

    /**
     * 이메일로 사용자를 찾습니다.
     * @param userId 사용자 이메일
     * @return Optional<User>
     */
    Optional<User> findByUserId(String userId);

    Page<User> findAll(Pageable pageable);

    void deleteAllByIdInBatch(List<Long>ids);

    Page<UserResponseDto> findUsersWithConditions(UserSearchConditionDto condition, Pageable pageable);

    List<User> saveAll(List<User> user);
}
