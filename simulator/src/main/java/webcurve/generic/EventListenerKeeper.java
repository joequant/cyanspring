package webcurve.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventListenerKeeper<T>
{
	private List<EventListener<T>> EventListeners = Collections.synchronizedList(new ArrayList<EventListener<T>>());
	
	public void addEventListener(EventListener<T> listener)
	{
		if (null == listener)
			return;
		if (!EventListeners.contains(listener))
			EventListeners.add(listener);
	}

	public void removeEventListener(EventListener<T> listener)
	{
		EventListeners.remove(listener);
	}
	
	public void updateEventListeners(T t)
	{
		for (EventListener<T> item: EventListeners)
		{
			try
			{
				item.onChangeEvent(t);
			}
			catch(Exception e)
			{
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
}
