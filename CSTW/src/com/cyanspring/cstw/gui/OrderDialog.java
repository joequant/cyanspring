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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.marketsession.DefaultStartEndTime;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.gui.common.PropertyTableViewer;

public class OrderDialog extends Dialog implements IAsyncEventListener {
	private static final Logger log = LoggerFactory.getLogger(OrderDialog.class);
	
	private Text txtSymbol;
	private Text txtPrice;
	private Text txtQuantity;
	private static final int height = 300;
	private static final int width = 730;
	private Composite composite_2;
	private Label lbStatus;
	private Label sep1;
	private Button btnMore;
	private Combo cbSide;
	private Combo cbType;
	private Combo cbStrategy;
	private Text txtStartTime;
	private Text txtEndTime;
	private PropertyTableViewer viewer;
	private Combo cbServer;
	private Button btnOk;
	private Button btnCancel;
	
	private static final String[] constantFields = 
		new String[]{
			OrderField.SYMBOL.value(),
			OrderField.SIDE.value(),
			OrderField.PRICE.value(),
			OrderField.TYPE.value(),
			OrderField.QUANTITY.value(),
			OrderField.STRATEGY.value(),
			OrderField.START_TIME.value(),
			OrderField.END_TIME.value(),
		};
		

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public OrderDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {  
		super.configureShell(newShell);  
		newShell.setText("Enter Order");
	}
	  
	
	private boolean isConstantField(String field) {
		for(String s: constantFields) {
			if(s.equals(field))
				return true;
		}
		return false;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		DefaultStartEndTime defaultStartEndTime = Business.getInstance().getDefaultStartEndTime();
		GenericDataConverter dataConverter = BeanHolder.getInstance().getDataConverter();
		String strStart = "";
		String strEnd = "";
		try {
			strStart = dataConverter.toString(OrderField.START_TIME.value(), defaultStartEndTime.getStart());
			strEnd = dataConverter.toString(OrderField.END_TIME.value(), defaultStartEndTime.getEnd());
		} catch (DataConvertException e) {
			log.error(e.getMessage(), e);
		}
		
		final Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.horizontalSpacing = 1;
		gridLayout.verticalSpacing = 1;
		gridLayout.marginWidth = 1;
		gridLayout.marginHeight = 1;
		gridLayout.numColumns = 2;
		GridData gd_container = (GridData) container.getLayoutData();
		gd_container.heightHint = 200;
		gd_container.widthHint = 350;
		
		final Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new GridLayout(4, true));
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_1.heightHint = 200;
		gd_composite_1.widthHint = 350;	
		composite_1.setLayoutData(gd_composite_1);
		
		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Symbol:");
		
		txtSymbol = new Text(composite_1, SWT.BORDER);
		txtSymbol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtSymbol.setText("0005.HK");
		
		Label lblNewLabel_1 = new Label(composite_1, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Side:");
		
		cbSide = new Combo(composite_1, SWT.READ_ONLY);
		cbSide.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label lblNewLabel_2 = new Label(composite_1, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Price:");
		
		txtPrice = new Text(composite_1, SWT.BORDER);
		txtPrice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblType = new Label(composite_1, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("Type:");
		
		cbType = new Combo(composite_1, SWT.READ_ONLY);
		cbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_3 = new Label(composite_1, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Quantity:");
		
		txtQuantity = new Text(composite_1, SWT.BORDER);
		txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblStrategy = new Label(composite_1, SWT.NONE);
		lblStrategy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStrategy.setText("Strategy:");
		
		cbStrategy = new Combo(composite_1, SWT.READ_ONLY);
		cbStrategy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String strategy = cbStrategy.getText();
				Map<String, Map<String, FieldDef>> map = Business.getInstance().getSingleOrderFieldDefs();
				Map<String, FieldDef> fieldDefs = map.get(strategy);
				Map<String, Object> data = new HashMap<String, Object>();
				List<String> fields = new ArrayList<String>();
				for (FieldDef fieldDef: fieldDefs.values()) {
					if(fieldDef.isInput() && !isConstantField(fieldDef.getName())) {
						data.put(fieldDef.getName(), fieldDef.getValue());
						fields.add(fieldDef.getName());
					}
				}
				viewer.turnOnEditMode(fields);
				viewer.setInput(data);
			}
		});
		cbStrategy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lbStartTime = new Label(composite_1, SWT.NONE);
		lbStartTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbStartTime.setText("StartTime:");
		
		txtStartTime = new Text(composite_1, SWT.NONE);
		GridData gd_StartTime = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_StartTime.heightHint = 21;
		txtStartTime.setLayoutData(gd_StartTime);
		txtStartTime.setText(strStart);
		
		Label lbEndTime = new Label(composite_1, SWT.NONE);
		lbEndTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lbEndTime.setText("EndTime:");
		
		txtEndTime = new Text(composite_1, SWT.NONE);
		GridData gd_EndTime = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_EndTime.heightHint = 21;
		txtEndTime.setLayoutData(gd_EndTime);
		txtEndTime.setText(strEnd);
		
		Label lblNewLabel_4 = new Label(composite_1, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("Server:");
		
		cbServer = new Combo(composite_1, SWT.READ_ONLY);
		cbServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		new Label(composite_1, SWT.NONE);
		
		btnMore = new Button(composite_1, SWT.NONE);
		btnMore.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnMore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				togglePropertyView(parent, container, true);
			}
		});
		
		btnMore.setText("Less <<<");
		
		composite_2 = new Composite(container, SWT.NONE);
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_2.heightHint = 200;
		gd_composite_2.widthHint = 250;
		gd_composite_2.exclude = false;
		composite_2.setLayoutData(gd_composite_2);
		composite_2.setLayout(new GridLayout(1, false));
		viewer = new PropertyTableViewer(composite_2, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL, BeanHolder.getInstance().getDataConverter());
		Table table_3 = viewer.getTable();
		table_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		viewer.init();
		lbStatus = new Label(container, SWT.WRAP); 
		Color red = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		lbStatus.setForeground(red);
		lbStatus.setText("");
		lbStatus.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1));
		
		sep1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL );
		sep1.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 2, 1));
//		Label sep2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);

		return container;
	}
	
	private void populateData() {
		populateOrderSides();
		populateOrderType();
		populateStrategies();
		populateServers();
	}
	
	private void populateOrderSides(){
		for(OrderSide side: OrderSide.values())
			cbSide.add(side.toString());
	}

	private void populateOrderType(){
		for(OrderType type: OrderType.values())
			cbType.add(type.toString());
	}

	private void populateStrategies() {
		Map<String, Map<String, FieldDef>> map = Business.getInstance().getSingleOrderFieldDefs();
		 Set<String> keys = map.keySet();
		 for(String str: keys) {
			 cbStrategy.add(str);
		 }
	}
	
	private void populateServers() {
		ArrayList<String> servers = Business.getInstance().getOrderManager().getServers();
		if(servers.size() == 0) {
			lbStatus.setText("Error: no server is available");
			btnOk.setEnabled(false);
			return;
		}
		for(String str: servers) {
			 cbServer.add(str);
		}
		cbServer.setText(servers.get(0));
	}
	
	private void togglePropertyView(Composite parent, Composite container, boolean layout) {
		GridData gridData = (GridData)composite_2.getLayoutData();
		if(gridData.exclude) {
			gridData.exclude = false;
			GridLayout gridLayout = (GridLayout) container.getLayout();
			gridLayout.numColumns = 2;
			gridData = (GridData)lbStatus.getLayoutData();
			gridData.horizontalSpan = 2;
			gridData = (GridData)sep1.getLayoutData();
			gridData.horizontalSpan = 2;
			Rectangle rect = OrderDialog.this.getShell().getBounds();
			rect.width = OrderDialog.width;
			OrderDialog.this.getShell().setBounds(rect);
			btnMore.setText("Less <<<");
			if (layout)
				parent.layout();
		} else {
			gridData.exclude = true;
			GridLayout gridLayout = (GridLayout) container.getLayout();
			gridLayout.numColumns = 1;
			gridData = (GridData)lbStatus.getLayoutData();
			gridData.horizontalSpan = 1;
			gridData = (GridData)sep1.getLayoutData();
			gridData.horizontalSpan = 1;
			Rectangle rect = OrderDialog.this.getShell().getBounds();
			rect.width = rect.width * 3/5;
			OrderDialog.this.getShell().setBounds(rect);
			btnMore.setText("More >>>");
			if (layout)
				parent.layout();
		}		
	}
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
//				true);
//		createButton(parent, IDialogConstants.CANCEL_ID,
//				IDialogConstants.CANCEL_LABEL, false);
		((GridLayout) parent.getLayout()).numColumns = 2;
		btnOk = new Button(parent, SWT.PUSH);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enterOrder();
			}
		});
		btnOk.setText("OK");
		setButtonLayoutData(btnOk);
		btnCancel = new Button(parent, SWT.PUSH);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OrderDialog.this.close();
			}
		});
		btnCancel.setText("Cancel");
		setButtonLayoutData(btnCancel);
		
		// setting default data
		populateData();
		
		Business.getInstance().getEventManager().subscribe(EnterParentOrderReplyEvent.class, Business.getInstance().getInbox(), this);

	}

	@SuppressWarnings("unchecked")
	private void enterOrder() {
		HashMap<String, Object> fields = new HashMap<String, Object>();

		fields.put(OrderField.SYMBOL.value(), txtSymbol.getText());
		fields.put(OrderField.SIDE.value(), cbSide.getText());
		fields.put(OrderField.TYPE.value(), cbType.getText());
		fields.put(OrderField.QUANTITY.value(), txtQuantity.getText());
		fields.put(OrderField.PRICE.value(), txtPrice.getText());
		fields.put(OrderField.STRATEGY.value(), cbStrategy.getText());
		fields.put(OrderField.START_TIME.value(), txtStartTime.getText());
		fields.put(OrderField.END_TIME.value(), txtEndTime.getText());
			
		HashMap<String, String> extraFields = (HashMap<String, String>)viewer.getInput();
		if(null != extraFields)
			fields.putAll(extraFields);

		if(cbServer.getText() == null || cbServer.getText() == "") {
			lbStatus.setText("Error: server is not specified");
			return;
		}
			
		EnterParentOrderEvent event = 
			new EnterParentOrderEvent(Business.getInstance().getInbox(), cbServer.getText(), fields, null, false);
		try {
			Business.getInstance().getEventManager().sendRemoteEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		lbStatus.setText("");
		btnOk.setEnabled(false);
	}
	
	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(OrderDialog.width, OrderDialog.height);
	}

	@Override
	public void onEvent(AsyncEvent e) {
		if (e instanceof EnterParentOrderReplyEvent) {
			final EnterParentOrderReplyEvent event = (EnterParentOrderReplyEvent)e;
			this.getContents().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					btnOk.setEnabled(true);
					if(event.isOk()) {
						lbStatus.setText("");
						OrderDialog.this.close();
					} else {
						lbStatus.setText(event.getMessage());
					}
						
				}
			});

		}
		
	}
}


