package v4lpt.vpt.f036.esw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WidgetConfigAdapter extends RecyclerView.Adapter<WidgetConfigAdapter.EventViewHolder> {
    private Context context;
    private List<Event> eventList;
    private OnEventSelectedListener onEventSelectedListener;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EE)");
    private int itemLayout = R.layout.item_event; // Default to Mode A layout

    public interface OnEventSelectedListener {
        void onEventSelected(Event event);
    }

    public WidgetConfigAdapter(Context context, List<Event> eventList, OnEventSelectedListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventSelectedListener = listener;
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
        Event event = eventList.get(position);
        holder.titleTextView.setText(event.getTitle());
        long daysLeft = event.getDaysLeft();
        String daysText;
        if (daysLeft < 0) {
            daysText = Math.abs(daysLeft) + (Math.abs(daysLeft) == 1 ? " day ago" : " days ago");
        } else if (daysLeft == 0) {
            daysText = "Today";
        } else {
            daysText = daysLeft + (daysLeft == 1 ? " day left" : " days left");
        }
        holder.daysLeftTextView.setText(daysText);
        String formattedDate = event.getDate().format(dateFormatter);
        holder.dateTextView.setText(formattedDate);

        if (event.getBackgroundImagePath() != null) {
            Glide.with(context)
                    .load(event.getBackgroundImagePath())
                    .centerCrop()
                    .into(holder.backgroundImageView);
        } else {
            holder.backgroundImageView.setImageResource(R.drawable.default_event_background);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onEventSelectedListener != null) {
                onEventSelectedListener.onEventSelected(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
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