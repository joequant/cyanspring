package webcurve.marketdata;

public class Trade {

	String symbol;
	double price;
	long quantity;
	boolean buyDriven;
	
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
	public long getQuantity() {
		return quantity;
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	public boolean isBuyDriven() {
		return buyDriven;
	}
	public void setBuyDriven(boolean buyDriven) {
		this.buyDriven = buyDriven;
	}
	
}
