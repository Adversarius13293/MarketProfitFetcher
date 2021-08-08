# MarketProfitFetcher

Small tool for the game [Albion Online](https://albiononline.com/). It fetches market data from the [Albion Online Data Project](https://www.albion-online-data.com/) to find items to buy and sell with potential profit.

Currently my program has no interface or even easy configuration options. All the parameters have to be changed inside the code. Maybe I will add something like that later.

Current features:
- Fetch all items from given markets, compute the profit after taxes for buying at highest buyprice, and selling at lowest sellprice. Filter for different parameters like items traded daily, price range, profit or quality.

Possible feature ideas:
- Compare prices of Caerleon market to Black Market prices
- Profit by salvaging artifacts and their equipment, to sell the materials
- Profit by salvaging normal equipment to sell the resources (would need database for ALL the crafting costs...)
- Buy equipment and materials (runes, relics, souls) to enchant and sell it
- Buy in one city, transport and sell in different city

Make sure to have the [Albion Data Client](https://github.com/BroderickHyman/albiondata-client/releases) active anytime browsing the ingame market, to keep all the data updated!
