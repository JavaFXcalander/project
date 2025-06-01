package main.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import java.io.IOException;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import main.models.EventModel;
import main.models.ProjectModel;
import main.models.UserModel;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DiaryDatabase {
    // ORMLite
    private static final String DATABASE_URL = "jdbc:h2:./diary_db;AUTO_SERVER=TRUE";
    private static DiaryDatabase instance;
    private ConnectionSource connectionSource;
    //private Dao<DiaryModel, Integer> diaryDao;
    private Dao<ProjectModel, Integer> projectDao;
    private Dao<UserModel, Integer> userDao;
    private Dao<EventModel, Integer> eventDao; 

    private DiaryDatabase() {
        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            
            // 創建資料表
            //TableUtils.createTableIfNotExists(connectionSource, DiaryModel.class);
            TableUtils.createTableIfNotExists(connectionSource, ProjectModel.class);
            TableUtils.createTableIfNotExists(connectionSource, UserModel.class);
            TableUtils.createTableIfNotExists(connectionSource, EventModel.class);
            
            // 初始化 DAO
            //diaryDao = DaoManager.createDao(connectionSource, DiaryModel.class);
            projectDao = DaoManager.createDao(connectionSource, ProjectModel.class);
            userDao = DaoManager.createDao(connectionSource, UserModel.class);
            eventDao = DaoManager.createDao(connectionSource, EventModel.class);
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static DiaryDatabase getInstance() {
        if (instance == null) {
            instance = new DiaryDatabase();
        }
        return instance;
    }

    

    
    // 專案相關操作
    public void saveProject(ProjectModel project , String userEmail) {
        try {
            UserModel user = getUserEntry(userEmail);
            if (user == null) throw new RuntimeException("User not found");

            project.setUser(user); // 确保关联用户

            List<ProjectModel> existingProjects = projectDao.queryBuilder()
            .where()
            .eq("year", project.getYear())
            .and()
            .eq("month", project.getMonth())
            .and()
            .eq("user_id", user.getId())
            .query();

           if (!existingProjects.isEmpty()) {
                ProjectModel existingProject = existingProjects.get(0);
                existingProject.setProject1(project.getProject1());
                existingProject.setProject2(project.getProject2());
                existingProject.setProject3(project.getProject3());
                existingProject.setProject4(project.getProject4());
                existingProject.setAbout1(project.getAbout1());
                existingProject.setAbout2(project.getAbout2());
                existingProject.setAbout3(project.getAbout3());
                existingProject.setAbout4(project.getAbout4());
                existingProject.setHabit1(project.getHabit1());
                existingProject.setHabit2(project.getHabit2());
                existingProject.setHabit3(project.getHabit3());
                existingProject.setHabit4(project.getHabit4());
                existingProject.setDailyChecks(project.getDailyChecks());
                projectDao.update(existingProject);
            } else {
                projectDao.create(project);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save project", e);
        }
    }

    public ProjectModel getProjectEntry(int year, int month, String userEmail) {
        try {
            UserModel user = getUserEntry(userEmail);
            if (user == null) return null;
            List<ProjectModel> projects = projectDao.queryBuilder()
                .where()
                .eq("year", year)
                .and()
                .eq("month", month)
                .and()
                .eq("user_id", user.getId())
                .query();

            return projects.isEmpty() ? null : projects.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get project entry", e);
        }
    }


    //事件相關操作
    public void saveEvent(EventModel event, String userEmail) {
        try {
            UserModel user = getUserEntry(userEmail);
            if (user == null) throw new RuntimeException("User not found");
            
            event.setUser(user);
            eventDao.create(event);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save event", e);
        }
    }
    
    public void updateEvent(EventModel event) {
        try {
            eventDao.update(event);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update event", e);
        }
    }
    
    public void deleteEvent(EventModel event) {
        try {
            eventDao.delete(event);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete event", e);
        }
    }
    
    public void deleteEventById(int eventId) {
        try {
            eventDao.deleteById(eventId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete event", e);
        }
    }
    
    public List<EventModel> getAllEventsForUser(String userEmail) {
        try {
            UserModel user = getUserEntry(userEmail);
            if (user == null) return List.of();
            
            return eventDao.queryBuilder()
                .where()
                .eq("user_id", user.getId())
                .query();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get events for user", e);
        }
    }
    
    public List<EventModel> getEventsForDate(LocalDate date, String userEmail) {
        try {
            UserModel user = getUserEntry(userEmail);
            if (user == null) return List.of();
            
            String dateStr = date.toString();
            return eventDao.queryBuilder()
                .where()
                .eq("user_id", user.getId())
                .and()
                .le("date", dateStr) // 事件開始日期 <= 查詢日期
                .and()
                .ge("endDate", dateStr) // 事件結束日期 >= 查詢日期
                .query();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get events for date", e);
        }
    }
    //新增


    public UserModel getUserEntry(String email) {
        try {
            // Query directly for the email, which is more efficient.
            // Assumes 'email' field is indexed or query is on a small table.
            List<UserModel> users = userDao.queryForEq("email", email);
            if (users != null && !users.isEmpty()) {
                return users.get(0); // Email should be unique
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user entry for email: " + email, e);
        }
    }

    public void saveUserEntry(UserModel entry) {
        try {
            
            userDao.create(entry);
        } catch (SQLException e) {
            // Wrap SQLException in a RuntimeException to simplify error handling upstream,
            // consistent with other database operations in this class.
            throw new RuntimeException("Failed to save user entry: " + e.getMessage(), e);
        }
    }


    public void close() {
        try {
            if (connectionSource != null) {
                try {
                    connectionSource.close();
                } catch (Exception e) {
                    e.printStackTrace(); // 或更適當的錯誤處理
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close database connection", e);
        }
    }
}