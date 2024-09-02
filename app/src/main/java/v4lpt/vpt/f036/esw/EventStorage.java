package v4lpt.vpt.f036.esw;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventStorage {
    private static final String PREF_NAME = "EventPrefs";
    private static final String EVENT_LIST_KEY = "eventList";
    private static final String GLOBAL_ID_KEY = "globalId";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    public static void saveEvents(Context context, List<Event> events) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(events);
        editor.putString(EVENT_LIST_KEY, json);
        editor.apply();
    }

    public static List<Event> loadEvents(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(EVENT_LIST_KEY, null);
        Type type = new TypeToken<ArrayList<Event>>() {}.getType();
        List<Event> events = gson.fromJson(json, type);
        return events != null ? events : new ArrayList<>();
    }

    public static long getNextId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long id = sharedPreferences.getLong(GLOBAL_ID_KEY, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(GLOBAL_ID_KEY, id + 1);
        editor.apply();
        return id;
    }

    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(formatter.format(value));
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString(), formatter);
        }
    }
}