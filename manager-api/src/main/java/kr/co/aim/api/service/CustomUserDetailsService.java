package kr.co.aim.api.service;

import kr.co.aim.domain.model.User;
import kr.co.aim.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. userRepository 를 통해 Optional<User> 객체를 받아온다.
        Optional<User> optionalUser = userRepository.findByUserId(username);

        // 2. optionalMember 에 User 객체가 존재하는지 확인한다.
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // 3. User 객체가 존재하면 UserDetails 객체로 변환하여 반환한다.
            return createUserDetails(user);
        } else {
            // 4. User 객체가 없으면 예외를 발생시킨다.
            throw new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다.");
        }
    }

    // 해당하는 User 의 데이터가 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                //.roles("USER") // 우선 하드코딩으로 "USER" 권한 부여
                .build();
    }
}
