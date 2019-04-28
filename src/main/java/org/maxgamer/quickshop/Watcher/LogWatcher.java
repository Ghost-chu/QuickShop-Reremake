package org.maxgamer.quickshop.Watcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Queue;

import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.QuickShop;

import com.google.common.collect.Lists;

public class LogWatcher implements Runnable {
	private PrintStream ps;
	private Queue<String> logs = Lists.newLinkedList();
	public BukkitTask task;

	public LogWatcher(QuickShop plugin, File log) {
		try {
			if (!log.exists()) {
				log.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(log, true);
			this.ps = new PrintStream(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			plugin.getLogger().severe("Log file not found!");
		} catch (IOException e) {
			e.printStackTrace();
			plugin.getLogger().severe("Could not create log file!");
		}
	}

	@Override
	public void run() {
		synchronized (logs) {
			Iterator<String> iterator = logs.iterator();
			while (iterator.hasNext()) {
			    ps.print(iterator.next());
			    iterator.remove();
			}
		}
	}

	public void add(String s) {
		synchronized (logs) {
			logs.add(s);
		}
	}

	public void close() {
		this.ps.close();
	}
}