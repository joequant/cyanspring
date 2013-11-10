package webcurve.common;

public class MarketMakerData {
	protected String stock;
	protected double basePrice;
	protected double priceVariant;
	protected double stdFactor;
	protected int tradingMinInterval;
	protected int tradingMaxInterval;
	protected int minQuantity;
	protected int maxQuantity;
	protected int lotSize;
	public MarketMakerData(String stock, double basePrice, double priceVariant,
			double stdFactor, int tradingMinInterval, int tradingMaxInterval,
			int minQuantity, int maxQuantity, int lotSize) {
		super();
		this.stock = stock;
		this.basePrice = basePrice;
		this.priceVariant = priceVariant;
		this.stdFactor = stdFactor;
		this.tradingMinInterval = tradingMinInterval;
		this.tradingMaxInterval = tradingMaxInterval;
		this.minQuantity = minQuantity;
		this.maxQuantity = maxQuantity;
		this.lotSize = lotSize;
	}
	public String getStock() {
		return stock;
	}
	public double getBasePrice() {
		return basePrice;
	}
	public double getPriceVariant() {
		return priceVariant;
	}
	public double getStdFactor() {
		return stdFactor;
	}
	public int getTradingMinInterval() {
		return tradingMinInterval;
	}
	public int getTradingMaxInterval() {
		return tradingMaxInterval;
	}
	public int getMinQuantity() {
		return minQuantity;
	}
	public int getMaxQuantity() {
		return maxQuantity;
	}
	public int getLotSize() {
		return lotSize;
	}

	
}
