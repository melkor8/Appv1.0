package com.example.frank47.parkit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PsownerLoginActivity extends AppCompatActivity {

    private EditText mEmail,mPassword;
    private Button mLogin,mRegistration;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psowner_login);

        mAuth=FirebaseAuth.getInstance();

        firebaseAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user!=null){
                    final Intent intent= new Intent(PsownerLoginActivity.this, PsownerMapsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmail=(EditText) findViewById(R.id.email_edittext);
        mPassword=(EditText) findViewById(R.id.password_edittext);

        mLogin=(Button)findViewById(R.id.login_button);
        mRegistration=(Button)findViewById(R.id.registration_button);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String password= mPassword.getText().toString().trim();

                if(!email.matches("") && !password.matches("")){
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(PsownerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!(task.isSuccessful())){
                                Toast.makeText(PsownerLoginActivity.this, "registration error", Toast.LENGTH_SHORT).show();
                            }else{
                                String user_Id= mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Parking_space_owner").child(user_Id);
                                current_user_db.setValue(true);

                            }
                        }
                    });
                }else{
                    Toast.makeText(PsownerLoginActivity.this, "Please provide necessary details!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String password= mPassword.getText().toString().trim();

                if(!email.matches("") && !password.matches("")){
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(PsownerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!(task.isSuccessful())){
                                Toast.makeText(PsownerLoginActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(PsownerLoginActivity.this, "Please provide necessary details!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}




