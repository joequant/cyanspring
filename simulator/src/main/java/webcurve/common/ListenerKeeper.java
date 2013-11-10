package webcurve.common;

import java.util.Vector;

/**
 * @author dennis_d_chen@yahoo.com
 */
public class ListenerKeeper<T>
{
	private Vector<ExchangeListener<T>> exchangeListeners = new Vector<ExchangeListener<T>>();
	
	public void addExchangeListener(ExchangeListener<T> listener)
	{
		if (null == listener)
			return;
		if (!exchangeListeners.contains(listener))
			exchangeListeners.add(listener);
	}

	public void removeExchangeListener(ExchangeListener<T> listener)
	{
		exchangeListeners.remove(listener);
	}
	
	public void updateExchangeListeners(T t)
	{
		for (ExchangeListener<T> item: exchangeListeners)
		{
			try
			{
				item.onChangeEvent(t);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}
}