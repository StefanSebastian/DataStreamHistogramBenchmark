package com.datastreams.histogram.benchmark;

import com.datastreams.histogram.benchmark.model.*;
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

    /**
     * Perform the uniform benchmark
     */
    public List<BenchmarkReport> performBenchmark(BenchmarkStats stats, Benchmark benchmark) {
        List<BenchmarkAction> benchmarkActions = new LinkedList<>();
        if (benchmark.equals(Benchmark.Uniform)) {
            benchmarkActions = generateUniformBenchmark(stats);
        } else if (benchmark.equals(Benchmark.LongTailed)) {
            benchmarkActions = generateLongTailedBenchmark(stats);
        } else if (benchmark.equals(Benchmark.TimeVarying)) {
            benchmarkActions = generateTimeVaryingBenchmark(stats);
        }
        BenchmarkReport report1 = performActions(nrHistTopicName, benchmarkActions, stats);
        BenchmarkReport report2 = performActions(osHistTopicName, benchmarkActions, stats);
        List<BenchmarkReport> reports = new LinkedList<>();
        reports.add(report1);
        reports.add(report2);
        return reports;
    }

    public List<BenchmarkAction> generateLongTailedBenchmark(BenchmarkStats stats) {
        List<BenchmarkAction> actions = new LinkedList<>();

        // head should contain 80% of the values from 20% of the range
        BenchmarkStats statsHead = new BenchmarkStats();
        statsHead.setLowerBound(stats.getLowerBound());
        int headUpperBound = stats.getLowerBound() + (int)((stats.getUpperBound() - stats.getLowerBound()) * 0.2);
        statsHead.setUpperBound(headUpperBound);
        statsHead.setQueryChance(stats.getQueryChance());
        int headNrOps = (int)(stats.getNrOps() * 0.8);
        statsHead.setNrOps(headNrOps);

        // tail should contain 20% of the values from 80% of the range
        BenchmarkStats statsTail = new BenchmarkStats();
        statsTail.setLowerBound(headUpperBound);
        statsTail.setUpperBound(stats.getUpperBound());
        statsTail.setQueryChance(stats.getQueryChance());
        statsTail.setNrOps(stats.getNrOps() - headNrOps);

        actions.addAll(generateUniformBenchmark(statsHead));
        actions.addAll(generateUniformBenchmark(statsTail));

        return actions;
    }

    /**
     * The value range changes 2 times at uniform intervals
     */
    public List<BenchmarkAction> generateTimeVaryingBenchmark(BenchmarkStats stats) {
        List<BenchmarkAction> actions = new LinkedList<>();

        BenchmarkStats first = new BenchmarkStats();
        first.setLowerBound(stats.getLowerBound());
        int firstUpperBound = stats.getLowerBound() + (int)((stats.getUpperBound() - stats.getLowerBound()) * 0.33);
        first.setUpperBound(firstUpperBound);
        first.setQueryChance(stats.getQueryChance());
        int nrOps = (int)(stats.getNrOps() * 0.33); // approx a third of operations
        first.setNrOps(nrOps);

        BenchmarkStats second = new BenchmarkStats();
        second.setLowerBound(firstUpperBound);
        int secondUpperBound = stats.getLowerBound() + (int)((stats.getUpperBound() - stats.getLowerBound()) * 0.66);
        second.setUpperBound(secondUpperBound);
        second.setQueryChance(stats.getQueryChance());
        second.setNrOps(nrOps);

        BenchmarkStats third = new BenchmarkStats();
        third.setLowerBound(secondUpperBound);
        third.setUpperBound(stats.getUpperBound());
        third.setQueryChance(stats.getQueryChance());
        third.setNrOps(nrOps);

        actions.addAll(generateUniformBenchmark(first));
        actions.addAll(generateUniformBenchmark(second));
        actions.addAll(generateUniformBenchmark(third));

        return actions;
    }

    public List<BenchmarkAction> generateUniformBenchmark(BenchmarkStats stats) {
        List<BenchmarkAction> actions = new LinkedList<>();

        Random random = new Random();
        Map<Integer, Integer> actualValues = new HashMap<>();
        for (int i = 0; i < stats.getNrOps(); i++) {
            if (random.nextDouble() < stats.getQueryChance()) {
                // generate query
                int lowerBoundQ = getRandomNumberInRange(stats.getLowerBound() + 1, stats.getUpperBound() - 1);
                int upperBoundQ = getRandomNumberInRange(stats.getLowerBound() + 1, stats.getUpperBound() - 1);
                if (lowerBoundQ > upperBoundQ) {
                    int tmp = lowerBoundQ; lowerBoundQ = upperBoundQ; upperBoundQ = tmp;
                }
                actions.add(new BenchmarkAction(lowerBoundQ, upperBoundQ));
            } else {
                int nr = getRandomNumberInRange(stats.getLowerBound(), stats.getUpperBound());
                if (actualValues.containsKey(nr)) {
                    actualValues.put(nr, actualValues.get(nr) + 1);
                } else {
                    actualValues.put(nr, 1);
                }
                actions.add(new BenchmarkAction(nr));
            }
        }
        return actions;
    }

    public BenchmarkReport performActions(String topic, List<BenchmarkAction> actions, BenchmarkStats stats) {
        // reset queries
        List<Query> queries = new LinkedList<>();
        // send init message
        sendInit(stats.getLowerBound(), stats.getUpperBound());
        Map<Integer, Integer> actualValues = new HashMap<>();
        for (BenchmarkAction action : actions) {
            if (action.getType().equals(BenchmarkAction.Type.QUERY)) {
                // send and measure query
                Query query = sendAndMeasureQuery(topic, actualValues, action.getLowerBound(), action.getUpperBound());
                queries.add(query);
            } else {
                kafkaTemplate.send(topic, String.valueOf(action.getValue()));
            }
        }

        return getReport(queries, topic);
    }

    private BenchmarkReport getReport(List<Query> queries, String algorithm) {
        double time = 0;
        double sqerror = 0;
        double abserror = 0;
        for (Query query : queries) {
            time += query.getDuration();
            sqerror += ((query.getActual() - query.getEstimated()) * (query.getActual() - query.getEstimated()));
            abserror += Math.abs(query.getActual() - query.getEstimated());
        }

        BenchmarkReport report = new BenchmarkReport();
        report.setAlgorithm(algorithm);
        report.setNrQueries(queries.size());
        report.setAverageReqTime(time / queries.size());
        report.setMeanAbsError(abserror / queries.size());
        report.setMeanSquaredError(sqerror / queries.size());
        return report;
    }

    private Query sendAndMeasureQuery(String channel, Map<Integer, Integer> actualValues, int lowerBoundQ, int upperBoundQ) {
        try {
            Map<String, String> body = new HashMap<>();

            body.put("lowerBound", String.valueOf(lowerBoundQ));
            body.put("upperBound", String.valueOf(upperBoundQ));
            String json = new ObjectMapper().writeValueAsString(body);

            RequestBody formBody = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("http://localhost:8080/" + channel + "/estimate")
                    .post(formBody)
                    .build();

            logger.info("Sending query " + lowerBoundQ + " " + upperBoundQ);

            double before = System.currentTimeMillis();
            Response response = httpClient.newCall(request).execute();
            double duration = System.currentTimeMillis() - before;
            double estimate = Double.valueOf(response.body().string());
            double actual = getActualValue(actualValues, lowerBoundQ, upperBoundQ);
            return new Query(lowerBoundQ, upperBoundQ, duration, estimate, actual);
        } catch (IOException e){
            e.printStackTrace();
            return null;
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
