package webcurve.ui;

import java.util.Vector;

import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import webcurve.client.ClientOrder;

/**
 * @author dennis_d_chen@yahoo.com
 */
public class TreeTableModel extends AbstractTableModel implements TreeModel {
	private static final long serialVersionUID = 7735193062259869579L;
	static String[] columnNames = {"OrderId", "Stock", "Side", "Quantity", 
									"Type", "Price", "CumQty", "Status"};
    @SuppressWarnings("rawtypes")
	static protected Class[]  columnClass = {TreeTableModel.class, String.class, String.class, String.class, 
    										String.class, String.class, String.class, String.class};
	
    private ClientOrder root;
	@SuppressWarnings("unused")
	private Vector<ClientOrder> orders;
	JTree tree;
    public JTree getTree() {
		return tree;
	}

	public void setTree(JTree tree) {
		this.tree = tree;
		tree.addTreeExpansionListener(new TreeExpansionListener() {
		    // Don't use fireTableRowsInserted() here; 
		    // the selection model would get  updated twice. 
		    public void treeExpanded(TreeExpansionEvent event) {  
		      fireTableDataChanged(); 
		    }
	        public void treeCollapsed(TreeExpansionEvent event) {  
		      fireTableDataChanged(); 
		    }
		});		
	}
	
	public TreeTableModel(Vector<ClientOrder> orders)
	{
		this.orders = orders;
		root = new ClientOrder();
		root.setClientOrderID("");
		root.setChildOrders(orders);
	}
	
	@Override
	public Class<?> getColumnClass(int col)
	{
		return columnClass[col];
	}

	@Override
	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	
//	@Override
	public Object getChild(Object parent, int index) {
		return ((ClientOrder)parent).getChildOrders().get(index);
	}

//	@Override
	public int getChildCount(Object parent) {
		return ((ClientOrder)parent).getChildOrders().size();
	}

//	@Override
	public int getIndexOfChild(Object parent, Object child) {
		ClientOrder order = (ClientOrder)parent;
		for (int i=0; i<order.getChildOrders().size(); i++)
		{
			if (child == order.getChildOrders().get(i))
				return i;
		}
		return -1;
	}

//	@Override
	public Object getRoot() {
		return root;
	}

//	@Override
	public boolean isLeaf(Object node) {
		return ((ClientOrder)node).getChildOrders().size()==0;
	}

    protected EventListenerList listenerList = new EventListenerList();
//	@Override
	public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
	}

//	@Override
	public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
	}

//	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {

	}

//	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

//	@Override
	public int getRowCount() {
		return tree.getRowCount();
	}
//	@Override
	public Object getValueAt(int row, int col) {
		TreePath path = tree.getPathForRow(row);
		ClientOrder order = (ClientOrder)path.getLastPathComponent();     
		switch (col)
		{
		case 0: return order.getClientOrderID();
		case 1: return order.getCode();
		case 2: return ClientOrder.sideToString(order.getSide(), order.isShortSell());
		case 3: return order.getQuantity();
		case 4: return ClientOrder.typeToString(order.getType());
		case 5: return order.getPrice();
		case 6: return order.getExecutedQty();
		case 7: return order.getStatus().toString();
		}
		
		System.out.println("return nothing!!!");
		return null;
	}
	
	public Object getOrderAt(int row)
	{
		TreePath path = tree.getPathForRow(row);
		return path.getLastPathComponent();     
	}
	
    public boolean isCellEditable(int row, int column) {
        return column == 0; 
   }

}
