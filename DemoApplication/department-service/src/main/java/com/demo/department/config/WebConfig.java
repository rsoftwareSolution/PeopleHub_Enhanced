package com.demo.department.config;

import com.demo.department.security.JwtFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@Configuration
public class WebConfig {
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter filter) {
        FilterRegistrationBean<JwtFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(1);
        return bean;
    }
}
