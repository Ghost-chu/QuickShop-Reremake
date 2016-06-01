package org.maxgamer.quickshop.Util;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

@SuppressWarnings("deprecation")
public class CustomPotionsName {
	private static String fullPotionFormat, signPotionFormat, splashSign, splashFull;
	private static Map<PotionType,Names> potionTypes;
	private static Map<PotionEffectType,String> potionEffects;
	
	private CustomPotionsName() {}
	
	public static String getSignName(ItemStack potionItemStack) {
		Potion potion = Potion.fromItemStack(potionItemStack);
		if (potion==null) {
			return "Invalid";
		}
		
		Names names = potionTypes.get(potion.getType());
		String type;
		if (names==null) {
			type = Util.prettifyText(potion.getType().toString());
		} else {
			type = names.getSign();
		}
		
		return (getSignPotionFormat().replace("%splash", potion.isSplash() ? splashSign : "").replace("%type", type).replace("%tier", (potion.getLevel()!=0 ? ""+potion.getLevel() : ""))).trim();
	}
	
	public static String getFullName(ItemStack potionItemStack) {
		Potion potion = Potion.fromItemStack(potionItemStack);
		if (potion==null) {
			return "Invalid";
		}
		
		Names names = potionTypes.get(potion.getType());
		String type;
		if (names==null) {
			type = Util.prettifyText(potion.getType().toString());
		} else {
			type = names.getFull();
		}
		
		return (getFullPotionFormat().replace("%splash", potion.isSplash() ? splashFull : "").replace("%type", type).replace("%tier", (potion.getLevel()!=0 ? ""+potion.getLevel() : ""))).trim();
	}
	
	public static String getEffects(ItemStack potionItemStack) {
		Potion potion = Potion.fromItemStack(potionItemStack);
		if (potion==null) {
			return "Invalid";
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (PotionEffect potionEffect : potion.getEffects()) {
			PotionEffectType type = potionEffect.getType();
			String name = potionEffects.get(type);
			if (name==null) {
				name = Util.prettifyText(type.getName());
			}
			
			sb.append(name+(potionEffect.getAmplifier()!=0 ? " "+potionEffect.getAmplifier() : "")+" ("+((int)(potionEffect.getDuration()/20))+"s) | ");
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


	public static String getFullPotionFormat() {
		return fullPotionFormat;
	}

	public static void setFullPotionFormat(String fullPotionFormat) {
		CustomPotionsName.fullPotionFormat = fullPotionFormat;
	}

	public static String getSignPotionFormat() {
		return signPotionFormat;
	}

	public static void setSignPotionFormat(String fullSignFormat) {
		CustomPotionsName.signPotionFormat = fullSignFormat;
	}

	public static String getSplashSign() {
		return splashSign;
	}

	public static void setSplashSign(String splashSign) {
		CustomPotionsName.splashSign = splashSign;
	}

	public static String getSplashFull() {
		return splashFull;
	}

	public static void setSplashFull(String splashFull) {
		CustomPotionsName.splashFull = splashFull;
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
