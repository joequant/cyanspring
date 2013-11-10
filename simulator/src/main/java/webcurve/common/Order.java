package webcurve.common;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dennis_d_chen@yahoo.com
 */
public class Order extends BaseOrder implements Serializable, Cloneable {
	private static final long serialVersionUID = 7337247332181120380L;
	public Order(String code, TYPE type, SIDE side, int quantity, double price, String broker) {
		super(code, side, quantity, price, broker);
		this.status = STATUS.NONE;
		this.type = type;
		originalQuantity = quantity;
		createTime = new Date();
		amendTime = new Date();
	}
	
	public Object clone (){
		Order order;
		try {
			order = (Order)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		return order;
	}

	/**
	 * 
	 */
	public static enum TYPE { LIMIT, MARKET, FAK }
	public static enum STATUS { NONE, NEW, FILLING, AMENDED, CANCELLED, REJECTED, DONE  }
    
	protected TYPE type; 
	public TYPE getType() {
		return type;
	}
	protected void setType(TYPE type) {
		this.type = type;
	}
	
	protected STATUS status;
	public STATUS getStatus() {
		return status;
	}
	public void setStatus(STATUS status) {
		this.status = status;
	}

	protected long prevOrderID;
	public long getPrevOrderID() {
		return prevOrderID;
	}
	
	public void setPrevOrderID(long prevOrderID)
	{
		this.prevOrderID = prevOrderID;
	}

	protected long parentOrderID;
	public void setParentOrderID(long prevOrderID) {
		this.prevOrderID = prevOrderID;
	}

	public long getParentOrderID() {
		return parentOrderID;
	}
	
	public String getOrigClOrderId() {
		return origClOrderId;
	}

	public String getClOrderId() {
		return clOrderId;
	}

	public void setOrigClOrderId(String origClOrderId) {
		this.origClOrderId = origClOrderId;
	}

	public void setClOrderId(String clOrderId) {
		this.clOrderId = clOrderId;
	}

	protected String origClOrderId;
	protected String clOrderId;
		
	
	
	protected int originalQuantity;
	protected Date createTime;
	protected Date amendTime;
	protected long tranSeqNo;
	
	//get/set
    public int getOriginalQuantity() {
		return originalQuantity;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public Date getAmendTime() {
		return amendTime;
	}
	public long getTranSeqNo() {
		return tranSeqNo;
	}
	public void amendQuantity(int newQty) {
		int delta = newQty - this.getQuantity();
		this.originalQuantity += delta;
		this.setQuantity(newQty);
	}
//	protected void setCreateTime(Date createTime) {
//		this.createTime = createTime;
//	}
	public void setAmendTime(Date amendTime) {
		this.amendTime = amendTime;
	}
	public void setTranSeqNo(long tranSeqNo) {
		this.tranSeqNo = tranSeqNo;
	}
	
	public int getCumQty() {
		return cumQty;
	}

	public double getAvgPrice() {
		return avgPx;
	}

	protected int cumQty;
	protected double avgPx;
	
	public int getLastQty() {
		return lastQty;
	}

	public double getLastPx() {
		return lastPx;
	}

	protected int lastQty;
	protected double lastPx;
	public void calOrder(int quantity, double price)
	{
    	avgPx = avgPx * cumQty /(cumQty + quantity)
			+ price * quantity / (cumQty + quantity);
    	cumQty += quantity;
    	lastQty = quantity;
    	lastPx = price;
	}
	


}
