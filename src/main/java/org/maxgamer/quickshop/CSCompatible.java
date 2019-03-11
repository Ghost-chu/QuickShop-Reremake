package org.maxgamer.quickshop;

import java.util.List;

import org.maxgamer.quickshop.Util.Util;

import catserver.api.CatServerApi;
import catserver.api.interfaces.ModContainer;

public class CSCompatible {
	QuickShop plugin;
	//Set to true when failed load CSCompatible module
	boolean failedLoading=false;
	
	public CSCompatible() {
		plugin = QuickShop.instance;
		try {
			List<ModContainer> modList = CatServerApi.getModList();
			int csBuild = CatServerApi.getBuildVersion();
			if (csBuild < 1) {
				failedLoading = true;
				return;
			}
			for (ModContainer modContainer : modList) {
				checkCompatibleIssues(modContainer);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			failedLoading = true;
			return;
		}

	}

	public boolean getFailedLoading() {
		return failedLoading;
	}
	private void checkCompatibleIssues(ModContainer modContainer) {
		String modId = modContainer.getModId();
		switch (modId) {
		case "flammpfeil.slashblade":
			applySlashBlade(modContainer);
			break;

		default:
			break;
		}
	}
	
	private void applySlashBlade(ModContainer modContainer) {
		if(!plugin.getConfig().getBoolean("compatible.SlashBlade")) {
			Util.debugLog("Setting up SlashBlade compatible...");
			plugin.getConfig().set("shop.display-items", false);
			plugin.getConfig().set("compatible.SlashBlade", true);
			plugin.saveConfig();
			sendWarning(modContainer, "DisplayItem will be SlashBlade's weapon and can pickup", "DisplayItem disabled.");
		}
		
	}
	
	private void sendWarning(ModContainer modContainer,String whatWillHappend, String action) {
		plugin.getLogger().warning("Compatibility issue detected: "+modContainer.getName()+"("+modContainer.getModId()+") Version: "+modContainer.getVersion());
		plugin.getLogger().warning("Compatibility issue description: "+whatWillHappend);
		plugin.getLogger().warning("Compatibility issue solution: "+action);
	}
}
