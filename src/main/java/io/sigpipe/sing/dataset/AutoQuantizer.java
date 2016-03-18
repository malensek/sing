package io.sigpipe.sing.dataset;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;

import io.sigpipe.sing.dataset.feature.Feature;
import io.sigpipe.sing.dataset.feature.FeatureType;
import io.sigpipe.sing.serialization.SerializationInputStream;
import io.sigpipe.sing.serialization.Serializer;
import io.sigpipe.sing.stat.OnlineKDE;
import io.sigpipe.sing.stat.RunningStatistics;
import io.sigpipe.sing.stat.SummaryStatistics;
import io.sigpipe.sing.util.PerformanceTimer;

public class AutoQuantizer {

    private static final String[] FEATURE_NAMES = {
        "temperature_surface",
        "temperature_tropopause",
        "relative_humidity_zerodegc_isotherm",
        "total_precipitation_surface_3_hour_accumulation",
        "snow_depth_surface",
        "snow_cover_surface",
        "pressure_tropopause",
        "precipitable_water_entire_atmosphere",
        "visibility_surface",
        "upward_short_wave_rad_flux_surface",
        "surface_wind_gust_surface",
        "total_cloud_cover_entire_atmosphere",
        "upward_long_wave_rad_flux_surface",
        "vegitation_type_as_in_sib_surface",
        "albedo_surface",
        "convective_inhibition_surface",
        "pressure_surface",
        "transpiration_stress-onset_soil_moisture_surface",
        "soil_porosity_surface",
        "vegetation_surface",
        "downward_long_wave_rad_flux_surface",
        "planetary_boundary_layer_height_surface",
        //"lightning_surface",
        //"ice_cover_ice1_no_ice0_surface",
        //"categorical_snow_yes1_no0_surface",
    };

    public static Quantizer create(OnlineKDE kde) {


        return null;
    }

    public static void main(String[] args)
    throws Exception {
        PerformanceTimer read = new PerformanceTimer("read");
        read.start();
        //for (String fileName : args) {
            FileInputStream fIn = new FileInputStream(args[0]);
            BufferedInputStream bIn = new BufferedInputStream(fIn);
            SerializationInputStream in = new SerializationInputStream(bIn);

            List<Feature> features = new ArrayList<>();
            int num = in.readInt();
            for (int i = 0; i < num; ++i) {
                /* Ignore lat, lon: */
                in.readFloat();
                in.readFloat();

                byte[] payload = in.readField();
                Metadata m = Serializer.deserialize(Metadata.class, payload);
                Feature f = m.getAttribute(FEATURE_NAMES[0]);
                features.add(f);
            }
        //}
        read.stop();

        PerformanceTimer seed = new PerformanceTimer("seed");
        seed.start();
        int seedSize = 100;
        List<Double> seedValues = new ArrayList<>();
        for (int i = 0; i < seedSize; ++i) {
            seedValues.add(features.get(i).getDouble());
        }
        OnlineKDE kde = new OnlineKDE(seedValues);
        //OnlineKDE kde = new OnlineKDE(seedValues, 1.0, 0.05);
        seed.stop();

        PerformanceTimer update = new PerformanceTimer("update");
        update.start();
        for (int i = seedSize; i < features.size(); i += 10) {
            kde.updateDistribution(features.get(i).getDouble());
        }
        update.stop();

        //System.out.println(kde);

        SimpsonIntegrator integrator = new SimpsonIntegrator();
        int ticks = Integer.parseInt(args[1]);
        double tickSize = 1.0 / (double) ticks;

        //System.out.println("Tick size: " + tickSize);
        System.err.println("Integrating");
        double start = kde.expandedMin();
        double end = kde.expandedMax();
        double step = ((end - start) / (double) ticks) * 0.01;
        List<Feature> tickList = new ArrayList<>();
        for (int t = 0; t < ticks; ++t) {
            double total = 0.0;
            tickList.add(new Feature(start));
            double increment = step;
            while (total < tickSize) {
                double integral = integrator.integrate(
                        Integer.MAX_VALUE, kde, start, start + increment);
                if (total + integral > (tickSize * 1.05)) {
                    System.err.println("Oversized: " + t + " ; " + total + " + " + integral + " [" + tickSize + "]");
                    increment = increment / 2.0;
                    continue;
                }

                total += integral;
                start = start + increment;
                if (start > end) {
                    break;
                }
            }
        }
        tickList.add(new Feature(start));

        Quantizer q = new Quantizer(tickList);
        //System.out.println("ticks=" + q.numTicks());
        //System.out.println(q);

        List<Feature> quantized = new ArrayList<>();
        for (Feature f : features) {
            /* Find the midpoint */
            Feature initial = q.quantize(f.convertTo(FeatureType.DOUBLE));
            Feature next = q.nextTick(initial);
            Feature difference = next.subtract(initial);
            Feature midpoint = difference.divide(new Feature(2.0f));
            Feature prediction = initial.add(midpoint);

            quantized.add(prediction);

            //System.out.println(f.getFloat() + "    " + predicted.getFloat());
        }

        SummaryStatistics ss = kde.summaryStatistics();
        double rmse = RMSE(features, quantized);
        double nrmse = rmse / (ss.max() - ss.min());
        double cvrmse = rmse / ss.mean();
        System.out.println(q.numTicks() + "    " + rmse + "    " + nrmse + "    " + cvrmse);
    }

        return Math.sqrt(rs.mean());
    }
}
