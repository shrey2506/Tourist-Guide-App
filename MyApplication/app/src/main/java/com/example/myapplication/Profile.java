package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Profile extends AppCompatActivity {
    private EditText name;
    private EditText phoneNo;
    private Button update;
    private DatabaseReference ref;
    private FirebaseUser user;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        name=findViewById(R.id.full_name);
        phoneNo=findViewById(R.id.phoneNumber);
        update=findViewById(R.id.UpdateProfile);
        ref= FirebaseDatabase.getInstance().getReference();
        user= FirebaseAuth.getInstance().getCurrentUser();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserProfile();
            }
        });
        retrieveUserInfo();
    }



    private void updateUserProfile() {
        String Name=name.getText().toString();
        String PhoneNumber=phoneNo.getText().toString();
        if(TextUtils.isEmpty(Name)){
            name.setError("Please Enter A Valid Name");
            name.requestFocus();
        }
        if(TextUtils.isEmpty(PhoneNumber)&&PhoneNumber.length()<10){
            phoneNo.setError("Please Enter A Valid Phone Number");
            phoneNo.requestFocus();
        }
        else{
            HashMap<String,String> profileMap=new HashMap<>();
            profileMap.put("name",Name);
            profileMap.put("phoneNo",PhoneNumber);
            ref.child("Users").child(uid).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Intent intent = new Intent(Profile.this, Homepage.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                Toast.makeText(Profile.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message=task.getException().toString();
                                Toast.makeText(Profile.this,"Error: "+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }
    private void retrieveUserInfo() {
        uid=user.getUid();
        ref.child("Users").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) ){
                            String Rname=dataSnapshot.child("name").getValue().toString();
                           String RphoneNumber=dataSnapshot.child("phoneNo").getValue().toString();
                            name.setText(Rname);
                            phoneNo.setText(RphoneNumber);
                        }
                        else{
                            Toast.makeText(Profile.this,"Please Update Your Profile",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
