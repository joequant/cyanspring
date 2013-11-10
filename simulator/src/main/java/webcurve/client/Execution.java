package webcurve.client;

/**
 * @author dennis_d_chen@yahoo.com
 */
public class Execution {
	protected String clientOrderID;
	protected String execID;
	public String getExecID() {
		return execID;
	}
	public void setExecID(String execID) {
		this.execID = execID;
	}
	protected String code;
	protected Integer quantity = 0;
	protected Double price = 0.0;
	
	//getters and setters
	public String getClientOrderID() {
		return clientOrderID;
	}
	public void setClientOrderID(String clientOrderID) {
		this.clientOrderID = clientOrderID;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}

}
