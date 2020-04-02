package org.maxgamer.quickshop.Database;


import lombok.NonNull;
import lombok.ToString;
import org.maxgamer.quickshop.QuickShop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ToString()
public class DatabaseTask {

    private String statement;
    private static final Database database=QuickShop.getInstance().getDatabase();
    private Task task;

    public DatabaseTask(String statement, Task task) {
        this.statement = statement;
        this.task = task;
    }

    public void run() {
        try (PreparedStatement ps = database.getConnection().prepareStatement(statement)) {
            task.edit(ps);
            ps.execute();
            task.onSuccess();
        } catch (SQLException e) {
            task.onFailed(e);
        }
    }
    public void run(@NonNull Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(statement);) {
            task.edit(ps);
            ps.execute();
            task.onSuccess();
        } catch (SQLException e) {
            task.onFailed(e);
        }
    }

    interface Task {
        void edit(PreparedStatement ps) throws SQLException;

        default void onSuccess() {
        }

        default void onFailed(SQLException e) {
            e.printStackTrace();
        }

    }

}
