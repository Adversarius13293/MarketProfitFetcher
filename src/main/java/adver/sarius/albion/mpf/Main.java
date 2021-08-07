package adver.sarius.albion.mpf;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.client.ApiException;
import io.swagger.client.api.ChartsApi;
import io.swagger.client.api.PricesApi;
import io.swagger.client.model.MarketResponse;

public class Main {

	static final double setupFee = 0.015; // for sell and buy orders

	private int minPrice = 0; // what I want to sell for at least
	private int maxPrice = Integer.MAX_VALUE; // what I want to pay at most
	private double minWinPercent = 0.10;
	private String cities = "Fort Sterling"; // processing currently only supports one city at a time
	private double tax = 0.03; // 0.03 for premium, 0.06 for f2p
	private int avgCountTimespan = 5; // in hours?
	private int minCount = 10;

	private PricesApi pricesApi = new PricesApi();

	public static void main(String[] args) {
		new Main().doStuff();
	}

	// Used swagger-codegen for client:
	// java -jar swagger-codegen-cli-3.0.27.jar generate -i
	// https://www.albion-online-data.com/api/v2/swagger.json -l java
	// Need to manually map returned attribute names, since they somehow don't match
	// the swagger.json
	// Can't get date parsing to work, so don't map them correctly.
	public Main() {
		pricesApi.getApiClient().setBasePath("https://www.albion-online-data.com");
	}

	public void doStuff() {

		ChartsApi chartsApi = new ChartsApi();
		chartsApi.getApiClient().setBasePath("https://www.albion-online-data.com");

		Map<String, String> allItemNames = ItemNameParser.parseInputFile("itemnames.txt");
		List<Item> matchingItems = new ArrayList<>();

		// try {
		// List<MarketHistoriesResponse> res =
		// chartsApi.apiV2StatsHistoryItemListFormatGet("T3_MEAL_OMELETTE", "json",
		// cities, OffsetDateTime.parse("2021-08-05T00:00:00+01:00"),
		// OffsetDateTime.parse("2021-08-07T00:00:00+01:00"), "1", new Integer(6));
		// System.out.println(res);
		// } catch (ApiException e1) {
		// // TODO Auto-generated catch block
		// System.out.println("Error: " + e1);
		// }
		final int maxLength = 7500; // query multiple items in one request. But the requests must not be too long.
		StringBuilder builder = new StringBuilder(maxLength);

		for (String i : allItemNames.keySet()) {
			if ((builder.length() + i.length()) >= maxLength) {
				builder.deleteCharAt(0);
				sendPriceRequest(builder.toString(), matchingItems);
				builder = new StringBuilder(maxLength);
			}
			builder.append(',');
			builder.append(i);
			System.out.println("done");
			break;

		}
		builder.deleteCharAt(0);
		sendPriceRequest(builder.toString(), matchingItems);
		
		// TODO: Now check avgCount for remaining ones?
		// and sort by profit%

	}

	private void sendPriceRequest(String items, List<Item> matchingItems) {
		try {
			List<MarketResponse> prices = pricesApi.apiV2StatsPricesItemListFormatGet(items, "json", cities, "");
			for (MarketResponse mr : prices) {
				if (mr.getSellPriceMax() > this.minPrice && mr.getBuyPriceMin() < this.maxPrice
						&& (mr.getSellPriceMin() * (1 - tax - setupFee) / (mr.getBuyPriceMax() * (1 + setupFee))
								- 1) > minWinPercent) {
					matchingItems.add(new Item(mr));
				}
			}
		} catch (ApiException e) {
			System.out.println("Error! " + e);
		}
	}
}
