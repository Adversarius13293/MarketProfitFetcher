package adver.sarius.albion.mpf;

import java.util.List;

import io.swagger.client.ApiException;
import io.swagger.client.api.PricesApi;
import io.swagger.client.model.MarketResponse;

public class Main {

	public static void main(String[] args) {
		
		PricesApi priceApi = new PricesApi();
		priceApi.getApiClient().setBasePath("https://www.albion-online-data.com");
		try {
			List<MarketResponse> prices = priceApi.apiV2StatsPricesItemListFormatGet("T2_BAG", "json", "Fortsterling", "1");
			System.out.println(prices.size());
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
