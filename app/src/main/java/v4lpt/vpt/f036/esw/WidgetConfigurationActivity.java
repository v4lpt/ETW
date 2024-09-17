package v4lpt.vpt.f036.esw;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WidgetConfigurationActivity extends Activity implements WidgetConfigAdapter.OnEventSelectedListener {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private List<Event> events;
    private WidgetConfigAdapter adapter;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        settingsManager = new SettingsManager(this);
        events = EventStorage.loadEvents(this);
        RecyclerView recyclerView = findViewById(R.id.event_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WidgetConfigAdapter(this, events, this);
        adapter.setItemLayout(settingsManager.isDisplayModeA() ? R.layout.item_event : R.layout.item_event_b);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEventSelected(Event event) {
        saveSelectedEvent(event.getId());

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WidgetConfigurationActivity.this);
        EventWidgetProvider.updateAppWidget(WidgetConfigurationActivity.this, appWidgetManager, appWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private void saveSelectedEvent(long eventId) {
        SharedPreferences.Editor prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE).edit();
        prefs.putLong("widget_" + appWidgetId, eventId);
        prefs.apply();
    }
}