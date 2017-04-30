package com.sjsu.obdreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    Button start;
    Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "onDestroy");
        if (null != intent)
            stopService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("MainActivity", "onStop");
//        if (intent != null)
//            stopService(intent);
    }

    public void addListenerOnButton() {
        start = (Button) findViewById(R.id.button1);
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                intent = new Intent(getBaseContext(), ObdGatewayService.class);
                startService(intent);
            }
        });

        stop = (Button) findViewById(R.id.button2);
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (null != intent)
                    stopService(intent);
            }
        });

    }


}
