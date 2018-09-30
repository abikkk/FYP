package com.guitarnotes.a48k.fyp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SearchHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_history);

        Toast.makeText(SearchHistory.this, "Retrieving search history...", Toast.LENGTH_SHORT).show();

        Context context = getApplicationContext();
        readhistory(context);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //starting mapactivity
                Intent intent = new Intent(SearchHistory.this, MapActivity.class);
                startActivity(intent);//running the map class from the button click
            }
        });
        findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                readhistory(context);
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("history.txt", Context.MODE_PRIVATE));
                    outputStreamWriter.write("");
                    outputStreamWriter.close();
                    Toast.makeText(SearchHistory.this,"History Cleared!",Toast.LENGTH_SHORT).show();

                    //starting mapactivity
                    Intent intent = new Intent(SearchHistory.this, MapActivity.class);
                    startActivity(intent);//running the map class from the button click
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            }
        });
    }

    //reading history file
    private void readhistory(Context context) {
        String text = "";
        try {
            InputStream inputStream = context.openFileInput("history.txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                text = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found!\n " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file!\n " + e.toString());
        }

        TextView textView= findViewById(R.id.search_history);//reading data to the textview
        textView.setText(text);
        textView.setVisibility(View.VISIBLE);//enabling text view
    }
}
