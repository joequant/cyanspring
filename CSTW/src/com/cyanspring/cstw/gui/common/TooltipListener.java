package com.cyanspring.cstw.gui.common;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.AlertType;

class TooltipListener implements Listener {
	Table table;
	Shell tooltip = null;
	Label label = null;
	
	public TooltipListener(Table table) {
		this.table = table;
	}
		@Override
		public void handleEvent(Event event) {

			switch (event.type) {
				case SWT.KeyDown:
				case SWT.Dispose:
				case SWT.MouseMove: {
					if (tooltip == null) 
						break;
					tooltip.dispose();
					tooltip = null;
					label = null;
					break;
				}

				case SWT.MouseHover: {
					Point coords = new Point(event.x, event.y);
					TableItem item = table.getItem(coords);
					if(null == item)
						return;
					
					Object data = item.getData();
					if (null != data && data instanceof HashMap) {
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>)data;
						AlertType alertType = (AlertType) map.get(OrderField.ALERT_TYPE.value());
						String alertMsg = (String) map.get(OrderField.ALERT_MSG.value());
						if(null != alertType && null != alertMsg) {
							/* Create a new Tooltip */
							tooltip = new Shell (table.getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
							tooltip.setBackground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
							FillLayout layout = new FillLayout();
							layout.marginWidth = 2;
							tooltip.setLayout (layout);
				
							label = new Label (tooltip, SWT.NONE);
							label.setForeground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
							label.setBackground (table.getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));

							/* Set the tooltip text */
							label.setText(alertMsg);

							/* Set the size and position of the tooltip */
							Point size = tooltip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
							Point pt = table.toDisplay (event.x, event.y);
							tooltip.setBounds (pt.x, pt.y, size.x, size.y);
							/* Show it */
							tooltip.setVisible (true);
						}
					}
					
			}

		}
		
	}

}
