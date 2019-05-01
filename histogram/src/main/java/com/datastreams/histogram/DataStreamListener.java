package com.datastreams.histogram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * @author stefansebii@gmail.com
 */
@Service
public class DataStreamListener {
    @Autowired
    private HistogramController controller;

    @KafkaListener(topics = "nrh", groupId="benchmark")
    public void listenNrH(Integer data) {
        controller.nrHistAdd(data);
    }

    @KafkaListener(topics = "osh", groupId = "benhmark")
    public void listenOsH(Integer data) {
        controller.osHistAdd(data);
    }
}
