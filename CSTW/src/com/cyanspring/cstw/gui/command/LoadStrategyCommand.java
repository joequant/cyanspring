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
package com.cyanspring.cstw.gui.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.cstw.business.Business;

public class LoadStrategyCommand extends AbstractHandler {
	private static final Logger log = LoggerFactory
			.getLogger(LoadStrategyCommand.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		loadStrategy();
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void loadStrategy() {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		
		ArrayList<String> servers = Business.getInstance().getOrderManager().getServers();
		if(servers.size() == 0){
			MessageDialog.openError(shell, "Can't find server", 
			"Is CSTW connected to any server?");
			return;
		}

		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterExtensions(new String[] {"*.xml"});
		String selectedFileName = dialog.open();
		if (selectedFileName == null){
			return;
		}
		
		File selectedFile = new File(selectedFileName); 
		RemoteAsyncEvent event = null;
		List<RemoteAsyncEvent> events = null;
		try {
			Object obj = Business.getInstance().getXstream().fromXML(selectedFile);
			if(obj instanceof RemoteAsyncEvent) {
				event = (RemoteAsyncEvent)obj;
			} else if(obj instanceof List){
				events = (List<RemoteAsyncEvent>)obj;
				for(RemoteAsyncEvent e: events) {
					if(!(e instanceof RemoteAsyncEvent))
						throw new Exception("List contains object not a subclass of RemoteAsyncEvent");
				}
			} else
				throw new Exception("Object is not subclass of or a list of RemoteAsyncEvent");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			MessageDialog.openError(shell, "Error loading RemoteAsyncEvent", 
			e.getMessage());
			return;
		}
		
		if(null == events)
			events = new ArrayList<RemoteAsyncEvent>();
		if(null != event)
			events.add(event);
		
		for(RemoteAsyncEvent remoteEvent: events) {
			String server = remoteEvent.getReceiver();
			if(remoteEvent.getReceiver() != null) {
				boolean found = false;
				for(String str: servers) {
					if(str.equals(server)) {
						found = true;
						break;
					}
				}
				if(!found) {
					MessageDialog.openError(shell, "Can't find server: " + server, 
					"Please check which server(s) CSTW is connected to?");
					return;
				}
			} else { // pick first server
				server = servers.get(0);
				remoteEvent.setReceiver(server);
			}
			try {
				Business.getInstance().getEventManager().sendRemoteEvent(remoteEvent);
			} catch (Exception e) {
				MessageDialog.openError(shell, "Error in sending event", 
				e.getMessage());
				return;
			}
		}
	}
}
