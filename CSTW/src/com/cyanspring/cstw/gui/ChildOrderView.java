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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.order.ChildOrderSnapshotEvent;
import com.cyanspring.common.event.order.ChildOrderSnapshotRequestEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.ManualActionReplyEvent;
import com.cyanspring.common.event.order.ManualAmendChildOrderEvent;
import com.cyanspring.common.event.order.ManualCancelChildOrderEvent;
import com.cyanspring.common.event.order.ManualNewChildOrderEvent;
import com.cyanspring.common.event.strategy.StrategyLogEvent;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.LogType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.event.InstrumentSelectionEvent;
import com.cyanspring.cstw.event.MultiInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.ObjectSelectionEvent;
import com.cyanspring.cstw.event.SingleInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.SingleOrderStrategySelectionEvent;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;

public class ChildOrderView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(ChildOrderView.class);
	public static final String ID = "com.cyanspring.cstw.gui.ChildOrderView";
	private Composite parent;
	private String objectId;
	private String currentParentId;
	private String currentSymbol;
	private boolean isSingleOrderStrategy;
	private DynamicTableViewer viewer;
	private boolean columnSet;
	private Composite panelComposite;
	private Label lbOrderId;
	private Label lbSymbol;
	private Text txtPrice;
	private Text txtQuantity;
	private Combo cbOrderSide;
	private Combo cbOrderType;
	private Button btAction;
	private Action cancelOrderAction;
	private Action enterOrderAction;
	private Action amendOrderAction;
	private ImageRegistry imageRegistry;
	private boolean enterOrderMode; // true entering order, false amending order

	public ChildOrderView() {
	}

	@Override
	public void createPartControl(final Composite parent) {
		// create ImageRegistery
		imageRegistry = Activator.getDefault().getImageRegistry();

		this.parent = parent;
		// create parent layout
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		// create table
	    String strFile = Business.getInstance().getConfigPath() + "ChildOrderTable.xml";
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

		createOrderPanel(parent);
		createEnterOrderAction(parent);
		createAmendOrderAction(parent);
		createCancelOrderAction(parent);
		
		final Table table = viewer.getTable();
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if(panelComposite.isVisible() && !enterOrderMode)
					showAmendOrderPanel(parent);
			}
		});

		
		// subscribe to business event
		Business.getInstance().getEventManager().subscribe(SingleOrderStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(SingleInstrumentStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(ChildOrderSnapshotEvent.class, this);
		Business.getInstance().getEventManager().subscribe(ChildOrderUpdateEvent.class, this);
		Business.getInstance().getEventManager().subscribe(ManualActionReplyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(MultiInstrumentStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(InstrumentSelectionEvent.class, this);
	}
	
	private void createOrderPanel(final Composite parent) {
		GridData gridData;
		
		panelComposite = new Composite(parent, SWT.NONE);
		GridData panelGridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		panelComposite.setLayoutData(panelGridData);
		GridLayout layout = new GridLayout(10, false);
		layout.marginHeight = 1;
		layout.marginWidth = 5;
		panelComposite.setLayout(layout);

		Label lb1 = new Label(panelComposite, SWT.NONE);
		lb1.setText("Order Id: ");
		lb1.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
		
		lbOrderId = new Label(panelComposite, SWT.BORDER);
		lbOrderId.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		lbSymbol = new Label(panelComposite, SWT.BORDER);
		lbSymbol.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

		Label lb2 = new Label(panelComposite, SWT.NONE);
		lb2.setText("Price: ");
		lb2.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		txtPrice = new Text(panelComposite, SWT.BORDER | SWT.SEARCH);
		gridData = new GridData(SWT.RIGHT, SWT.FILL, false, true);
		gridData.widthHint = 100;
		txtPrice.setLayoutData(gridData);

		Label lb3 = new Label(panelComposite, SWT.NONE);
		lb3.setText("Quantity: ");
		lb3.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		txtQuantity = new Text(panelComposite, SWT.BORDER | SWT.SEARCH);
		gridData = new GridData(SWT.RIGHT, SWT.FILL, false, true);
		gridData.widthHint = 100;
		txtQuantity.setLayoutData(gridData);

		cbOrderSide = new Combo(panelComposite, SWT.BORDER | SWT.SEARCH);
		cbOrderSide.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		
		cbOrderType = new Combo(panelComposite, SWT.BORDER | SWT.SEARCH);
		cbOrderType.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		
		btAction = new Button(panelComposite, SWT.FLAT);
		btAction.setText("Amend");
		btAction.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
		
		btAction.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if(enterOrderMode) {
					enterOrder(parent);
				} else {
					amendOrder(parent);
				}
			}
			
		});
		
		showPanel(parent, false);
	}
	
	private void showPanel(Composite parent, boolean show) {
		panelComposite.setVisible(show);
		GridData data = (GridData) panelComposite.getLayoutData();
		data.exclude = !show;
		parent.layout();
	}

	private Map<String, Object> retrieveParent(Composite com) {
		if(objectId == null)
			return null;
		
		Map<String, Object> parent = Business.getInstance().getOrderManager().getStrategyData(objectId);
		StrategyState state = (StrategyState)parent.get(OrderField.STATE.value());
		if(!state.equals(StrategyState.Paused) && !state.equals(StrategyState.Stopped)) {
//			Business.getInstance().getEventManager().sendEvent(
//					new StrategyLogEvent(objectId, null, LogType.Warn, 
//							"Strategy must be paused or stopped to manually operate on child order"));
			MessageDialog.openError(com.getShell(), "Strategy state disallows this action", 
			"Please stop/pause the order before manually operate on child orders");
			return null;
		}
		
		return parent;
	}
	
	private void enterOrder(final Composite parent) {
		Map<String, Object> parentMap = retrieveParent(parent);
		if(null == parentMap)
			return;
		
		String server = (String)parentMap.get(OrderField.SERVER_ID.value());
		String strategyId = (String)parentMap.get(OrderField.ID.value());

		int qty = 0;
		double price = 0;
		try {
			qty = Integer.parseInt(txtQuantity.getText());
			price = Double.parseDouble(txtPrice.getText());
		} catch (NumberFormatException e) {
			try {
				Business.getInstance().getEventManager().sendEvent(
						new StrategyLogEvent(objectId, null, LogType.Error, 
								"Error: " + e.getMessage()));
			} catch (Exception ex) {
				log.error(e.getMessage(), ex);
				ex.printStackTrace();
			}
			return;
		}
		
		OrderSide side;
		if(isSingleOrderStrategy) {
			side = (OrderSide)parentMap.get(OrderField.SIDE.value());
		} else {
			side = OrderSide.valueOf(cbOrderSide.getText());
		}
		String parentId = currentParentId == null? strategyId : currentParentId;
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(
					new ManualNewChildOrderEvent(strategyId, server, parentId, side, price, qty, 
							ExchangeOrderType.valueOf(cbOrderType.getText())));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		showPanel(parent, false);
	}
	
	private void amendOrder(final Composite parent) {
		Map<String, Object> parentMap = retrieveParent(parent);
		if(null == parentMap)
			return;
		
		if(lbOrderId.getText().equals(""))
			return;
		
		final Table table = viewer.getTable();
		TableItem item = table.getItem(table.getSelectionIndex());
		Object obj = item.getData();
		if(obj == null)
			return;

		@SuppressWarnings("unchecked")
		HashMap<String, Object> order = (HashMap<String, Object>)obj;
		String server = (String)order.get(OrderField.SERVER_ID.value());
		int qty = 0;
		double price = 0;
		try {
			qty = Integer.parseInt(txtQuantity.getText());
			price = Double.parseDouble(txtPrice.getText());
		} catch (NumberFormatException e) {
			try {
				Business.getInstance().getEventManager().sendEvent(
						new StrategyLogEvent(objectId, null, LogType.Error, 
								"Error: " + e.getMessage()));
			} catch (Exception ex) {
				log.error(e.getMessage(), ex);
				ex.printStackTrace();
			}
			return;
		}
		
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(
					new ManualAmendChildOrderEvent(objectId, server, lbOrderId.getText(), price, qty));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		showPanel(parent, false);
		
	}

	private void cancelOrder(Composite parent) {
		if(null == retrieveParent(parent))
			return;
		
		final Table table = viewer.getTable();
		TableItem items[] = table.getSelection();
		List<String> childOrderIds = new ArrayList<String>();
		String server = null;
		for(TableItem item: items) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> order = (HashMap<String, Object>)item.getData();
			server = (String)order.get(OrderField.SERVER_ID.value());
			String childId = (String)order.get(OrderField.ID.value());
			childOrderIds.add(childId);
		}
		
		if(childOrderIds.size()>0) {
			try {
				Business.getInstance().getEventManager().sendRemoteEvent(
						new ManualCancelChildOrderEvent(objectId, server, childOrderIds));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void setFocus() {
	}

	synchronized void showOrders(ChildOrderSnapshotEvent event) {
		List<ChildOrder> orders = event.getOrders();
		
		ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		if(null != orders)
			for(ChildOrder child: orders) {
				if(currentParentId != null && !currentParentId.equals(child.getParentOrderId()))
					continue;
//				child.put(OrderField.SERVER.value(), event.getSender());
				data.add(child.getFields());
			}

		show(data);
	}
	
	synchronized private void show(ArrayList<Map<String, Object>> data) {
		if (data == null)
			return;
		
		if (!columnSet && data.size()>0) {
			Map<String, Object> map = data.get(0);
			ArrayList<ColumnProperty> columnProperties = new ArrayList<ColumnProperty>();
			Set<String> titles = map.keySet();
			for(String title: titles)
				columnProperties.add(new ColumnProperty(title, 100));
			
			viewer.setSmartColumnProperties(viewer.getTableLayoutFile(), columnProperties);
			columnSet = true;
			viewer.setInput(data);
		}
		if(viewer.getInput() == null || viewer.getInput() != data)
			viewer.setInput(data);
		viewer.refresh();
	}

	private void createCancelOrderAction(final Composite parent) {
		// create local toolbars
		cancelOrderAction = new Action() {
			public void run() {
				cancelOrder(parent);
			}
		};
		cancelOrderAction.setText("Cancel child order");
		cancelOrderAction.setToolTipText("Cancel child order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.FALSE_ICON.toString());
		cancelOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(cancelOrderAction);
	}

	private void showEnterOrderPanel(final Composite parent) {
		if(null == retrieveParent(parent))
			return;
		
		if(currentSymbol == null) {
//			Business.getInstance().getEventManager().sendEvent(
//					new StrategyLogEvent(objectId, null, LogType.Warn, 
//							"Please select an instrument before create a child order"));
			MessageDialog.openError(parent.getShell(), "Please select an instrument", 
					"Please click on an instrument under a strategy that you want to create the child order on");
			return;
		}
		
		showPanel(parent, true);
		lbOrderId.setText("          "); 
		if(currentSymbol != null)
			lbSymbol.setText(currentSymbol);
		btAction.setText("  Enter  ");
		txtPrice.setText("");		
		txtQuantity.setText("");
		
		for(ExchangeOrderType type: ExchangeOrderType.values())
			cbOrderType.add(type.toString());
		cbOrderType.setText(ExchangeOrderType.LIMIT.toString());
		cbOrderType.setEnabled(true);
		
		for(OrderSide side: OrderSide.values()) {
			cbOrderSide.add(side.toString());
		}
		if(isSingleOrderStrategy)
			cbOrderSide.setEnabled(false);
		else 
			cbOrderSide.setEnabled(true);
		enterOrderMode = true;
		panelComposite.layout();
	}
	
	private void showAmendOrderPanel(final Composite parent) {
		if(null == retrieveParent(parent))
			return;
		
		final Table table = viewer.getTable();
		TableItem item = table.getItem(table.getSelectionIndex());
		Object obj = item.getData();
		if(obj == null)
			return;

		showPanel(parent, true);
		@SuppressWarnings("unchecked")
		HashMap<String, Object> order = (HashMap<String, Object>)obj;
		String childId = (String)order.get(OrderField.ID.value());
		String symbol = (String)order.get(OrderField.SYMBOL.value());

		lbOrderId.setText(childId); 
		lbSymbol.setText(symbol);
		
		btAction.setText("  Amend  ");
		String qty = "error";
		try {
			qty = BeanHolder.getInstance().getDataConverter().toString(OrderField.QUANTITY.value(), order.get(OrderField.QUANTITY.value()));
		} catch (DataConvertException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		txtPrice.setText(((Double)order.get(OrderField.PRICE.value())).toString());		
		txtQuantity.setText(qty);		
		cbOrderType.setText(order.get(OrderField.TYPE.value()).toString());		
		cbOrderType.setEnabled(false);
		cbOrderSide.setText(order.get(OrderField.SIDE.value()).toString());
		cbOrderSide.setEnabled(false);
		enterOrderMode = false;
		panelComposite.layout();
	}


	private void createEnterOrderAction(final Composite parent) {
		// create local toolbars
		enterOrderAction = new Action() {
			public void run() {
				showEnterOrderPanel(parent);
			}
		};
		enterOrderAction.setText("Create child order");
		enterOrderAction.setToolTipText("Create a new child order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.PLUS_ICON.toString());
		enterOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(enterOrderAction);
		
	}

	private void createAmendOrderAction(final Composite parent) {

		// create local toolbars
		amendOrderAction = new Action() {
			public void run() {
				showAmendOrderPanel(parent);
			}
		};
		amendOrderAction.setText("Amend child order");
		amendOrderAction.setToolTipText("Amend a child order");

		ImageDescriptor imageDesc = imageRegistry.getDescriptor(ImageID.AMEND_ICON.toString());
		amendOrderAction.setImageDescriptor(imageDesc);

		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(amendOrderAction);
		
	}

	@Override
	public void onEvent(final AsyncEvent e) {
		if (e instanceof ObjectSelectionEvent) {
			ObjectSelectionEvent event = (ObjectSelectionEvent)e;
			Map<String, Object> map = event.getData();
			objectId = (String)map.get(OrderField.ID.value());
			String server = (String)map.get(OrderField.SERVER_ID.value());
			if(e instanceof InstrumentSelectionEvent) {
				objectId = (String)map.get(OrderField.STRATEGY_ID.value());
				currentParentId = (String)map.get(OrderField.ID.value());
				currentSymbol = (String)map.get(OrderField.SYMBOL.value());
				isSingleOrderStrategy = false;
			} else if (e instanceof MultiInstrumentStrategySelectionEvent) {
				currentParentId = null;
				currentSymbol = null;
				isSingleOrderStrategy = false;
			} else if(e instanceof SingleOrderStrategySelectionEvent) {
				currentSymbol = (String)map.get(OrderField.SYMBOL.value());
				currentParentId = null;
				isSingleOrderStrategy = true;
			} else if(e instanceof SingleInstrumentStrategySelectionEvent) {
				currentSymbol = (String)map.get(OrderField.SYMBOL.value());
				currentParentId = null;
				isSingleOrderStrategy = false;
			}
			
			ChildOrderSnapshotRequestEvent request = new ChildOrderSnapshotRequestEvent(objectId, server);
			try {
				Business.getInstance().getEventManager().sendRemoteEvent(request);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				ex.printStackTrace();
			}
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					showPanel(parent, false);
				}
			});

		} else if (e instanceof ChildOrderSnapshotEvent) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					showOrders((ChildOrderSnapshotEvent)e);
					final Table table = viewer.getTable();
					if(table.getItemCount()>0)
						table.setSelection(0);

				}
			});
			
		} else if (e instanceof ChildOrderUpdateEvent) {
			final ChildOrderUpdateEvent event = (ChildOrderUpdateEvent)e;
			if(objectId == null || !objectId.equals(event.getOrder().getStrategyId()) || 
			   ((currentParentId != null) && !currentParentId.equals(event.getOrder().getParentOrderId())))
				return;
			
			ChildOrder order = event.getOrder();
//			order.put(OrderField.SERVER.value(), event.getSender());
			@SuppressWarnings("unchecked")
			final ArrayList<Map<String, Object>> data = null == viewer.getInput()?
					new ArrayList<Map<String, Object>>() : (ArrayList<Map<String, Object>>)viewer.getInput();
			boolean found = false;
			for(int i=0; i<data.size(); i++) {
				Map<String, Object> map = data.get(i);
				String id = (String)map.get(OrderField.ID.value());
				if(id.equals(order.getId())) {
					data.set(i, order.getFields());
					found = true;
					break;
				}
			}
			if(!found)
				data.add(event.getOrder().getFields());
			
			// will loop through to remove completed child orders from the view
			ArrayList<Map<String, Object>> toBeRemoved = new ArrayList<Map<String, Object>>();
			for(Map<String, Object> map: data) {
				if(((OrdStatus)map.get(OrderField.ORDSTATUS.value())).isCompleted()) {
					toBeRemoved.add(map);
				}
			}
			
			for(Map<String, Object> map: toBeRemoved) {
				data.remove(map);
			}

			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					show(data);
				}
			});
			
		} else if (e instanceof ManualActionReplyEvent) {
			ManualActionReplyEvent event = (ManualActionReplyEvent)e;
			if(!event.isSuccess()) {
				Business.getInstance().getEventManager().sendEvent(
						new StrategyLogEvent(objectId, null, LogType.Warn, 
								event.getMessage()));
			}
			
		}
		
	}

}
