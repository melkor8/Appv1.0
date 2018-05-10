package com.example.frank47.parkit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PaymentActivity extends AppCompatActivity {

    private TextView mAmount_textview,mDuration_textview;
    private String duration;
    private double amount;
    private Button mEnd_Parking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mAmount_textview = (TextView) findViewById(R.id.amount_textview);
        mDuration_textview = (TextView) findViewById(R.id.duration_textview);
        mEnd_Parking= (Button) findViewById(R.id.end_parking_button);

        calculateamount();

        mEnd_Parking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             reassigndb();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    private void reassigndb() {

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("parkingspaces");
        DatabaseReference dbrefremove = FirebaseDatabase.getInstance().getReference().child("assignment");


        final GeoFire geofire = new GeoFire(dbref);
        final GeoFire geofireremove = new GeoFire(dbrefremove);



        geofire.setLocation(CustomerMapsActivity.rentId, new GeoLocation(CustomerMapsActivity.parking_space_latlng.latitude,CustomerMapsActivity.parking_space_latlng.longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });

        geofireremove.removeLocation(CustomerMapsActivity.rentId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                System.out.println("gvgvgvl removed");

            }
        });
    }

    private void calculateamount() {
        switch(CustomerMapsActivity.spinner_selection){
            case 0 : {
                duration = new String("30 minutes");
                amount=20;
                break;
            }
            case 1 : {
                duration = new String("1 hour");
                amount=30;
                break;
            }
            case 2 : {
                duration = new String("2 hours");
                amount=40;
                break;
            }
            case 3 : {
                duration = new String("5 hours");
                amount=60;
                break;
            }
            case 4 : {
                duration = new String("10 hours");
                amount=110;
                break;
            }
            case 5 : {
                duration = new String("12 hours");
                amount=150;
                break;
            }
            default : {

            }
        }

        mAmount_textview.setText("Rs. "+amount);
        mDuration_textview.setText(duration);



    }
}
