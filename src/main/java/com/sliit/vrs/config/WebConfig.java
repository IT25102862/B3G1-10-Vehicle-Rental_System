package com.sliit.vrs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionInterceptor())
                // Pages that DO need login protection
                .addPathPatterns("/dashboard/**", "/vehicles/**", "/reservations/**",
                        "/emergencies/**", "/returns/**", "/tripplans/**", "/maintenance/**",
                        "/drivers/**")
                // Public pages stay open
                .excludePathPatterns("/", "/login", "/register", "/css/**", "/js/**");
    }
}
