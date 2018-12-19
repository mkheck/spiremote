package com.thehecklers.spiremote;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import java.time.Instant;

@Component
//@EnableScheduling
public class ReadingSender {
    @Value("${powerstation.edge-service.url:'localhost:8080'}")
    private String postURL;
    private RestOperations restOps;

    public ReadingSender(RestOperations restOps) {
        this.restOps = restOps;
    }

//    @Scheduled(fixedRate = 1000L)
    public Reading sendTestReading() {
        return sendReading(new Reading(1,
                (short) 0,
                800.0,
                32.0,
                12.0,
                1.2,
                1));
    }

    public Reading sendReading(Reading reading) {
        LogMonkey.logIt(reading + " " + Instant.now());
        return restOps.postForObject(postURL, reading, Reading.class);
    }
}
