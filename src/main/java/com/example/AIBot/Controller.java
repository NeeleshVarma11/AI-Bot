package com.example.AIBot;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Controller {

    private final RestTemplate restTemplate;
    private final String model;
    private final String apiURL;

    @Autowired
    public Controller(RestTemplate restTemplate,
                      @Value("${openai.model}") String model,
                      @Value("${openai.api.url}") String apiURL) {
        this.restTemplate = restTemplate;
        this.model = model;
        this.apiURL = apiURL;
    }

    @PostMapping("/send")
    public void sendSmss(String message) {
        String to = "whatsapp:+918099667733";
        String from = "whatsapp:+14155238886";
        Message message1 = Message.creator(
            new PhoneNumber(to),
            new PhoneNumber(from),
            message).create();
    }

    @RequestMapping("/receivexml")
    public void handleSms(@RequestParam(name = "Body", required = false) String incomingMessage) {
        System.out.println(incomingMessage);
        String response = chat(incomingMessage);
    }

   

    @RequestMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt) {
        // Creating the message map
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        // Creating the request body map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", Collections.singletonList(message));

        // Setting up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Creating the HTTP entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Sending the request
            ResponseEntity<ChatGptResponse> response = restTemplate.postForEntity(apiURL, entity, ChatGptResponse.class);

            // Checking the response
            if (response.getBody() != null && !response.getBody().getChoices().isEmpty()) {
                String finalResponse = response.getBody().getChoices().get(0).getMessage().getContent();
                // Add any additional processing you need here
                System.out.println(finalResponse);
                sendSmss(finalResponse);
                return finalResponse;
            } else {
                return "No response from OpenAI";
            }
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
