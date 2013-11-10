package webcurve.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MultiSubscriptionManager<K, T> {

	Map<String, ArrayList<ExchangeListener<T>>> subscriptions = Collections.synchronizedMap(new HashMap<String, ArrayList<ExchangeListener<T>>>());
	
	String getStringKey(K key, Class<T> type)
	{
		return type.toString() + "-" + key.toString();
	}
	
	public boolean subscribe(K key, Class<T> type, ExchangeListener<T> listener)
	{
		ArrayList<ExchangeListener<T>> listeners = subscriptions.get(getStringKey(key, type));
		if ( null == listeners)
		{
			listeners = new ArrayList<ExchangeListener<T>>();
			subscriptions.put(getStringKey(key, type), listeners);
		}
		else if (listeners.contains(listener))
				return false;
	
		listeners.add(listener);
		return true;
		
	}

	public boolean unsubscribe(K key, Class<T> type, ExchangeListener<T> listener)
	{
		ArrayList<ExchangeListener<T>> listeners = subscriptions.get(getStringKey(key, type));
		if ( null == listeners)
			return false;
		
		return listeners.remove(listener);
	}
	
	
	public void update(K key, Class<T> type, T t)
	{
		ArrayList<ExchangeListener<T>> listeners = subscriptions.get(getStringKey(key, type));
		if ( null == listeners)
			return;
		
		for (ExchangeListener<T> listener: listeners)
		{
			listener.onChangeEvent(t);
		}
		
	}
}
