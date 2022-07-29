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

public class LoginActivity extends AppCompatActivity implements DroidListener {

    private DroidNet mDroidNet;
    private String chkConn;

    private TextView loginNewAccount;
    private Button loginBtn;
    private EditText loginEmail, loginPassword;

    private ProgressDialog mProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DroidNet.init(this);
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        loginNewAccount = findViewById(R.id.login_new_account);
        loginBtn = findViewById(R.id.login_btn);
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        loginNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setTitle("Logging In");
                mProgress.setMessage("Please weight while we check your credentials");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                if(chkConn != null)
                {
                    if(chkConn.equals("yes"))
                    {
                        String email = loginEmail.getText().toString();
                        String password = loginPassword.getText().toString();
                        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                            login_User(email,password);
                        }
                    }
                    else
                    {
                        mProgress.dismiss();
                        Toast.makeText(LoginActivity.this,"Internet Required", Toast.LENGTH_LONG).show();
                    }
                }
//                if(chkConn != null)
//                {
//                    if(chkConn.equals("yes"))
//                    {
//
//                    }
//                    else
//                    {
//                        mProgress.dismiss();
//                        Toast.makeText(LoginActivity.this,"Internet Required", Toast.LENGTH_LONG).show();
//                    }
//                }
            }
        });

    }

    private void login_User(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful())
                {
                    mProgress.dismiss();
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
                else
                {
                    mProgress.dismiss();
                    Toast.makeText(LoginActivity.this,"Cannot Sign In. Please check the form and try again.", Toast.LENGTH_LONG).show();
                }

            }
        });

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
