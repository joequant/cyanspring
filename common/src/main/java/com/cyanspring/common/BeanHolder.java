/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.data.AlertType;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.transport.IObjectTransportService;

public class BeanHolder {
	private static BeanHolder instance;
	@Autowired
	GenericDataConverter dataConverter;
	
	@Autowired
	IObjectTransportService transportService;

	@Autowired
	IRemoteEventManager eventManager;
	
	@Autowired
	@Qualifier("alertColorConfig")
	private HashMap<AlertType, Integer> alertColorConfig;

	public void setDataConverter(GenericDataConverter dataConverter) {
		this.dataConverter = dataConverter;
	}
	public static void setInstance(BeanHolder instance) {
		BeanHolder.instance = instance;
	}
	public static BeanHolder getInstance() {
		return instance;
	}
	public GenericDataConverter getDataConverter() {
		return dataConverter;
	}
	
	public IObjectTransportService getTransportService() {
		return transportService;
	}
	public void setTransportService(IObjectTransportService transportService) {
		this.transportService = transportService;
	}
	public IRemoteEventManager getEventManager() {
		return eventManager;
	}
	public void setEventManager(IRemoteEventManager eventManager) {
		this.eventManager = eventManager;
	}
	public HashMap<AlertType, Integer> getAlertColorConfig() {
		return alertColorConfig;
	}
	public void setAlertColorConfig(HashMap<AlertType, Integer> alertColorConfig) {
		this.alertColorConfig = alertColorConfig;
	}

}
