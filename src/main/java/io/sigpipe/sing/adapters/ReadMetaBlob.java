
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

        loadData(args[0], s);

        System.gc();
        System.gc();
        
        System.out.println();
        Runtime runtime = Runtime.getRuntime();
        System.out.println("max=" + runtime.maxMemory());
        System.out.println("total=" + runtime.totalMemory());
        System.out.println("free=" + runtime.freeMemory());
        System.out.println("used=" + (runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("estimate=" + estimateMemoryUsage(
                    s,
                    s.getMetrics().getVertexCount(),
                    s.getMetrics().getLeafCount()));

        scan.nextInt();

        PerformanceTimer info = new PerformanceTimer("info");
        info.start();
        System.out.println(s.getRoot().numLeaves());
        System.out.println(s.getRoot().numDescendants());
        System.out.println(s.getRoot().numDescendantEdges());
        info.stopAndPrint();
        System.out.println(s.getMetrics());

        SerializationOutputStream out = new SerializationOutputStream(
                new GZIPOutputStream(
                new FileOutputStream(new File("d.bin"))));
        //s.getRoot().find(new Feature("location", 319920l));
        PartitionQuery pq = new PartitionQuery(s.getMetrics());
//        rq.addExpression(
//                new Expression(
//                    Operator.RANGE_INC, new Feature("temperature_surface", 260.0f), new Feature(300.0f)));
        String removePrefix = "d";
        pq.addExpression(
                new Expression(
                    Operator.STR_PREFIX, new Feature("location", removePrefix)));

        PerformanceTimer exec = new PerformanceTimer("exec");
        exec.start();
        pq.execute(s.getRoot());
        pq.serializeResults(s.getRoot(), out);
        out.close();
        exec.stopAndPrint();

        s.geoTrie.remove(removePrefix);

        System.gc();
        System.gc();
        System.out.println();
        System.out.println("max=" + runtime.maxMemory());
        System.out.println("total=" + runtime.totalMemory());
        System.out.println("free=" + runtime.freeMemory());
        System.out.println("used=" + (runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("estimate=" + estimateMemoryUsage(
                    s,
                    s.getMetrics().getVertexCount(),
                    s.getMetrics().getLeafCount()));


        info.start();
        System.out.println(s.getRoot().numLeaves());
        System.out.println(s.getRoot().numDescendants());
        System.out.println(s.getRoot().numDescendantEdges());
        info.stopAndPrint();


        scan.nextInt();

        System.out.println(s.getMetrics());

        System.out.println(s.geoTrie.query("dj").b);

        loadData(args[1], s);
        System.gc();
        System.gc();
        System.out.println();
        System.out.println("max=" + runtime.maxMemory());
        System.out.println("total=" + runtime.totalMemory());
        System.out.println("free=" + runtime.freeMemory());
        System.out.println("used=" + (runtime.totalMemory() - runtime.freeMemory()));
        System.out.println("estimate=" + estimateMemoryUsage(
                    s,
                    s.getMetrics().getVertexCount(),
                    s.getMetrics().getLeafCount()));


    }

    private static void loadData(String fileName, Sketch s)
    throws Exception {
        System.out.println("Reading metadata blob: " + fileName);
        FileInputStream fIn = new FileInputStream(fileName);
        BufferedInputStream bIn = new BufferedInputStream(fIn);
        SerializationInputStream in = new SerializationInputStream(bIn);

        int num = in.readInt();
        System.out.println("Records: " + num);

        PerformanceTimer addAllPaths = new PerformanceTimer("addAllPaths");
        addAllPaths.start();
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
