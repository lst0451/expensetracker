package com.example.expensetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String H2_CONSOLE_PATH = "/h2-console/**";
    private static final String API_PATH = "/api/**";
    private static final String EXPENSE_API_PATH = "/api/expenses/**";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors() // 使用WebConfig中的CORS配置
                .and()
            .csrf()
                .disable() // 禁用CSRF保护
            .headers()
                .frameOptions().sameOrigin() // 允许H2控制台的框架同源加载
                .and()
            .authorizeRequests(authorize -> authorize
                .antMatchers(H2_CONSOLE_PATH).permitAll() // 允许所有用户访问H2控制台
                .antMatchers(HttpMethod.OPTIONS, API_PATH).permitAll() // 允许CORS预检请求
                .antMatchers(EXPENSE_API_PATH).authenticated() // 要求认证才能访问费用API
                .anyRequest().authenticated()) // 所有其他请求都需要认证
            .httpBasic() // 使用HTTP基本认证
                .and()
            .build();
    }
}