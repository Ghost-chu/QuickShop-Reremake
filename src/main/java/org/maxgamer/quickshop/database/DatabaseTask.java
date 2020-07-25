package org.maxgamer.quickshop.database;


import lombok.NonNull;
import lombok.ToString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ToString()
public class DatabaseTask {

    private final String statement;
    private final Task task;

    public DatabaseTask(String statement, Task task) {
        this.statement = statement;
        this.task = task;
    }


    public void run(@NonNull Connection connection) {
        try (final PreparedStatement ps = connection.prepareStatement(statement)) {
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
