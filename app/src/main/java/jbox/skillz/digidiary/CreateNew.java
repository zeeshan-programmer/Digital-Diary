package jbox.skillz.digidiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateNew extends AppCompatActivity implements DroidListener {

    private DroidNet mDroidNet;
    private String chkConn;

    private Toolbar mToolbar;
    private TextView newDateTime;
    private EditText newNoteTitle;
    private TextInputLayout newNote;
    private Button newCancel, newCreate;
    private String saveCurrentTime, saveCurrentDate;

    private ProgressDialog mProgress;
    private DatabaseReference mAccountDatabase, mAccountNotes, mAccountPublished;
    private DatabaseReference mRootRef;

    private int nt,pb;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new);

        DroidNet.init(this);
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        newDateTime = findViewById(R.id.new_date_time);
        newNoteTitle = findViewById(R.id.new_note_title);
        newNote = findViewById(R.id.new_note);
        newCancel = findViewById(R.id.new_cancel);
        newCreate = findViewById(R.id.new_create);

        mToolbar = findViewById(R.id.create_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Make Note");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        uid = current_user.getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAccountDatabase = FirebaseDatabase.getInstance().getReference().child("Accounts");
//        mAccountNotes = FirebaseDatabase.getInstance().getReference().child("Accounts").child(uid);
//        mAccountPublished = FirebaseDatabase.getInstance().getReference().child("Accounts");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        newDateTime.setText(saveCurrentTime +" _ " + saveCurrentDate);

        newCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newNoteTitle.setText("");
                newNote.getEditText().setText("");
            }
        });

        newCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = new ProgressDialog(CreateNew.this);
                mProgress.setTitle("creating Note");
                mProgress.setMessage("please weight while we create your Note!");
                mProgress.show();
                mProgress.setCanceledOnTouchOutside(false);

                if(chkConn != null)
                {
                    if(chkConn.equals("yes"))
                    {
                        String datetime = newDateTime.getText().toString();
                        String title = newNoteTitle.getText().toString();
                        String note = newNote.getEditText().getText().toString();

                        if (!TextUtils.isEmpty(datetime) || !TextUtils.isEmpty(title) || !TextUtils.isEmpty(note) )
                        {
                            create_note(datetime, title, note);
                        }
                        else{
                            Toast.makeText(CreateNew.this,"All fields are required", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        mProgress.dismiss();
                        Toast.makeText(CreateNew.this,"Internet Required", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    private void create_note(final String datetime, final String title, final String note)
    {
        String current_user_ref = "Notes/" + uid;
        DatabaseReference new_note_push = mRootRef.child("Notes")
                .child(uid).push();
        String push_id = new_note_push.getKey();

        Map userMap = new HashMap<>();
        userMap.put("userID",uid);
        userMap.put("noteID",push_id);
        userMap.put("date", datetime);
        userMap.put("title",title);
        userMap.put("note", note);

        Map note_user_map = new HashMap();
        note_user_map.put(current_user_ref + "/" + push_id, userMap);
        mRootRef.updateChildren(note_user_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null){
                    mProgress.dismiss();
                    Toast.makeText(CreateNew.this, error.getMessage() , Toast.LENGTH_LONG).show();
                }
                else
                {
                    mProgress.dismiss();
                    CharSequence options[] = new CharSequence[]{"Yes", "No"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateNew.this);
                    builder.setTitle("Publish this Note... ?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0){
                                if(chkConn != null)
                                {
                                    if(chkConn.equals("yes"))
                                    {
                                        publish_note(note);
                                    }
                                    else
                                    {
                                        Toast.makeText(CreateNew.this,"Internet Required", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    });
                    builder.show();
                }
            }
        });

    }

    private void publish_note(final String note)
    {
        mAccountDatabase.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String location = dataSnapshot.child("location").getValue().toString();

                String current_user_ref = "Published";
                DatabaseReference new_note_push = mRootRef.child("Published")
                        .child(uid).push();
                String push_id = new_note_push.getKey();

                Map userMap = new HashMap<>();
                userMap.put("userID",uid);
                userMap.put("noteID",push_id);
                userMap.put("name", name);
                userMap.put("image",image);
                userMap.put("note", note);
                userMap.put("location", location);
                Map note_user_map = new HashMap();
                note_user_map.put(current_user_ref + "/" +push_id, userMap);

                mRootRef.updateChildren(note_user_map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null){
                            Toast.makeText(CreateNew.this, error.getMessage() , Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(CreateNew.this,"Note Published.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgress.dismiss();
            }
        });

    }

//    private void update_published()
//    {
//        mAccountPublished.child(uid).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String published = dataSnapshot.child("published").getValue().toString();
//                pb = Integer.valueOf(published);
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                mProgress.dismiss();
//            }
//        });
//
//        HashMap<String, Object> updating = new HashMap<>();
//            updating.put("published", String.valueOf(pb+1));
//
//        mAccountPublished.child(uid).updateChildren(updating);
//    }
//
//    private void update_notes()
//    {
//        mAccountNotes.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                String notes = dataSnapshot.child("notes").getValue().toString();
//                nt = Integer.valueOf(notes);
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                mProgress.dismiss();
//            }
//        });
//
//        HashMap<String, Object> updating = new HashMap<>();
//            updating.put("notes", String.valueOf(nt+1));
//
//        mAccountNotes.updateChildren(updating);
//    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (isConnected) {
            chkConn = "yes";
        } else {
            chkConn = "no";
        }
    }
}
