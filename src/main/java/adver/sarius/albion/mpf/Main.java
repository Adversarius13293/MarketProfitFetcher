package adver.sarius.albion.mpf;

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

	private long minPrice = 0; // what I want to sell for at least
	private long maxPrice = 25000; // what I want to pay at most
	private double minWinPercent = 0.10; // how much percent of the investment after taxes
	private String cities = "Fort Sterling"; // comma separated, or empty for all
	private int avgCountTimespan = 3; // in days
	private int minCount = 100; // average count over the given timespan, to filter out dead items
	private boolean filterMissingBuyPrice = true; // filter out results with buyprice 0 and therefore infinite profit.
	private int showResults = 70; // number of displayed results

	public static void main(String[] args) {
		Item.setupFee = 0.015; // for sell and buy orders
		Item.tax = 0.06; // 0.03 for premium, 0.06 for f2p
		
		new Main().getFlippingProfits();
	}

	// Used swagger-codegen for client:
	// java -jar swagger-codegen-cli-3.0.27.jar generate -i
	// https://www.albion-online-data.com/api/v2/swagger.json -l java
	// Need to manually map returned attribute names, since they somehow don't match
	// the swagger.json
	// Can't get date parsing to work, so don't map them correctly.
	public Main() {
		pricesApi.getApiClient().setBasePath("https://www.albion-online-data.com");
		chartsApi.getApiClient().setBasePath("https://www.albion-online-data.com");
	}

	// TODO: Just collect prices and history for every item, and filter afterwards?
	// TODO: Black Market and Caerleon fetcher
	// TODO: Artifact salvage profits
	
	public void getFlippingProfits() {
		System.out.println("Reading all item names");
		allItemNames = ItemNameParser.parseInputFile("itemnames.txt");
		List<Item> matchingItems = new ArrayList<>();
		System.out.println("Requesting prices");
		int maxLength = 7000; // query multiple items in one request. But the requests must not be too long.
		StringBuilder builder = new StringBuilder(maxLength);

		for (String i : allItemNames.keySet()) {
			if ((builder.length() + i.length()) >= maxLength) {
				builder.deleteCharAt(0);
				sendPriceRequest(builder.toString(), matchingItems);
				builder = new StringBuilder(maxLength);
//				System.out.println("debug break");break;
			}
			builder.append(',');
			builder.append(i);

		}
		if (builder.length() > 0) {
			builder.deleteCharAt(0);
			sendPriceRequest(builder.toString(), matchingItems);
		}

		maxLength = 5500;
		for (int quality = 1; quality <= 5; quality++) {
			System.out.println("Getting history for quality " + quality);
			builder = new StringBuilder(maxLength);
			for (Item i : matchingItems) {
				if (i.getQuality() != quality) {
					continue;
				}
				if ((builder.length() + i.getItemTypeId().length()) >= maxLength) {
					builder.deleteCharAt(0);
					sendHistoryRequest(builder.toString(), matchingItems, quality);
					builder = new StringBuilder(maxLength);
				}
				builder.append(',');
				builder.append(i.getItemTypeId());
			}
			if (builder.length() > 0) {
				builder.deleteCharAt(0);
				sendHistoryRequest(builder.toString(), matchingItems, quality);

			}
		}

		List<Item> finalResult = matchingItems.stream().filter(i -> {
			return i.getAvgItemCount() >= minCount;
		}).sorted(Comparator.comparingDouble((Item i) -> i.getProfitFactor()).reversed()).collect(Collectors.toList());

		System.out.println("Found results:");
		for (int i = 0; i < Math.min(showResults, finalResult.size()); i++) {
			System.out.println(finalResult.get(i));
		}
	}

	private void sendPriceRequest(String items, List<Item> matchingItems) {
		try {
			List<MarketResponse> prices = pricesApi.apiV2StatsPricesItemListFormatGet(items, "json", cities, "");
			for (MarketResponse mr : prices) {
				Item item = new Item(mr);
				if (item.getSellPriceMin() > this.minPrice && item.getBuyPriceMax() < this.maxPrice
						&& item.getProfitFactor() > minWinPercent
						&& (!filterMissingBuyPrice || (item.getBuyPriceMax() > 0
								&& !(item.getBuyPriceMax() == 1 && item.getSellPriceMin() > 50)))) {
					item.setDisplayName(allItemNames.get(item.getItemTypeId()));
					matchingItems.add(item);
				}
			}
		} catch (ApiException e) {
			System.out.println("Error! " + e);
		}
	}

	private void sendHistoryRequest(String items, List<Item> matchingItems, int quality) {
		try {
			// some weird date manipulation until it matched the ingame stats.
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
					System.out.println("Got history for unknown item: " + mhr);
					continue;
				}
				found.get().setAvgItemCount(sum / avgCountTimespan);
			}
		} catch (ApiException e) {
			System.out.println("Error! " + e);
		}
	}
}
