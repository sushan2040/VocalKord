package com.language.detector;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Cors implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
	registry.addMapping("/**")
	.allowedOrigins("http://localhost:3000",
	"http://31.97.110.118:90",
	"http://31.97.110.118:3001",
	"http://31.97.110.118:3001/ecommerce",
	 "http:31.97.110.118:90")
	.allowedMethods("GET", "POST", "PUT", "DELETE")
	.allowedHeaders("*")
	.allowCredentials(true)
	.maxAge(3600);
	}
}
