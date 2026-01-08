package kr.co.aim.api.service;

import kr.co.aim.api.jwt.JwtTokenProvider;
import kr.co.aim.common.dto.*;
import kr.co.aim.common.enums.EventName;
import kr.co.aim.common.error.ExcelValidationException;
import kr.co.aim.common.record.TransactionInfo;
import kr.co.aim.domain.command.UserCreateCommand;
import kr.co.aim.domain.command.UserUpdateCommand;
import kr.co.aim.domain.model.User;
import kr.co.aim.domain.repository.UserRepository;
import kr.co.aim.infra.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (DI)
public class UserService {

    private final UserRepository userRepository; // 구현체(Infra)가 아닌 인터페이스(Domain)에 의존
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findUsers(UserSearchConditionDto condition,Pageable pageable) {
        //1. Repository에서 Page<Entity>를 조회합니다.

        Page<UserResponseDto> page = userRepository.findUsersWithConditions(condition,pageable);

        return page;
    }

    @Transactional
    public void deleteUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return; // 삭제할 ID가 없으면 아무 작업도 하지 않음
        }
        // 여러 건을 삭제할 때는 이 메서드가 성능상 가장 효율적입니다.
        // DELETE ... WHERE id IN (...) 쿼리를 한 번에 실행합니다.
        userRepository.deleteAllByIdInBatch(ids);
    }

    /**
     * 사용자의 데이터를 변경합니다.
     * @param requestDto 사용자의 변경 데이터
     * @return 변경된 사용자 도메인 객체
     */
    @Transactional // 이 메소드가 하나의 트랜잭션으로 동작하도록 보장합니다.
    public User changeUser(Long userId, UserUpdateRequestDto requestDto) {
        // 1. Repository를 통해 Domain 객체를 가져온다.
        User user;
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()){
            user = optionalUser.get();
        }
        else {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + requestDto.getId());
        }
        String eventName = EventName.UPDATED.getValue();

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        TransactionInfo tx = TransactionInfo.now(eventName,requestDto.getEventUser(),requestDto.getEventComment());
        UserUpdateCommand command =
                UserUpdateCommand.builder()
                        .authorityId(requestDto.getAuthorityId())
                        .email(requestDto.getEmail())
                        .userName(requestDto.getUserName())
                        .phone1(requestDto.getPhone1())
                        .phone2(requestDto.getPhone2())
                        .password(encodedPassword)
                        .transactionInfo(tx)
                        .build();

        user.changeUser(command);

        return userRepository.save(user);
    }

    /**
     * 사용자의 데이터를 생성합니다.
     * @param requestDto 사용자의 생성 데이터
     * @return 생성된 사용자 도메인 객체
     */
    @Transactional // 이 메소드가 하나의 트랜잭션으로 동작하도록 보장합니다.
    public User createUser(UserCreateRequestDto requestDto) {
        // 1. Repository를 통해 Domain 객체를 가져온다.

        Optional<User> optionalUser = userRepository.findByUserId(requestDto.getUserId());
        if(optionalUser.isPresent()){
            throw new IllegalArgumentException("이미 생성된 사용자입니다. ID: " + requestDto.getUserId());
        }

        String eventName = EventName.UPDATED.getValue();
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        TransactionInfo tx = TransactionInfo.now(eventName,requestDto.getEventUser(),requestDto.getEventComment());
        UserCreateCommand command =
                UserCreateCommand.builder()
                        .userId(requestDto.getUserId())
                        .authorityId(requestDto.getAuthorityId())
                        .email(requestDto.getEmail())
                        .userName(requestDto.getUserName())
                        .phone1(requestDto.getPhone1())
                        .phone2(requestDto.getPhone2())
                        .password(encodedPassword)
                        .transactionInfo(tx)
                        .build();

        User user = User.create(command);
        return userRepository.save(user);
    }

    /**
     * 사용자의 데이터를 생성합니다.
     * @param requestDtoList 사용자의 생성 데이터
     * @return 생성된 사용자 도메인 객체
     */
    @Transactional
    public void createUsers(List<UserCreateRequestDto> requestDtoList) {

        if (requestDtoList == null || requestDtoList.isEmpty()) {
            return;
        }

        List<String> errorMessages = new ArrayList<>();
        for(UserCreateRequestDto userDto : requestDtoList ){
            Optional<User> optionalUser = userRepository.findByUserId(userDto.getUserId());
            if(optionalUser.isPresent()){
                errorMessages.add("이미 생성된 사용자입니다. ID: " + userDto.getUserId());
            }
            if(!StringUtils.hasText(userDto.getUserId())) {
                errorMessages.add("사용자 ID 가 비어있습니다.");
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new ExcelValidationException(errorMessages);
        }

        String eventName = EventName.UPDATED.getValue();
        LocalDateTime date = LocalDateTime.now();
        // [수정] 엔티티를 담을 리스트를 생성합니다.
        List<User> usersToSave = new ArrayList<>();
        for(UserCreateRequestDto requestDto : requestDtoList ){
            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

            TransactionInfo tx = TransactionInfo.now(eventName,requestDto.getEventUser(),requestDto.getEventComment(),date);
            UserCreateCommand command =
                    UserCreateCommand.builder()
                            .userId(requestDto.getUserId())
                            .authorityId(requestDto.getAuthorityId())
                            .email(requestDto.getEmail())
                            .userName(requestDto.getUserName())
                            .phone1(requestDto.getPhone1())
                            .phone2(requestDto.getPhone2())
                            .password(encodedPassword)
                            .transactionInfo(tx)
                            .build();
            User user = User.create(command);
            usersToSave.add(user);
        }
        userRepository.saveAll(usersToSave);
    }

    @Transactional
    public Long join(MemberJoinRequestDto memberJoinRequestDto) {
        // 이메일 중복 검사
        if (userRepository.findByUserId(memberJoinRequestDto.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 계정입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberJoinRequestDto.getPassword());

        // 회원 정보 생성
        User user = User.builder()
                .userId(memberJoinRequestDto.getUserId())
                .password(encodedPassword)
                .userName(memberJoinRequestDto.getUserName())
                .build();

        User savedUser = userRepository.save(user);
        return savedUser.getId();

    }

    @Transactional
    public TokenDto login(MemberLoginRequestDto memberLoginRequestDto) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberLoginRequestDto.getUserId(), memberLoginRequestDto.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        // ↓↓↓↓↓↓ Refresh Token 생성 로직 추가 ↓↓↓↓↓↓
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        Optional<User> optionalUser = userRepository.findByUserId(authentication.getName());
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.updateRefreshToken(refreshToken);
            userRepository.save(user);
        }
        else{
            throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다.");
        }

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 Member ID (authentication) 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
        Optional<User> optionalUser = userRepository.findByEmail(authentication.getName());
        String refreshToken;
        User user;
        if(optionalUser.isPresent()){
            user = optionalUser.get();
            refreshToken = user.getRefreshToken();
        }
        else {
            throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다.");
        }

        // 4. Refresh Token 일치하는지 검사
        if (refreshToken == null || !refreshToken.equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // 6. 저장소 정보 업데이트 (Refresh Token Rotation)
        user.updateRefreshToken(newRefreshToken);

        userRepository.save(user);

        // 7. 토큰 발급
        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


}