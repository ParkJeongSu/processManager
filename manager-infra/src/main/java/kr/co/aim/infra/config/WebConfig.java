package kr.co.aim.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins("http://localhost:5173") // 이 출처의 요청을 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 실제 파일(js, css, 이미지)이 들어있는 assets 폴더를 최우선으로 찾게 합니다.
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 1단계 경로 대응 (예: /login, /dashboard)
        registry.addViewController("/{p1:[^\\.]*}")
                .setViewName("forward:/index.html");

        // 2단계 경로 대응 (예: /order/list) - 변수명을 p1, p2로 다르게 설정
        registry.addViewController("/{p1:[^\\.]*}/{p2:[^\\.]*}")
                .setViewName("forward:/index.html");

        // 3단계 경로 대응 (예: /order/detail/1)
        registry.addViewController("/{p1:[^\\.]*}/{p2:[^\\.]*}/{p3:[^\\.]*}")
                .setViewName("forward:/index.html");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

        // 1페이지부터 시작하도록 설정 (0-indexed를 1-indexed로 변경)
        resolver.setOneIndexedParameters(true);

        // 페이지 사이즈 제한 등 추가 설정이 필요하다면 여기서 가능합니다.
        // resolver.setMaxPageSize(2000);

        resolvers.add(resolver);
    }
}