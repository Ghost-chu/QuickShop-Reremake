package org.maxgamer.quickshop.Database;

import java.sql.SQLException;

public interface DatabaseTask {

    default void run(){
        try {
            execute();
            onSuccess();
        }catch (SQLException e){
            onFailed(e);
        }
    }

    void execute() throws SQLException;
    default void onSuccess(){}
    default void onFailed(SQLException e){
        e.printStackTrace();
    }
}
