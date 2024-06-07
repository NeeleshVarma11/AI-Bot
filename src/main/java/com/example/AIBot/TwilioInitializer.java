package com.example.AIBot;

import com.twilio.Twilio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioInitializer {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwilioInitializer.class);

  
    @Autowired
    public TwilioInitializer() {
      
        Twilio.init(
                "AC1d65110e4e6d27f889bcf9e05384cfe2",
                "fd4fea58e7bcb0b725e215fe4c90845f"
        );
        
    }
}
