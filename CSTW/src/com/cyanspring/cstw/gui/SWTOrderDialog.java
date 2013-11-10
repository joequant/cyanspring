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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class SWTOrderDialog extends Dialog {

	protected Object result;
	protected Shell shlOrderDialog;
	private Text txtSymbol;
	private Table table;
	private Text txtPrice;
	private Text txtQuantity;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SWTOrderDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlOrderDialog.open();
		shlOrderDialog.layout();
		Display display = getParent().getDisplay();
		while (!shlOrderDialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlOrderDialog = new Shell(getParent(), getStyle());
		shlOrderDialog.setSize(572, 270);
		shlOrderDialog.setText("Order Dialog");
		shlOrderDialog.setLayout(new GridLayout(9, false));
		
		Composite composite = new Composite(shlOrderDialog, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 9, 1));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_1.widthHint = 320;
		composite_1.setLayoutData(gd_composite_1);
		composite_1.setLayout(new GridLayout(1, false));
		
		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		GridLayout gl_composite_2 = new GridLayout(4, false);
		composite_2.setLayout(gl_composite_2);
		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_2.widthHint = 375;
		composite_2.setLayoutData(gd_composite_2);
		
		Label lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Symbol:");
		
		txtSymbol = new Text(composite_2, SWT.BORDER);
		txtSymbol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtSymbol.setText("0005.HK");
		
		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Side:");
		
		Combo cbSide = new Combo(composite_2, SWT.NONE);
		cbSide.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label lblNewLabel_2 = new Label(composite_2, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Price:");
		
		txtPrice = new Text(composite_2, SWT.BORDER);
		txtPrice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblType = new Label(composite_2, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("Type:");
		
		Combo cbType = new Combo(composite_2, SWT.NONE);
		cbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_3 = new Label(composite_2, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Quantity:");
		
		txtQuantity = new Text(composite_2, SWT.BORDER);
		txtQuantity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblStrategy = new Label(composite_2, SWT.NONE);
		lblStrategy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStrategy.setText("Strategy:");
		
		Combo cbStrategy = new Combo(composite_2, SWT.NONE);
		cbStrategy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label label = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_3 = new Composite(composite_1, SWT.NONE);
		GridData gd_composite_3 = new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1);
		gd_composite_3.heightHint = 55;
		composite_3.setLayoutData(gd_composite_3);
		
		Button btnOk = new Button(composite_3, SWT.NONE);
		btnOk.setBounds(213, 10, 75, 25);
		btnOk.setText("OK");
		
		Button btnNewButton = new Button(composite_3, SWT.NONE);
		btnNewButton.setBounds(290, 10, 75, 25);
		btnNewButton.setText("Cancel");
		
		Label lbInfo = new Label(composite_3, SWT.WRAP);
		lbInfo.setBounds(10, 8, 198, 37);
		lbInfo.setText("Please enter an order");
		
		Composite composite_4 = new Composite(composite, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_4.setLayout(new GridLayout(1, false));
		
		TableViewer tableViewer = new TableViewer(composite_4, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	}
}
