package main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.database.DiaryDatabase;
import main.models.EventModel;

/**
 * 管理日曆事件的類
 */
public class EventManager {
    // 用於存儲事件的Map，按日期組織（用於快速存取）
    private Map<LocalDate, List<Event>> eventMap = new HashMap<>();
    private ObservableList<Event> currentDateEvents = FXCollections.observableArrayList();
    private LocalDate selectedDate;
    private String currentUserEmail; // 當前用戶郵箱
    private DiaryDatabase database;
    
    public EventManager() {
        this.selectedDate = LocalDate.now();
        this.database = DiaryDatabase.getInstance();
    }
    
    /**
     * 設置當前用戶
     */
    public void setCurrentUser(String userEmail) {
        this.currentUserEmail = userEmail;
        loadAllEventsFromDatabase();
    }
    
    /**
     * 從資料庫載入所有事件
     */
    private void loadAllEventsFromDatabase() {
        if (currentUserEmail == null) return;
        
        eventMap.clear();
        List<EventModel> eventModels = database.getAllEventsForUser(currentUserEmail);
        
        for (EventModel eventModel : eventModels) {
            Event event = eventModel.toEvent();
            addEventToMap(event);
        }
        
        updateCurrentDateEvents();
    }
    
    /**
     * 將事件添加到記憶體Map中（不存資料庫）
     */
    private void addEventToMap(Event event) {
        LocalDate date = event.getDate();
        if (event.isContinuous()) {
            // 如果是連續事件，將事件添加到每一天
            LocalDate currentDate = date;
            LocalDate endDate = event.getEndDate();
            
            while (!currentDate.isAfter(endDate)) {
                if (!eventMap.containsKey(currentDate)) {
                    eventMap.put(currentDate, new ArrayList<>());
                }
                eventMap.get(currentDate).add(event);
                
                currentDate = currentDate.plusDays(1);
            }
        } else {
            // 單一日期事件
            if (!eventMap.containsKey(date)) {
                eventMap.put(date, new ArrayList<>());
            }
            eventMap.get(date).add(event);
        }
    }
    
    /**
     * 從記憶體Map中移除事件（不動資料庫）
     */
    private void removeEventFromMap(Event event) {
        if (event.isContinuous()) {
            // 如果是連續事件，從每一天移除
            LocalDate currentDate = event.getDate();
            LocalDate endDate = event.getEndDate();
            
            while (!currentDate.isAfter(endDate)) {
                if (eventMap.containsKey(currentDate)) {
                    eventMap.get(currentDate).remove(event);
                }
                currentDate = currentDate.plusDays(1);
            }
        } else {
            // 單一日期事件
            LocalDate date = event.getDate();
            if (eventMap.containsKey(date)) {
                eventMap.get(date).remove(event);
            }
        }
    }
    
    /**
     * 設置當前選中的日期
     */
    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        updateCurrentDateEvents();
    }
    
    /**
     * 獲取當前選中日期
     */
    public LocalDate getSelectedDate() {
        return selectedDate;
    }
    
    /**
     * 更新當前日期的事件列表
     */
    private void updateCurrentDateEvents() {
        currentDateEvents.clear();
        if (eventMap.containsKey(selectedDate)) {
            currentDateEvents.addAll(eventMap.get(selectedDate));
        }
    }
    
    /**
     * 添加新事件
     */
    public void addEvent(Event event) {
        if (currentUserEmail == null) {
            throw new RuntimeException("No user logged in");
        }
        
        // 儲存到資料庫
        EventModel eventModel = new EventModel(event, 
                                 database.getUserEntry(currentUserEmail));
        database.saveEvent(eventModel, currentUserEmail);
        
        // 添加到記憶體Map
        addEventToMap(event);
        
        // 如果事件在選中日期範圍內，更新事件列表
        if (event.isOnDate(selectedDate)) {
            currentDateEvents.add(event);
        }
    }
    
    /**
     * 刪除事件
     */
    public void removeEvent(Event event) {
        if (currentUserEmail == null) return;
        
        // 先從記憶體移除
        removeEventFromMap(event);
        currentDateEvents.remove(event);
        
        // 從資料庫刪除 - 需要找到對應的 EventModel
        List<EventModel> userEvents = database.getAllEventsForUser(currentUserEmail);
        for (EventModel eventModel : userEvents) {
            Event dbEvent = eventModel.toEvent();
            if (eventsEqual(event, dbEvent)) {
                database.deleteEvent(eventModel);
                break;
            }
        }
    }
    
    /**
     * 更新事件 
     */
    public void updateEvent(Event event) {
        if (currentUserEmail == null) return;
        
        // 找到資料庫中對應的事件並更新
        List<EventModel> userEvents = database.getAllEventsForUser(currentUserEmail);
        for (EventModel eventModel : userEvents) {
            Event dbEvent = eventModel.toEvent();
            if (eventsEqual(event, dbEvent)) {
                // 更新 EventModel
                eventModel.setDate(event.getDate().toString());
                eventModel.setEndDate(event.getEndDate().toString());
                eventModel.setTime(String.format("%02d:%02d", 
                                   event.getTime().getHour(), 
                                   event.getTime().getMinute()));
                eventModel.setDescription(event.getDescription());
                eventModel.setColorHex(toHex(event.getColor()));
                eventModel.setContinuous(event.isContinuous());
                
                database.updateEvent(eventModel);
                break;
            }
        }
        
        // 重新載入所有事件以確保一致性
        loadAllEventsFromDatabase();
    }
    
    /**
     * 比較兩個事件是否相等（用於在資料庫中找到對應事件）
     */
    private boolean eventsEqual(Event event1, Event event2) {
        return event1.getDate().equals(event2.getDate()) &&
               event1.getTime().equals(event2.getTime()) &&
               event1.getDescription().equals(event2.getDescription());
    }
    
    /**
     * 將 Color 轉換為 HEX 字串
     */
    private String toHex(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    
    /**
     * 獲取指定日期的事件列表
     */
    public List<Event> getEventsForDate(LocalDate date) {
        if (eventMap.containsKey(date)) {
            return eventMap.get(date);
        }
        return new ArrayList<>();
    }
    
    /**
     * 獲取當前日期的可觀察事件列表
     */
    public ObservableList<Event> getCurrentDateEvents() {
        return currentDateEvents;
    }
}