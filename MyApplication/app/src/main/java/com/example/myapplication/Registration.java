package com.example.myapplication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {
    EditText emailAddress, password;
    private Button registorButton;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference ref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth=FirebaseAuth.getInstance();

        emailAddress=(EditText) findViewById(R.id.emailRegister);
        password=(EditText) findViewById(R.id.passwordRegister);
        loadingBar=new ProgressDialog(this);
        ref= FirebaseDatabase.getInstance().getReference();


        registorButton=(Button) findViewById(R.id.registerButton);
        registorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String email=emailAddress.getText().toString();
               String Password=password.getText().toString();
               Register(email,Password);
            }
        });
    }
    private void Register( String email, String Password){
         if(TextUtils.isEmpty(email)){
            Toast.makeText(Registration.this,"Enter your Email Please!",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(Password)){
            Toast.makeText(Registration.this,"Enter your Password Please!",Toast.LENGTH_LONG).show();
        }
        else{
            loadingBar.setTitle("User Registration");
            loadingBar.setMessage("Please wait, while we register you! ");
            loadingBar.show();
              mAuth.createUserWithEmailAndPassword(email,Password)
                      .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                          @Override
                          public void onComplete(@NonNull Task<AuthResult> task) {
                              if (task.isSuccessful()) {
                                  Toast.makeText(Registration.this,"Registration Succesfull!",Toast.LENGTH_SHORT).show();
                                  loadingBar.dismiss();
                                  Intent intent=new Intent(Registration.this,Homepage.class);
                                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                  startActivity(intent);
                              }
                              else{
                                  Toast.makeText(Registration.this,"Registration Failed!",Toast.LENGTH_SHORT).show();
                                  loadingBar.dismiss();
                              }

                          }
                      });
        }
    }



}
