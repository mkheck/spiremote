package com.thehecklers.spiremote;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import java.time.Instant;

@Component
@EnableScheduling
public class ReadingSender {
    private RestOperations restOps;

    public ReadingSender(RestOperations restOps) {
        this.restOps = restOps;
    }

    @Scheduled(fixedRate = 1000L)
    public Reading sendMessage() {
        Reading reading = new Reading(1,
                (short) 0,
                800.0,
                32.0,
                12.0,
                1.2,
                1);

        System.out.println(reading + " " + Instant.now());
        return restOps.postForObject("https://psedgeservice.apps.pcfone.io/reading", reading, Reading.class);
    }
}
