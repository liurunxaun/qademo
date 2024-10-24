package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


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
                registry.addMapping("/api/**").allowedOrigins("http://localhost:8081");
            }
        };
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
    @GetMapping("/qa")
    public ResponseEntity<String> QAndA(@RequestParam String question){
        try {
            System.out.println("question: " + question);
            // 先从前端接收数据，准备发送的数据
            String jsonData = "{\"question\": \"" + question.replace("\"", "\\\"") + "\"}";


            // 再把问题传给python

            // 创建 HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // 创建 POST 请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5002/process-data"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                    .build();

            // 发送请求并接收响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 解析并处理 Python 返回的结果
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray answers = jsonResponse.getJSONArray("answer");

            System.out.println("answer1: " + answers + '\n');

            // 将答案传给前端
            return ResponseEntity.ok(answers.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }
}

