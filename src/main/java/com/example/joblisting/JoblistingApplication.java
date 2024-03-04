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

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableSwagger2
public class JoblistingApplication {

    private static final RateLimiter rateLimiter = RateLimiter.create(10.0);
    private static final int REQUEST_THRESHOLD = 50;
    private static final int IP_TRACKING_THRESHOLD = 10;
    private static final long IP_TRACKING_WINDOW_MILLIS = 60000;
    private static final Map<String, Integer> ipRequestCountMap = new ConcurrentHashMap<>();
    private static int totalRequestCount = 0;

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
        try {
            System.out.println("Server IP address: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @RestController
    @RequestMapping("/api")
    public static class RateLimitController {

        @GetMapping("/your-endpoint")
        public ResponseEntity<String> yourEndpoint(HttpServletRequest request) {
            String clientIP = getClientIP(request);

            if (ipRateLimitExceeded(clientIP)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("IP rate limit exceeded");
            }

            if (rateLimiter.tryAcquire()) {
                incrementIPRequestCount(clientIP);
                totalRequestCount++;

                if (totalRequestCount > REQUEST_THRESHOLD) {
                    System.out.println("Potential DDoS attack detected!");
                    // Additional actions to handle the suspected DDoS attack can be added here
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Potential DDoS detected");
                }
                return ResponseEntity.ok("Request processed successfully");
            } else {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Global rate limit exceeded");
            }
        }

        private boolean ipRateLimitExceeded(String clientIP) {
            int ipRequestCount = ipRequestCountMap.getOrDefault(clientIP, 0);
            return ipRequestCount > IP_TRACKING_THRESHOLD;
        }

        private void incrementIPRequestCount(String clientIP) {
            ipRequestCountMap.merge(clientIP, 1, Integer::sum);
            ipRequestCountMap.entrySet().removeIf(entry ->
                    System.currentTimeMillis() - entry.getValue() > IP_TRACKING_WINDOW_MILLIS);
        }

        private String getClientIP(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
    }
}
