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

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesAdapterViewHolder>
{

    public Context c;
    public ArrayList<Notes> existingArrayList;

    public NotesAdapter(Context c, ArrayList<Notes> existingArrayList) {
        this.c = c;
        this.existingArrayList = existingArrayList;
    }

    @Override
    public int getItemCount() {
        return existingArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public NotesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.existing_list_layout, parent, false);

        return new NotesAdapterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final NotesAdapterViewHolder holder, int position)
    {
        final Notes notes = existingArrayList.get(position);

        holder.eDate.setText(notes.getDate());
        holder.eTitle.setText(notes.getTitle());
        holder.eNote.setText(notes.getNote());

        holder.eNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, PopActivity.class);
                i.putExtra("note_text", notes.getNote());
                holder.itemView.getContext().startActivity(i);
            }
        });

    }

    public class NotesAdapterViewHolder extends RecyclerView.ViewHolder
    {

        public TextView eDate, eNote, eTitle;

        public NotesAdapterViewHolder(View itemView)
        {
            super(itemView);

            eDate = itemView.findViewById(R.id.existing_list_layout_date);
            eTitle = itemView.findViewById(R.id.existing_list_layout_title);
            eNote = itemView.findViewById(R.id.existing_list_layout_note);
        }
    }

}
