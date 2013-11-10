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
package com.cyanspring.cstw.gui.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.data.AlertType;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;
import com.thoughtworks.xstream.XStream;


public class DynamicTableViewer extends TableViewer {
	private static final Logger log = LoggerFactory.getLogger(DynamicTableViewer.class);
	private String columnLayoutKey;
	private Map<String, List<ColumnProperty>> columnProperties = new HashMap<String, List<ColumnProperty>>();
	private Menu headerMenu;
	private Menu bodyMenu;
	private Composite parent;
	private String tableLayoutFile;
	private XStream xstream;
	private DynamicTableComparator comparator;
	private GenericDataConverter dataConverter;
	private ImageRegistry imageRegistry;
	private Image trueImage;
	private Image falseImage;

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent instanceof ArrayList<?>) {
				return ((ArrayList<?>)parent).toArray();
			}
	        return null;
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider, IColorProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof HashMap) {
				Table table = DynamicTableViewer.this.getTable();
				if (index>=table.getColumnCount())
					return null;
				
				TableColumn col = table.getColumn(index);
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>)obj;
				if (dataConverter != null) {
					try {
						return dataConverter.toString(col.getText(), map.get(col.getText()));
					} catch (DataConvertException e) {
						log.error(e.getMessage(), e);
						e.printStackTrace();
					}
				}
				else {
					return getText(map.get(col.getText()));
				}
			}
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			if (obj instanceof HashMap) {
				Table table = DynamicTableViewer.this.getTable();
				if (index>=table.getColumnCount())
					return null;
				
				TableColumn col = table.getColumn(index);
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>)obj;
				Object value = map.get(col.getText());
				if(value instanceof Boolean) {
					return ((Boolean)value)?trueImage : falseImage;
				}
					
			}
			return null;
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}

		@Override
		public Color getForeground(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			if (element instanceof HashMap) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>)element;
				AlertType alertType = (AlertType) map.get(OrderField.ALERT_TYPE.value());
				if(null != alertType) {
					Display display = Display.getCurrent();
					int color = Business.getInstance().getAlertColorConfig().get(alertType);
					return display.getSystemColor(color);
				}
			}
			return null;
		}
	}

	public DynamicTableViewer(Composite parent, int i, XStream xstream, String tableLayoutFile, GenericDataConverter dataConverter) {
		super(parent, i);
		this.parent = parent;
		this.xstream = xstream;
		this.tableLayoutFile = tableLayoutFile;
		this.dataConverter = dataConverter;
	}

//	private Point lastPopupMenuLocation;
	public void init() {
		// setting up table part
		imageRegistry = Activator.getDefault().getImageRegistry();
		trueImage = imageRegistry.getDescriptor(ImageID.TRUE_ICON.toString()).createImage();
		falseImage = imageRegistry.getDescriptor(ImageID.FALSE_ICON.toString()).createImage();
		
		ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE); 
		setContentProvider(new ViewContentProvider());
		setLabelProvider(new ViewLabelProvider());
		final Table table = this.getTable();
		table.setLinesVisible(true);
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		headerMenu = new Menu(this.parent.getShell(), SWT.POP_UP);

		// Provide the input to the ContentProvider
		table.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				Display display = DynamicTableViewer.this.getControl().getDisplay();
//				lastPopupMenuLocation = new Point(event.x, event.y);
				Point pt = display.map(null, table, new Point(event.x, event.y));
				Rectangle clientArea = table.getClientArea();
				boolean header = clientArea.y <= pt.y && pt.y < (clientArea.y + table.getHeaderHeight());
				table.setMenu(header ? headerMenu : bodyMenu);
			}
		});
		
		// add tooltip listener
		TooltipListener tooltipListener = new TooltipListener(table);
		table.addListener (SWT.Dispose, tooltipListener);
		table.addListener (SWT.KeyDown, tooltipListener);
		table.addListener (SWT.MouseMove, tooltipListener);
		table.addListener (SWT.MouseHover, tooltipListener);
		
		comparator = new DynamicTableComparator();
		setComparator(comparator);
		
		setContentProvider(new ViewContentProvider());
		setLabelProvider(new ViewLabelProvider());

		loadColumnLayout();
		
		
	}
	
	
	public Menu getBodyMenu() {
		return bodyMenu;
	}

	public void setBodyMenu(Menu bodyMenu) {
		this.bodyMenu = bodyMenu;
	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				getTable().setSortDirection(dir);
				refresh();
			}
		};
		return selectionAdapter;
	}

	
	@Override
	protected void handleDispose(DisposeEvent event) {
		this.saveColumnLayout();
		super.handleDispose(event);
	}


	
	private void updateColumnLayout() {
		List<ColumnProperty> columnProperty = new ArrayList<ColumnProperty>();
		Table table = getTable();
		int[] seq = table.getColumnOrder();
		for(int i: seq) {
			columnProperty.add(new ColumnProperty(table.getColumn(i).getText(), table.getColumn(i).getWidth()));
		}
		columnProperties.put(columnLayoutKey, columnProperty);
	}
	
	private void saveColumnLayout() {
		if(tableLayoutFile == null)
			return;
		
		File file = new File(tableLayoutFile);
		try {
			log.info("Saving table layout: " + tableLayoutFile);
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			xstream.toXML(columnProperties, os);
			os.close();
			log.info("Table layout saved: " + tableLayoutFile);

		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadColumnLayout() {
		if(tableLayoutFile == null)
			return;
		File file = new File(tableLayoutFile);
		if (file.exists()) {
			ClassLoader save = xstream.getClassLoader();
			ClassLoader cl = ColumnProperty.class.getClassLoader();
			if (cl != null)
				xstream.setClassLoader(cl);

			columnProperties = (Map<String, List<ColumnProperty>>)xstream.fromXML(file);
			xstream.setClassLoader(save);
			log.info("Table layout file loaded: " + tableLayoutFile);
		}
		
	}
	
	private void createMenuItem(final Menu parent, final TableColumn column) {
		final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
		itemName.setText(column.getText());
		itemName.setSelection(column.getWidth() == 0?false:true);
		itemName.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (itemName.getSelection()) {
					column.setWidth(150);
					column.setResizable(true);
				} else {
					column.setWidth(0);
					column.setResizable(false);
				}
			}
		});

	}
	
	public List<ColumnProperty> getDynamicColumnProperties() {
		return this.columnProperties.get(columnLayoutKey);
	}
	
	public void setSmartColumnProperties(String columnLayoutKey, List<ColumnProperty> columnProperty) {
		List<ColumnProperty> existingProperties = columnProperties.get(columnLayoutKey);
		List<ColumnProperty> newColumnProperty;
		if (null == existingProperties) {
			newColumnProperty = columnProperty;
		} else { //reconcile what we received from server
			newColumnProperty = new ArrayList<ColumnProperty>();
			// firstly add the ones both appear in the two lists
			for(ColumnProperty prop1: existingProperties) { 
				for (ColumnProperty prop2: columnProperty) {
					if (prop1.getTitle().equals(prop2.getTitle())) {
						newColumnProperty.add(prop1);
						break;
					}
				}
			}
			
			// then add the ones appear in the order but not in the existing list
			for(ColumnProperty prop1: columnProperty) { 
				boolean found = false;
				for (ColumnProperty prop2: existingProperties) {
					if (prop1.getTitle().equals(prop2.getTitle())) {
						found = true;
						break;
					}
				}
				if (!found)
					newColumnProperty.add(prop1);
			}
		}
		setColumnProperties(columnLayoutKey, newColumnProperty);
	}
	
	private ControlListener columnChangeListener = new ControlListener() {

		@Override
		public void controlMoved(ControlEvent e) {
			updateColumnLayout();
		}

		@Override
		public void controlResized(ControlEvent e) {
			updateColumnLayout();
		}
		
	};
	
	protected void setColumnProperties(String columnLayoutKey, List<ColumnProperty> columnProperty) {
		this.columnLayoutKey = columnLayoutKey;
		this.columnProperties.put(columnLayoutKey, columnProperty);
		Table table = DynamicTableViewer.this.getTable();
		
		for(TableColumn column: table.getColumns()) {
			column.removeControlListener(columnChangeListener);
		}
		
		setInput(null);
		table.setHeaderVisible(true);
		table.setRedraw(false); 
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		
		if(headerMenu != null)
			headerMenu.dispose();
		headerMenu = new Menu(this.parent.getShell(), SWT.POP_UP);

		for (int i=0; i<columnProperty.size(); i++) {
			ColumnProperty prop = columnProperty.get(i);
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(prop.getTitle());
			column.setWidth(prop.getWidth());
			column.setResizable(true);
			column.setMoveable(true);
			createMenuItem(headerMenu, column);
			column.addSelectionListener(getSelectionAdapter(column, i));
		}

		table.setRedraw(true); 
		refresh();
		
		
		for(TableColumn column: table.getColumns()) {
			column.addControlListener(columnChangeListener);
		}
	}

	public String getTableLayoutFile() {
		return tableLayoutFile;
	}


}
