package adver.sarius.albion.mpf;

import io.swagger.client.model.MarketResponse;

public class Item {

	public static double tax = 0.06;
	public static double setupFee = 0.015;

	private String itemTypeId;
	private String displayName;
	private String city;
	private int quality;
	private long sellPriceMin;
	private long buyPriceMax;
	private int avgItemCount;

	public Item(String itemTypeId, String displayName, String city, int quality, long sellPriceMin, long buyPriceMax,
			int avgItemCount) {
		this.itemTypeId = itemTypeId;
		this.displayName = displayName;
		this.city = city;
		this.quality = quality;
		this.sellPriceMin = sellPriceMin;
		this.buyPriceMax = buyPriceMax;
		this.avgItemCount = avgItemCount;
	}

	public Item(MarketResponse mr) {
		this(mr.getItemTypeId(), "", mr.getCity(), mr.getQualityLevel(), mr.getSellPriceMin(), mr.getBuyPriceMax(), -1);
	}

	public String getItemTypeId() {
		return itemTypeId;
	}

	public void setItemTypeId(String itemTypeId) {
		this.itemTypeId = itemTypeId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public long getSellPriceMin() {
		return sellPriceMin;
	}

	public void setSellPriceMin(long sellPriceMin) {
		this.sellPriceMin = sellPriceMin;
	}

	public long getBuyPriceMax() {
		return buyPriceMax;
	}

	public void setBuyPriceMax(long buyPriceMax) {
		this.buyPriceMax = buyPriceMax;
	}

	public int getAvgItemCount() {
		return avgItemCount;
	}

	public void setAvgItemCount(int avgItemCount) {
		this.avgItemCount = avgItemCount;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public int getQuality() {
		return quality;
	}

	public String getEnchantment() {
		if (itemTypeId.contains("@")) {
			return itemTypeId.split("@")[1];
		} else {
			return "0";
		}
	}

	public String getTier() {
		String[] splitted = itemTypeId.split("_");
		if (splitted.length >= 2 && splitted[0].startsWith("T") && splitted[0].length() == 2) {
			return splitted[0].substring(1);
		} else
			return "-";
	}

	public String getQualityString() {
		switch (quality) {
		case 1:
			return "Normal";
		case 2:
			return "Good";
		case 3:
			return "Outstanding";
		case 4:
			return "Excellent";
		case 5:
			return "Masterpiece";
		default:
			return "Unknown";
		}
	}

	public double getProfitFactor() {
		return sellPriceMin * (1 - tax - setupFee) / (buyPriceMax * (1 + setupFee)) - 1;
	}

	@Override
	public String toString() {
		return "Profit: " + String.format("%.2f%%", getProfitFactor() * 100) + ", Name:" + displayName + ", Tier:"
				+ getTier() + ", Enchantment:" + getEnchantment() + ", Quality:" + getQualityString() + ", Buy:"
				+ buyPriceMax + ", Sell:" + sellPriceMin + ", DailyCount:" + avgItemCount + ", City:" + city + ", Id:"
				+ itemTypeId;
	}
}