package webcurve.fix;

import java.util.Hashtable;

import webcurve.client.ClientOrder;
/**
 * @author dennis_d_chen@yahoo.com
 */
public class OrderFixManager {
	protected Hashtable<String, ClientOrder> clientOrders = new Hashtable<String, ClientOrder>();
	protected Hashtable<String, ClientOrder> childOrders = new Hashtable<String, ClientOrder>();

	
}
