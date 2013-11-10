package webcurve.common;

public class QuantityPrice implements Cloneable{
	public long quantity;
	public double price;
	
	
	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public QuantityPrice(long quantity, double price)
	{
		this.quantity = quantity;
		this.price = price;
	}
	
	public String toString(){
		return "[" + quantity + ", " + price + "]";
	}
	
	public QuantityPrice clone()
	{
		try {
			return (QuantityPrice)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
