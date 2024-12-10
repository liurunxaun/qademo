package com.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuestionController {

    @Autowired
    private PythonWebSocketService pythonService;

    @PostMapping("/question")
    public String receiveQuestion(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        pythonService.sendQuestionToPython(question);
        return "Question sent to Python";
    }
}

