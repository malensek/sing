package io.sigpipe.sing.dataset;

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
        "lightning_surface",
        "convective_inhibition_surface",
        "pressure_surface",
        "transpiration_stress-onset_soil_moisture_surface",
        "soil_porosity_surface",
        "vegetation_surface",
        "downward_long_wave_rad_flux_surface",
        "planetary_boundary_layer_height_surface",
        "ice_cover_ice1_no_ice0_surface",
        "categorical_snow_yes1_no0_surface",
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
    }
    public static double RMSE(List<Feature> actual, List<Feature> predicted) {
        RunningStatistics rs = new RunningStatistics();
        //TODO check to make sure dimensions are equal
        for (int i = 0; i < actual.size(); ++i) {

            Feature a = actual.get(i);
            Feature b = predicted.get(i);
            Feature err = a.subtract(b);
            double p = Math.pow(err.getDouble(), 2.0);
            rs.put(p);
        }
        return Math.sqrt(rs.mean());
    }
}
