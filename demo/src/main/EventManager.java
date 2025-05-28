package main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 管理日曆事件的類
 */
public class EventManager {
    // 用於存儲事件的Map，按日期組織
    private Map<LocalDate, List<Event>> eventMap = new HashMap<>();
    private ObservableList<Event> currentDateEvents = FXCollections.observableArrayList();
    private LocalDate selectedDate;
    
    public EventManager() {
        this.selectedDate = LocalDate.now();
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
                
                // 下一天
                currentDate = currentDate.plusDays(1);
            }
        } else {
            // 單一日期事件
            if (!eventMap.containsKey(date)) {
                eventMap.put(date, new ArrayList<>());
            }
            eventMap.get(date).add(event);
        }
        
        // 如果事件在選中日期範圍內，更新事件列表
        if (event.isOnDate(selectedDate)) {
            currentDateEvents.add(event);
        }
    }
    
    /**
     * 刪除事件
     */
    public void removeEvent(Event event) {
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
        
        // 從當前選中日期的事件列表中移除
        currentDateEvents.remove(event);
    }
    
    /**
     * 更新事件 
     */
    public void updateEvent(Event event) {
        // 先從所有日期中移除舊事件
        removeEvent(event);
        
        // 然後再添加更新後的事件
        addEvent(event);
        
        // ListView需要刷新顯示
        updateCurrentDateEvents();
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