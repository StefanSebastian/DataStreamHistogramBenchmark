package com.datastreams.histogram.benchmark;

import com.datastreams.histogram.benchmark.model.Benchmark;
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
        return benchmarkEngine.performBenchmark(stats, Benchmark.Uniform);
    }

    @PostMapping("/longTailed")
    public @ResponseBody
    List<BenchmarkReport> longTailedDistribution(@RequestBody BenchmarkStats stats) {
        return benchmarkEngine.performBenchmark(stats, Benchmark.LongTailed);
    }

    @PostMapping("/timeVarying")
    public @ResponseBody
    List<BenchmarkReport> timeVaryingDistribution(@RequestBody BenchmarkStats stats) {
        return benchmarkEngine.performBenchmark(stats, Benchmark.TimeVarying);
    }
}
