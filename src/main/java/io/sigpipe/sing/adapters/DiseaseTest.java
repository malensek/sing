package io.sigpipe.sing.adapters;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.sigpipe.sing.dataset.analysis.AutoQuantizer;
import io.sigpipe.sing.dataset.analysis.Quantizer;
import io.sigpipe.sing.dataset.feature.Feature;
import io.sigpipe.sing.dataset.feature.FeatureType;
import io.sigpipe.sing.stat.SquaredError;

public class DiseaseTest {

    public static void main(String[] args) throws Exception {
        List<Feature> features = Files.lines(
                Paths.get(args[0]), Charset.defaultCharset())
            .map(line -> Float.parseFloat(line))
            .map(fl -> new Feature("disease_duration", fl))
            .collect(Collectors.toList());

        Quantizer q = null;
        int ticks = 10;
        double err = Double.MAX_VALUE;
        while (err > 0.025) {
            q = AutoQuantizer.fromList(features, ticks);
            List<Feature> quantized = new ArrayList<>();
            for (Feature f : features) {
                /* Find the midpoint */
                Feature initial = q.quantize(f.convertTo(FeatureType.DOUBLE));
                Feature next = q.nextTick(initial);
                if (next == null) {
                    next = initial;
                }
                Feature difference = next.subtract(initial);
                Feature midpoint = difference.divide(new Feature(2.0f));
                Feature prediction = initial.add(midpoint);

                quantized.add(prediction);
            }

            SquaredError se = new SquaredError(features, quantized);
            System.out.println(features.get(0).getName()
                    + "    " + q.numTicks() + "    " + se.RMSE() + "    "
                    + se.NRMSE() + "    " + se.CVRMSE());
            err = se.NRMSE();
            ticks += 1;
        }
        System.out.println(q);
    }
}
