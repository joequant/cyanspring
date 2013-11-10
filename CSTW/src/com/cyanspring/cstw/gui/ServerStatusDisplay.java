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
package com.cyanspring.cstw.gui;

import java.util.HashMap;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.event.ServerStatusEvent;
import com.cyanspring.cstw.gui.common.CsStatusLineContributionItem;

public class ServerStatusDisplay implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(ServerStatusDisplay.class);
	static private ServerStatusDisplay instance; // Singleton
	// singleton implementation
	private ServerStatusDisplay() {
	}
	
	static public ServerStatusDisplay getInstance() {
		if (null == instance) {
			instance = new ServerStatusDisplay();
		}
		return instance;
	}
	
	private IStatusLineManager statusLine;
	private HashMap<String, CsStatusLineContributionItem> servers = 
					new HashMap<String, CsStatusLineContributionItem>();
	
	public void setStatusLineManager(IStatusLineManager statusLine) {
		this.statusLine = statusLine;
	}

	public void init() {
		Business.getInstance().getEventManager().subscribe(ServerStatusEvent.class, this);
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
        if(event instanceof ServerStatusEvent) {
        	processServerStatusEvent((ServerStatusEvent)event);
        } else {
        	log.error("Unhandled event: " + event);
        }
        
	}
	
	private void processServerStatusEvent(ServerStatusEvent event) {
		CsStatusLineContributionItem item = servers.get(event.getServer());
		if(null == item) {
			item = new CsStatusLineContributionItem(event.getServer());
			item.setText(event.getServer());
			statusLine.add(item);
			servers.put(event.getServer(), item);
		}
		if(event.isUp()) {
			item.setBackground(SWT.COLOR_GREEN);
		} else {
			item.setBackground(SWT.COLOR_RED);
		}
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				statusLine.update(true);
			}
		});


	}

}
