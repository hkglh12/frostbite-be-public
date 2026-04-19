package com.uruz.frostbite.security;

import com.uruz.frostbite.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import com.uruz.frostbite.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
// 데이터베이스로부터 "요청받은 인증" 비교에 사용할 객체를 생산하는 역할을 담당
// 이렇게 유저를 조회해오는 DetailsService는 *AuthenticationProvider에 등록되어야 한다.
// 전체에서 클래스찾기 : CTRL + N
// 구현체찾기 : CTRL+ALT+B(구현체이동), CTRL + H (구현체 트리찾기), ALT + F7 (사용처찾기)
// CF : 콜스택찾기 : CTRL + ALT + H
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Autowired
    public CustomUserDetailsService(UserService userService){
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userService.loadUserByEmail(email);

        if(user == null){
            throw new UsernameNotFoundException("USER NOT FOUND");
        }
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        return userDetails;
    }



}
