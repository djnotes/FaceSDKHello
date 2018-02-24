package me.mehdi.facesdkhello;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.luxand.FSDK;

public class MainActivity extends AppCompatActivity {


    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String license = getString(R.string.luxand_license);
        int status = FSDK.ActivateLibrary(license);
        if(status != FSDK.FSDKE_OK) {
            Toast.makeText(mContext, R.string.license_invalid_quitting, Toast.LENGTH_LONG).show();
            return;
        }


    }
}
