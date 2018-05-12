package com.example.frank47.parkit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


//describes the initial activity
public class MainActivity extends AppCompatActivity {
    private Button mCustomer,mPsowner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCustomer=(Button)findViewById(R.id.Customer_button);
        mPsowner=(Button)findViewById(R.id.Parking_space_owner_button);

        //customer button onclicklistener
        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //launches the customerloginactivity intent
                Intent intent= new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        //parking_space button onclicklistener
        mPsowner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, PsownerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }


}
