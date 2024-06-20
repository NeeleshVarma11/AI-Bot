package com.example.AIBot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DbMessageService {

    @Autowired
    private DbMessageRepository dbMessageRepository;

    public DbMessage saveMessage(String role, String content, String messageType) {
        DbMessage message = new DbMessage();
        message.setRole(role);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setTimestamp(new Date());
        return dbMessageRepository.save(message);
    }

    public void deleteAllMessages() {
        dbMessageRepository.deleteAll();
    }

    public List<DbMessage> getAllMessages() {
        return dbMessageRepository.findAll();
    }
}
