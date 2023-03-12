package com.yeoyeo.config;

import com.yeoyeo.adapter.filter.CustomAuthenticationFilter;
import com.yeoyeo.adapter.handler.*;
import com.yeoyeo.application.admin.auth.CustomAuthenticationProvider;
import com.yeoyeo.application.admin.repository.AdministratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // Admin repository
    @Autowired
    private AdministratorRepository adminRepository;
    // 권한이 없는 사용자 접근에 대한 handler
    @Autowired private WebAccessDeniedHandler webAccessDeniedHandler;
    // 인증되지 않은 사용자 접근에 대한 handler
    @Autowired private WebAuthenticationEntryPoint webAuthenticationEntryPoint;
    @Autowired private ImplLogoutSuccessHandler logoutSuccessHandler;

    // 스프링 시큐리티가 사용자를 인증하는 방법이 담긴 객체
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
        authenticationManagerBuilder.authenticationProvider(new CustomAuthenticationProvider(adminRepository));
    }

    // 스프링 시큐리티 규칙
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() // csrf 보안 설정 비활성화
                .headers()
                .frameOptions().sameOrigin()
                .and()

                .authorizeRequests() // 보호된 리소스 URI에 접근할 수 있는 권한 설정
                .antMatchers("/admin/signup").permitAll()
                .antMatchers("/admin/login").permitAll()
                .antMatchers("/admin/logout").permitAll()
                .antMatchers("/admin/**").authenticated() // 인증된 사용자만 접근 허용
                .antMatchers("/index.html").authenticated() // testtest
                .anyRequest().permitAll()
                .and()

//                 exception 처리 (인증/인가 실패에 따른 리다이렉트)
                .exceptionHandling()
                .accessDeniedHandler(webAccessDeniedHandler) // 권한이 없는 사용자 접근 시
                .authenticationEntryPoint(webAuthenticationEntryPoint) // 인증되지 않은 사용자 접근 시
                .and()

                .formLogin()
                .loginProcessingUrl("/admin/login")
                .usernameParameter("userId")
                .passwordParameter("userPassword")
                .and()

                .logout()
                .logoutUrl("/admin/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .deleteCookies("SESSION")
                .and()

                // 사용자 인증 필터 적용;
                .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // 세션 관리
                .sessionManagement()
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                ;

    }

    /*
     * customLoginSuccessHandler를 CustomAuthenticationFilter의 인증 성공 핸들러로 추가
     * 로그인 성공 시 /user/login 로그인 url을 체크하고 인증 토큰 발급
     */
    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager());
        customAuthenticationFilter.setFilterProcessesUrl("/admin/login");
        customAuthenticationFilter.setUsernameParameter("userId");
        customAuthenticationFilter.setPasswordParameter("userPassword");
        customAuthenticationFilter.setAuthenticationSuccessHandler(new CustomLoginSuccessHandler()); // 로그인 성공 시 실행될 handler bean
        customAuthenticationFilter.setAuthenticationFailureHandler(new CustomLoginFailHandler()); // 로그인 실패 시 실행될 handler bean
        customAuthenticationFilter.afterPropertiesSet();
        return customAuthenticationFilter;
    }

}