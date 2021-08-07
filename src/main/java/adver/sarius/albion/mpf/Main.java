package adver.sarius.albion.mpf;

import java.util.ArrayList;
import java.util.List;

import io.swagger.client.ApiException;
import io.swagger.client.api.ChartsApi;
import io.swagger.client.api.PricesApi;
import io.swagger.client.model.MarketResponse;

public class Main {

	static final double setupFee = 0.015; // for sell and buy orders

	private int minPrice = 0; // what I want to sell for at least
	private int maxPrice = Integer.MAX_VALUE; // what I want to pay at most
	private double minWinPercent = 0.10;
	private String cities = "Fort Sterling";
	private double tax = 0.03; // 0.03 for premium, 0.06 for f2p
	private int avgCountTimespan = 5; // in hours?
	private int minCount = 10;

	private PricesApi pricesApi = new PricesApi();

	public static void main(String[] args) {
		new Main().doStuff();
	}

	public Main() {
		pricesApi.getApiClient().setBasePath("https://www.albion-online-data.com");
	}

	public void doStuff() {

		ChartsApi chartsApi = new ChartsApi();
		chartsApi.getApiClient().setBasePath("https://www.albion-online-data.com");

		List<Item> allItems = ItemNameParser.parseInputFile("itemnames.txt");
		List<MarketResponse> matchingItems = new ArrayList<>();

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

		for (Item i : allItems) {
			if ((builder.length() + i.getUniqueName().length()) >= maxLength) {
				builder.deleteCharAt(0);
				sendPriceRequest(builder.toString(), matchingItems);
				builder = new StringBuilder(maxLength);
			}
			builder.append(',');
			builder.append(i.getUniqueName());

		}
		builder.deleteCharAt(0);
		sendPriceRequest(builder.toString(), matchingItems);
		
		// TODO: Now check avgCount for remaining ones?
		// and sort by profit%
		

	}

	private void sendPriceRequest(String items, List<MarketResponse> matchingItems) {
		try {
			List<MarketResponse> prices = pricesApi.apiV2StatsPricesItemListFormatGet(items, "json", cities, "");
			for (MarketResponse mr : prices) {
				if (mr.getSellPriceMax() > this.minPrice && mr.getBuyPriceMin() < this.maxPrice
						&& (mr.getSellPriceMin() * (1 - tax - setupFee)
								/ (mr.getBuyPriceMax() * (1 + setupFee)) - 1) > minWinPercent) {
					matchingItems.add(mr);
				}
			}
		} catch (ApiException e) {
			System.out.println("Error! " + e);
		}
	}
}
