package com.datastreams.histogram.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author stefansebii@gmail.com
 */
@Service
public class BenchmarkEngine {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${kafka.nrh.topic.name}")
    private String nrHistTopicName;

    @Value("${kafka.osh.topic.name}")
    private String osHistTopicName;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private OkHttpClient httpClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private List<Query> queries = new LinkedList<>();

    /**
     * Perform the uniform benchmark
     */
    public void uniformBenchmark() {
        int nrOps = 1000;
        double queryChance = 0.01;
        int lowerBound = 0; int upperBound = 100;

        performBenchmark(nrHistTopicName, nrOps, queryChance, lowerBound, upperBound);
        performBenchmark(osHistTopicName, nrOps, queryChance, lowerBound, upperBound);
    }

    public void performBenchmark(String topic, int nrOps, double queryChance, int lowerBound, int upperBound) {
        logger.info("Performing benchmark " + topic + " " + nrOps);
        Random random = new Random();
        // reset queries
        queries = new LinkedList<>();
        // send init message
        sendInit(lowerBound, upperBound);
        Map<Integer, Integer> actualValues = new HashMap<>();
        for (int i = 0; i < nrOps; i++) {
            if (random.nextDouble() < queryChance) {
                // generate and measure query
                sendAndMeasureQuery(topic, actualValues, lowerBound, upperBound);
            } else {
                int nr = getRandomNumberInRange(lowerBound, upperBound);
                if (actualValues.containsKey(nr)) {
                    actualValues.put(nr, actualValues.get(nr) + 1);
                } else {
                    actualValues.put(nr, 1);
                }
                // generate kafka msg
                kafkaTemplate.send(topic, String.valueOf(nr));
            }
        }

        System.out.println(queries);
    }

    private void sendAndMeasureQuery(String channel, Map<Integer, Integer> actualValues, int lowerBound, int upperBound) {
        try {
            Map<String, String> body = new HashMap<>();
            // get random bounds
            int lowerBoundQ = getRandomNumberInRange(lowerBound + 1, upperBound - 1);
            int upperBoundQ = getRandomNumberInRange(lowerBound + 1, upperBound - 1);
            if (lowerBoundQ > upperBoundQ) {
                int tmp = lowerBoundQ; lowerBoundQ = upperBoundQ; upperBoundQ = tmp;
            }
            body.put("lowerBound", String.valueOf(lowerBoundQ));
            body.put("upperBound", String.valueOf(upperBoundQ));
            String json = new ObjectMapper().writeValueAsString(body);

            RequestBody formBody = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("http://localhost:8080/" + channel + "/estimate")
                    .post(formBody)
                    .build();

            double before = System.currentTimeMillis();
            Response response = httpClient.newCall(request).execute();
            double duration = System.currentTimeMillis() - before;
            double estimate = Double.valueOf(response.body().string());
            double actual = getActualValue(actualValues, lowerBoundQ, upperBoundQ);
            queries.add(new Query(lowerBoundQ, upperBoundQ, duration, estimate, actual));
        } catch (IOException e){
            e.printStackTrace();
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

            httpClient.newCall(request).execute();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private double getActualValue(Map<Integer, Integer> actualValues, int lowerBound, int upperBound) {
        double actual = 0;
        for (Integer key : actualValues.keySet()) {
            if (key >= lowerBound && key <= upperBound) {
                actual += actualValues.get(key);
            }
        }
        return actual;
    }

    private int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
