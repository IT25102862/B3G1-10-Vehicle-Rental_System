package com.sliit.vrs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadRootDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionInterceptor())
                // Pages that DO need a logged-in session.
                // Fleet/booking/returns/maintenance management = staff pages.
                // my-bookings/profile/book = logged-in customer pages.
                .addPathPatterns("/dashboard/**", "/admin/**", "/vehicles/**", "/reservations/**",
                        "/emergencies/**", "/returns/**", "/tripplans/**", "/maintenance/**",
                        "/drivers/**", "/my-bookings/**", "/profile/**", "/book/**", "/fuel/**")
                // Public pages: landing, catalog browsing, auth, static assets, uploaded photos
                .excludePathPatterns("/", "/catalog", "/catalog/**", "/login", "/register",
                        "/css/**", "/js/**", "/images/**", "/uploads/**", "/error");
    }

    // Serves uploaded vehicle/profile photos from an on-disk "uploads" folder
    // (outside the packaged jar) at the public URL "/uploads/**".
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadRootDir + "/");
    }
}
