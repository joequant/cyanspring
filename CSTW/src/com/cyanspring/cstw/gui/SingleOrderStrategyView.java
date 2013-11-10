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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.Clock;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.alert.ClearSingleAlertEvent;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.strategy.PauseStrategyEvent;
import com.cyanspring.common.event.strategy.StartStrategyEvent;
import com.cyanspring.common.event.strategy.StopStrategyEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.GuiSingleOrderStrategyUpdateEvent;
import com.cyanspring.cstw.event.OrderCacheReadyEvent;
import com.cyanspring.cstw.event.SingleOrderStrategySelectionEvent;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;
import com.cyanspring.cstw.gui.filter.ParentOrderFilter;

public class SingleOrderStrategyView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory.getLogger(SingleOrderStrategyView.class);
	public static final String ID = "com.cyanspring.cstw.gui.ParentOrderView";
	private DynamicTableViewer viewer;
	// filter controls
	private Composite filterComposite;
	private Label filterLabel;
	private Combo filterField;
	private Text filterText;
	private Button filterButton;
	private Action filterAction;
	private ParentOrderFilter viewFilter;
	private ImageRegistry imageRegistry;
	private Action enterOrderAction;
	private Action cancelOrderAction;
	private Action pauseOrderAction;
	private Action stopOrderAction;
	private Action startOrderAction;
	private Action saveOrderAction;
	private OrderDialog orderDialog;
	private AmendDialog amendDialog;
	private Menu menu;
	private boolean setColumns;
	private AsyncTimerEvent timerEvent;
	private Date lastRefreshTime = Clock.getInstance().now();
	private final long maxRefreshInterval = 300;
	
	private String maTag;
	private String maValue;

	
	private enum StrategyAction { Pause, Stop, Start, ClearAlert, MultiAmend };
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		log.info("Creating parent order view");
		// create ImageRegistery
		imageRegistry = Activator.getDefault().getImageRegistry();
		
		// create layout for parent
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		
		// create pause order action
		createPauseOrderAction(parent);
		
		// create stop order action
		createStopOrderAction(parent);

		// create start order action
		createStartOrderAction(parent);

		// create enter order actions
		createEnterOrderAction(parent);
		
		// create enter order actions
		createCancelOrderAction(parent);
		
		// create filter controls
		createFilterControls(parent);
		
		// create save order action
		createSaveOrderAction(parent);
		
		// create table
	    String strFile = Business.getInstance().getConfigPath() + "ParentOrderTable.xml";
		viewer = new DynamicTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, Business.getInstance().getXstream(), strFile, BeanHolder.getInstance().getDataConverter());
		viewer.init();
		
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		
		final Table table = viewer.getTable();
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = table.getItem(table.getSelectionIndex());
				Object obj = item.getData();
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>)obj;
					String strategyName = (String)map.get(OrderField.STRATEGY.value());
					Business.getInstance().getEventManager().
						sendEvent(new SingleOrderStrategySelectionEvent(map, Business.getInstance().getSingleOrderAmendableFields(strategyName)));
				}
			}
		});

		createBodyMenu(parent);

		// business logic goes here
		Business.getInstance().getEventManager().subscribe(OrderCacheReadyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(GuiSingleOrderStrategyUpdateEvent.class, this);
		showOrders();
	}

	private void createBodyMenu(final Composite parent) {
		final Table table = viewer.getTable();
		menu = new Menu(table.getShell(), SWT.POP_UP);

		MenuItem item;
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
		item.setText("Start");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.Start);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Clear alert");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				strategyAction(StrategyAction.ClearAlert);
			}
		});
		
		item = new MenuItem(menu, SWT.PUSH);
		item.setText("Multi Amend");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				amendDialog = new AmendDialog(parent.getShell());
				amendDialog.open();
				if(amendDialog.getReturnCode() == org.eclipse.jface.window.Window.OK ) {
					maTag = amendDialog.getField();
					maValue = amendDialog.getValue();
					if(null == maTag || maTag.equals(""))
						return;
					strategyAction(StrategyAction.MultiAmend);
				}
			}
		});
		
		viewer.setBodyMenu(menu);
	}
	
	private void createEnterOrderAction(final Composite parent) {
		orderDialog = new OrderDialog(parent.getShell());
		// create local toolbars
		enterOrderAction = new Action() {
			public void run() {
				//orderDialog.open();
				orderDialog.open();
			}
		};
		enterOrderAction.setText("Enter Order");
		enterOrderAction.setToolTipText("Create an order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PLUS_ICON.toString());
		enterOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(enterOrderAction);
	}
	
	private void createCancelOrderAction(Composite parent) {
		// create local toolbars
		cancelOrderAction = new Action() {
			public void run() {
				cancelOrders();
			}
		};
		cancelOrderAction.setText("Cancel Order");
		cancelOrderAction.setToolTipText("Cancel the order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.FALSE_ICON.toString());
		cancelOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(cancelOrderAction);
	}
	
	private void createSaveOrderAction(Composite parent) {
		// create local toolbars
		saveOrderAction = new Action() {
			public void run() {
				saveOrder();
			}
		};
		
		saveOrderAction.setText("Save order as xml");
		saveOrderAction.setToolTipText("Save order as xml");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.SAVE_ICON.toString());
		saveOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(saveOrderAction);
	}

	private void saveOrder() {
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		Table table = SingleOrderStrategyView.this.viewer.getTable();

		TableItem items[] = table.getSelection();
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for(TableItem item: items) {
				Object obj = item.getData();
				if (obj instanceof HashMap) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>)obj;
					list.add(map);
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
				EnterParentOrderEvent event = new EnterParentOrderEvent(null, null, list.get(0), "", false);
				Business.getInstance().getXstream().toXML(event, os);
			} else {  // more than one
				List<EnterParentOrderEvent> events = new ArrayList<EnterParentOrderEvent>();
				for(Map<String, Object> map: list) {
					events.add(new EnterParentOrderEvent(null, null, map, "", false) );
				}
				Business.getInstance().getXstream().toXML(events, os);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	protected void cancelOrders() {
		Table table = SingleOrderStrategyView.this.viewer.getTable();

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
								new CancelParentOrderEvent(id, server, id, null));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}

	}

	private void strategyAction(StrategyAction action) {
		Table table = SingleOrderStrategyView.this.viewer.getTable();

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
						sendRemoteEvent(new ClearSingleAlertEvent(id, server));
					} else if(StrategyAction.MultiAmend.equals(action)) {
						Map<String, Object> changes = new HashMap<String, Object>();
						changes.put(OrderField.ID.value(), id);
						changes.put(maTag, maValue);
						AmendParentOrderEvent event = new AmendParentOrderEvent(id, server, changes, IdGenerator.getInstance().getNextID());
						Business.getInstance().getEventManager().
							sendRemoteEvent(event);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}
		}
	}
	
	private void createPauseOrderAction(final Composite parent) {
		// create local toolbars
		pauseOrderAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Pause);
			}
		};
		pauseOrderAction.setText("Pause Order");
		pauseOrderAction.setToolTipText("Pause order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PAUSE_ICON.toString());
		pauseOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(pauseOrderAction);
	}

	private void createStopOrderAction(final Composite parent) {
		// create local toolbars
		stopOrderAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Stop);
			}
		};
		stopOrderAction.setText("Stop Order");
		stopOrderAction.setToolTipText("Stop order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.STOP_ICON.toString());
		stopOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(stopOrderAction);
	}
	
	private void createStartOrderAction(final Composite parent) {
		// create local toolbars
		startOrderAction = new Action() {
			public void run() {
				strategyAction(StrategyAction.Start);
			}
		};
		startOrderAction.setText("Start Order");
		startOrderAction.setToolTipText("Start order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.START_ICON.toString());
		startOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(startOrderAction);
	}
	
	private void createFilterControls(final Composite parent) {
		filterComposite = new Composite(parent, SWT.NONE);
		GridData filterGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		filterComposite.setLayout(layout);

		filterLabel = new Label(filterComposite, SWT.NONE);
		filterLabel.setText("Filter: ");
		filterLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		
		filterField = new Combo(filterComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		filterField.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		filterText = new Text(filterComposite, SWT.BORDER | SWT.SEARCH);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		filterButton = new Button(filterComposite, SWT.FLAT);
		filterButton.setText("Apply Filter");
		filterButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
		filterButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (viewFilter == null) {
					viewFilter = new ParentOrderFilter();
					viewFilter.setMatch(filterField.getText(), filterText.getText());
					filterButton.setText("Remove Filter");
					viewer.addFilter(viewFilter);
					filterButton.pack();
					filterComposite.layout();
				} else {
					filterButton.setText("Apply Filter");
					viewer.removeFilter(viewFilter);
					viewFilter = null;
					filterButton.pack();
					filterComposite.layout();
				}
				viewer.refresh();
			}
			
		});

		filterComposite.setLayoutData(filterGridData);
		filterComposite.layout();

		filterGridData.exclude = true;
		filterComposite.setVisible(false);
		
		// create local toolbars
		filterAction = new Action() {
			public void run() {
				if (filterComposite.isVisible()) {
					showFilter(false);
				} else {
					showFilter(true);
				}
				parent.layout();
			}
		};
		filterAction.setText("Filter");
		filterAction.setToolTipText("show or hide filter");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.FILTER_ICON.toString());
		filterAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(filterAction);
		
//		IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager();
//		manager.setMessage("Information for the status line");

	}
	
	private void showFilter(boolean show) {
		if (viewFilter != null) {
			filterField.setText(viewFilter.getColumn());
			filterText.setText(viewFilter.getPattern());
		}
			
		filterComposite.setVisible(show);
		GridData data = (GridData) filterComposite.getLayoutData();
		data.exclude = !show;
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void showOrders() {
		lastRefreshTime = Clock.getInstance().now();
		if(!setColumns) {
			List<Map<String, Object>> orders = Business.getInstance().getOrderManager().getParentOrders();
			
			if (orders.size() == 0)
				return;
			
			ArrayList<ColumnProperty> columnProperties = new ArrayList<ColumnProperty>();
			List<String> displayFields = Business.getInstance().getParentOrderDisplayFields();
			
			//add fields exists in both list
			for(String field: displayFields) {
//				if(titles.contains(field))
					columnProperties.add(new ColumnProperty(field, 100));
			}
			
			viewer.setSmartColumnProperties("Parent Order", columnProperties);
			filterField.removeAll();
			for(ColumnProperty prop: viewer.getDynamicColumnProperties()) {
				filterField.add(prop.getTitle());
			}
			viewer.setInput(orders);
			setColumns = true;
		}
		viewer.refresh();
	}
	
	private void asyncShowOrders() {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
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
		if (event instanceof OrderCacheReadyEvent) {
			log.debug("Recieved event: " + event);
			smartShowOrders();
		} else if (event instanceof GuiSingleOrderStrategyUpdateEvent) {
			log.debug("Recieved event: " + event);
			smartShowOrders();
		} else if (event instanceof AsyncTimerEvent) {
			timerEvent = null;
			asyncShowOrders();
		} else {
			log.warn("Unhandled event: " + event);
		}
				
	}
}
