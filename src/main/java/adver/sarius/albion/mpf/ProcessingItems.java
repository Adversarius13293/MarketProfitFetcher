package adver.sarius.albion.mpf;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class for processing input items into output items and listing the profits.
 * For crafting, enchanting, salvaging, or even city-to-city transports.
 */
public class ProcessingItems {

	public static final String OPERATION_SALVAGE_ARTIFACT = "Salvage Artifact (Artifact Foundry/Repair Station)";
//	public static final String OPERATION_TRANSMUTE = "Transmute (Artifact Foundry)";
	// TODO: .... more operations

	public static boolean useDirectBuy = true;
	public static boolean useDirectSell = false;

	// amounts as double, since things like salvaging are percentage based and may
	// return on average .5
	private Map<Item, Double> itemsIn;
	private double silverIn;
	private Map<Item, Double> itemsOut;
	private double silverOut;
	private String operation;

	public ProcessingItems(String operation) {
		this(new HashMap<>(), 0, new HashMap<>(), 0, operation);
	}

	public ProcessingItems(Map<Item, Double> itemsIn, double silverIn, Map<Item, Double> itemsOut, double silverOut,
			String operation) {
		this.itemsIn = itemsIn;
		this.silverIn = silverIn;
		this.itemsOut = itemsOut;
		this.silverOut = silverOut;
		this.operation = operation;
	}

	public double getSilverIn() {
		return this.silverIn;
	}

	public double getSilverOut() {
		return this.silverOut;
	}

	public Map<Item, Double> getItemsIn() {
		return itemsIn;
	}

	public void setItemsIn(Map<Item, Double> itemsIn) {
		this.itemsIn = itemsIn;
	}

	public Map<Item, Double> getItemsOut() {
		return itemsOut;
	}

	public void setItemsOut(Map<Item, Double> itemsOut) {
		this.itemsOut = itemsOut;
	}

	public void addItemIn(Item item, double count) {
		if (itemsIn.containsKey(item)) {
			itemsIn.put(item, itemsIn.get(item) + count);
		} else {
			itemsIn.put(item, count);
		}
	}

	public void addSilverIn(double silver) {
		silverIn += silver;
	}

	public void addItemOut(Item item, double count) {
		if (itemsOut.containsKey(item)) {
			itemsOut.put(item, itemsOut.get(item) + count);
		} else {
			itemsOut.put(item, count);
		}
	}

	public void addSilverOut(double silver) {
		silverOut += silver;
	}

	public double getBuyValue() {
		double value = silverIn;
		for (Entry<Item, Double> entry : itemsIn.entrySet()) {
			if (useDirectBuy) {
				value += entry.getKey().getSellPriceMin() * entry.getValue();
			} else {
				value += entry.getKey().getBuyPriceMax() * entry.getValue();
			}
		}
		return value;
	}

	public double getSellValue() {
		double value = silverOut;
		for (Entry<Item, Double> entry : itemsOut.entrySet()) {
			if (useDirectSell) {
				value += entry.getKey().getBuyPriceMax() * entry.getValue();
			} else {
				value += entry.getKey().getSellPriceMin() * entry.getValue();
			}
		}
		return value;
	}

	public double getProfitFactor() {
		double buyTax = useDirectBuy ? 0 : Item.setupFee;
		double sellTax = useDirectSell ? Item.tax : Item.tax - Item.setupFee;
		return ((getSellValue() - silverOut) * (1 - sellTax) + silverOut)
				/ ((getBuyValue() - silverIn) * (1 + buyTax) + silverIn) - 1;
	}

	// TODO: Need to find a good way to display everything
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Profit:").append(String.format("%.2f%%", getProfitFactor() * 100)).append(", ");

		builder.append("In:").append(getBuyValue()).append(", Out:").append(getSellValue()).append(", ");

		for (Entry<Item, Double> entry : itemsIn.entrySet()) {
			builder.append(doubleToString(entry.getValue())).append("x ").append(entry.getKey()).append(", ");
		}
		builder.append("Pay extra silver:").append(doubleToString(silverIn)).append(", ");
		builder.append(operation).append(", ");
		for (Entry<Item, Double> entry : itemsOut.entrySet()) {
			builder.append(doubleToString(entry.getValue())).append("x ").append(entry.getKey()).append(", ");
		}
		builder.append("Get extra silver:").append(doubleToString(silverOut));
		return builder.toString();
	}

	private String doubleToString(double d) {
		if (d == (long) d) {
			return String.format("%d", (long) d);
		} else {
			return String.format("%s", d);
		}
	}
}