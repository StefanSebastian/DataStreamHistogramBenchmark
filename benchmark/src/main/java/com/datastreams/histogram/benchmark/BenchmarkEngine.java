package com.datastreams.histogram.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author stefansebii@gmail.com
 */
@Service
public class BenchmarkEngine {

    @Value("${kafka.nrh.topic.name")
    private String nrHistTopicName;

    @Value("${kafka.osh.topic.name")
    private String osHistTopicName;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private OkHttpClient httpClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private void addActual(Map<Integer, Integer> actualValues, Integer nr) {
        if (actualValues.containsKey(nr)) {
            actualValues.put(nr, actualValues.get(nr) + 1);
        } else {
            actualValues.put(nr, 1);
        }
    }

    public void uniformBenchmark() {
        int nrOps = 1000000;
        double queryChance = 0.01;
        Random random = new Random();
        double lowerBound = 0; double upperBound = 100;

        // send init message
        sendInit(lowerBound, upperBound);
        Map<Integer, Integer> actualValues = new HashMap<>();
        for (int i = 0; i < nrOps; i++) {
            if (random.nextDouble() < queryChance) {
                // generate and measure query
            } else {
                int nr = getRandomNumberInRange(0, 100);
                addActual(actualValues, nr);
                // generate kafka msg
            }

        }
    }

    private void sendInit(double lowerBound, double upperBound){
        try {
            Map<String, String> body = new HashMap<>();
            body.put("lowerBound", String.valueOf(lowerBound));
            body.put("upperBound", String.valueOf(upperBound));
            String json = new ObjectMapper().writeValueAsString(body);

            RequestBody formBody = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("http://localhost:8080/init")
                    .post(formBody)
                    .build();

            Response response = httpClient.newCall(request).execute();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
