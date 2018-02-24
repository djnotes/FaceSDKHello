import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.luxand.FSDK;

import me.mehdi.facesdkhello.R;

/**
 * Created by johndoe on 2/25/18.
 */

public class MyApplication extends Application {

private Context mContext = this;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
