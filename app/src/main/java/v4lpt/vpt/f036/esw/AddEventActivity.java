package v4lpt.vpt.f036.esw;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Space;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AddEventActivity extends AppCompatActivity implements PreviewEventAdapter.OnPreviewClickListener {
    private static final int PICK_IMAGE = 100;
    private EditText titleEditText, dateEditText;
    private RecyclerView previewRecyclerView;
    private Button saveButton, deleteButton;
    private Space bottomSpacer;
    private Uri selectedImageUri;
    private long editEventId = -1;
    private PreviewEventAdapter previewAdapter;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        settingsManager = new SettingsManager(this);

        titleEditText = findViewById(R.id.titleEditText);
        dateEditText = findViewById(R.id.dateEditText);
        previewRecyclerView = findViewById(R.id.previewRecyclerView);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        bottomSpacer = findViewById(R.id.bottomSpacer);
        setupPreviewRecyclerView();
        setupSaveButton();
        setupDeleteButton();
        setupInputBehaviors();
        setupTextWatchers();

        if (getIntent().hasExtra("event_id")) {
            loadExistingEvent();
        } else {
            displayDefaultPreview();
        }
    }

    private void setupInputBehaviors() {
        titleEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        titleEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                dateEditText.requestFocus();
                return true;
            }
            return false;
        });

        dateEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void setupPreviewRecyclerView() {
        previewAdapter = new PreviewEventAdapter(this, this);
        previewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        previewRecyclerView.setAdapter(previewAdapter);

        boolean isModeA = settingsManager.isDisplayModeA();
        previewAdapter.setItemLayout(isModeA ? R.layout.item_event : R.layout.item_event_b);
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> saveEvent());
    }

    private void setupDeleteButton() {
        if (getIntent().hasExtra("event_id")) {
            editEventId = getIntent().getLongExtra("event_id", -1);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> confirmDelete());
            adjustSpacerHeight(0f); // 0% height when delete button is visible
        } else {
            deleteButton.setVisibility(View.GONE);
            adjustSpacerHeight(0.123f); // 20% height when delete button is not visible
        }
    }

    private void adjustSpacerHeight(float heightPercent) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) bottomSpacer.getLayoutParams();
        params.matchConstraintPercentHeight = heightPercent;
        bottomSpacer.setLayoutParams(params);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePreview();
            }
        };

        titleEditText.addTextChangedListener(textWatcher);
        dateEditText.addTextChangedListener(new DateInputTextWatcher());
        dateEditText.addTextChangedListener(textWatcher);
    }

    private void loadExistingEvent() {
        String title = getIntent().getStringExtra("event_title");
        String date = getIntent().getStringExtra("event_date");
        String imagePath = getIntent().getStringExtra("event_image");

        titleEditText.setText(title);
        dateEditText.setText(formatDateString(date));
        if (imagePath != null) {
            selectedImageUri = Uri.parse(imagePath);
        }

        updatePreview();
    }

    private void displayDefaultPreview() {
        // Leave EditText fields empty
        titleEditText.setText("");
        dateEditText.setText("");

        // Create a default event for the preview
        LocalDate currentDate = LocalDate.now();
        Event defaultEvent = new Event(0, getString(R.string.default_preview_title), currentDate, null);
        previewAdapter.updatePreviewEvent(defaultEvent);
    }

    private void updatePreview() {
        String title = titleEditText.getText().toString().trim();
        String dateString = dateEditText.getText().toString().trim();

        if (title.isEmpty()) {
            title = getString(R.string.default_preview_title);
        }

        // Use the current date if the date field is empty or invalid
        LocalDate date;
        if (dateString.isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                date = LocalDate.now();
            }
        }

        Event previewEvent = new Event(editEventId != -1 ? editEventId : 0, title, date, selectedImageUri != null ? selectedImageUri.toString() : null);
        previewAdapter.updatePreviewEvent(previewEvent);
    }

    private void saveEvent() {
        String title = titleEditText.getText().toString().trim();
        String dateString = dateEditText.getText().toString().trim().replaceAll("-", "");

        if (title.isEmpty() || dateString.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateString, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            Toast.makeText(this, "Invalid date format. Please use YYYYMMDD", Toast.LENGTH_SHORT).show();
            return;
        }

        String imagePath = selectedImageUri != null ? selectedImageUri.toString() : null;

        List<Event> events = EventStorage.loadEvents(this);
        Event newEvent;

        if (editEventId != -1) {
            newEvent = new Event(editEventId, title, date, imagePath);
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).getId() == editEventId) {
                    events.set(i, newEvent);
                    break;
                }
            }
        } else {
            long newId = EventStorage.getNextId(this);
            newEvent = new Event(newId, title, date, imagePath);
            events.add(newEvent);
        }

        EventStorage.saveEvents(this, events);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("event_id", newEvent.getId());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void deleteEvent() {
        List<Event> events = EventStorage.loadEvents(this);
        boolean removed = events.removeIf(event -> event.getId() == editEventId);
        if (removed) {
            EventStorage.saveEvents(this, events);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("event_id", editEventId);
            resultIntent.putExtra("event_deleted", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            selectedImageUri = data.getData();
            updatePreview();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> deleteEvent())
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onPreviewClick() {
        openGallery();
    }

    private class DateInputTextWatcher implements TextWatcher {
        private boolean mFormatting;
        private int mLastLength;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (mFormatting) return;

            mFormatting = true;

            String input = s.toString().replaceAll("[^\\d]", "");
            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < input.length() && i < 8; i++) {
                if (i == 4 || i == 6) {
                    formatted.append('-');
                }
                formatted.append(input.charAt(i));
            }

            String newFormatted = formatted.toString();
            if (!s.toString().equals(newFormatted)) {
                s.replace(0, s.length(), newFormatted);
            }

            // Set cursor to end
            dateEditText.setSelection(newFormatted.length());

            // Check if we've just entered the last digit
            if (newFormatted.length() == 10 && mLastLength < 10) {
                hideKeyboard();
            }

            mLastLength = newFormatted.length();
            mFormatting = false;
        }
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(dateEditText.getWindowToken(), 0);
        }
        dateEditText.clearFocus();
    }

    private String formatDateString(String date) {
        if (date == null || date.length() != 10) return date;
        return date.replaceAll("-", "");
    }
}