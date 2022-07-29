package jbox.skillz.digidiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity implements DroidListener {

    private DroidNet mDroidNet;
    private String chkConn;

    private ImageView changeProfile, logOut, userProfileImg;
    private TextView userName, userLoc, notesNo, publishedNo, likesNo;
    public Uri fileUri;

    private ProgressDialog mProgress;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrent_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        DroidNet.init(this);
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        changeProfile = findViewById(R.id.change_profile);
        logOut = findViewById(R.id.log_out);
        userProfileImg = findViewById(R.id.user_profile_image);
        userName = findViewById(R.id.user_name);
        userLoc = findViewById(R.id.user_loc);
        notesNo = findViewById(R.id.notes_no);
        publishedNo = findViewById(R.id.published_no);
        likesNo = findViewById(R.id.likes_no);

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Accounts").child(mCurrent_user.getUid());
        mUserDatabase.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading User Data");
        mProgress.setMessage("Weight while we load the User Data.");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String username = dataSnapshot.child("name").getValue().toString();
                String location = dataSnapshot.child("location").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String notes = dataSnapshot.child("notes").getValue().toString();
                String published = dataSnapshot.child("published").getValue().toString();
                String likes = dataSnapshot.child("likes").getValue().toString();

                userName.setText(username);
                userLoc.setText(location);
                notesNo.setText(notes);
                publishedNo.setText(published);
                likesNo.setText(likes);
                Picasso.get().load(image).placeholder(R.drawable.dp).into(userProfileImg);
                mProgress.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgress.dismiss();
            }
        });
        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(i);
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{"Logout", "cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Are you Sure ?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            if(chkConn != null)
                            {
                                if(chkConn.equals("yes"))
                                {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent i = new Intent(ProfileActivity.this,LoginActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(ProfileActivity.this,"Internet Required", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
                builder.show();
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
