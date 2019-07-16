package org.maxgamer.quickshop.Database;

import java.sql.Connection;

public interface DatabaseCore {
    void close();

    void flush();

    void queue(BufferStatement bs);

    Connection getConnection();
}