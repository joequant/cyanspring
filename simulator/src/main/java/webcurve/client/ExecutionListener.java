package webcurve.client;

/**
 * @author dennis_d_chen@yahoo.com
 */
public interface ExecutionListener {
	public void OnOrder(ClientOrder order, String info);
	public void OnExecution(Execution exec, String info);
}
