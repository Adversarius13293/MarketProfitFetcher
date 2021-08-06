package adver.sarius.albion.mpf;

public class Item {

	private int order = -1;
	private String uniqueName = "undefined";
	private String displayName = "undefined";

	public Item() {

	}

	public Item(int order, String uniqueName, String displayName) {
		this.order = order;
		this.uniqueName = uniqueName;
		this.displayName = displayName;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
	public String getEnchantment() {
		if(uniqueName.contains("@")) {
			return uniqueName.split("@")[1];
		} else {
			return "-1";
		}
	}
	
	public String getQuality() {
		return "TODO";
	}
	
	
	@Override
	public String toString() {
		return displayName;
	}

}
