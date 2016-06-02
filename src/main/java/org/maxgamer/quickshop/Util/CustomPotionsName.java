package org.maxgamer.quickshop.Util;

import java.util.Collection;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class CustomPotionsName {
	private static Map<PotionType,Names> potionTypes;
	private static Map<PotionEffectType,String> potionEffects;
	private static String[] signFormat, shopInfoFormat;
	
	private CustomPotionsName() {}
	
	public static String getSignName(ItemStack potionItemStack) {
		GenericPotionData potion = NMS.getPotionData(potionItemStack);
		if (potion==null) {
			return "InvalidPotion";
		}
		
		if (potion.isCustom() && potionItemStack.hasItemMeta() && potionItemStack.getItemMeta().hasDisplayName()) {
			// return potion name for custom potions
			return potionItemStack.getItemMeta().getDisplayName().replace(" ", "");
		}
		
		Names names = potionTypes.get(potion.getType());
		String type;
		if (names==null) {
			if (potion.getType()==PotionType.WATER) {
				return "WaterBottle";
			}
			type = Util.prettifyText(potion.getType().toString()).replace(" ", "")+"Potion";
		} else {
			type = names.getSign();
		}
		
		type+=(potion.getAmplifier()>1 ? potion.getAmplifier() : "")+(potion.getDuration()==-1 ? "+" : "");
		
		String variety;
		switch(potion.getCategory()) {
		case NORMAL:
			variety = getSignFormat()[1];
			break;
		case SPLASH:
			variety = getSignFormat()[2];
			break;
		case LINGERING:
			variety = getSignFormat()[3];
			break;
		default:
			variety = "Invalid";
			break;
		}
		
		return (getSignFormat()[0].replace("%variety", variety).replace("%type", type)).trim();
	}
	
	public static String getFullName(ItemStack potionItemStack) {
		GenericPotionData potion = NMS.getPotionData(potionItemStack);
		if (potion==null) {
			return "InvalidPotion";
		}
		
		if (potion.isCustom() && potionItemStack.hasItemMeta() && potionItemStack.getItemMeta().hasDisplayName()) {
			// return potion name for custom potions
			return potionItemStack.getItemMeta().getDisplayName().replace(" ", "");
		}
		
		Names names = potionTypes.get(potion.getType());
		String type;
		if (names==null) {
			if (potion.getType()==PotionType.WATER) {
				return "Water Bottle";
			}
			type = Util.prettifyText(potion.getType().toString())+" Potion";
		} else {
			type = names.getFull();
		}
		
		type+=(potion.getAmplifier()>1 ? " "+potion.getAmplifier() : "")+(potion.getDuration()>0 ? " ("+((int)(potion.getDuration()/20))+"s)" : potion.getDuration()==-1 ? "+" : "");
		
		String variety;
		switch(potion.getCategory()) {
		case NORMAL:
			variety = getShopInfoFormat()[1];
			break;
		case SPLASH:
			variety = getShopInfoFormat()[2];
			break;
		case LINGERING:
			variety = getShopInfoFormat()[3];
			break;
		default:
			variety = "Invalid";
			break;
		}
		
		return (getShopInfoFormat()[0].replace("%variety", variety).replace("%type", type)).trim();
	}
	
	public static String getEffects(ItemStack potionItemStack) {
		GenericPotionData potion = NMS.getPotionData(potionItemStack);
		if (potion==null) {
			return "InvalidPotion";
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (PotionEffect potionEffect : potion.getEffects()) {
			PotionEffectType type = potionEffect.getType();
			String name = potionEffects.get(type);
			if (name==null) {
				name = Util.prettifyText(type.getName());
			}
			
			sb.append(name.trim()+(potionEffect.getAmplifier()>1 ? " "+potionEffect.getAmplifier() : "")+(potionEffect.getDuration()>0 ? " ("+((int)(potionEffect.getDuration()/20))+"s)" : potionEffect.getDuration()==-1 ? "+" : "")+" | ");
		}
		
		if (sb.length()>0) {
			sb.setLength(sb.length()-3);
		}
		
		return sb.toString();
	}

	public static class Names {
		private String sign, full;

		public Names(String sign, String full) {
			this.sign = sign;
			this.full = full;
		}

		public String getSign() {
			return sign;
		}

		public String getFull() {
			return full;
		}

		@Override
		public String toString() {
			return "Names [sign=" + sign + ", full=" + full + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((full == null) ? 0 : full.hashCode());
			result = prime * result + ((sign == null) ? 0 : sign.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Names)) {
				return false;
			}
			Names other = (Names) obj;
			if (full == null) {
				if (other.full != null) {
					return false;
				}
			} else if (!full.equals(other.full)) {
				return false;
			}
			if (sign == null) {
				if (other.sign != null) {
					return false;
				}
			} else if (!sign.equals(other.sign)) {
				return false;
			}
			return true;
		}
		
	}
	
	public static class GenericPotionData {
		private PotionType type;
		private Collection<PotionEffect> effects;
		private Category category;
		private boolean custom;
		private int amplifier, duration;
		
		public GenericPotionData(PotionType type, Collection<PotionEffect> effects, Category category, boolean isCustom, int duration, int amplifier) {
			this.type = type;
			this.effects = effects;
			this.category = category;
			this.custom = isCustom;
			this.duration = duration;
			this.amplifier = amplifier;
		}

		public PotionType getType() {
			return type;
		}

		public Collection<PotionEffect> getEffects() {
			return effects;
		}

		public Category getCategory() {
			return category;
		}

		public boolean isCustom() {
			return custom;
		}

		public int getAmplifier() {
			return amplifier;
		}

		public int getDuration() {
			return duration;
		}

		public enum Category {
			NORMAL, SPLASH, LINGERING;
		}
	}

	public static String[] getSignFormat() {
		return signFormat;
	}

	public static String[] getShopInfoFormat() {
		return shopInfoFormat;
	}
	
	public static void setSignFormat(String[] signFormat) {
		CustomPotionsName.signFormat = signFormat;
	}

	public static void setShopInfoFormat(String[] shopInfoFormat) {
		CustomPotionsName.shopInfoFormat = shopInfoFormat;
	}

	public static Map<PotionType, Names> getPotionTypes() {
		return potionTypes;
	}

	public static void setPotionTypes(Map<PotionType, Names> potionTypes) {
		CustomPotionsName.potionTypes = potionTypes;
	}

	public static Map<PotionEffectType,String> getPotionEffects() {
		return potionEffects;
	}

	public static void setPotionEffects(Map<PotionEffectType,String> potionEffects) {
		CustomPotionsName.potionEffects = potionEffects;
	}
}
