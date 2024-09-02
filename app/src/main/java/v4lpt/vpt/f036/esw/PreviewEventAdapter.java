package v4lpt.vpt.f036.esw;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.time.format.DateTimeFormatter;

public class PreviewEventAdapter extends RecyclerView.Adapter<PreviewEventAdapter.EventViewHolder> {
    private Context context;
    private Event previewEvent;
    private OnPreviewClickListener onPreviewClickListener;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EE)");
    private int itemLayout;

    public interface OnPreviewClickListener {
        void onPreviewClick();
    }

    public PreviewEventAdapter(Context context, OnPreviewClickListener listener) {
        this.context = context;
        this.onPreviewClickListener = listener;
        this.itemLayout = R.layout.item_event; // Default to Mode A layout
    }

    public void setItemLayout(int layout) {
        if (this.itemLayout != layout) {
            this.itemLayout = layout;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(itemLayout, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        if (previewEvent != null) {
            holder.titleTextView.setText(previewEvent.getTitle());
            long daysLeft = previewEvent.getDaysLeft();
            holder.daysLeftTextView.setText(daysLeft + " days left");

            String formattedDate = previewEvent.getDate().format(dateFormatter);
            holder.dateTextView.setText(formattedDate);

            if (previewEvent.getBackgroundImagePath() != null) {
                Uri imageUri = Uri.parse(previewEvent.getBackgroundImagePath());
                Glide.with(context)
                        .load(imageUri)
                        .centerCrop()
                        .into(holder.backgroundImageView);
            } else {
                holder.backgroundImageView.setImageResource(R.drawable.default_event_background);
            }

            holder.itemView.setOnClickListener(v -> {
                if (onPreviewClickListener != null) {
                    onPreviewClickListener.onPreviewClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return previewEvent != null ? 1 : 0;
    }

    public void updatePreviewEvent(Event event) {
        this.previewEvent = event;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView backgroundImageView;
        TextView titleTextView;
        TextView daysLeftTextView;
        TextView dateTextView;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            backgroundImageView = itemView.findViewById(R.id.backgroundImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            daysLeftTextView = itemView.findViewById(R.id.daysLeftTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}