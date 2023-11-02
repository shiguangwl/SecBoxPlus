package com.xxhoz.secbox.module.detail.popup.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xxhoz.secbox.R;
import com.xxhoz.secbox.bean.EpsodeEntity;

import java.util.List;


public class VideoEpisodeAdapter extends RecyclerView.Adapter<VideoEpisodeAdapter.ViewHolder> {
    private Context mC;
    private List<EpsodeEntity> entities;
    private OnItemClickListener mOnItemClickListener;

    public VideoEpisodeAdapter(Context context, List<EpsodeEntity> entities) {
        this.mC = context;
        this.entities = entities;
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_episode, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.textView.setText(position + 1 + "");
        if (entities.get(position).isPlay()) {
            holder.textView.setTextColor(mC.getResources().getColor(R.color.ThemeColor));
            holder.textView.setBackground(mC.getResources().getDrawable(R.drawable.bg_video_episodes_check));
        } else {
            holder.textView.setTextColor(mC.getResources().getColor(R.color.colorWhite));
            holder.textView.setBackground(mC.getResources().getDrawable(R.drawable.bg_video_episodes_uncheck));
        }

        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_episodeNum);
        }
    }
}
