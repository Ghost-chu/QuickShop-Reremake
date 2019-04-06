package org.maxgamer.quickshop;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Language {
	QuickShop plugin;
	List<String> languages = new ArrayList<>();
	public Language(QuickShop plugin) {
		this.plugin = plugin;
		languages.clear();
		languages.add("en_US");
	}
	public String getComputerLanguage() {
		if(plugin.getConfig().getString("language").equals("default"))//Allow user replace this
			return Locale.getDefault().toString();
		return plugin.getConfig().getString("language");
	}
	public List<String> getSupportsLanguageList() {
		return languages;
	}
	public InputStream getFile(String language, String type) {
		if((language==null) || !languages.contains(language))
			language="en_US";
		if(type == null || type == "")
			throw new IllegalArgumentException("Type cannot be null or empty");
		return plugin.getResource(type+"-"+language+".yml");
		//File name should call    type-language.yml    ---> config-zh_CN.yml
	}
	//Write file under plugin folder
	public void saveFile(String language, String type, String fileName) {
		File targetFile = new File(Bukkit.getPluginManager().getPlugin(plugin.getName()).getDataFolder().toPath()+"/"+fileName);
		if(!targetFile.exists()) {
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try{
		InputStream is = getFile(language,type);
		FileOutputStream fos = new FileOutputStream(targetFile);
		byte[] b = new byte[1024];
		int length;
			while ((length = is.read(b)) != -1) {
			fos.write(b,0,length);
		}
		is.close();
		fos.close();
		}catch (Exception err){
			err.printStackTrace();
		}
		//File name should call    type-language.yml    ---> config-zh_CN.yml
	}
}
