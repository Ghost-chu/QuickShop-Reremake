package org.maxgamer.quickshop.Watcher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Database.DatabaseHelper;
import org.maxgamer.quickshop.Database.MySQLCore;
import org.maxgamer.quickshop.Economy.Economy;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

public class OngoineFeeWatcher_TODO {
	BukkitTask task = null;
	int shops_price;
	boolean pass_offline_players;
	Economy eco = null;
	public void init() {
		int checkTime = 0;
		checkTime = QuickShop.instance.getConfig().getInt("ongoingfee.checkTime");
		shops_price =  QuickShop.instance.getConfig().getInt("ongoingfee.shops-price");
		pass_offline_players =  QuickShop.instance.getConfig().getBoolean("ongoingfee.pass-offline-player");
		eco =  new Economy( QuickShop.instance.getEcon());
		task = new BukkitRunnable() {
			@Override
			public void run() {
				if(!QuickShop.instance.getConfig().getBoolean("ongoingfee.enable")) {
					return;
				}
				try {
					doit();
				} catch (SQLException | InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(QuickShop.instance, 80, checkTime);
	}
	public void uninit() {
		if(task!=null) {
			task.cancel();
		}
		try {
			if(QuickShop.instance.getConfig().getBoolean("ongoingfee.reset-on-startup")) {
				QuickShop.instance.getLogger().info("Cleaning up database schedule table...");
			QuickShop.instance.getDB().getConnection().createStatement().execute("delete from schedule");
			QuickShop.instance.getLogger().info("Finished database cleanup.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void doit() throws SQLException, InvalidConfigurationException {
		//Begin!
		Statement st = QuickShop.instance.getDB().getConnection().createStatement();
		String checkq = "SELECT * FROM schedule";
		ResultSet resultSet = st.executeQuery(checkq);
		// Stupid SQLite...
		if(resultSet.getString(1)!=null) {
			withdrawMoney(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6));
			while (resultSet.next()) {
				withdrawMoney(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getInt(4), resultSet.getInt(5), resultSet.getInt(6));
			}
		}
		//Finish check, reset all shops time...		
		QuickShop.instance.getLogger().info("Finished Ongoingfee checking.");
	}
	public void withdrawMoney(String argUUID, String World, int X, int Y, int Z ,int timeStamp) throws SQLException, InvalidConfigurationException {
		if(pass_offline_players) {
			boolean online = false;
			UUID uuid = java.util.UUID.fromString(argUUID);
			Collection<? extends Player> onlines = Bukkit.getServer().getOnlinePlayers();
			for (Player onlinePlayer : onlines) {
				if(onlinePlayer.getUniqueId().equals(uuid)) {
					online = true;
					break;
				}
			}
			if(!online) {
//				QuickShop.instance.getDB().getConnection().createStatement()
//				.executeUpdate("DELETE FROM schedule WHERE x = " + X + " AND y = " + Y + " AND z = " + Z
//						+ " AND world = \"" + World + "\""
//						+ (QuickShop.instance.getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
				DatabaseHelper.removeShop(QuickShop.instance.getDB(), X, Y, Z, World);
				DatabaseHelper.insertSchedule(argUUID, World, X, Y, Z, System.currentTimeMillis());
				return;
				//Not online and turn on it from config, Return
			}
			double moneyRemining = eco.getBalance(uuid);
			if(moneyRemining<shops_price) {
				MsgUtil.send(uuid, MsgUtil.getMessage("no-enough-money-to-keep-shops"));
				//Start scanning player's all shops in database	
				ResultSet rs = DatabaseHelper.selectAllShops(QuickShop.instance.getDB());
				while (rs.next()) {
					int x = 0;
					int y = 0;
					int z = 0;
					String worldName = null;
					ItemStack item = null;
					UUID ownerUUID = null;
					String owner;
						x = rs.getInt("x");
						y = rs.getInt("y");
						z = rs.getInt("z");
						owner = rs.getString("owner");
						worldName = rs.getString("world");
						World world = Bukkit.getWorld(worldName);
						item = Util.deserialize(rs.getString("itemConfig"));
						ownerUUID = java.util.UUID.fromString(owner);
						double price = rs.getDouble("price");
						Location loc = new Location(world, x, y, z);
						item = Util.deserialize(rs.getString("itemConfig"));
						Shop shop = new ContainerShop(loc, price, item, ownerUUID);
						if(shop.getOwner().equals(uuid)) {
							shop.delete();
						}
						QuickShop.instance.getLogger().info("Removed "+Bukkit.getOfflinePlayer(uuid)+"'s all shops, because no enough money to keep it.");
				}
			}else {
				eco.withdraw(uuid, shops_price);
			}
			//Finished and reset shops time
			QuickShop.instance.getDB().getConnection().createStatement()
			.executeUpdate("DELETE FROM schedule WHERE x = " + X + " AND y = " + Y + " AND z = " + Z
					+ " AND world = \"" + World + "\""
					+ (QuickShop.instance.getDB().getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
			if(!(moneyRemining < shops_price)) {
				// Reset time
				String scheduleq = "INSERT INTO schedule (owner, world, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
			QuickShop.instance.getDB().execute(scheduleq , argUUID, World, X, Y, Z, System.currentTimeMillis());
			}
		}
	}
}
	
