package org.maxgamer.quickshop.Shop;

public enum ShopType {
	SELLING(0), BUYING(1);
	private int id;
	private ShopType(int id){
		this.id = id;
	}
	
	public static ShopType fromID(int id) {
		for(ShopType type:ShopType.values()){
			if(type.id==id){
				return type;
			}
		}
		return null;
	}

	public static int toID(ShopType shopType) {
		return shopType.id;
	}

	public int toID() {
		return id;
	}
}