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
import android.text.method.ScrollingMovementMethod;
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

public class PublishedActivity extends AppCompatActivity implements DroidListener {

    private DroidNet mDroidNet;
    private String chkConn;

    private RecyclerView publishedList;
    private ImageView publishedHome, publishedSearch, publishedLikes;
    private EditText publishedSearchText;

    private ArrayList<Published> publishedArrayList;

    private DatabaseReference publishedDatabase;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_published);

        DroidNet.init(this);
        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

        publishedArrayList = new ArrayList<>();

        publishedSearchText = findViewById(R.id.published_search_text);
        publishedHome = findViewById(R.id.published_home);
        publishedSearch = findViewById(R.id.published_search);
        publishedList = findViewById(R.id.published_list);
        publishedList.setHasFixedSize(true);
        publishedList.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = current_user.getUid();

        publishedDatabase = FirebaseDatabase.getInstance().getReference().child("Published");
        publishedDatabase.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        publishedSearchText.setVisibility(View.GONE);
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading published Notes");
        mProgress.setMessage("Weight while we load All Notes");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        if (chkConn != null){
            if (chkConn.equals("yes")) {
                FirebaseRecyclerOptions<Published> options =
                        new FirebaseRecyclerOptions.Builder<Published>()
                                .setQuery(publishedDatabase, Published.class)
                                .build();

                FirebaseRecyclerAdapter<Published, PublishedViewHolder> adapter =
                        new FirebaseRecyclerAdapter<Published, PublishedViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull final PublishedViewHolder holder, int position, @NonNull final Published model) {
                                holder.publishedListLayoutNote.setText(model.getNote());
                                holder.publishedListname.setText(model.getName());
                                holder.publishedListLayoutLocation.setText(model.getLocation());
                                Picasso.get().load(model.getImage()).placeholder(R.drawable.dp).into(holder.publishedListLayoutImage);

                                mProgress.dismiss();

                                holder.publishedListLayoutNote.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(PublishedActivity.this, PopActivity.class);
                                        i.putExtra("note_text", model.getNote());
                                        startActivity(i);
                                    }
                                });
                                holder.publishedListLayoutLikes.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        holder.publishedListLayoutLikes.setBackgroundResource(R.drawable.liked_24dp);
                                    }
                                });

                            }

                            @NonNull
                            @Override
                            public PublishedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.published_list_layout, parent, false);
                                PublishedViewHolder viewHolder = new PublishedViewHolder(view);
                                return viewHolder;
                            }
                        };
                publishedList.setAdapter(adapter);
                adapter.startListening();
            } else {
                mProgress.dismiss();
                Toast.makeText(PublishedActivity.this,"Internet Rquired", Toast.LENGTH_SHORT).show();
            }

        }

        publishedHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        publishedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishedSearchText.setVisibility(View.VISIBLE);
            }
        });

        publishedSearchText.addTextChangedListener(new TextWatcher() {
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

        Query query = publishedDatabase.orderByChild("name")
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren())
                {
                    publishedArrayList.clear();
                    for (DataSnapshot dss: snapshot.getChildren())
                    {
                        final Published published = dss.getValue(Published.class);
                        publishedArrayList.add(published);
                    }

                    MyAdapter myAdapter = new MyAdapter(getApplicationContext(), publishedArrayList);
                    publishedList.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static class PublishedViewHolder extends RecyclerView.ViewHolder
    {
        TextView publishedListname,publishedListLayoutLocation;
        TextView publishedListLayoutNote;
        CircleImageView publishedListLayoutImage;
        ImageView publishedListLayoutLikes;
        View mView;

        public PublishedViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            publishedListLayoutImage = itemView.findViewById(R.id.published_list_layout_img);
            publishedListname = itemView.findViewById(R.id.published_list_layout_name);
            publishedListLayoutLocation = itemView.findViewById(R.id.published_list_layout_location);
            publishedListLayoutNote = itemView.findViewById(R.id.published_list_layout_note);
            publishedListLayoutLikes = itemView.findViewById(R.id.published_list_layout_likes);


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
