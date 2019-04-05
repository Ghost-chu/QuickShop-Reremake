package org.maxgamer.quickshop;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class Language {
	QuickShop plugin;
	ArrayList<String> languages = new ArrayList<>();
	public Language(QuickShop plugin) {
		this.plugin = plugin;
		languages.clear();
		languages.add("en_US");
	}
	public String getComputerLanguage() {
		return Locale.getDefault().toString();
	}
	public ArrayList<String> getSupportsLanguageList() {
		return languages;
	}
	public InputStream getFile(String language, String type) {
		if((language==null) || !languages.contains(language) || (language==""))
			language="en_US";
		if(type == null || type == "")
			throw new IllegalArgumentException("Type cannot be null or empty");
		return plugin.getResource(type+"-"+language+".yml");
		//File name should call    type-language.yml    ---> config-zh_CN.yml
	}
}
