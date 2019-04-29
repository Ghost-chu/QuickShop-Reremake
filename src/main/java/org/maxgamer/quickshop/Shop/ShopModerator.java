package org.maxgamer.quickshop.Shop;

import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;

public class ShopModerator {
	private UUID owner;
	private ArrayList<UUID> staffs;
	
	public ShopModerator(ShopModerator shopModerator) {
		this.owner = shopModerator.owner;
		this.staffs = shopModerator.staffs;
	}
	public ShopModerator(UUID owner) {
		this.owner = owner;
		this.staffs = new ArrayList<UUID>();
	}
	
	public ShopModerator clone() {
		return new ShopModerator(this);
	}
	
	public UUID getOwner() {
		return owner;
	}
	public void setOwner(UUID player) {
		this.owner=player;
	}
	public void setStaffs(ArrayList<UUID> players) {
		this.staffs=players;
	}
	public boolean addStaff(UUID player) {
		if(staffs.contains(player))
			return false;
		staffs.add(player);
		return true;
	}
	public boolean delStaff(UUID player) {
		if(!staffs.contains(player))
			return false;
		staffs.remove(player);
		return true;
	}
	public void clearStaffs() {
		staffs.clear();
	}
	public ArrayList<UUID> getStaffs() {
		return staffs;
	}
	public boolean isOwner(UUID player) {
		return player.equals(owner);
	}
	public boolean isStaff(UUID player) {
		return staffs.contains(player);
	}
	public boolean isModerator(UUID player) {
		if(isOwner(player))
			return true;
		if(isStaff(player))
			return true;
		return false;
	}
	public static String serialize(ShopModerator shopModerator) {
		Gson gson = new Gson();
		return gson.toJson(shopModerator); //Use Gson serialize this class
	}
	public static ShopModerator deserialize(String serilized) {
		//Use Gson deserialize data
		Gson gson = new Gson();
		return gson.fromJson(serilized, ShopModerator.class);
	}
}
