package com.yhp.studybbs.configs;

import com.yhp.studybbs.interceptors.CommonInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.commonInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/resources/**")
                .excludePathPatterns("/user/resources/**")
                .excludePathPatterns("/article/resources/**");
    }
    @Bean
    public CommonInterceptor commonInterceptor(){ //빈으로 등록시키면 클래스 이름이 같은 녀석의 객체의 선언부를 초기화시킨다.
        return new CommonInterceptor();
    }
}
