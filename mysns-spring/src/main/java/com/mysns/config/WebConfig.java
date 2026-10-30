package com.mysns.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mysns.service.FileStorageService;

/** 업로드된 이미지를 /images/** 로 서빙한다. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    public WebConfig(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = fileStorageService.getImagesDir().toUri().toString();
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location.endsWith("/") ? location : location + "/");
    }
}
