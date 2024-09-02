package v4lpt.vpt.f036.esw;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Event implements Serializable {
    private long id;
    private String title;
    private LocalDate date;
    private String backgroundImagePath;

    public Event(long id, String title, LocalDate date, String backgroundImagePath) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.backgroundImagePath = backgroundImagePath;
    }

    public long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public long getDaysLeft() {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    public String getFormattedDate() {
        return date.toString();
    }
}