package v4lpt.vpt.f036.esw;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;



public class EventWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "EventWidgetProvider";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EE)");

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        scheduleUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        cancelUpdate(context);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_event);

        Event event = getSelectedEvent(context, appWidgetId);

        if (event != null) {
            views.setTextViewText(R.id.widget_title, event.getTitle());

            // Update this line to use the new date formatter
            String formattedDate = event.getDate().format(dateFormatter);
            views.setTextViewText(R.id.widget_date, formattedDate);

            views.setTextViewText(R.id.widget_days_left, getDaysLeftText(event));

            if (event.getBackgroundImagePath() != null) {
                AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.widget_background_image, views, appWidgetId);

                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(event.getBackgroundImagePath())
                        .centerCrop()
                        .override(2000, 1024)
                        .into(appWidgetTarget);
            } else {
                views.setImageViewResource(R.id.widget_background_image, R.drawable.default_event_background);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        } else {
            views.setTextViewText(R.id.widget_title, "Please delete and re-add this event to widget it.");
            views.setTextViewText(R.id.widget_date, "");
            views.setTextViewText(R.id.widget_days_left, "");
            views.setImageViewResource(R.id.widget_background_image, R.drawable.default_event_background);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    private void scheduleUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WidgetUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to start at approximately 2:00 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If it's already past 2:00 a.m., set it for the next day
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule the alarm to repeat every day
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void cancelUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WidgetUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private static String getDaysLeftText(Event event) {
        long daysLeft = event.getDaysLeft();
        if (daysLeft < 0) {
            return Math.abs(daysLeft) + (Math.abs(daysLeft) == 1 ? " day ago" : " days ago");
        } else if (daysLeft == 0) {
            return "Today";
        } else {
            return daysLeft + (daysLeft == 1 ? " day left" : " days left");
        }
    }

    private static Event getSelectedEvent(Context context, int appWidgetId) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE);
        long selectedEventId = prefs.getLong("widget_" + appWidgetId, -1);

        if (selectedEventId == -1) {
            Log.d(TAG, "No event ID found for widget: " + appWidgetId);
            return null;
        }

        java.util.List<Event> events = EventStorage.loadEvents(context);
        for (Event event : events) {
            if (event.getId() == selectedEventId) {
                return event;
            }
        }

        Log.d(TAG, "Event not found for ID: " + selectedEventId);
        return null;
    }
}