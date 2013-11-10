package webcurve.common;

import java.io.Serializable;
/**
 * @author dennis_d_chen@yahoo.com
 */
public class BaseOrder implements Serializable, Cloneable {
	public static enum SIDE { BID, ASK }
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8463848083656731711L;
	
	// get/set
	public long getOrderID() {
		return orderID;
	}

	public String getCode() {
		return code;
	}

	public SIDE getSide() {
		return side;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getPrice() {
		return price;
	}

	public String getBroker() {
		return broker;
	}

	public void setOrderID(long orderID) {
		this.orderID = orderID;
	}

	protected void setCode(String code) {
		this.code = code;
	}

	protected void setSide(SIDE side) {
		this.side = side;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	private long orderID;
	private String code;
	private SIDE side;
	private int quantity;
	private double price;
	private String broker;

    public BaseOrder ( String code, SIDE side, int quantity, double price, String broker)
    {
        this.code = code;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.broker = broker;
    }
	public static String sideToString(BaseOrder.SIDE side)
	{
		if (side == BaseOrder.SIDE.BID)
			return "B";
		if (side == BaseOrder.SIDE.ASK)
			return "A";
		return "";
		
	}
}
