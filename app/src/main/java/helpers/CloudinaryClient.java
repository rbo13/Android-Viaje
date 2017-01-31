package helpers;

import android.content.Context;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import models.Motorist;

/**
 * Created by richard on 31/01/2017.
 */

public class CloudinaryClient {

    private static String imageUrl;

    static Cloudinary cloudinary = new Cloudinary(CloudinaryConfiguration.getConfigs());

    public static void upload(final InputStream inputStream) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
                    Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
                    String url = (String) uploadResult.get("url");
                    Log.d("IMAGE_URL", url);

                    Motorist motorist = new Motorist();
                    motorist.setProfile_pic(url);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }


}
