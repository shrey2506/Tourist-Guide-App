package com.example.myapplication;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {
       private TextView newUser;
       private EditText email;
       private  EditText password;
       private Button login;
       private FirebaseUser user;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newUser=(TextView) findViewById(R.id.newUser);
        email=(EditText) findViewById(R.id.emailLogin);
        password=(EditText)findViewById(R.id.passwordLogin);
        login=(Button)findViewById(R.id.loginButton);
        loadingBar=new ProgressDialog(this);
        mAuth= FirebaseAuth.getInstance();
        user= FirebaseAuth.getInstance().getCurrentUser();

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Registration.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email=email.getText().toString();
                String Password=password.getText().toString();
                Login(Email,Password);
            }
        });

    }
    private void Login(String Email,String Password){
        if(TextUtils.isEmpty(Email)){
            Toast.makeText(MainActivity.this,"Enter your Email Please!",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(Password)){
            Toast.makeText(MainActivity.this,"Enter your Password Please!",Toast.LENGTH_LONG).show();
        }
        else{
            loadingBar.setTitle("User Login");
            loadingBar.setMessage("Logging In ! ");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(Email,Password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this,"Login Succesfull!",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent=new Intent(MainActivity.this,Homepage.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(MainActivity.this,"Login Failed!",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
    @Override
      protected void onStart(){
        super.onStart();
        if(user!=null){
            Intent intent=new Intent(MainActivity.this,Homepage.class);
            startActivity(intent);
        }
    }


}
