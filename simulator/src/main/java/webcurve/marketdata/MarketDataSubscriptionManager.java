package webcurve.marketdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MarketDataSubscriptionManager {
    private static Logger log = LoggerFactory.getLogger(MarketDataSubscriptionManager.class);
	private Map<String, List<MarketDataListener>> subscriptions = 
		Collections.synchronizedMap(new HashMap<String, List<MarketDataListener>>());

	public boolean subscribe(String key, MarketDataListener listener)
	{
		if (null == listener)
		{
			log.error("addEventListener null: " + key);
			return false;
		}
		
		List<MarketDataListener> listeners = subscriptions.get(key);
		if (null == listeners)
		{
			listeners = Collections.synchronizedList(new ArrayList<MarketDataListener>());
			subscriptions.put(key, listeners);
		}
		
		if (!listeners.contains(listener))
			listeners.add(listener);
		else
			return false;
		
		return true;
	}

	public void removeEventListener(String key, MarketDataListener listener)
	{
	
		if (null == listener)
		{
			log.error("removeEventListener null: " + key);
			return;
		}
		
		List<MarketDataListener> eventListeners = subscriptions.get(key);
		if (null == eventListeners)
			return;
		
		eventListeners.remove(listener);
	}

	public void removeEventListener(String key)
	{
	
		List<MarketDataListener> eventListeners = subscriptions.get(key);
		if (null == eventListeners)
			return;
		
		eventListeners.clear();
	}
	
	public void updateListeners(String key, Quote quote)
	{
		List<MarketDataListener> listeners = subscriptions.get(key);
		if (null == listeners)
			return;
		for (MarketDataListener listener: listeners)
		{
			try
			{
				listener.onQuote(key, quote);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}	
	
	public void updateListeners(String key, Trade trade)
	{
		List<MarketDataListener> listeners = subscriptions.get(key);
		if (null == listeners)
			return;
		for (MarketDataListener listener: listeners)
		{
			try
			{
				listener.onTrade(key, trade);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}	
}
