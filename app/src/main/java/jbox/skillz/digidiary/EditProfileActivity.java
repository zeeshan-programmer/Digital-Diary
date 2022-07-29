package jbox.skillz.digidiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements DroidListener {

    private DroidNet mDroidNet;
    private String chkConn;

    private Toolbar mToolbar;
    private ImageView editPfImgBtn;
    private CircleImageView editProfileImg;
    private Button editCancelBtn, editUpdateBtn;
    private TextInputLayout editUsername, editLoc, editEmail, editSong;

    private ProgressDialog mProgress;
    private Uri fileUri;

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrent_user;
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        DroidNet.init(this);
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        mToolbar = findViewById(R.id.edit_pf_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editPfImgBtn = findViewById(R.id.edit_pf_img_btn);
        editProfileImg = findViewById(R.id.edit_profile_image);
        editUpdateBtn = findViewById(R.id.edit_update);
        editCancelBtn = findViewById(R.id.edit_cancel);
        editUsername = findViewById(R.id.edit_display_name);
        editLoc = findViewById(R.id.edit_loc);
        editEmail = findViewById(R.id.edit_email);
        editSong = findViewById(R.id.edit_song);

        mProgress = new ProgressDialog(this);

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Accounts").child(mCurrent_user.getUid());
        mUserDatabase.keepSynced(true);
        mImageStorage = FirebaseStorage.getInstance().getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String image = dataSnapshot.child("image").getValue().toString();
                String username = dataSnapshot.child("name").getValue().toString();
                String location = dataSnapshot.child("location").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String song = dataSnapshot.child("song").getValue().toString();

                editUsername.getEditText().setText(username);
                editLoc.getEditText().setText(location);
                editEmail.getEditText().setText(email);
                editSong.getEditText().setText(song);
                Picasso.get().load(image).placeholder(R.drawable.dp).into(editProfileImg);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        editPfImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "SELECT IMAGE"), 438);

            }
        });
        editCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(EditProfileActivity.this,ProfileActivity.class);
//                startActivity(i);
                finish();
            }
        });
        editUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chkConn != null)
                {
                    if(chkConn.equals("yes"))
                    {
                        String username = editUsername.getEditText().getText().toString();
                        String location = editLoc.getEditText().getText().toString();
                        String song = editSong.getEditText().getText().toString();

                        if (!TextUtils.isEmpty(username) || !TextUtils.isEmpty(location) || !TextUtils.isEmpty(song))
                        {
                            update_user(username, location, song);
                        }
                        else{
                            Toast.makeText(EditProfileActivity.this,"All fields are required", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        mProgress.dismiss();
                        Toast.makeText(EditProfileActivity.this,"Internet Required", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    private void update_user(final String username, final String location, String song)
    {
        HashMap<String, Object> updating = new HashMap<>();
        updating.put("name", username);
        updating.put("location", location);
        updating.put("song", song);

        mUserDatabase.updateChildren(updating).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(EditProfileActivity.this,"Profile Updated", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this,"Oops! something went wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK){

            fileUri = data.getData();

            CropImage.activity(fileUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                mProgress.setTitle("Updating Image");
                mProgress.setMessage("Please weight while update finishes");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                if (chkConn != null)
                {
                    if (chkConn.equals("yes"))
                    {
                        Uri resultUri = result.getUri();
                        String current_user_id = mCurrent_user.getUid();

                        final StorageReference filePath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {

                                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String link = uri.toString();

                                            mUserDatabase.child("image").setValue(link).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        mProgress.dismiss();
                                                        Toast.makeText(EditProfileActivity.this, "Profile Image updated", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mProgress.dismiss();
                                            Toast.makeText(EditProfileActivity.this, "Error in Updating Image", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {
                                    mProgress.dismiss();
                                    Toast.makeText(EditProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else
                    {
                        mProgress.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Internet Required", Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Exception error = result.getError();

            }

        }
//        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {
//
//            mProgress.setTitle("Updating Image");
//            mProgress.setMessage("Please weight while update finishes");
//            mProgress.setCanceledOnTouchOutside(false);
//            mProgress.show();
//
//            fileUri = data.getData();
//            Picasso.get().load(fileUri).placeholder(R.drawable.dp).into(editProfileImg);
//            editProfileImg.invalidate();
//            mProgress.dismiss();
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

