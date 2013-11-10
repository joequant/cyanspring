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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.ExecutionSnapshotEvent;
import com.cyanspring.common.event.order.ExecutionSnapshotRequestEvent;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.event.InstrumentSelectionEvent;
import com.cyanspring.cstw.event.MultiInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.ObjectSelectionEvent;
import com.cyanspring.cstw.event.SingleInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.SingleOrderStrategySelectionEvent;
import com.cyanspring.cstw.gui.common.ColumnProperty;
import com.cyanspring.cstw.gui.common.DynamicTableViewer;

public class ExecutionView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(ExecutionView.class);
	
	public static final String ID = "com.cyanspring.cstw.gui.ExecutionView";
	private DynamicTableViewer viewer;
	private String objectId;
	private String currentParentId;
	@SuppressWarnings("unused")
	private String currentSymbol;
	@SuppressWarnings("unused")
	private boolean isSingleOrderStrategy;
	private boolean columnSet;

	public ExecutionView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		// create parent layout
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		// create table
	    String strFile = Business.getInstance().getConfigPath() + "ExecutionTable.xml";
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

		// subscribe to business event
		Business.getInstance().getEventManager().subscribe(SingleOrderStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(SingleInstrumentStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(ExecutionSnapshotEvent.class, this);
		Business.getInstance().getEventManager().subscribe(ChildOrderUpdateEvent.class, this);
		Business.getInstance().getEventManager().subscribe(MultiInstrumentStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(InstrumentSelectionEvent.class, this);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private void showSnapshot(ExecutionSnapshotEvent event) {
		List<Execution> executions = event.getExecutions();
		ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		if(executions != null)
			for(Execution exec: executions) {
				if(currentParentId != null && !currentParentId.equals(exec.getParentOrderId()))
					continue;
				data.add(exec.getFields());
			}
		show(data);
	}
	
	private void show(ArrayList<Map<String, Object>> data) {
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

	private void updateExecution(Execution execution) {
		if(objectId == null || !objectId.equals(execution.getStrategyId()) || 
				   ((currentParentId != null) && !currentParentId.equals(execution.getParentOrderId())))
					return;
				
		@SuppressWarnings("unchecked")
		final ArrayList<Map<String, Object>> data = null == viewer.getInput()?
				new ArrayList<Map<String, Object>>() : (ArrayList<Map<String, Object>>)viewer.getInput();
		boolean found = false;
		for(int i=0; i<data.size(); i++) {
			Map<String, Object> map = data.get(i);
			String id = (String)map.get(OrderField.ID.value());
			if(id.equals(execution.getId())) {
				data.set(i, execution.getFields());
				found = true;
				break;
			}
		}
		if(!found)
			data.add(execution.getFields());
		
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				show(data);
			}
		});
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
			} else if(e instanceof SingleOrderStrategySelectionEvent  ||
					  e instanceof SingleInstrumentStrategySelectionEvent) {
				currentSymbol = (String)map.get(OrderField.SYMBOL.value());
				currentParentId = null;
				isSingleOrderStrategy = true;
			}
			
			ExecutionSnapshotRequestEvent request = new ExecutionSnapshotRequestEvent(objectId, server);
			try {
				Business.getInstance().getEventManager().sendRemoteEvent(request);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				ex.printStackTrace();
			}

		} else if (e instanceof ExecutionSnapshotEvent) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					showSnapshot((ExecutionSnapshotEvent)e);
					final Table table = viewer.getTable();
					if(table.getItemCount()>0)
						table.setSelection(0);
				}
			});
			
		} else if (e instanceof ChildOrderUpdateEvent) {
			ChildOrderUpdateEvent event = (ChildOrderUpdateEvent)e;
			Execution execution = event.getExecution();
			if(event.getExecution() == null)
				return;
			
			updateExecution(execution);
		} else {
			log.error("Unhandled event: " + e.getClass());
		}
	}

}
