package io.sigpipe.sing.util;

import java.util.HashMap;
import java.util.Map;

import io.sigpipe.sing.dataset.Quantizer;
import io.sigpipe.sing.dataset.feature.Feature;

public class TestConfiguration {

    public static final String[] FEATURE_NAMES = {
        "temperature_surface",
        "temperature_tropopause",
        "relative_humidity_zerodegc_isotherm", // 0-100 range
        "snow_depth_surface",
        "snow_cover_surface", // 0-100 range
        "pressure_tropopause",
        "precipitable_water_entire_atmosphere",
        "visibility_surface",
        "upward_short_wave_rad_flux_surface",
        "surface_wind_gust_surface",
        "total_cloud_cover_entire_atmosphere", // 0-100 range
        "upward_long_wave_rad_flux_surface",
        "vegitation_type_as_in_sib_surface",
        "albedo_surface",
        "pressure_surface",
        "vegetation_surface",
        "downward_long_wave_rad_flux_surface",
        "planetary_boundary_layer_height_surface",
        "lightning_surface", // boolean
        "ice_cover_ice1_no_ice0_surface", // boolean
        "categorical_snow_yes1_no0_surface", // boolean
    };

    /* Temporarily disabled features:
     * ------------------------------
     *  "convective_inhibition_surface",
     *  "transpiration_stress-onset_soil_moisture_surface",
     *  "soil_porosity_surface",
     *  "total_precipitation_surface_3_hour_accumulation", // not in all files
     */

    public static final Map<String, Quantizer> quantizers = new HashMap<>();

    static {
        quantizers.put("temp", new Quantizer(
                    new Feature(0.0),
                    new Feature(221.1)));
    }
}
