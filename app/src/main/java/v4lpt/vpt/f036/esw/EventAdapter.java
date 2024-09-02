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
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EVENT = 0;
    private static final int TYPE_ADD_EVENT = 1;
    private static final int TYPE_GUIDE = 2;

    private Context context;
    private List<Event> eventList;
    private OnEventClickListener onEventClickListener;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EE)");
    private int itemLayout = R.layout.item_event; // Default to Mode A layout

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onAddEventClick();
    }

    public EventAdapter(Context context, List<Event> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventClickListener = listener;
    }

    public void setItemLayout(int layout) {
        if (this.itemLayout != layout) {
            this.itemLayout = layout;
            notifyDataSetChanged(); // Force a complete redraw
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EVENT) {
            View view = LayoutInflater.from(context).inflate(itemLayout, parent, false);
            return new EventViewHolder(view);
        } else if (viewType == TYPE_ADD_EVENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_add_event, parent, false);
            return new AddEventViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_guide, parent, false);
            return new GuideViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EventViewHolder) {
            Event event = eventList.get(position);
            EventViewHolder eventHolder = (EventViewHolder) holder;

            eventHolder.titleTextView.setText(event.getTitle());
            long daysLeft = event.getDaysLeft();
            eventHolder.daysLeftTextView.setText(daysLeft + " days left");

            String formattedDate = event.getDate().format(dateFormatter);
            eventHolder.dateTextView.setText(formattedDate);

            if (event.getBackgroundImagePath() != null) {
                Uri imageUri = Uri.parse(event.getBackgroundImagePath());
                Glide.with(context)
                        .load(imageUri)
                        .centerCrop()
                        .into(eventHolder.backgroundImageView);
            } else {
                eventHolder.backgroundImageView.setImageResource(R.drawable.default_event_background);
            }

            holder.itemView.setOnClickListener(v -> {
                if (onEventClickListener != null) {
                    onEventClickListener.onEventClick(event);
                }
            });
        } else if (holder instanceof AddEventViewHolder) {
            AddEventViewHolder addEventHolder = (AddEventViewHolder) holder;
            addEventHolder.itemView.setOnClickListener(v -> {
                if (onEventClickListener != null) {
                    onEventClickListener.onAddEventClick();
                }
            });
        } else if (holder instanceof GuideViewHolder) {
            GuideViewHolder guideHolder = (GuideViewHolder) holder;
            if (eventList.size() == 0) {
                guideHolder.guideTextView.setText(R.string.introduction);
            } else if (eventList.size() == 1) {
                guideHolder.guideTextView.setText(R.string.introduction2);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = eventList.size();
        if (count > 0) {
            count++; // Add 1 for the "Add New Event" item
        }
        if (count <= 1) {
            count++; // Add 1 for the guide item when there are 0 or 1 events
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < eventList.size()) {
            return TYPE_EVENT;
        } else if (eventList.size() > 0 && position == eventList.size()) {
            return TYPE_ADD_EVENT;
        } else {
            return TYPE_GUIDE;
        }
    }

    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
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

    static class AddEventViewHolder extends RecyclerView.ViewHolder {
        AddEventViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class GuideViewHolder extends RecyclerView.ViewHolder {
        TextView guideTextView;

        GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            guideTextView = itemView.findViewById(R.id.guideTextView);
        }
    }
}