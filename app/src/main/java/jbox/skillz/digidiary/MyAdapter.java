package jbox.skillz.digidiary;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyAdapterViewHolder>
{

    public Context c;
    public ArrayList<Published> publishedArrayList;

    public MyAdapter(Context c, ArrayList<Published> publishedArrayList) {
        this.c = c;
        this.publishedArrayList = publishedArrayList;
    }

    @Override
    public int getItemCount() {
        return publishedArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public MyAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.published_list_layout, parent, false);

        return new MyAdapterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyAdapterViewHolder holder, int position)
    {
        final Published published = publishedArrayList.get(position);

        holder.pNote.setText(published.getNote());
        holder.pName.setText(published.getName());
        holder.pLoc.setText(published.getLocation());
        Picasso.get().load(published.getImage()).placeholder(R.drawable.dp).into(holder.pImage);

        holder.pNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, PopActivity.class);
                i.putExtra("note_text", published.getNote());
                holder.itemView.getContext().startActivity(i);
            }
        });
        holder.pLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.pLikes.setBackgroundResource(R.drawable.liked_24dp);
            }
        });

    }

    public class MyAdapterViewHolder extends RecyclerView.ViewHolder
    {

        public TextView pName, pLoc, pNote;
        public ImageView pLikes;
        public CircleImageView pImage;


        public MyAdapterViewHolder(View itemView)
        {
            super(itemView);

            pName = itemView.findViewById(R.id.published_list_layout_name);
            pLoc = itemView.findViewById(R.id.published_list_layout_location);
            pNote = itemView.findViewById(R.id.published_list_layout_note);
            pImage = itemView.findViewById(R.id.published_list_layout_img);
            pLikes = itemView.findViewById(R.id.published_list_layout_likes);
        }
    }

}
