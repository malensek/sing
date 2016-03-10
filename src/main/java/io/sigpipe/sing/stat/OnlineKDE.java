package io.sigpipe.sing.stat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.okde.model.SampleModel;

public class OnlineKDE {

    private static final int DIM = 1;

    public static void main(String[] args) throws Exception {
        OnlineKDE test = new OnlineKDE();
    }

    public OnlineKDE() throws Exception {
        List<Double> temperatures = new ArrayList<>();
        BufferedReader br = new BufferedReader(
                new FileReader("temperatures.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            temperatures.add(Double.parseDouble(line));
        }
        System.out.println("Samples: " + temperatures.size());

        // disable the forgetting factor
        double forgettingFactor = 1;
        // set the compression threshold
        double compressionThreshold = 0.02;
        // number of samples to genereate
        int noOfSamples = 1000;

        // sample model object used for sample distribution estimation
        SampleModel sampleDistribution = new SampleModel(forgettingFactor,
                compressionThreshold);

        double[][] c = { { 0 } };

        SimpleMatrix[] samples = new SimpleMatrix[temperatures.size()];
        for (int i = 0; i < temperatures.size(); ++i) {
            SimpleMatrix sample = new SimpleMatrix(
                    new double[][] {{ temperatures.get(i) }});
            samples[i] = sample;
        }

        // generate sample points using three different Gaussian distributions
        // first define the means and standard deviations
//        int[] mean1 = { 3, 2 };
//        float stddev1 = .2f;
//        int[] mean2 = { 7, 4 };
//        float stddev2 = .2f;
//        int[] mean3 = { 3, 8 };
//        float stddev3 = .5f;
//        SimpleMatrix[] samples = new SimpleMatrix[noOfSamples];
//        // now generate the random samples
//        for (int i = 0; i < noOfSamples; i++) {
//            // 30% are distributed with mean1 and stddev1
//            // 20% are distributed with mean2 and stddev2
//            // 50% are distributed with mean3 and stddev3
//            if (i < noOfSamples * 0.3) {
//                double[][] sampleArray = { { gaussian(mean1[0], stddev1) } };
//                SimpleMatrix sample = new SimpleMatrix(sampleArray);
//                samples[i] = sample;
//            } else if (i < noOfSamples * 0.5) {
//                double[][] sampleArray = { { gaussian(mean2[0], stddev2) } };
//                SimpleMatrix sample = new SimpleMatrix(sampleArray);
//                samples[i] = sample;
//            } else if (i < noOfSamples) {
//                double[][] sampleArray = { { gaussian(mean3[0], stddev3) } };
//                SimpleMatrix sample = new SimpleMatrix(sampleArray);
//                samples[i] = sample;
//            }
//        }

        /*
         * Now the sample model is updated using the generated sample data.
         */
        try {
            // Add three samples at once to initialize the sample model
            ArrayList<SimpleMatrix> initSamples = new ArrayList<SimpleMatrix>();
            initSamples.add(samples[0]);
            initSamples.add(samples[1]);
            initSamples.add(samples[2]);
            double[] w = { 1, 1, 1 };
            SimpleMatrix[] cov = { new SimpleMatrix(c), new SimpleMatrix(c),
                new SimpleMatrix(c) };
            sampleDistribution.updateDistribution(
                    initSamples.toArray(new SimpleMatrix[3]), cov, w);

            // Update the sample model with all generated samples one by one.
            for (int i = 3; i < noOfSamples; i++) {
                SimpleMatrix pos = samples[i];
                sampleDistribution.updateDistribution(pos, new SimpleMatrix(c),
                        1d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 270; i < 320; ++i) {
                double[][] point = {{ (double) i }};
                SimpleMatrix pointVector = new SimpleMatrix(point);
                System.out.println(i + "\t" + sampleDistribution.evaluate(pointVector));
        }
    }
}
