package com.cyanspring.common.event;

public interface IAsyncEventManager {
	public boolean subscribe(Class<? extends AsyncEvent> clazz, IAsyncEventListener listener);
	public boolean subscribe(Class<? extends AsyncEvent> clazz, String key, IAsyncEventListener listener);
	public void unsubscribe(Class<? extends AsyncEvent> clazz, IAsyncEventListener listener);
	public void unsubscribe(Class<? extends AsyncEvent> clazz, String key, IAsyncEventListener listener);
	
	public void clearAllSubscriptions();
	public boolean isSync();
	public void setSync(boolean sync);

	// send event to in-process
	public void sendEvent(AsyncEvent event);
}
