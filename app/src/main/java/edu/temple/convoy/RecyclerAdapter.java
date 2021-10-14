package edu.temple.convoy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private LayoutInflater mInflater;
    ArrayList<File> files;
    private onRecordClickListener monRecordClickListener;
    Context context;

    public RecyclerAdapter(@NonNull Context cntext, onRecordClickListener onRecordClickListeners, ArrayList<File> filings) {
        context = cntext;
        files = filings;
        monRecordClickListener = onRecordClickListeners;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.my_row,parent,false);

        return new MyViewHolder(view, monRecordClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String[]splitting = files.get(position).getName().split("~");
        String users = splitting[0];
        String timings = splitting[1];

        holder.name.setText(users);
        holder.time.setText(timings);
        holder.imageView.setImageResource(R.drawable.common_google_signin_btn_icon_light_normal);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView time;
        ImageView imageView;
        onRecordClickListener onRecordClickListenerr;

        public MyViewHolder(@NonNull View itemView, onRecordClickListener onRecordClickListeners) {
            super(itemView);
            name = itemView.findViewById(R.id.txtUserRecord);
            time = itemView.findViewById(R.id.txtTime);
            imageView = itemView.findViewById(R.id.imgIcon);
            itemView.setOnClickListener(this);
            onRecordClickListenerr = onRecordClickListeners;
        }

        @Override
        public void onClick(View v) {
            onRecordClickListenerr.onRecordClick(files.get(getAdapterPosition()));
        }
    }

    public interface onRecordClickListener{
        void onRecordClick(File audioPlay);
    }
}
