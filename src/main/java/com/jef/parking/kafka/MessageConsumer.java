package com.jef.parking.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.jef.parking.data.services.LotAvailabilityService;

@Component
public class MessageConsumer {

    @KafkaListener(topics = "park_availability_topic", groupId = "parking")
    public void listen(String message) 
    {
    	new LotAvailabilityService().importData(message);
    }

}
