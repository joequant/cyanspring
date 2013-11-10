/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webcurve.common;
import java.io.Serializable;
import java.util.Date;


/**
 *
 * @author dennis
 */
public class Trade implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8147893468341381188L;

	public long getTradeID() {
		return tradeID;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getPrice() {
		return price;
	}

	public Date getTranTime() {
		return tranTime;
	}

	public Order getBidOrder() {
		return bidOrder;
	}

	public Order getAskOrder() {
		return askOrder;
	}

	public void setTradeID(long tradeID) {
		this.tradeID = tradeID;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setTranTime(Date tranTime) {
		this.tranTime = tranTime;
	}

	public void setBidOrder(Order bidOrder) {
		this.bidOrder = bidOrder;
	}

	public void setAskOrder(Order askOrder) {
		this.askOrder = askOrder;
	}

	protected long tradeID;
	protected int quantity;
	protected double price;
	protected Date tranTime;
	protected Order bidOrder;
	protected Order askOrder;
	protected long tranSeqNo;	
	
	public long getTranSeqNo() {
		return tranSeqNo;
	}

	public void setTranSeqNo(long tranSeqNo) {
		this.tranSeqNo = tranSeqNo;
	}


    public Trade ( long tradeID, int quantity, double price, Order order1, Order order2)
    {
    	this.tradeID = tradeID;
        this.quantity = quantity;
        this.price = price;
        // to make this smart and flexible
        if ( order1.getSide() == Order.SIDE.BID )
        {
        	this.bidOrder = order1;
        	this.askOrder = order2;
        }
        else
        {
        	this.bidOrder = order2;
        	this.askOrder = order1;       	
        }
        tranTime = new Date();
    }
}
