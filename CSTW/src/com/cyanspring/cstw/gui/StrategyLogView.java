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

import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.strategy.StrategyLogEvent;
import com.cyanspring.common.type.LogType;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.MultiInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.SingleOrderStrategySelectionEvent;
import com.cyanspring.cstw.event.ServerStatusEvent;
import com.cyanspring.cstw.gui.common.StyledAction;

public class StrategyLogView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(StrategyLogView.class);
	public static final String ID = "com.cyanspring.cstw.gui.StrategyLogView"; //$NON-NLS-1$
	private String id;
	private Color red;
	private StyledText console;
	private boolean pinned;
	private Action pinAction;
	private ImageRegistry imageRegistry;
	private boolean refreshing;
	
	public StrategyLogView() {
	}
	
	class LogLabelProvider extends LabelProvider implements IColorProvider {

		@Override
        public String getText(Object element) {
	          return element==null?"":((StrategyLogEvent)element).getMessage();
	    }
		
		@Override
		public void dispose() {
			
		}

		@Override
		public Color getForeground(Object element) {
			return red;
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}

	}
	


	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		imageRegistry = Activator.getDefault().getImageRegistry();
		
		console = new StyledText(parent, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		
		red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		//black = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);

		FontRegistry fontRegistry = new FontRegistry(parent.getDisplay());
	    fontRegistry.put("Courier New", new FontData[]{new FontData("Courier New", 8, SWT.NORMAL)});
	 	Font font = fontRegistry.get("Courier New");
		console.setFont(font);

		createPauseOrderAction(parent);

		createActions();
		initializeToolBar();
		initializeMenu();
		
		// subscribe to business event
		Business.getInstance().getEventManager().subscribe(SingleOrderStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(MultiInstrumentStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(ServerStatusEvent.class, this);
	}

	
	private void createPauseOrderAction(final Composite parent) {
		// create local toolbars
		pinAction = new StyledAction("", org.eclipse.jface.action.IAction.AS_CHECK_BOX) {
			public void run() {
				pinned = pinned?false:true;
			}
		};
		
		pinAction.setText("Pin console");
		pinAction.setToolTipText("Pin console");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PIN_ICON.toString());
		pinAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(pinAction);
	}

	public void dispose() {
		super.dispose();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		@SuppressWarnings("unused")
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		@SuppressWarnings("unused")
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	private void appendEvent(StrategyLogEvent event) {
		int before = console.getLineCount();
		console.append(event.getMessage() + "\n");
		int after = console.getLineCount();
		if(event.getLogType().equals(LogType.Warn) || event.getLogType().equals(LogType.Error)) {
			console.setLineBackground(before-1, after-before, red);
		}
	}
	
	private void processParentOrderSelectionEvent(SingleOrderStrategySelectionEvent event) {
		Map<String, Object> parentOrder = event.getData();
		final String orderId = (String)parentOrder.get(OrderField.ID.value());
		smartShowLog(orderId);
	}
	
	private void processMultiInstrumentStrategySelectionEvent(
			MultiInstrumentStrategySelectionEvent e) {
		final String id = (String)e.getData().get(OrderField.ID.value());
		smartShowLog(id);
	}

	private void smartShowLog(final String strategyId) {
		if(id != null && id.equals(strategyId))
			return;
		
		if(refreshing)
			return;
		
		refreshing = true;
		console.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				Business.getInstance().getEventManager().unsubscribe(StrategyLogEvent.class, id, StrategyLogView.this);
				console.setText("");
				id = strategyId;
				List<StrategyLogEvent> list = Business.getInstance().getOrderManager().getLogEvents(id);
				console.setRedraw(false);
				for(StrategyLogEvent logEvent: list) {
					appendEvent(logEvent);
				}
				if(!pinned)
					console.setTopIndex(console.getLineCount() - 1); 
				console.setRedraw(true);
				Business.getInstance().getEventManager().subscribe(StrategyLogEvent.class, id, StrategyLogView.this);
				refreshing = false;
			}
		});
	}
	
	private void processStrategyLogEvent(final StrategyLogEvent event) {
		if(id == null || !id.equals(event.getKey()))
			return;

		if(refreshing)
			return;

		console.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				appendEvent(event);
				if(!pinned)
					console.setTopIndex(console.getLineCount() - 1); 
			}
		});
	}
	
	private void processServerStatusEvent(ServerStatusEvent event) {
		log.debug("ServerStatusEvent: " + event.getServer() + " " + (event.isUp()?"up":"down"));
	}

	@Override
	public void onEvent(AsyncEvent e) {
		
		if (e instanceof SingleOrderStrategySelectionEvent) {
			processParentOrderSelectionEvent((SingleOrderStrategySelectionEvent)e);
		} else if (e instanceof MultiInstrumentStrategySelectionEvent) {
			processMultiInstrumentStrategySelectionEvent((MultiInstrumentStrategySelectionEvent)e);
		} else if (e instanceof StrategyLogEvent) {
			processStrategyLogEvent((StrategyLogEvent)e);
		} else if (e instanceof ServerStatusEvent) {
			processServerStatusEvent((ServerStatusEvent)e);
		}
	}




}
