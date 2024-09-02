package v4lpt.vpt.f036.esw;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private TextView dateBar;
    private MaterialButton fabAddEvent;
    private MaterialButton bigAddEventButton;
    private Button infoButton;
    private MaterialButton modeToggleButton;
    private SettingsManager settingsManager;
    public static final int ADD_EVENT_REQUEST = 1;
    public static final int EDIT_EVENT_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsManager = new SettingsManager(this);

        recyclerView = findViewById(R.id.recyclerView);
        fabAddEvent = findViewById(R.id.fabAddEvent);
        bigAddEventButton = findViewById(R.id.bigAddEventButton);
        dateBar = findViewById(R.id.dateBar);
        modeToggleButton = findViewById(R.id.modeToggleButton);
        infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> openInfoFragment());
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);

        fabAddEvent.setOnClickListener(v -> addEvent());
        bigAddEventButton.setOnClickListener(v -> addEvent());

        modeToggleButton.setOnClickListener(v -> toggleDisplayMode());

        updateDateBar();
        loadEvents();
        updateDisplayMode();
    }

    private void toggleDisplayMode() {
        boolean newMode = !settingsManager.isDisplayModeA();
        settingsManager.setDisplayMode(newMode);
        updateDisplayMode();
    }

    private void updateDisplayMode() {
        boolean isModeA = settingsManager.isDisplayModeA();

        modeToggleButton.setIcon(getDrawable(isModeA ? R.drawable.icon_a : R.drawable.icon_b));

        // Update RecyclerView layout
        recyclerView.setVerticalScrollBarEnabled(!isModeA);
        eventAdapter.setItemLayout(isModeA ? R.layout.item_event : R.layout.item_event_b);

        // Force a complete redraw of the RecyclerView
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(eventAdapter);
    }


    private void openInfoFragment() {
        InfoFragment infoFragment = new InfoFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, infoFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void closeInfoFragment() {
        getSupportFragmentManager().popBackStack();
    }

    private void updateDateBar() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EE)");
        String formattedDate = currentDate.format(formatter);
        dateBar.setText(formattedDate);
    }

    private void loadEvents() {
        eventList.clear();
        eventList.addAll(EventStorage.loadEvents(this));

        Collections.sort(eventList, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));

        eventAdapter.updateEvents(eventList);
        updateBigAddEventButtonVisibility();
    }

    private void updateBigAddEventButtonVisibility() {
        bigAddEventButton.setVisibility(eventList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void addEvent() {
        Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
        startActivityForResult(intent, ADD_EVENT_REQUEST);
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_title", event.getTitle());
        intent.putExtra("event_date", event.getFormattedDate());
        intent.putExtra("event_image", event.getBackgroundImagePath());
        startActivityForResult(intent, EDIT_EVENT_REQUEST);
    }

    @Override
    public void onAddEventClick() {
        addEvent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_EVENT_REQUEST || requestCode == EDIT_EVENT_REQUEST) {
                if (data != null && data.getBooleanExtra("event_deleted", false)) {
                    long deletedEventId = data.getLongExtra("event_id", -1);
                    if (deletedEventId != -1) {
                        eventList.removeIf(event -> event.getId() == deletedEventId);
                        eventAdapter.updateEvents(eventList);
                        updateBigAddEventButtonVisibility();
                    }
                } else {
                    loadEvents();
                }
            }
        }
    }
}