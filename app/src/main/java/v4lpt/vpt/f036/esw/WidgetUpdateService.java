package v4lpt.vpt.f036.esw;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;

public class WidgetUpdateService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, EventWidgetProvider.class));

        // Update all widgets
        for (int appWidgetId : appWidgetIds) {
            EventWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}