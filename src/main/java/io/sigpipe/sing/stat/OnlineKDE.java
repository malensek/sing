package io.sigpipe.sing.stat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.okde.model.SampleModel;

public class OnlineKDE implements UnivariateFunction {

    private RunningStatistics stats = new RunningStatistics();
    private SampleModel model;

    public static void main(String[] args) throws Exception {
        OnlineKDE test = new OnlineKDE();
    }

    public OnlineKDE() throws Exception {
        List<Double> temperatures = new ArrayList<>();
        BufferedReader br = new BufferedReader(
                new FileReader("temperatures.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            double temp = Double.parseDouble(line);
            temperatures.add(temp);
            stats.put(temp);
        }
        br.close();

        System.out.println(stats);

        // disable the forgetting factor
        double forgettingFactor = 1;
        // set the compression threshold
        double compressionThreshold = 0.02;

        // sample model object used for sample distribution estimation
        this.model = new SampleModel(forgettingFactor, compressionThreshold);

        double[][] c = { { 0 } };

        SimpleMatrix[] samples = new SimpleMatrix[temperatures.size()];
        for (int i = 0; i < temperatures.size(); ++i) {
            SimpleMatrix sample = new SimpleMatrix(
                    new double[][] {{ temperatures.get(i) }});
            samples[i] = sample;
        }

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
            this.model.updateDistribution(
                    initSamples.toArray(new SimpleMatrix[3]), cov, w);

            // Update the sample model with all generated samples one by one.
            for (int i = 3; i < temperatures.size(); i++) {
                SimpleMatrix pos = samples[i];
                this.model.updateDistribution(pos, new SimpleMatrix(c),
                        1d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        double min = expandedMin();
        double max = expandedMax();
        for (double i = min; i <= max; ++i) {
                double[][] point = {{ i }};
                SimpleMatrix pointVector = new SimpleMatrix(point);
                System.out.println(i + "\t" + this.model.evaluate(pointVector));
        }
        System.out.println(expandedMin());
        System.out.println(expandedMax());
    }

    private double expandedMin() {
        double val = stats.min();
        while (this.model.evaluate(
                    new SimpleMatrix(
                        new double[][] { { val } })) != 0.0) {
            val = val - 0.1;
        }

        return val;
    }

    private double expandedMax() {
        double val = stats.min();
        while (this.model.evaluate(
                    new SimpleMatrix(
                        new double[][] { { val } })) != 0.0) {
            val = val + 0.1;
        }
        return val;
    }

    public double value(double x) {
        return this.model.evaluate(new SimpleMatrix(new double[][] { { x } }));
    }

}
