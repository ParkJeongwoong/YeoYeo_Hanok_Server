package com.yeoyeo.config;

import com.yeoyeo.adapter.filter.ApiAuthenticationFilter;
import com.yeoyeo.adapter.filter.CustomAuthFilter;
import com.yeoyeo.adapter.handler.LoginFailHandler;
import com.yeoyeo.adapter.handler.LoginSuccessHandler;
import com.yeoyeo.adapter.handler.LogoutSuccessHandler;
import com.yeoyeo.adapter.handler.WebAccessDeniedHandler;
import com.yeoyeo.adapter.handler.WebAuthenticationEntryPoint;
import com.yeoyeo.adapter.provider.CustomAuthenticationProvider;
import com.yeoyeo.application.admin.service.CustomPersistentTokenBasedRememberMeServices;
import com.yeoyeo.application.admin.service.CustomUserDetailsService;
import java.util.Arrays;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final static String REMEMBER_ME_KEY = "yeoyeoAdmin";

    // 권한이 없는 사용자 접근에 대한 handler
    @Autowired private WebAccessDeniedHandler webAccessDeniedHandler;
    // 인증되지 않은 사용자 접근에 대한 handler
    @Autowired private WebAuthenticationEntryPoint webAuthenticationEntryPoint;
    @Autowired private LoginSuccessHandler loginSuccessHandler;
    @Autowired private LoginFailHandler loginFailHandler;
    @Autowired private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired private DataSource dataSource;

    @Autowired private CustomUserDetailsService customUserDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(rememberMeAuthenticationProvider());
        authenticationManagerBuilder.authenticationProvider(new CustomAuthenticationProvider(REMEMBER_ME_KEY, customUserDetailsService));
        return authenticationManagerBuilder.build();
    }

    // 스프링 시큐리티 규칙
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable) // csrf 보안 설정 비활성화
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
//                .headers().frameOptions().sameOrigin() // 개발 중 h2-console 사용을 위한 설정
//                .and()

                .authorizeHttpRequests(requests->requests // 보호된 리소스 URI에 접근할 수 있는 권한 설정
//                    .requestMatchers("/admin/login", "/admin/logout", "/dateroom/**", "/calendar/**", "/guest/**","/payment/**","/reservation/**", "/login**").permitAll() // TEST
//                    .requestMatchers("/admin/reservation/list/**", "/admin/manage/**").permitAll() // TEST
//                    .requestMatchers("/admin/reservation/list/**").authenticated() // TEST
                    .requestMatchers("/admin/login").permitAll()
                    .requestMatchers("/admin/**", "/swagger-ui/**", "/adminManage.html", "/index.html", "/").authenticated() // 인증된 사용자만 접근 허용
                    .requestMatchers("/web/*", "/actuator/*").permitAll()
                    .anyRequest().permitAll()
                )

                .exceptionHandling(exception->exception // exception 처리 (인증/인가 실패에 따른 리다이렉트)
                        .accessDeniedHandler(webAccessDeniedHandler) // 권한이 없는 사용자 접근
                        .authenticationEntryPoint(webAuthenticationEntryPoint) // 인증되지 않은 사용자 접근
                )

                .rememberMe(rememberMe->rememberMe // rememberMe 설정
                        .rememberMeParameter("remember-me")
                        .rememberMeServices(rememberMeServices(tokenRepository()))
                        .key(REMEMBER_ME_KEY)
                        .tokenValiditySeconds(2592000)
                        .alwaysRemember(false)
                        .userDetailsService(customUserDetailsService)
                )

                .logout(logout->logout // 로그아웃 설정
                        .logoutUrl("/admin/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .deleteCookies("SESSION")
                )

                // 세션 관리
                .sessionManagement(session->session
                    .sessionFixation().migrateSession()
                    .maximumSessions(1)
                )

                // 사용자 인증 필터 적용
                .addFilterBefore(apiAuthenticationFilter(rememberMeServices(tokenRepository()), authenticationManager(http)), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rememberMeAuthenticationFilter(rememberMeServices(tokenRepository()), http), ApiAuthenticationFilter.class)
                ;

        return http.build();
    }

    @Bean
    public ApiAuthenticationFilter apiAuthenticationFilter(RememberMeServices rememberMeServices, AuthenticationManager authenticationManager) throws Exception {
        ApiAuthenticationFilter apiAuthenticationFilter = new ApiAuthenticationFilter(authenticationManager);
        apiAuthenticationFilter.setRememberMeServices(rememberMeServices); // rememberMeServices 설정
        apiAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler); // 로그인 성공 시 실행될 handler bean
        apiAuthenticationFilter.setAuthenticationFailureHandler(loginFailHandler); // 로그인 실패 시 실행될 handler bean
        apiAuthenticationFilter.afterPropertiesSet();
        return apiAuthenticationFilter;
    }

    public RememberMeAuthenticationFilter customAuthFilter(RememberMeServices rememberMeServices, AuthenticationManager authenticationManager) throws Exception {
        return new CustomAuthFilter(authenticationManager, rememberMeServices);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:8080", "http://localhost:3005", "http://3.35.98.5:8080/",
                "https://api.yeoyeo.co.kr", "https://admin.yeoyeo.co.kr", "https://api.yeoyeo.kr", "https://admin.yeoyeo.kr",
                "https://yeoyeo.co.kr", "https://www.yeoyeo.co.kr", "https://yeoyeo.kr", "https://www.yeoyeo.kr"));
        configuration.addAllowedMethod("*");
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        return repository;
    }

    @Bean
    public RememberMeServices rememberMeServices(PersistentTokenRepository tokenRepository) {
        CustomPersistentTokenBasedRememberMeServices services = new CustomPersistentTokenBasedRememberMeServices(REMEMBER_ME_KEY, customUserDetailsService, tokenRepository);
        services.setAlwaysRemember(false);
        services.setTokenValiditySeconds(60 * 60 * 24 * 30);
        services.setParameter("remember-me");
        return services;
    }

    @Bean
    public RememberMeAuthenticationFilter rememberMeAuthenticationFilter(RememberMeServices rememberMeServices, HttpSecurity http)
        throws Exception {
        return new RememberMeAuthenticationFilter(authenticationManager(http), rememberMeServices);
    }

    @Bean
    public RememberMeAuthenticationProvider rememberMeAuthenticationProvider() {
        return new RememberMeAuthenticationProvider(REMEMBER_ME_KEY);
    }

}
