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
    private boolean isCollaborationMode = false; // 新增
      public EventManager() {
        this.selectedDate = LocalDate.now();
        this.database = DiaryDatabase.getInstance();
    }

    public void enableCollaborationMode() {
        this.isCollaborationMode = true;
        loadAllEventsFromDatabase();
        System.out.println("已切換到協作模式：所有用戶的協作事件將會共享顯示");
    }

    public void enablePersonalMode() {
        this.isCollaborationMode = false;
        loadAllEventsFromDatabase();
    }

    public boolean isCollaborationMode() {
        return isCollaborationMode;
    }

    public String getCurrentModeText() {
        return isCollaborationMode ? "協作模式" : "個人模式";
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
     */    private void loadAllEventsFromDatabase() {
        eventMap.clear(); // 確保清空現有的事件映射
        List<EventModel> eventModels;
        
        if (isCollaborationMode) {
            eventModels = database.getAllCollaborationEvents();
        } else {
            if (currentUserEmail == null) return;
            eventModels = database.getPersonalEventsForUser(currentUserEmail);
        }
        
        for (EventModel eventModel : eventModels) {
            try {
                Event event = eventModel.toEvent();
                addEventToMap(event);
            } catch (Exception e) {
                // 處理可能發生的轉換錯誤，例如 NullPointerException
                System.err.println("Error converting event model to event: " + e.getMessage());
                // 繼續處理下一個事件
            }
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
        
        // 使用新構造器，明確指定是否為協作事件
        EventModel eventModel = new EventModel(event, 
                                database.getUserEntry(currentUserEmail), 
                                isCollaborationMode);
        
        if (isCollaborationMode) {
            // 如果是協作模式，使用 saveCollaborationEvent 方法保存
            // 這個方法會將事件標記為協作事件，並記錄創建者信息
            database.saveCollaborationEvent(eventModel, currentUserEmail);
            System.out.println("已在協作模式下添加事件，所有用戶可見。");
        } else {
            // 如果是個人模式，使用 savePersonalEvent 方法保存
            // 這個方法會將事件標記為個人事件，只有創建者可見
            database.savePersonalEvent(eventModel, currentUserEmail);
        }
        
        addEventToMap(event);
        
        if (event.isOnDate(selectedDate)) {
            currentDateEvents.add(event);
        }
    }
    
    /**
     * 刪除事件
     */
    public void removeEvent(Event event) {
        if (currentUserEmail == null) return;
    
        removeEventFromMap(event);
        currentDateEvents.remove(event);
        
        List<EventModel> eventModels;
        if (isCollaborationMode) {
            eventModels = database.getAllCollaborationEvents();
        } else {
            eventModels = database.getPersonalEventsForUser(currentUserEmail);
        }
        
        for (EventModel eventModel : eventModels) {
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
        
        List<EventModel> eventModels;
        if (isCollaborationMode) {
            eventModels = database.getAllCollaborationEvents();
        } else {
            eventModels = database.getPersonalEventsForUser(currentUserEmail);
        }
        
        for (EventModel eventModel : eventModels) {
            Event dbEvent = eventModel.toEvent();
            if (eventsEqual(event, dbEvent)) {
                updateEventModelFromEvent(eventModel, event);
                database.updateEvent(eventModel);
                break;
            }
        }
        
        loadAllEventsFromDatabase();
    }

    private void updateEventModelFromEvent(EventModel eventModel, Event event) {
        eventModel.setDate(event.getDate().toString());
        eventModel.setEndDate(event.getEndDate().toString());
        eventModel.setTime(String.format("%02d:%02d", 
                        event.getTime().getHour(), 
                        event.getTime().getMinute()));
        eventModel.setDescription(event.getDescription());
        eventModel.setColorHex(toHex(event.getColor()));
        eventModel.setContinuous(event.isContinuous());
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