package webcurve.marketdata;

public interface MarketDataListener {
	void onQuote(String Symbol, Quote quote);
	void onTrade(String Symbol, Trade trade);
}
