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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.Clock;
import com.cyanspring.common.business.MultiInstrumentStrategyDisplayConfig;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.alert.ClearMultiAlertEvent;
import com.cyanspring.common.event.strategy.CancelMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.PauseStrategyEvent;
import com.cyanspring.common.event.strategy.StartStrategyEvent;
import com.cyanspring.common.event.strategy.StopStrategyEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.GuiMultiInstrumentStrategyUpdateEvent;
import com.cyanspring.cstw.event.InstrumentSelectionEvent;
import com.cyanspring.cstw.event.MultiInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;
import com.cyanspring.cstw.gui.command.LoadStrategyCommand;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;

public class MultiInstrumentStrategyView extends ViewPart  implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(MultiInstrumentStrategyView.class);
	public static final String ID = "com.cyanspring.cstw.gui.MultiInstrumentStrategyView";
	
	private DynamicTableViewer strategyViewer;
	private DynamicTableViewer instrumentViewer;
	private AsyncTimerEvent timerEvent;
	private boolean setColumns;
	private Date lastRefreshTime = Clock.getInstance().now();
	private final long maxRefreshInterval = 300;
	private String currentStrategyId;
	private String currentStrategyType;
	private Menu menu;
	private Menu instrMenu;
	private ImageRegistry imageRegistry;
	private Action enterStrategyAction;
	private Action cancelStrategyAction;
	private Action pauseStrategyAction;
	private Action stopStrategyAction;
	private Action startStrategyAction;
	private Action saveStrategyAction;
	private enum StrategyAction { Pause, Stop, Start, Terminate, ClearAlert };


	@Override
	public void createPartControl(Composite parent) {
		log.info("Creating multi order strategy view");
		// create ImageRegistery
		imageRegistry = Activator.getDefault().getImageRegistry();
		
		// create pause order action
		createPauseStrategyAction(parent);
		
		// create stop order action
		createStopStrategyAction(parent);

		// create start order action
		createStartStrategyAction(parent);

		// create enter order actions
		createEnterStrategyAction(parent);
		
		// create enter order actions
		createCancelStrategyAction(parent);

		// create enter order actions
		createSaveStrategyAction(parent);

		// create views
		final Composite mainComposite = new Composite(parent,SWT.NONE);
    	//create left composite
		Composite leftComposite = new Composite(mainComposite,SWT.BORDER);
		leftComposite.setLayout(new FillLayout());
		// create table
	    String strFile = Business.getInstance().getConfigPath() + "MultiInstrumentStrategyTable.xml";
		strategyViewer = new DynamicTableViewer(leftComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
		strategyViewer.init();

		final Sash sash = new Sash (mainComposite, SWT.VERTICAL);
         //create right composite
		Composite rightComposite = new Composite(mainComposite,SWT.BORDER);
		rightComposite.setLayout(new FillLayout());
	    strFile = Business.getInstance().getConfigPath() + "MultiInstrumentTable.xml";
		instrumentViewer = new DynamicTableViewer(rightComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
		instrumentViewer.init();
		
		final FormLayout form = new FormLayout ();
		mainComposite.setLayout (form);
		
		FormData leftData = new FormData ();
		leftData.left = new FormAttachment (0, 0);
		leftData.right = new FormAttachment (sash, 0);
		leftData.top = new FormAttachment (0, 0);
		leftData.bottom = new FormAttachment (100, 0);
		leftComposite.setLayoutData (leftData);

		final int limit = 20;
		final FormData sashData = new FormData ();
		sashData.left = new FormAttachment (sash, 250);
		sashData.top = new FormAttachment (0, 0);
		sashData.bottom = new FormAttachment (100, 0);
		sash.setLayoutData (sashData);
		
		sash.addListener (SWT.Selection, new Listener() {
			public void handleEvent (Event e) {
				Rectangle sashRect = sash.getBounds ();
				Rectangle shellRect = mainComposite.getClientArea();
				int right = shellRect.width - sashRect.width - limit;
				e.x = Math.max (Math.min (e.x, right), limit);
				if (e.x != sashRect.x)  {
					sashData.left = new FormAttachment (0, e.x);
					mainComposite.layout ();
				}
			}
		});
		
		FormData rightData = new FormData ();
		rightData.left = new FormAttachment (sash, 0);
		rightData.right = new FormAttachment (100, 0);
		rightData.top = new FormAttachment (0, 0);
		rightData.bottom = new FormAttachment (100, 0);
		rightComposite.setLayoutData (rightData);


		final Table table = strategyViewer.getTable();
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = table.getItem(table.getSelectionIndex());
				Object obj = item.getData();
				if (obj instanceof Map) {
					showSelectedInstrument();
					
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (HashMap<String, Object>)obj;
					currentStrategyType = (String)map.get(OrderField.STRATEGY.value());
					currentStrategyId = (String)map.get(OrderField.ID.value());
					MultiInstrumentStrategyDisplayConfig config 
						= Business.getInstance().getMultiInstrumentFieldDefs().get(currentStrategyType);
					List<String> editableFields = config == null? null : config.getStrategyAmendable();
					if(null == editableFields)
						editableFields = new ArrayList<String>();
					Business.getInstance().getEventManager().
						sendEvent(new MultiInstrumentStrategySelectionEvent(map, editableFields));
				}
			}
		});

		final Table instrumentTable = instrumentViewer.getTable();
		instrumentTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = instrumentTable.getItem(instrumentTable.getSelectionIndex());
				Object obj = item.getData();
				if (obj instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (HashMap<String, Object>)obj;
					MultiInstrumentStrategyDisplayConfig config 
						= Business.getInstance().getMultiInstrumentFieldDefs().get(currentStrategyType);
					List<String> editableFields = config == null? null : config.getInstrumentAmendable();
					Business.getInstance().getEventManager().
						sendEvent(new InstrumentSelectionEvent(map, editableFields));
				}
			}
		});
		
		createBodyMenu();
		
		// business logic goes here
		Business.getInstance().getEventManager().subscribe(OrderCacheReadyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(GuiMultiInstrumentStrategyUpdateEvent.class, this);
		showOrders();
	}
	
	private void createBodyMenu() {
		final Table table = strategyViewer.getTable();
		menu = new Menu(table.getShell(), SWT.POP_UP);

		MenuItem item;
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Start");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Start);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Pause");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Pause);
			}
		});

		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Stop");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Stop);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Terminate");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Terminate);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Clear alert");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.ClearAlert);
			}
		});
		
		strategyViewer.setBodyMenu(menu);

		final Table instrTable = instrumentViewer.getTable();
		instrMenu = new Menu(instrTable.getShell(), SWT.POP_UP);
		
		item = new MenuItem(instrMenu, SWT.PUSH);
		item.setText("Clear alert");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				clearInstrumentAlert();
			}
		});
		
		instrumentViewer.setBodyMenu(instrMenu);

	}
	
	private void clearInstrumentAlert() {
		final Table table = instrumentViewer.getTable();
		TableItem items[] = table.getSelection();
		try {
			for(TableItem item: items) {
				Object obj = item.getData();
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>)obj;
					String strategyId = (String)map.get(OrderField.STRATEGY_ID.value());
					String id = (String)map.get(OrderField.ID.value());
					String server = (String)map.get(OrderField.SERVER_ID.value());
					Business.getInstance().getEventManager().
					sendRemoteEvent(new ClearMultiAlertEvent(strategyId, id, server));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		
	}
	
	private void createEnterStrategyAction(final Composite parent) {
		enterStrategyAction = new Action() {
			public void run() {
				LoadStrategyCommand.loadStrategy();
			}
		};
		enterStrategyAction.setText("Create strategy");
		enterStrategyAction.setToolTipText("Create a multi order strategy");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PLUS_ICON.toString());
		enterStrategyAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(enterStrategyAction);
	}
	
	private void createCancelStrategyAction(Composite parent) {
		// create local toolbars
		cancelStrategyAction = new Action() {
			public void run() {
				cancelStrategies();
			}
		};
		cancelStrategyAction.setText("Cancel Order");
		cancelStrategyAction.setToolTipText("Terminate strategy");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.FALSE_ICON.toString());
		cancelStrategyAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(cancelStrategyAction);
	}

	protected void cancelStrategies() {
		Table table = strategyViewer.getTable();

		TableItem items[] = table.getSelection();
		try {
			for(TableItem item: items) {
				Object obj = item.getData();
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>)obj;
					String id = (String)map.get(OrderField.ID.value());
					String server = (String)map.get(OrderField.SERVER_ID.value());
						Business.getInstance().getEventManager().sendRemoteEvent(
								new CancelMultiInstrumentStrategyEvent(id, server, IdGenerator.getInstance().getNextID()));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}

	private void createSaveStrategyAction(Composite parent) {
		// create local toolbars
		saveStrategyAction = new Action() {
			public void run() {
				saveStrategies();
			}
		};
		saveStrategyAction.setText("Save strategy");
		saveStrategyAction.setToolTipText("Save strategy");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.SAVE_ICON.toString());
		saveStrategyAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(saveStrategyAction);
	}

	private void saveStrategies() {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		Table table = strategyViewer.getTable();

		TableItem items[] = table.getSelection();
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<List<Map<String, Object>>> instruments = new ArrayList<List<Map<String, Object>>>();
			for(TableItem item: items) {
				Object obj = item.getData();
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>)obj;
					list.add(map);
					String strategyId = (String)map.get(OrderField.ID.value());
					List<Map<String, Object>> instr = Business.getInstance().getOrderManager().getMultiInstruments(strategyId);
					instruments.add(instr);
				}
			}
			if(list.size() == 0) {
				MessageDialog.openError(shell, "No strategy is selected", 
				"Please select the strategies you want to save");
				return;
			}
			
			FileDialog dialog = new FileDialog(shell, SWT.SAVE);
			dialog.setFilterExtensions(new String[] {"*.xml"});

			String selectedFileName = dialog.open();
			if (selectedFileName == null){
				return;
			}
			
			File selectedFile = new File(selectedFileName); 
			selectedFile.createNewFile();
			FileOutputStream os = new FileOutputStream(selectedFile);

			if(list.size() == 1) {
				NewMultiInstrumentStrategyEvent event = new NewMultiInstrumentStrategyEvent(null, null, list.get(0), instruments.get(0));
				Business.getInstance().getXstream().toXML(event, os);
			} else {  // more than one
				List<NewMultiInstrumentStrategyEvent> events = new ArrayList<NewMultiInstrumentStrategyEvent>();
				for(int i=0; i<list.size(); i++) {
					events.add(new NewMultiInstrumentStrategyEvent(null, null, list.get(i), instruments.get(i)));
				}
				Business.getInstance().getXstream().toXML(events, os);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	private void strategyAction(StrategyAction action) {
		Table table = strategyViewer.getTable();

		TableItem items[] = table.getSelection();
		for(TableItem item: items) {
			Object obj = item.getData();
			if (obj instanceof HashMap) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>)obj;
				String id = (String)map.get(OrderField.ID.value());
				String server = (String)map.get(OrderField.SERVER_ID.value());
				try {
					if(StrategyAction.Pause.equals(action)) {
						Business.getInstance().getEventManager().
							sendRemoteEvent(new PauseStrategyEvent(id, server));
					} else if(StrategyAction.Stop.equals(action)) {
						Business.getInstance().getEventManager().
							sendRemoteEvent(new StopStrategyEvent(id, server));
					} else if(StrategyAction.Start.equals(action)) {
						Business.getInstance().getEventManager().
							sendRemoteEvent(new StartStrategyEvent(id, server));
					} else if(StrategyAction.ClearAlert.equals(action)) {
						Business.getInstance().getEventManager().
							sendRemoteEvent(new ClearMultiAlertEvent(id, null, server));
					} else if(StrategyAction.Terminate.equals(action)) {
						Business.getInstance().getEventManager().
							sendRemoteEvent(new CancelMultiInstrumentStrategyEvent(id, server, IdGenerator.getInstance().getNextID()));
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}
	}
	
	private void createPauseStrategyAction(final Composite parent) {
		// create local toolbars
		pauseStrategyAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Pause);
			}
		};
		pauseStrategyAction.setText("Pause strategy");
		pauseStrategyAction.setToolTipText("Pause strategy");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PAUSE_ICON.toString());
		pauseStrategyAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(pauseStrategyAction);
	}

	private void createStopStrategyAction(final Composite parent) {
		// create local toolbars
		stopStrategyAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Stop);
			}
		};
		stopStrategyAction.setText("Stop strategy");
		stopStrategyAction.setToolTipText("Stop strategy");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.STOP_ICON.toString());
		stopStrategyAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(stopStrategyAction);
	}
	
	private void createStartStrategyAction(final Composite parent) {
		// create local toolbars
		startStrategyAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Start);
			}
		};
		startStrategyAction.setText("Start strategy");
		startStrategyAction.setToolTipText("Start strategy");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.START_ICON.toString());
		startStrategyAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(startStrategyAction);
	}
	
	

	@Override
	public void setFocus() {
		strategyViewer.getControl().setFocus();
	}
	
	private void showSelectedInstrument() {
		final Table table = strategyViewer.getTable();
		if(table.getSelectionIndex() < 0)
			return;
		TableItem item = table.getItem(table.getSelectionIndex());
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>)item.getData();
		String id = (String)map.get(OrderField.ID.value());
		showInstruments(id);
	}
	
	private void showInstruments(String strategyId) {
		Map<String, Object> map = (Map<String, Object>)Business.getInstance().getOrderManager().getMultiInstrumentStrategy(strategyId);
		String strategy = (String)map.get(OrderField.STRATEGY.value());
		MultiInstrumentStrategyDisplayConfig config = Business.getInstance().getMultiInstrumentFieldDefs().get(strategy);
		List<Map<String, Object>> instruments = Business.getInstance().getOrderManager().getMultiInstruments(strategyId);
		if(null == instruments || instruments.size() == 0)
			return;
		
		if(!strategy.equals(currentStrategyType)) {
			ArrayList<ColumnProperty> colProperties = new ArrayList<ColumnProperty>();
			List<String> instrumentFields = null;
			if(null != config)
				instrumentFields = config.getInstrumentDisplayFields();
				
			if(null == instrumentFields || instrumentFields.size() == 0) {
				log.debug("display all instrument fields for: " + strategy);
				instrumentFields = new ArrayList<String>();
				for(String field: instruments.get(0).keySet())
					instrumentFields.add(field);
			}
			
			for(String field: instrumentFields) {
				colProperties.add(new ColumnProperty(field, 100));
			}
			instrumentViewer.setSmartColumnProperties(strategy, colProperties);
			currentStrategyType = strategy;
		}
		instrumentViewer.setInput(instruments);
	}
	
	private void showOrders() {
		lastRefreshTime = Clock.getInstance().now();
		if(!setColumns) {
			List<Map<String, Object>> strategies = Business.getInstance().getOrderManager().getMultiInstrumentStrategies();
			
			if (strategies.size() == 0)
				return;
			
			ArrayList<ColumnProperty> columnProperties = new ArrayList<ColumnProperty>();
			List<String> displayFields = Business.getInstance().getMultiInstrumentDisplayFields();
			
			for(String field: displayFields) {
					columnProperties.add(new ColumnProperty(field, 100));
			}
			
			strategyViewer.setSmartColumnProperties("Multi Order Strategy", columnProperties);
			strategyViewer.setInput(strategies);
			setColumns = true;
			
			final Table table = strategyViewer.getTable();
			if(table.getSelectionIndex() < 0)
				table.setSelection(0);

		}
		strategyViewer.refresh();
		if(currentStrategyId != null) {
			showInstruments(currentStrategyId);
			instrumentViewer.refresh();
		}
	}
	
	private void asyncShowOrders() {
		strategyViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				showOrders();
			}
		});
	}

	private void smartShowOrders() {
		if(TimeUtil.getTimePass(lastRefreshTime) > maxRefreshInterval) {
			asyncShowOrders();
		} else if(timerEvent == null) {
			timerEvent = new AsyncTimerEvent();
			Business.getInstance().getScheduleManager().scheduleTimerEvent(maxRefreshInterval, this, timerEvent);
		}
	}

	@Override
	public void onEvent(AsyncEvent event) {
		log.debug("Recieved event: " + event);
		if (event instanceof OrderCacheReadyEvent) {
			smartShowOrders();
		} else if (event instanceof GuiMultiInstrumentStrategyUpdateEvent) {
			smartShowOrders();
		} else if (event instanceof AsyncTimerEvent) {
			Business.getInstance().getScheduleManager().cancelTimerEvent(timerEvent);
			timerEvent = null;
			asyncShowOrders();
		} else {
			log.warn("Unhandled event: " + event);
		}
	}

}
