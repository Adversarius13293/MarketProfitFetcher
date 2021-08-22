package adver.sarius.albion.mpf;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import io.swagger.client.ApiException;
import io.swagger.client.api.ChartsApi;
import io.swagger.client.api.PricesApi;
import io.swagger.client.model.MarketHistoriesResponse;
import io.swagger.client.model.MarketHistoryResponse;
import io.swagger.client.model.MarketResponse;

public class Main {
	private Map<String, String> allItemNames;
	private PricesApi pricesApi = new PricesApi();
	private ChartsApi chartsApi = new ChartsApi();

	// TODO: move config values in each print method, to configure them separately
	private long minPrice = 0; // what I want to sell for at least
	private long maxPrice = 500000; // what I want to pay at most
	private double minWinPercent = 0.05; // how much percent of the investment after taxes
	private String cities = "Fort Sterling"; // comma separated, or empty for all
	private String qualities = "1"; // qualities to search for, comma separated, empty for all
	private int avgCountTimespan = 3; // in days
	private int minCount = 5; // average count over the given timespan, to filter out dead items
	// TODO: Switch to minBuyPrice? To Filter out 0, or 0 and 1?
	private boolean filterOutMissingBuyPrice = true; // filter out results with buyprice 0 and therefore infinite
														// profit.
	private int showResults = 100; // number of displayed results

	// internal counter for logging
	private int reusableCounter = 0;

	// TODO: Use local swagger.json for correct names and maybe dates?
	// TODO: Evaluate the date on prices. To show old ones I should check manually.
	// Also if there is history for every day.

	public static void main(String[] args) {
		Item.setupFee = 0.015; // for sell and buy orders
		Item.tax = 0.06; // 0.03 for premium, 0.06 for f2p
		ProcessingItems.useDirectBuy = false;
		ProcessingItems.useDirectSell = false;

		Main myMain = new Main();
		// TODO: Need to fix this, probably by giving each method its own filter params
		String oldCities = myMain.cities;
		if (!myMain.cities.isEmpty()) {
			myMain.cities = myMain.cities + ",Caerleon,Black Market";
		}
		List<Item> items = myMain.loadItemsAndPrices();
		myMain.cities = oldCities;

		log("Parsing xml...");
		ItemXmlParser xmlParser = new ItemXmlParser("items.xml");
		log("...Done parsing xml");

		log("Removing invalid item qualities...");
		int size = items.size();
		// TODO: Some items still need to be filtered by quality. Like empty journals,
		// since the xml does not contain the _EMTPY item id
		// --> Journals are fixed now, but there might be more?
		xmlParser.removeInvalidItemQualities(items);
		log("...Removed " + (size - items.size()) + " items");

		myMain.filterOutSpecificItems(items);

		myMain.loadItemHistories(items);

		log("Filtering missing prices...");
		myMain.printMissingBuyPrices(items);
		myMain.printMissingSellPrices(items);

		log("Reading artifact salvaging...");
		List<ProcessingItems> artifactSalvage = xmlParser.readArtifactSalvage(items);
		myMain.printArtifactSalvageProfits(artifactSalvage);

		// currently too many old offers to make profit in a reasonable time
//		myMain.printMarketToMarket("Caerleon", "Black Market", items);

		myMain.printFlippingProfits(items);
		log("Program finished.");
	}

	// Used swagger-codegen for client:
	// java -jar swagger-codegen-cli-3.0.27.jar generate -i
	// https://www.albion-online-data.com/api/v2/swagger.json -l java
	//
	// Need to manually map returned attribute names, since they somehow don't match
	// the swagger.json
	// Can't get date parsing to work, so don't map them correctly.
	public Main() {
		pricesApi.getApiClient().setBasePath("https://www.albion-online-data.com");
		chartsApi.getApiClient().setBasePath("https://www.albion-online-data.com");
	}

	public void printMissingBuyPrices(List<Item> items) {
		List<Item> finalResult = items.stream().filter(i -> {
			return i.getBuyPriceMax() <= 0 && (cities.isEmpty() || cities.contains(i.getCity()))
					&& (qualities.isEmpty() || qualities.contains(i.getQuality() + ""));
		}).sorted(Comparator.comparingDouble((Item i) -> i.getProfitFactor()).reversed()).collect(Collectors.toList());
		printList(finalResult, "Missing buy prices:");
	}

	public void printMissingSellPrices(List<Item> items) {
		List<Item> finalResult = items.stream().filter(i -> {
			return i.getSellPriceMin() <= 0 && (cities.isEmpty() || cities.contains(i.getCity()))
					&& (qualities.isEmpty() || qualities.contains(i.getQuality() + ""));
		}).sorted(Comparator.comparingDouble((Item i) -> i.getProfitFactor()).reversed()).collect(Collectors.toList());
		printList(finalResult, "Missing sell prices:");
	}

	/**
	 * Manually filter out specific items which can not be traded at the market.
	 * 
	 * @param items list to remove items from.
	 */
	private void filterOutSpecificItems(List<Item> items) {
		// TODO: Need to filter out cosmetics like CAPE_PLATE_KEEPER. Which is probably
		// not tradeable and the relevant item only is
		// UNIQUE_UNLOCK_T6_CAPE_PLATE_KEEPER. (The craftable item itself is
		// T#_CAPEITEM_KEEPER)
		int size = items.size();
		items.removeIf(i -> i.getDisplayName().contains("(Partially Full)")
				|| i.getDisplayName().startsWith("Delivery:") || i.getDisplayName().equals("Letter of Transfer")
				|| i.getDisplayName().startsWith("Rogue Adventurer's") || i.getItemTypeId().contains("_CRYSTALLEAGUE"));
		log("Filtered out " + (size - items.size()) + " specific items");
	}

	public void printArtifactSalvageProfits(List<ProcessingItems> salvaging) {
		// TODO: filtering by count
		log("Filtering artifact salvage results...");
		List<ProcessingItems> finalResult = salvaging.stream().filter(i -> {
			boolean citiesFit = i.getItemsIn().keySet().stream().allMatch(item -> cities.contains(item.getCity()))
					&& i.getItemsOut().keySet().stream().allMatch(item -> cities.contains(item.getCity()));

			return i.getSellValue() > this.minPrice && i.getBuyValue() < this.maxPrice
					&& i.getProfitFactor() > minWinPercent && (!filterOutMissingBuyPrice || i.getBuyValue() > 0)
					&& (cities.isEmpty() || citiesFit) && i.doAllQualitiesMatch(qualities);
		}).sorted(Comparator.comparingDouble((ProcessingItems i) -> i.getProfitFactor()).reversed())
				.collect(Collectors.toList());
		printList(finalResult, "Artifact salvaging:");
	}

	public void printFlippingProfits(List<Item> items) {
		// qualities check this way only works for up to 9, but since 5 is the current
		// max it should be fine
		List<Item> finalResult = items.stream().filter(i -> {
			return i.getSellPriceMin() > this.minPrice && i.getBuyPriceMax() < this.maxPrice
					&& i.getProfitFactor() > minWinPercent && i.getAvgItemCount() >= minCount
					&& (!filterOutMissingBuyPrice || i.getBuyPriceMax() > 0)
					&& (cities.isEmpty() || cities.contains(i.getCity()))
					&& (qualities.isEmpty() || qualities.contains(i.getQuality() + ""));
		}).sorted(Comparator.comparingDouble((Item i) -> i.getProfitFactor()).reversed()).collect(Collectors.toList());
		printList(finalResult, "Flipping profits:");
	}

	public void printMarketToMarket(String fromMarket, String toMarket, List<Item> items) {
		List<Item> source = items.stream().filter(i -> fromMarket.equals(i.getCity())).collect(Collectors.toList());
		List<Item> target = items.stream().filter(i -> toMarket.equals(i.getCity())).collect(Collectors.toList());
		List<ProcessingItems> processed = new ArrayList<>();
		for (Item t : source) {
			// TODO: Should I error check for more than one result?
			Optional<Item> match = target.stream().filter(i -> i.equalItem(t)).findFirst();
			if (match.isPresent()) {
				// TODO: TODO: TODO: Filtering
				ProcessingItems pi = new ProcessingItems(fromMarket + " to " + toMarket);
				pi.addItemIn(t, 1);
				pi.addItemOut(match.get(), 1);
				processed.add(pi);
			}
		}
		
		List<ProcessingItems> filtered = processed.stream().filter(i -> {
			return i.getSellValue() > this.minPrice && i.getBuyValue() < this.maxPrice
					&& i.getProfitFactor() > minWinPercent 
					&& (!filterOutMissingBuyPrice || i.getBuyValue() > 0)
					&& i.doAllQualitiesMatch(qualities);
		}).sorted(Comparator.comparingDouble((ProcessingItems i) -> i.getProfitFactor()).reversed()).collect(Collectors.toList());
		
		printList(filtered, "Transport from market " + fromMarket + " to market " + toMarket + ":");
	}

	private void printList(List<?> items, String listName) {
		System.out.print(System.lineSeparator());
		log(listName);
		for (int i = 0; i < Math.min(showResults, items.size()); i++) {
			System.out.println(items.get(i));
		}
	}

	public List<Item> loadItemsAndPrices() {
		// TODO: file parsing in extra method?
		log("Reading all item names...");
		allItemNames = ItemNameParser.parseInputFile("itemnames.txt");
		log("...Successfully read " + allItemNames.size() + " item names");

		List<Item> items = new ArrayList<>();
		log("Requesting all prices...");
		// minimize number of requests. But the requests must not be too long.
		// arbitrary picked this number after some tests.
		int maxLength = 7000;
		StringBuilder builder = new StringBuilder(maxLength);
		reusableCounter = 0;
		for (String i : allItemNames.keySet()) {
			if ((builder.length() + i.length()) >= maxLength) {
				// unnecessary comma seem to be ignored, but just do it clean
				builder.deleteCharAt(0);
				sendPriceRequest(builder.toString(), items);
				builder = new StringBuilder(maxLength);
			}
			builder.append(',');
			builder.append(i);
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(0);
			sendPriceRequest(builder.toString(), items);
		}
		if (items.stream().anyMatch(i -> i.getQuality() < 1)) {
			logError("Found items with unknow quality!");
		}
		log("...Received " + items.size() + " item prices from " + reusableCounter + " requests");
		return items;
	}

	public void loadItemHistories(List<Item> items) {
		int maxLength = 5500;
		StringBuilder builder = new StringBuilder(maxLength);
		log("Getting histories for items...");
		// TODO: Get possible qualities from items list to have it generic?
		for (int quality = 1; quality <= 5; quality++) {
			builder = new StringBuilder(maxLength);
			for (Item i : items) {
				// TODO: is a filtered stream more efficient/simpler?
				if (i.getQuality() != quality) {
					continue;
				}
				if ((builder.length() + i.getItemTypeId().length()) >= maxLength) {
					builder.deleteCharAt(0);
					sendHistoryRequest(builder.toString(), items, quality);
					builder = new StringBuilder(maxLength);
				}
				builder.append(',');
				builder.append(i.getItemTypeId());
			}
			if (builder.length() > 0) {
				builder.deleteCharAt(0);
				sendHistoryRequest(builder.toString(), items, quality);
			}
		}
		log("...Histories done");
	}

	private void sendPriceRequest(String items, List<Item> matchingItems) {
		try {
			reusableCounter++;
			// note: requesting things without qualities like resources will return only the
			// no-quality-1 result, if "qualities" string is kept empty. But as soon other
			// items of the same request return other qualities, the non-quality items will
			// also be returned with all the same qualities and therefore without any price?
			List<MarketResponse> prices = pricesApi.apiV2StatsPricesItemListFormatGet(items, "json", cities, qualities);
			for (MarketResponse mr : prices) {
				Item item = new Item(mr);
				item.setDisplayName(allItemNames.get(item.getItemTypeId()));
				matchingItems.add(item);
			}
		} catch (ApiException e) {
			logError("Api error while requesting prices! " + e);
		}
	}

	private void sendHistoryRequest(String items, List<Item> matchingItems, int quality) {
		try {
			// some weird date manipulation until it matched the ingame stats
			// TODO: should make some more tests if it works for all times of the day
			List<MarketHistoriesResponse> res = chartsApi.apiV2StatsHistoryItemListFormatGet(items, "json", cities,
					OffsetDateTime.now().minusDays(avgCountTimespan).truncatedTo(ChronoUnit.DAYS).plusHours(5),
					OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS).plusHours(5), quality + "", 24);
			for (MarketHistoriesResponse mhr : res) {
				int sum = 0;
				for (MarketHistoryResponse r : mhr.getData()) {
					sum += r.getItemCount();
				}
				Optional<Item> found = matchingItems.stream().filter(i -> {
					return i.getCity().equals(mhr.getLocation()) && i.getItemTypeId().equals(mhr.getItemTypeId())
							&& i.getQuality() == mhr.getQualityLevel();
				}).findAny();
				if (!found.isPresent()) {
					logError("Got history for unknown item, will skip it: " + mhr);
					continue;
				}
				// many items do not return a history entry for every day, which can greatly
				// reduce the average. But there are also items that just did not get sold that
				// day. Probably have to live with that.
				found.get().setAvgItemCount(sum / avgCountTimespan);
			}
		} catch (ApiException e) {
			logError("Api error while requesting Histories " + e);
		}
	}

	// proper logging framework would be nice, but I am too lazy
	public static void log(String message) {
		System.out.println(LocalTime.now() + ": " + message);
	}

	public static void logError(String message) {
		System.err.println(LocalTime.now() + ": " + message);
	}
}
