package org.maxgamer.quickshop.Database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

public class SQLiteCore implements DatabaseCore {
	private Connection connection;
	private File dbFile;
	private volatile Thread watcher;
	private volatile LinkedList<BufferStatement> queue = new LinkedList<BufferStatement>();

	public SQLiteCore(File dbFile) {
		this.dbFile = dbFile;
	}

	/**
	 * Gets the database connection for executing queries on.
	 * 
	 * @return The database connection
	 */
	public Connection getConnection() {
		try {
			// If we have a current connection, fetch it
			if (this.connection != null && !this.connection.isClosed()) {
				return this.connection;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (this.dbFile.exists()) {
			// So we need a new connection
			try {
				Class.forName("org.sqlite.JDBC");
				this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
				return this.connection;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			// So we need a new file too.
			try {
				// Create the file
				this.dbFile.createNewFile();
				// Now we won't need a new file, just a connection.
				// This will return that new connection.
				return this.getConnection();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public void queue(BufferStatement bs) {
		synchronized (queue) {
			queue.add(bs);
		}
		if (watcher == null || !watcher.isAlive()) {
			startWatcher();
		}
	}

	@Override
	public void flush() {
		while (queue.isEmpty() == false) {
			BufferStatement bs;
			synchronized (queue) {
				bs = queue.removeFirst();
			}
			synchronized (dbFile) {
				try {
					PreparedStatement ps = bs.prepareStatement(getConnection());
					ps.execute();
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();

				}
			}
		}
	}

	@Override
	public void close() {
		flush();
	}

	private void startWatcher() {
		watcher = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
				}
				flush();
			}
		};
		watcher.start();
	}
}