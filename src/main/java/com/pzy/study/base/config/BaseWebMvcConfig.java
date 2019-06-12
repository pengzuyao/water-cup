package com.pzy.study.base.config;

import com.pzy.study.base.commons.interceptors.HttpInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Destription:
 * Author: pengzuyao
 * Time: 2019-06-08
 */
@Configuration
public class BaseWebMvcConfig extends WebMvcConfigurationSupport {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        super.addResourceHandlers(registry);
    }

    @Bean
    public HttpInterceptor httpInterceptor(){
        return new HttpInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpInterceptor()).
                addPathPatterns("/acl/**").
                addPathPatterns("/aclModule/**").
                addPathPatterns("/dept/**").
                addPathPatterns("/log/**").
                addPathPatterns("/role/**").
                addPathPatterns("/user/**");
        super.addInterceptors(registry);
    }
}