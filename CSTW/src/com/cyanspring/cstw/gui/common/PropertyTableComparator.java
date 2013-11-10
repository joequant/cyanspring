package com.cyanspring.cstw.gui.common;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import com.cyanspring.common.type.KeyValue;

public class PropertyTableComparator extends ViewerComparator {
	private int propertyIndex;
	private boolean descending;

	public PropertyTableComparator() {
		this.propertyIndex = 0;
		descending = true;
	}

	public int getDirection() {
		return descending ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			descending = descending?false:true;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			descending = true;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof KeyValue && 
				e2 instanceof KeyValue && 
				viewer instanceof PropertyTableViewer)) 
			return 0;
		
		KeyValue kv1 = (KeyValue)e1;
		KeyValue kv2 = (KeyValue)e2;
		String obj1 = kv1.key;
		String obj2 = kv2.key;

		int result;
		if(null == obj1 && null == obj2) {
			return 0;
		} else if (null == obj1 && null != obj2) {
			return 1;
		} else if (null != obj1 && null == obj2) {
			return -1;
		} else {
			result = obj1.compareTo(obj2);
		}
		// If descending order, flip the direction
		if (descending) {
			result = -result;
		}
		return result;

	}

}
