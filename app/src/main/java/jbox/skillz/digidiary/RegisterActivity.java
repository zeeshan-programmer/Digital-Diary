package jbox.skillz.digidiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements DroidListener {

    private DroidNet mDroidNet;
    private String chkConn;

    private TextView registerLoginAccount;
    private Button registerBtn;
    private EditText registerUsername, registerEmail, registerLocation, registerPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        DroidNet.init(this);
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        registerLoginAccount = findViewById(R.id.register_login_account);
        registerUsername = findViewById(R.id.register_username);
        registerEmail = findViewById(R.id.register_email);
        registerLocation = findViewById(R.id.register_location);
        registerPassword = findViewById(R.id.register_password);
        registerBtn = findViewById(R.id.register_btn);

        mProgress = new ProgressDialog(this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        registerLoginAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setTitle("Registring User");
                mProgress.setMessage("please weight while we create your account !");
                mProgress.show();
                mProgress.setCanceledOnTouchOutside(false);

                if(chkConn != null)
                {
                    if(chkConn.equals("yes"))
                    {
                        String username = registerUsername.getText().toString();
                        String email = registerEmail.getText().toString();
                        String location = registerLocation.getText().toString();
                        String password = registerPassword.getText().toString();

                        if (!TextUtils.isEmpty(username) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(location) || !TextUtils.isEmpty(password))
                        {
                            register_user(username, email, location, password);
                        }
                        else{
                            Toast.makeText(RegisterActivity.this,"All fields are required", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        mProgress.dismiss();
                        Toast.makeText(RegisterActivity.this,"Internet Required", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

    }

    private void register_user(final String username, final String email, final String location, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Accounts").child(uid);

                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("name",username);
                    userMap.put("image","default");
                    userMap.put("email", email);
                    userMap.put("location",location);
                    userMap.put("notes", String.valueOf(0));
                    userMap.put("published", String.valueOf(0));
                    userMap.put("likes",String.valueOf(0));
                    userMap.put("song","slipery");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mProgress.dismiss();
                                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            }
                        }
                    });
                }
                else
                {
                    mProgress.hide();
                    Toast.makeText(RegisterActivity.this,"Cannot Sign In. Please check the form and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (isConnected) {
            chkConn = "yes";
        } else {
            chkConn = "no";
        }
    }
}
