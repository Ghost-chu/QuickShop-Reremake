package org.maxgamer.quickshop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

public class Language {
	private QuickShop plugin;
	private List<String> languages = new ArrayList<>();

	Language(QuickShop plugin) {
		this.plugin = plugin;
		languages.clear();
		languages.add("en");
		languages.add("fr");
		languages.add("de");
		languages.add("ko");
		languages.add("pl");
		languages.add("ru");
		languages.add("sv");
		languages.add("zh_TW");
	}
	// public String getComputerLanguage() {
	// 	if(plugin.getConfig().getString("language")==null || plugin.getConfig().getString("language").equals("default")) {//Allow user replace this
	// 		return Locale.getDefault().getLanguage().toString();
	// 	}
	// 	return plugin.getConfig().getString("language");
	// }
	public List<String> getSupportsLanguageList() {
		return languages;
	}
	public InputStream getFile(String language, String type) {
		if((language==null))
			language="en";
		if(type == null || type.isEmpty())
			throw new IllegalArgumentException("Type cannot be null or empty");
		InputStream inputStream = plugin.getResource(type+"-"+language+".yml");
		if(inputStream == null)
            plugin.getResource(type+"-"+"en"+".yml");
		return inputStream;
		//File name should call    type-language.yml    ---> config-zh.yml
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
		//File name should call    type-language.yml    ---> config-zh.yml
	}
}
