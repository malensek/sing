package io.sigpipe.sing.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;

import io.sigpipe.sing.adapters.ReadMetadata;
import io.sigpipe.sing.dataset.feature.Feature;
import io.sigpipe.sing.dataset.feature.FeatureType;
import io.sigpipe.sing.stat.OnlineKDE;
import io.sigpipe.sing.stat.SquaredError;

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

    public static Quantizer fromKDE(OnlineKDE kde, int ticks) {
        SimpsonIntegrator integrator = new SimpsonIntegrator();
        double tickSize = 1.0 / (double) ticks;
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
                    //System.err.println("Oversized: " + t + " ; " + total + " + " + integral + " [" + tickSize + "]");
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

        return new Quantizer(tickList);
    }

    public static Quantizer fromList(List<Feature> features, int ticks) {
        /* Seed the oKDE */
        int seedSize = 1000;
        List<Double> seedValues = new ArrayList<>();
        for (int i = 0; i < seedSize; ++i) {
            seedValues.add(features.get(i).getDouble());
        }
        OnlineKDE kde = new OnlineKDE(seedValues);

        /* Populate the rest of the data */
        for (int i = seedSize; i < features.size(); i += 50) {
            kde.updateDistribution(features.get(i).getDouble());
        }

        return AutoQuantizer.fromKDE(kde, ticks);
    }

    public static void main(String[] args)
        throws Exception {
        for (String name : FEATURE_NAMES) {
            List<Feature> features = new ArrayList<>();

            for (String fileName : args) {
                System.err.println("Reading: " + fileName);
                List<Metadata> meta = ReadMetadata.readMetaBlob(new File(fileName));
                for (Metadata m : meta) {
                    Feature f = m.getAttribute(name);
                    if (f != null) {
                        features.add(m.getAttribute(name));
                    } else {
                        System.err.println("null feature: " + name);
                    }
                }
            }

            Quantizer q = null;
            int ticks = 10;
            double err = Double.MAX_VALUE;
            while (err > 0.025) {
                q = AutoQuantizer.fromList(features, ticks);
                //System.out.println(q);

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

                    //System.out.println(f.getFloat() + "    " + predicted.getFloat());
                }

                SquaredError se = new SquaredError(features, quantized);
                System.out.println(name + "    " + q.numTicks() + "    " + se.RMSE() + "    "
                        + se.NRMSE() + "    " + se.CVRMSE());
                err = se.NRMSE();
                ticks += 1;
            }
            System.out.println(q);
        }
    }
}
