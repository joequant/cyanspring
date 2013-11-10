package com.cyanspring.common.data;

import java.util.LinkedList;

public class AlertSet {
	private static int size = 10; 
	private LinkedList<AlertItem> items = new LinkedList<AlertItem>();
	class AlertItem {
		AlertType alertType;
		String	alertMsg;
		public AlertItem(AlertType alertType, String alertMsg) {
			super();
			this.alertType = alertType;
			this.alertMsg = alertMsg;
		}
		public AlertType getAlertType() {
			return alertType;
		}
		public String getAlertMsg() {
			return alertMsg;
		}
		public void setAlertType(AlertType alertType) {
			this.alertType = alertType;
		}
		public void setAlertMsg(String alertMsg) {
			this.alertMsg = alertMsg;
		}
		
	}
	
	public void addAlert(AlertType alertType, String alertMsg) {
		boolean found = false;
		for(AlertItem item: items) {
			if(item.getAlertMsg().equals(alertMsg)) {
				found = true;
				item.setAlertType(alertType);
			}
		}
		
		if(!found) {
			if(items.size() >= size) {
				items.remove();
			}
			items.add(new AlertItem(alertType, alertMsg));
		}
	}
	
	public AlertType getAlertType(){
		AlertType result = null;
		for(AlertItem item: items) {
			if(null == result || item.getAlertType().compareTo(result) > 0) {
				result = item.getAlertType();
			} 
		}
		return result;
	}
	
	public String getAlertMsg() {
		String result = "";
		for(AlertItem item: items) {
			result += item.getAlertMsg() + "\n";
		}
		return result;
	}
}
