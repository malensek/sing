package io.sigpipe.sing.stat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import org.ejml.simple.SimpleMatrix;

import de.tuhh.luethke.okde.model.SampleModel;

import io.sigpipe.sing.dataset.Quantizer;
import io.sigpipe.sing.dataset.feature.Feature;

public class OnlineKDE implements UnivariateFunction {

    private RunningStatistics stats = new RunningStatistics();
    private SampleModel model;

    public static void main(String[] args) throws Exception {
        OnlineKDE test = new OnlineKDE();
        SimpsonIntegrator si = new SimpsonIntegrator();
        int ticks = 20;
        double tickSize = 1.0 / (double) ticks;

        System.out.println("Tick size: " + tickSize);
        double start = test.expandedMin();
        double increment = 0.05;
        List<Feature> tickList = new ArrayList<>();
        for (int t = 0; t < ticks; ++t) {
            double total = 0.0;
            tickList.add(new Feature(start));
            while (total < tickSize) {
                double z = si.integrate(Integer.MAX_VALUE, test, start, start + increment);
                total += z;
                start = start + increment;
                if (t == ticks - 1 && z <= 0.0) {
                    break;
                }
            }
        }
        tickList.add(new Feature(start));

        Quantizer q = new Quantizer(tickList);
        System.out.println(q);

        List<Feature> temperatures = new ArrayList<>();
        BufferedReader br = new BufferedReader(
                new FileReader("temperatures.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            double temp = Double.parseDouble(line);
            temperatures.add(new Feature("temperature", temp));
        }
        br.close();

        for (Feature f : temperatures) {
            /* Find the midpoint */
            Feature initial = q.quantize(f);
            Feature next = q.nextTick(initial);
            Feature difference = next.subtract(initial);
            Feature midpoint = difference.divide(new Feature(2.0));
            Feature predicted = initial.add(midpoint);

            System.out.println(f.getDouble() + "    " + predicted.getFloat());
        }

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

        SimpleMatrix[] samples = new SimpleMatrix[temperatures.size()];
        SimpleMatrix[] covs = new SimpleMatrix[temperatures.size()];
        for (int i = 0; i < temperatures.size(); ++i) {
            SimpleMatrix sample = new SimpleMatrix(
                    new double[][] { { temperatures.get(i) } });
            samples[i] = sample;
            covs[i] = new SimpleMatrix(1, 1);
        }
        double[] weights = new double[temperatures.size()];
        Arrays.fill(weights, 1.0);

        /*
         * Now the sample model is updated using the generated sample data.
         */
        try {

            // Add three samples at once to initialize the sample model
            ArrayList<SimpleMatrix> initSamples = new ArrayList<SimpleMatrix>();
            initSamples.add(samples[0]);
            initSamples.add(samples[1]);
            initSamples.add(samples[2]);
            initSamples.add(samples[3]);
            double[] w = { 1, 1, 1, 1 };
            SimpleMatrix[] cov = {
                new SimpleMatrix(1, 1),
                new SimpleMatrix(1, 1),
                new SimpleMatrix(1, 1),
                new SimpleMatrix(1, 1) };
            this.model.updateDistribution(samples, cov, w);

            // Update the sample model with all generated samples one by one.
            for (int i = 3; i < temperatures.size(); i++) {
                SimpleMatrix pos = samples[i];
                this.model.updateDistribution(pos, new SimpleMatrix(1, 1),
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

    public double expandedMin() {
        double val = stats.min();
        while (this.model.evaluate(
                    new SimpleMatrix(
                        new double[][] { { val } })) != 0.0) {
            val = val - 0.1;
        }

        return val;
    }

    public double expandedMax() {
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
