package main.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.paint.Color;

@DatabaseTable(tableName = "events")
public class EventModel {
    
    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField(foreign = true, columnName = "user_id")
    private UserModel user;
    
    @DatabaseField(canBeNull = false)
    private String date; // 存儲為 YYYY-MM-DD 格式
    
    @DatabaseField(canBeNull = false)
    private String endDate; // 存儲為 YYYY-MM-DD 格式
    
    @DatabaseField
    private String time; // 存儲為 HH:MM 格式
    
    @DatabaseField(canBeNull = false)
    private String description;
    
    @DatabaseField
    private String colorHex; // 存儲為 HEX 格式 (#RRGGBB)
    
    @DatabaseField
    private boolean isContinuous;
    
    // ORMLite 需要無參構造器
    public EventModel() {
    }
    
    // 從 Event 轉換為 EventModel 的構造器
    public EventModel(main.Event event, UserModel user) {
        this.user = user;
        this.date = event.getDate().toString();
        this.endDate = event.getEndDate().toString();
        this.time = String.format("%02d:%02d", 
                     event.getTime().getHour(), 
                     event.getTime().getMinute());
        this.description = event.getDescription();
        this.colorHex = toHex(event.getColor());
        this.isContinuous = event.isContinuous();
    }
    
    // 轉換為 Event 對象
    public main.Event toEvent() {
        LocalDate eventDate = LocalDate.parse(this.date);
        LocalDate eventEndDate = LocalDate.parse(this.endDate);
        LocalTime eventTime = LocalTime.parse(this.time);
        Color eventColor = fromHex(this.colorHex);
        
        if (this.isContinuous) {
            return new main.Event(eventDate, eventEndDate, eventTime, 
                                  this.description, eventColor);
        } else {
            return new main.Event(eventDate, eventTime, this.description, eventColor);
        }
    }
    
    // 將 Color 轉換為 HEX 字串
    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    
    // 將 HEX 字串轉換為 Color
    private Color fromHex(String hex) {
        try {
            if (hex == null || hex.isEmpty()) {
                return Color.CORNFLOWERBLUE;
            }
            return Color.web(hex);
        } catch (Exception e) {
            return Color.CORNFLOWERBLUE;
        }
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public UserModel getUser() {
        return user;
    }
    
    public void setUser(UserModel user) {
        this.user = user;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getColorHex() {
        return colorHex;
    }
    
    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
    
    public boolean isContinuous() {
        return isContinuous;
    }
    
    public void setContinuous(boolean continuous) {
        isContinuous = continuous;
    }
}