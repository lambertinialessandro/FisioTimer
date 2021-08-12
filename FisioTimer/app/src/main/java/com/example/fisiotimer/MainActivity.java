package com.example.fisiotimer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText exerciseTimeEditText;
    private EditText pauseTimeEditText;
    private Button btnStartTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exerciseTimeEditText = (EditText) findViewById(R.id.exerciseTimeEditText);
        pauseTimeEditText = (EditText) findViewById(R.id.pauseTimeEditText);

        btnStartTimer = (Button) findViewById(R.id.btnStartTimer);
        btnStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !(pauseTimeEditText.getText().toString().equals("") || exerciseTimeEditText.getText().toString().equals("")) ){
                    Intent intent = new Intent( MainActivity.this, TimerActivity.class);//HomeActivity.class);
                    intent.putExtra("pauseTime", Integer.valueOf(pauseTimeEditText.getText().toString()));
                    intent.putExtra("exerciseTime", Integer.valueOf(exerciseTimeEditText.getText().toString()));
                    startActivity(intent);
                } else {
                    Intent intent = new Intent( MainActivity.this, TimerActivity.class);//HomeActivity.class);
                    intent.putExtra("pauseTime", 20);
                    intent.putExtra("exerciseTime", 10);
                    startActivity(intent);
                }
            }
        });
    }
}
