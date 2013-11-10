package webcurve.common;


public interface ITickTable {

	public double getRoundedPrice(double price, boolean up);
	public double tickUp(double price, boolean roundUp);
	public double tickDown(double price, boolean roundUp);
	public double tickUp(double price, int ticks, boolean roundUp);
	public double tickDown(double price, int ticks, boolean roundUp);
	public boolean validPrice(double price);
}
