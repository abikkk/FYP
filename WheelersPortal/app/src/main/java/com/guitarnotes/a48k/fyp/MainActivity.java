package com.guitarnotes.a48k.fyp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    //variables
    private static final String TAG = "MainActivity";
    private static  final  int error_dialog_req=9001;

    @Override//call check method and initialize the map page
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //check for google services in the device
        if (ServiceCheck()) {
                //call the initialize method
                initialize();
        }
    }

    //initialize the main screen
    private void initialize(){
        Button btnmap =(Button) findViewById(R.id.btnmap);
        //importing btnmap from the mainactivity
        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check for internet connection in the device
                if(connection()){
                    //starting mapactivity
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);//running the map class from the button click
                }
                else {
                    Toast.makeText(MainActivity.this,"Internet connection not available.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //checking google play service in the device
    public  boolean ServiceCheck(){//checking for the google services in the device
        Log.d(TAG, "ServiceCheck: Checking Google services version");

        int available= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if ((available== ConnectionResult.SUCCESS)){
            //services found, everything fine
            Log.d(TAG, "ServiceCheck: Google API service working properly");
            return  true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //error found, can be fixed
            Log.d(TAG, "ServiceCheck: An error occured, there is a solution though");
            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,error_dialog_req);
            dialog.show();
        }
        else   {
            Toast.makeText(this, "You can't make map request from the device", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    //checking for internet connection
    public boolean connection(){
        boolean isConnected=false;
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return  isConnected;
    }
}

