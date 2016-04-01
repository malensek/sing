
package io.sigpipe.sing.adapters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import io.sigpipe.sing.dataset.Metadata;
import io.sigpipe.sing.dataset.feature.Feature;
import io.sigpipe.sing.dataset.feature.FeatureType;
import io.sigpipe.sing.graph.DataContainer;
import io.sigpipe.sing.graph.FeatureHierarchy;
import io.sigpipe.sing.graph.Path;
import io.sigpipe.sing.graph.Sketch;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.Serializer;
import io.sigpipe.sing.stat.FeatureSurvey;
import io.sigpipe.sing.util.Geohash;
import io.sigpipe.sing.util.PerformanceTimer;
import io.sigpipe.sing.util.TestConfiguration;

public class ReadMetaBlob {

    public static void main(String[] args) throws Exception {
        File bundle = new File(args[0]);

        System.out.println("Reading metadata blob...");
        FileInputStream fIn = new FileInputStream(bundle);
        BufferedInputStream bIn = new BufferedInputStream(fIn);
        SerializationInputStream in = new SerializationInputStream(bIn);

        int num = in.readInt();
        System.out.println("Records: " + num);

        FeatureHierarchy fh = new FeatureHierarchy();
        for (String featureName : TestConfiguration.FEATURE_NAMES) {
            System.out.println(
                    TestConfiguration.quantizers.get(featureName).numTicks()
                    + "   " + featureName);
            fh.addFeature(featureName, FeatureType.FLOAT);
        }
        fh.addFeature("location", FeatureType.STRING);
        Sketch s = new Sketch(fh);

        FeatureSurvey fs = new FeatureSurvey();
        for (int i = 0; i < num; ++i) {
            float lat = in.readFloat();
            float lon = in.readFloat();
            byte[] payload = in.readField();

            Metadata m = Serializer.deserialize(Metadata.class, payload);


            Path p = new Path(m.getAttributes().toArray());
            String location = Geohash.encode(lat, lon, 4);
            p.add(new Feature("location", location));
            s.addPath(p);

            for (Vertex v : p) {
                if (v.getLabel().getType() != FeatureType.STRING) {
                    fs.add(v.getLabel());
                }
            }

            if (i % 1000 == 0) {
                System.out.print('.');
            }
        }
        System.gc();

        Scanner scan=new Scanner(System.in);
        scan.nextInt();

        System.out.println(s.getRoot().numLeaves());
        System.out.println(s.getRoot().numDescendants());
        System.out.println(s.getRoot().numDescendantEdges());

        scan.nextInt();

        //fs.printAll();
        PerformanceTimer serpt = new PerformanceTimer("serialize");
        serpt.start();
        Serializer.persistCompressed(s.getRoot(), "testvertex.bin");
        serpt.stopAndPrint();

        in.close();
    }
}
