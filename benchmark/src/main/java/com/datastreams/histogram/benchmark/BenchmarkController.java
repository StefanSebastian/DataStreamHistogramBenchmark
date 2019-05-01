package com.datastreams.histogram.benchmark;

import com.datastreams.histogram.benchmark.model.BenchmarkReport;
import com.datastreams.histogram.benchmark.model.BenchmarkStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author stefansebii@gmail.com
 */
@Controller
public class BenchmarkController {

    @Autowired
    private BenchmarkEngine benchmarkEngine;

    @PostMapping("/uniform")
    public @ResponseBody
    List<BenchmarkReport> balancedDistribution(@RequestBody BenchmarkStats stats) {
        return benchmarkEngine.uniformBenchmark(stats);
    }
}
