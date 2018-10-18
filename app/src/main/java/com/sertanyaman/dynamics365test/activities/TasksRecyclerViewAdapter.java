package com.sertanyaman.dynamics365test.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.CardView;

import com.sertanyaman.dynamics365test.R;
import com.sertanyaman.dynamics365test.models.Task;

import java.util.List;

public class TasksRecyclerViewAdapter extends RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder> {
    private List<Task> tasks;
    private Context context;

    public TasksRecyclerViewAdapter(List<Task> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false );

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task curTask = tasks.get(position);

        holder.tvTitle.setText(curTask.getCustName());
        holder.tvDate.setText(curTask.getReadableDate());
        holder.tvOverview.setText(curTask.getAddress());

        if(curTask.isNewRecord()) {
            holder.ivTaskImage.setImageResource(R.drawable.ic_mail_black_24dp);
        }
        else
        {
            holder.ivTaskImage.setImageResource(0);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public Context getContext() {
        return context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView ivTaskImage;
        TextView tvTitle;
        TextView tvOverview;
        TextView tvDate;
        CardView cvMovie;

        ViewHolder(View view) {
            super(view);
            cvMovie = (CardView) view ;
            ivTaskImage = (ImageView) view.findViewById(R.id.ivTaskImage);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvOverview = (TextView) view.findViewById(R.id.tvOverview);
            tvDate = (TextView) view.findViewById(R.id.tvDate);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Task task = tasks.get(getAdapterPosition());

            Intent intent = new Intent(getContext(), ShowInMapsActivity.class);
            intent.putExtra("TASK", (Parcelable) task);
            getContext().startActivity(intent);

        }
    }

    public void clear() {
        tasks.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Task> list) {
        tasks.addAll(list);
        notifyDataSetChanged();
    }
}
