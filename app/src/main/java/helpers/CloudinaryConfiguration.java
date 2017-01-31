package helpers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard on 31/01/2017.
 */

public class CloudinaryConfiguration {

    public static HashMap getConfigs() {

        HashMap config = new HashMap();

        config.put("cloud_name", ViajeConstants.CLOUD_NAME);
        config.put("api_key", ViajeConstants.API_KEY);
        config.put("api_secret", ViajeConstants.API_SECRET);

        return config;
    }
}
