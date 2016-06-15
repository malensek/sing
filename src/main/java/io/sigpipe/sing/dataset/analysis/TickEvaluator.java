package io.sigpipe.sing.dataset.analysis;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.sigpipe.sing.adapters.ReadMetadata;
import io.sigpipe.sing.dataset.Quantizer.QuantizerBuilder;
import io.sigpipe.sing.dataset.feature.Feature;
import io.sigpipe.sing.stat.RunningStatistics;
import io.sigpipe.sing.stat.SquaredError;
import io.sigpipe.sing.util.TestConfiguration;

public class TickEvaluator {

    private Quantizer quantizer;
    private Map<Feature, RunningStatistics> statMap = new HashMap<>();

    public TickEvaluator(Quantizer quantizer) {
        this.quantizer = quantizer;
    }

    public void train(List<Feature> features) {
        for (Feature feature : features) {
            Feature q = quantizer.quantize(feature);
            RunningStatistics rs = statMap.get(q);
            if (rs == null) {
                rs = new RunningStatistics();
                statMap.put(q, rs);
            }
            rs.put(feature.getDouble());
        }
    }

    public void evaluate(List<Feature> features) {
        RunningStatistics allStats = new RunningStatistics();

        Map<Feature, SquaredError> errors = new HashMap<>();
        for (Feature feature : features) {
            allStats.put(feature.getDouble());

            Feature q = quantizer.quantize(feature);
            RunningStatistics rs = statMap.get(q);
            double mean = rs.mean();
            double actual = feature.getDouble();
            SquaredError se = errors.get(q);
            if (se == null) {
                se = new SquaredError();
                errors.put(q, se);
            }
            se.put(actual, mean);
        }

        for (Feature q : errors.keySet()) {
            SquaredError se = errors.get(q);
            double nrmse = se.RMSE() / (allStats.max() - allStats.min());
            double cvrmse = se.RMSE() / allStats.mean();
            System.out.println(q + "\t" + se.RMSE() + "\t" + nrmse
                    + "\t" + cvrmse);
        }

        System.out.println(allStats);
    }

    public static void main(String[] args)
    throws Exception {

        List<Feature> features = Files.lines(
                Paths.get(args[0]), Charset.defaultCharset())
            .map(line -> Float.parseFloat(line))
            .map(fl -> new Feature("disease_duration", fl))
            .collect(Collectors.toList());

        Quantizer q = new QuantizerBuilder()
            .addTick(new Feature("disease_duration", 10.600000000000014f))
            .addTick(new Feature("disease_duration", 18.96948000000025f))
            .addTick(new Feature("disease_duration", 19.70504000000027f))
            .addTick(new Feature("disease_duration", 20.241800000000286f))
            .addTick(new Feature("disease_duration", 20.6990400000003f))
            .addTick(new Feature("disease_duration", 21.11652000000031f))
            .addTick(new Feature("disease_duration", 21.534000000000322f))
            .addTick(new Feature("disease_duration", 21.951480000000334f))
            .addTick(new Feature("disease_duration", 22.368960000000346f))
            .addTick(new Feature("disease_duration", 22.80632000000036f))
            .addTick(new Feature("disease_duration", 23.24368000000037f))
            .addTick(new Feature("disease_duration", 23.681040000000383f))
            .addTick(new Feature("disease_duration", 24.118400000000396f))
            .addTick(new Feature("disease_duration", 24.555760000000408f))
            .addTick(new Feature("disease_duration", 25.03288000000042f))
            .addTick(new Feature("disease_duration", 25.589520000000437f))
            .addTick(new Feature("disease_duration", 26.305200000000458f))
            .addTick(new Feature("disease_duration", 27.259440000000485f))
            .addTick(new Feature("disease_duration", 28.332960000000515f))
            .addTick(new Feature("disease_duration", 29.58540000000055f))
            .addTick(new Feature("disease_duration", 31.493880000000605f))
            .addTick(new Feature("disease_duration", 33.22344000000065f))
            .addTick(new Feature("disease_duration", 35.489760000000715f))
            .addTick(new Feature("disease_duration", 39.60492000000083f))
            .addTick(new Feature("disease_duration", 45.568920000001f))
            .addTick(new Feature("disease_duration", 60.30000000000142f))
            .build();

        TickEvaluator te = new TickEvaluator(q);
        te.train(features);
        te.evaluate(features);
    }
}
