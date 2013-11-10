package webcurve.client;

public class FixMsg {
	public FixMsg(boolean inbound, String msg) {
		this.inbound = inbound;
		this.msg = msg;
	}
	boolean inbound;
	String msg;
	public boolean isInbound() {
		return inbound;
	}
	public void setInbound(boolean inbound) {
		this.inbound = inbound;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return (inbound?"<<<":">>>") + msg;
	}
}
