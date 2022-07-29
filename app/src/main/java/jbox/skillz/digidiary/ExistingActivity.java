package jbox.skillz.digidiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExistingActivity extends AppCompatActivity implements DroidListener {

    private DroidNet mDroidNet;
    private String chkConn;

    private RecyclerView existingList;
    private ImageView existingHome, existingAdd, existingSearch;
    private EditText existingSearchText;

    private ArrayList<Notes> existingArrayList;

    private DatabaseReference existingDatabase;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing);

        DroidNet.init(this);
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        existingArrayList = new ArrayList<>();

        existingSearchText = findViewById(R.id.existing_search_text);
        existingSearch = findViewById(R.id.existing_search);
        existingAdd = findViewById(R.id.existing_add);
        existingHome = findViewById(R.id.existing_home);
        existingList = findViewById(R.id.existing_list);
        existingList.setHasFixedSize(true);
        existingList.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();

        existingDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(uid);
        existingDatabase.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        existingSearchText.setVisibility(View.GONE);

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading All Existing Notes");
        mProgress.setMessage("Weight while we load All Notes");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        if (chkConn != null){
            if (chkConn.equals("yes")) {
                FirebaseRecyclerOptions<Notes> options =
                        new FirebaseRecyclerOptions.Builder<Notes>()
                                .setQuery(existingDatabase, Notes.class)
                                .build();

                FirebaseRecyclerAdapter<Notes, NotesViewHolder> adapter =
                        new FirebaseRecyclerAdapter<Notes, NotesViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull final Notes model) {
                                holder.exixtingListLayoutDate.setText(model.getDate());
                                holder.exixtingListLayoutTitle.setText(model.getTitle());
                                holder.exixtingListLayoutNote.setText(model.getNote());

                                mProgress.dismiss();

                                holder.exixtingListLayoutNote.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(ExistingActivity.this, PopActivity.class);
                                        i.putExtra("note_text", model.getNote());
                                        startActivity(i);
                                    }
                                });

                            }

                            @NonNull
                            @Override
                            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.existing_list_layout, parent, false);
                                NotesViewHolder viewHolder = new NotesViewHolder(view);
                                return viewHolder;
                            }
                        };
                existingList.setAdapter(adapter);
                adapter.startListening();
            } else {
                mProgress.dismiss();
                Toast.makeText(ExistingActivity.this,"Internet Rquired", Toast.LENGTH_SHORT).show();
            }

        }

        existingHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        existingAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ExistingActivity.this,CreateNew.class);
                startActivity(i);
                finish();
            }
        });

        existingSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                existingSearchText.setVisibility(View.VISIBLE);
            }
        });

        existingSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty())
                {
                    search(s.toString());
                }
                else
                {
                    search("");
                }
            }
        });

    }

    private void search(String s) {

        Query query = existingDatabase.orderByChild("title")
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren())
                {
                    existingArrayList.clear();
                    for (DataSnapshot dss: snapshot.getChildren())
                    {
                        final Notes notes = dss.getValue(Notes.class);
                        existingArrayList.add(notes);
                    }
                    NotesAdapter notesAdapter = new NotesAdapter(getApplicationContext(), existingArrayList);
                    existingList.setAdapter(notesAdapter);
                    notesAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder
    {
        TextView exixtingListLayoutDate,exixtingListLayoutTitle;
        TextView exixtingListLayoutNote;
        View mView;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            exixtingListLayoutDate = itemView.findViewById(R.id.existing_list_layout_date);
            exixtingListLayoutTitle = itemView.findViewById(R.id.existing_list_layout_title);
            exixtingListLayoutNote = itemView.findViewById(R.id.existing_list_layout_note);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
