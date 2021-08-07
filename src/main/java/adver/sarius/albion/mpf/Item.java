package adver.sarius.albion.mpf;

import io.swagger.client.model.MarketResponse;

public class Item {

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
			return "-1";
		}
	}

	@Override
	public String toString() {
		return displayName;
	}
}