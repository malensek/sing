package io.sigpipe.sing.adapters;

import io.sigpipe.sing.dataset.feature.FeatureType;
import io.sigpipe.sing.graph.FeatureHierarchy;
import io.sigpipe.sing.graph.Sketch;
import io.sigpipe.sing.util.TestConfiguration;

public class BigSketch {

    public static void main(String[] args)
    throws Exception {
        for (String featureName : TestConfiguration.FEATURE_NAMES) {
            ReadMetaBlob.activeFeatures.add(featureName);
        }

        FeatureHierarchy fh = new FeatureHierarchy();
        for (String featureName : TestConfiguration.FEATURE_NAMES) {
            System.out.println(
                    TestConfiguration.quantizers.get(featureName).numTicks()
                    + "   " + featureName);
            fh.addFeature(featureName, FeatureType.FLOAT);
        }
        //fh.addFeature("location", FeatureType.STRING);
        Sketch s = new Sketch(fh);

        long last = 0;
        for (String fileName : args) {
            ReadMetaBlob.loadData(fileName, s);
            System.out.println(s.getMetrics().getLeafCount() - last);
            last = s.getMetrics().getLeafCount();
        }

    }
}
