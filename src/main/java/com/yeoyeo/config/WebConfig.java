//package com.yeoyeo.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.session.web.http.CookieSerializer;
//import org.springframework.session.web.http.DefaultCookieSerializer;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:8080", "http://localhost:3005", "http://3.35.98.5:8080/", "https://yeoyeo.co.kr", "https://www.yeoyeo.co.kr");
//    }
////
////    @Bean
////    public CookieSerializer cookieSerializer() {
////        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
////        serializer.setSameSite("none");
////        serializer.setUseSecureCookie(true);
////        return serializer;
////    }
//
//}
