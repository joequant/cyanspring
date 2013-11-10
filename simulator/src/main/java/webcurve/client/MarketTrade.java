package webcurve.client;

public class MarketTrade {
	String symbol;
	double price;
	long vol;
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public long getVol() {
		return vol;
	}
	public void setVol(long vol) {
		this.vol = vol;
	}
}
