package com.uruz.frostbite.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uruz.frostbite.security.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.Provider;
import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

    @Bean
    @SuppressWarnings("deprecation")
    public SecurityFilterChain securityFilterChain(HttpSecurity security,
                                                   @Autowired CustomUsernamePasswordAuthenticationFilter filter,
                                                   @Autowired CustomAuthenticationEntryPoint customEntryPoint,
                                                   @Autowired CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception{

        security.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/api/user/").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/login").permitAll()
                .anyRequest().authenticated())
                // 세션베이스에서는 CSRF 활성화 필요 (쿠키 베이스 공격이므로)
                .csrf(csrf -> csrf
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .csrfTokenRepository(customCsrfTokenRepository())
                        .ignoringRequestMatchers(
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/v1/api/user/"),
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/v1/login"),
                                PathPatternRequestMatcher.withDefaults().matcher(null, "/swagger-ui.html"),
                                PathPatternRequestMatcher.withDefaults().matcher(null, "/swagger-ui/**"),
                                PathPatternRequestMatcher.withDefaults().matcher(null, "/v3/api-docs/**"),
                                PathPatternRequestMatcher.withDefaults().matcher(null, "/swagger-resources/**"),
                                PathPatternRequestMatcher.withDefaults().matcher(null, "/webjars/**")
                        )
                )
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .exceptionHandling(ex -> {
                    // 인증 안된상태에서 보호자원 접근 시, 최초대응
                    ex.authenticationEntryPoint(customEntryPoint);
                    // 로그인은 했으나 권한부족시 튕겨낼 Handler
                    ex.accessDeniedHandler(customAccessDeniedHandler);
                })
                // 과도기(session base API), 추후 JWT시 stateless로 전환필요
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                })

                .addFilterAt(filter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }

    // Swagger-UI 쓸때 CSRF-TOKEN 검증 실패하는 현상을 아래와 같이 처리했다.
    //20260109 19:42:58.955 [http-nio-9514-exec-6] TRACE o.s.s.w.c.CsrfTokenRequestAttributeHandler - Wrote a CSRF token to the following request attributes: [_csrf, org.springframework.security.web.csrf.CsrfToken]
    //20260109 19:42:58.955 [http-nio-9514-exec-6] TRACE o.s.s.w.c.XorCsrfTokenRequestAttributeHandler - Not returning the CSRF token since its Base64-decoded length (27) is not equal to (72)

    // 위의 로그로 다시 검색해서 공부할 필요 있음
    @Bean
    public CookieCsrfTokenRepository customCsrfTokenRepository(){
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieName("XSRF-TOKEN");      // 쿠키명에 맞춰야함
        repo.setHeaderName("X-XSRF-TOKEN");    // Swagger에서 보낼 헤더명에 맞춰야함
        //repo.setParameterName("_csrf");

        return repo;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 프리플라이트 허용 URL
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // allowedOrigins는 "정확한 Origin"만 허용해야 함.
        // - Origin은 (scheme + host + port) 조합이라서 포트가 다르면 다른 Origin임.
        // - allowCredentials(true)와 "*"는 CORS 스펙상 같이 쓸 수 없음.
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // FE dev server
                "http://localhost:5173",    // Vite dev server 등
                "http://localhost:9410"     // swagger-ui 사용 용 자기자신

        ));
        // 브라우저에게 아래 리스트 내의 헤더를 '허용' 함을 알려줌
        // 브라우저가 "이 헤더 붙여서 보낼 건데 OK?"라고 프리플라이트로 물어봄.
        // 여기 없으면 브라우저가 실제 요청을 보내기 전에 차단함.
        config.setAllowedHeaders(List.of(
                "Content-Type",    // application/json 요청 시 필요
                "X-XSRF-TOKEN",    // CSRF 헤더 (CookieCsrfTokenRepository 기본 조합)
                "Authorization",   // JWT/Ext API 등에서 필요할 수 있음
                "Accept"
        ));

        // 과도기(Session 베이스 API 통신)에 필요한 값들 (브라우저로 하여금 credential 관련 쿠키 보낼것을 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 전체 엔드포인트에 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // customDetailsService 등록
    // 내가 작성한 detailsService를 쓰게하려면 Provider에 detailsService를 등록하고, 이 Provider를 ProviderManager에 등록해야함
    // AuthenticationProvider 자체를 다시만들면 AdditionalCheck mitigatingAttackCheck등을 다시만들어야하니 구성요소만 등록하도록 구성
    @Bean
    public AuthenticationManager authenticationManager(@Autowired  BCryptPasswordEncoder bCryptPasswordEncoder,
                                                       @Autowired CustomUserDetailsService customUserDetailsService,
                                                        @Autowired AuthenticationEventPublisher authenticationEventPublisher){
        // DB에서 계정을 가져오는 방법을 정의한 UserDetailsService를 주입
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder);

        ProviderManager manager = new ProviderManager(provider);
        // 인증 시 이벤트 로깅할 주체를 등록한다.
        manager.setAuthenticationEventPublisher(authenticationEventPublisher);

        return manager;
    }

    // 만약 JWT로 전환한다면, 요청마다 JWT를 검증하는 Filter를 만들어서 이 위치에 등록해버리면 됨.
    // 단, JWT의 경우 AbstractAuthenticationProcessingFilter 보단 oncePerRequestFilter가 더 적합하다.
    // UsernamePasswordAuthenticationFilter 는 특정 URL에 1번 동작하는 성격이지만, JWT는 매 요청마다 검증해야 하기 때문
    @Bean
    public CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter(
            @Autowired AuthenticationManager authenticationManager,
            @Autowired CustomAuthenticationFailureHandler customFailureHandler,
            @Autowired CustomAuthenticationSuccessHandler customSuccessHandler,
            @Autowired ObjectMapper mapper
            ){
        CustomUsernamePasswordAuthenticationFilter filter = new CustomUsernamePasswordAuthenticationFilter(mapper, authenticationManager,
                new HttpSessionSecurityContextRepository());
        filter.setUsernameParameter("email");
        filter.setFilterProcessesUrl("/v1/login");

        // 로그인 성공 및 실패 동작을 제어해야한다. 안하면 reidrect 당함
        filter.setAuthenticationSuccessHandler(customSuccessHandler);
        filter.setAuthenticationFailureHandler(customFailureHandler);
        return filter;
    }

    // 이벤트리스너 붙이려면 publisher bean 등록 필요
    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

        return bcrypt;
    }
}
