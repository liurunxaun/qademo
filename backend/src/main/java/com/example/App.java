package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;



@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOrigins("*");
            }
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@RestController
@RequestMapping("/api")
class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "123";
    }
}

@RestController
@RequestMapping("/api")
class QAndAController {
    private final RestTemplate restTemplate;

    public QAndAController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/qa")
    public SseEmitter QAndA(@RequestParam String question) {
        SseEmitter emitter = new SseEmitter();

        try {
            // 构建 JSON 数据
            String jsonData = "{\"question\": \"" + question.replace("\"", "\\\"") + "\"}";
            System.out.println(jsonData);

            // 设置请求头 Content-Type 为 application/json
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 将 JSON 数据和头部一起放入 HttpEntity 中
            HttpEntity<String> entity = new HttpEntity<>(jsonData, headers);

            // 发送 POST 请求
            String pythonUrl = "http://10.43.108.62:5002/process-data";
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(pythonUrl, entity, String.class);
            String response = responseEntity.getBody();
            System.out.println(response);

            // 假设 Python 后端返回的 JSON 格式是正确的
            JSONObject jsonResponse = new JSONObject(response);
            // 获取 answer 字段，支持字符串或数组类型
            Object answer = jsonResponse.get("answer");
            System.out.println(answer);

            emitter.send(answer);

            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}

