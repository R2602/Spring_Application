package com.example.joblisting;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class JoblistingApplication {

    // Rate limiting setup
    private static final RateLimiter rateLimiter = RateLimiter.create(10.0); // Adjust the rate limit as needed
    private static final int REQUEST_THRESHOLD = 50; // Adjust the threshold as needed
    private static int requestCount = 0;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class)).paths(PathSelectors.any())
                .build().apiInfo(apiInfo()).useDefaultResponseMessages(false);
    }

    @Bean
    public ApiInfo apiInfo() {
        final ApiInfoBuilder builder = new ApiInfoBuilder();
        return builder.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(JoblistingApplication.class, args);
    }

    // Rate limiting check before processing each request
    @RestController
    @RequestMapping("/api")
    public static class RateLimitController {

        @GetMapping("/your-endpoint")
        public ResponseEntity<String> yourEndpoint() {
            if (rateLimiter.tryAcquire()) {
                // Process the request
                requestCount++;
                if (requestCount > REQUEST_THRESHOLD) {
                    // DDoS detection: Block requests exceeding the threshold
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Potential DDoS detected");
                }
                return ResponseEntity.ok("Request processed successfully");
            } else {
                // Request exceeded rate limit
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded");
            }
        }
    }
}
