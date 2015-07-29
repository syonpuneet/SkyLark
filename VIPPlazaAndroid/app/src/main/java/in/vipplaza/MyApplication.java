package in.vipplaza;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

//application for create the ins5tance of Universal Image Loader
//resToastText = R.string.crash_toast_text

@ReportsCrashes(formKey = "", // will not be used
mailTo = "manish.gupta@syoninfomedia.com",
mode = ReportingInteractionMode.SILENT
)
public class MyApplication extends Application {
	
	
    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    	
        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
           
        ImageLoader.getInstance().init(config);
    }
    
  
}