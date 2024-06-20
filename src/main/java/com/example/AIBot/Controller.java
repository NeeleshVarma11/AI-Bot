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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private DbMessageService dbMessageService;

    @Autowired
    public Controller(RestTemplate restTemplate,
                      @Value("${openai.model}") String model,
                      @Value("${openai.api.url}") String apiURL,
                      DbMessageService dbMessageService) {
        this.restTemplate = restTemplate;
        this.model = model;
        this.apiURL = apiURL;
        this.dbMessageService = dbMessageService; 
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

        if (prompt.equalsIgnoreCase("#delete")) {
            dbMessageService.deleteAllMessages();
            return "All conversations cleared.";
        }

        if (prompt.equalsIgnoreCase("#save")) {
            // Save functionality will already be handled after the response from OpenAI
            return "Conversation saved.";
        }

        // Prepare messages for the OpenAI request
        List<Map<String, String>> openAiMessages = new ArrayList<>();
        List<DbMessage> dbMessages = dbMessageService.getAllMessages();

        for (DbMessage dbMessage : dbMessages) {
            if (dbMessage.getMessageType().equals("text")) {
                Map<String, String> msg = new HashMap<>();
                msg.put("role", dbMessage.getRole());
                msg.put("content", dbMessage.getContent());
                openAiMessages.add(msg);
            }
        }

        // Add the current prompt to the conversation
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        openAiMessages.add(userMessage);

        // Make request to OpenAI API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", openAiMessages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiURL, entity, String.class);
        String openAiResponse = response.getBody();

        // Parse the response to get the assistant's reply content
        JSONObject jsonResponse = new JSONObject(openAiResponse);
        JSONArray choicesArray = jsonResponse.getJSONArray("choices");
        JSONObject firstChoice = choicesArray.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        String assistantReplyContent = message.getString("content");

        // Save the user's prompt and the assistant's reply content
        dbMessageService.saveMessage("user", prompt, "text");
        dbMessageService.saveMessage("assistant", assistantReplyContent, "text");

        System.out.println(assistantReplyContent);
        sendSmss(assistantReplyContent);
        return assistantReplyContent;
    }

   
    
}
