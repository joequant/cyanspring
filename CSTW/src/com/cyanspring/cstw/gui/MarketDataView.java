package com.cyanspring.cstw.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.cstw.business.Business;
import com.cyanspring.cstw.event.InstrumentSelectionEvent;
import com.cyanspring.cstw.event.MultiInstrumentStrategySelectionEvent;
import com.cyanspring.cstw.event.ObjectSelectionEvent;
import com.cyanspring.cstw.event.SingleOrderStrategySelectionEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;

public class MarketDataView extends ViewPart implements IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(MarketDataView.class);
	
	public static final String ID = "com.cyanspring.cstw.gui.MarketDataView"; //$NON-NLS-1$
	private static final DecimalFormat priceFormat = new DecimalFormat("#0.####");
	private Composite topComposite;
	private Table table;
	private TableViewer tableViewer;
	private String symbol = "";
	private CCombo cbSymbol;
	private Label lbBid;
	private Label lbBidVol;
	private Label lbAsk;
	private Label lbAskVol;
	private Label lbLast;
	private Label lbLastVol;
	private Label lbOpen;
	private Label lbClose;
	private Label lbHigh;
	private Label lbLow;
	private Map<String, String> symbolServer = new HashMap<String, String>();
	private Label lblMktVol;
	private Label lbMktVol;
	private Label lblNewLabel_1;
	private Label lbChange;
	private Label lbChangePercent;
	
	public MarketDataView() {
	}

	class DepthItem {
		Double bid, ask, bidVol, askVol;
	}
	
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if(parent instanceof Quote) {
				Quote quote = (Quote)parent;
				List<DepthItem> list = new ArrayList<DepthItem>();
				for(int i=0; i<Math.max(quote.getBids().size(), quote.getAsks().size()); i++) {
					DepthItem item = new DepthItem();
					if(i<quote.getBids().size()) {
						QtyPrice qp = quote.getBids().get(i);
						item.bid = qp.price;
						item.bidVol = qp.quantity;
					}
					
					if(i<quote.getAsks().size()) {
						QtyPrice qp = quote.getAsks().get(i);
						item.ask = qp.price;
						item.askVol = qp.quantity;
					}
					list.add(item);
				}
				return list.toArray();
			}
			return null;
		}
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		topComposite = new Composite(composite, SWT.NONE);
		GridLayout gl_topComposite = new GridLayout(3, true);
		gl_topComposite.marginLeft = 10;
		topComposite.setLayout(gl_topComposite);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		cbSymbol = new CCombo(topComposite, SWT.BORDER);
		cbSymbol.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		cbSymbol.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	String symbol = cbSymbol.getText();
	        	if(!symbol.equals(MarketDataView.this.symbol)) {
	        		String server = symbolServer.get(symbol);
	        		if(null != server)
	        			subscribeMD(symbol, server);
	        	}
	        }
	      });
	   
		cbSymbol.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.character == SWT.CR) {
    	        	String symbol = cbSymbol.getText();
	        		String server = symbolServer.get(symbol);
	        		if(null == server) {
	        			ArrayList<String> servers = Business.getInstance().getOrderManager().getServers();
	        			if(servers.size() == 0) {
	        				log.warn("No server is available for market data request");
	        				return;
	        			} else {
	        				server = servers.get(0);
	        				symbolServer.put(symbol, server);
	        			}
	        		}
        			subscribeMD(symbol, server);
        			addSymbol(symbol);
				}
				
			}
			
		});
		
		Label lblNewLabel = new Label(topComposite, SWT.NONE);
		
		Label lblNewLabel_2 = new Label(topComposite, SWT.NONE);
		
		
		Label lblBidvolume = new Label(topComposite, SWT.NONE);
		lblBidvolume.setText("Bid/Vol");
		
		lbBid = new Label(topComposite, SWT.NONE);
		
		lbBidVol = new Label(topComposite, SWT.NONE);
		
		Label lblNewLabel_5 = new Label(topComposite, SWT.NONE);
		lblNewLabel_5.setText("Ask/Vol");
		
		lbAsk = new Label(topComposite, SWT.NONE);
		
		lbAskVol = new Label(topComposite, SWT.NONE);
		
		Label lblLastvolume = new Label(topComposite, SWT.NONE);
		lblLastvolume.setText("Last/Vol");
		
		lbLast = new Label(topComposite, SWT.NONE);
		
		lbLastVol = new Label(topComposite, SWT.NONE);
		
		lblMktVol = new Label(topComposite, SWT.NONE);
		lblMktVol.setText("Mkt Vol");
		
		lbMktVol = new Label(topComposite, SWT.NONE);
		new Label(topComposite, SWT.NONE);
		
		Label lblOpenclose = new Label(topComposite, SWT.NONE);
		lblOpenclose.setText("Open/Close");
		
		lbOpen = new Label(topComposite, SWT.NONE);
		
		lbClose = new Label(topComposite, SWT.NONE);
		
		Label lblHighlow = new Label(topComposite, SWT.NONE);
		lblHighlow.setText("High/Low");
		
		lbHigh = new Label(topComposite, SWT.NONE);
		
		lbLow = new Label(topComposite, SWT.NONE);
		
		lblNewLabel_1 = new Label(topComposite, SWT.NONE);
		lblNewLabel_1.setText("Change/%");
		
		lbChange = new Label(topComposite, SWT.NONE);
		
		lbChangePercent = new Label(topComposite, SWT.NONE);
		
		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		createActions();
		initializeToolBar();
		initializeMenu();
		
		createColumns();

		tableViewer.setContentProvider(new ViewContentProvider());
		Business.getInstance().getEventManager().subscribe(SingleOrderStrategySelectionEvent.class, this);
		Business.getInstance().getEventManager().subscribe(QuoteEvent.class, this);
		Business.getInstance().getEventManager().subscribe(QuoteReplyEvent.class, this);
		Business.getInstance().getEventManager().subscribe(InstrumentSelectionEvent.class, this);

	}

	private void createColumns() {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText("Volume");
		column.setWidth(table.getBorderWidth()/4);
		column.setResizable(true);
		column.setMoveable(false);
		
		TableViewerColumn tvColumn = new TableViewerColumn(tableViewer, column);
		tvColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof DepthItem)
					try {
						Object value = ((DepthItem)obj).bidVol;
						if (value != null)
							return BeanHolder.getInstance().getDataConverter().toString("Qty", value);
					} catch (DataConvertException e) {
						log.error(e.getMessage(), e);
					}
				return null;
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});

		column = new TableColumn(table, SWT.NONE);
		column.setText("Bid");
		column.setWidth(table.getBorderWidth()/4);
		column.setResizable(true);
		column.setMoveable(false);
		
		tvColumn = new TableViewerColumn(tableViewer, column);
		tvColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof DepthItem) {
					Object value = ((DepthItem)obj).bid;
					if (value != null)
						return value.toString();
				}
				return null;
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});

		column = new TableColumn(table, SWT.NONE);
		column.setText("Ask");
		column.setWidth(table.getBorderWidth()/4);
		column.setResizable(true);
		column.setMoveable(false);
		
		tvColumn = new TableViewerColumn(tableViewer, column);
		tvColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof DepthItem) {
					Object value = ((DepthItem)obj).ask;
					if (value != null)
						return value.toString();
				}
				return null;
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});

		column = new TableColumn(table, SWT.NONE);
		column.setText("Volume");
		column.setWidth(table.getBorderWidth()/4);
		column.setResizable(true);
		column.setMoveable(false);

		tvColumn = new TableViewerColumn(tableViewer, column);
		tvColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof DepthItem)
					try {
						Object value = ((DepthItem)obj).askVol;
						if (value != null)
							return BeanHolder.getInstance().getDataConverter().toString("Qty", value);
					} catch (DataConvertException e) {
						log.error(e.getMessage(), e);
					}
				return null;
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});

		table.setHeaderVisible(true);
		table.setRedraw(true); 
		tableViewer.refresh();

		// set to same size
		table.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				for(int i=0; i<table.getColumnCount(); i++) {
					table.getColumn(i).setWidth(table.getClientArea().width/table.getColumnCount());
				}
				table.removeListener(SWT.Paint, this);
			}
		});

	}

	public void dispose() {
		super.dispose();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	@SuppressWarnings("unused")
	private void initializeToolBar() {
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	@SuppressWarnings("unused")
	private void initializeMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
	
	private void subscribeMD(String symbol, String server) {
		if(!this.symbol.equals(symbol))
			Business.getInstance().getEventManager().unsubscribe(QuoteEvent.class, this.symbol, this);
			
		this.symbol = symbol;
		QuoteSubEvent request = new QuoteSubEvent(symbol, server, symbol);
		try {
			Business.getInstance().getEventManager().subscribe(QuoteEvent.class, this.symbol, this);
			Business.getInstance().getEventManager().sendRemoteEvent(request);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	private String blankZero(double value) {
		return PriceUtils.isZero(value)?"": ""+value; 
	}
	
	private void showQuote(final Quote quote) {
		
		tableViewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				cbSymbol.setText(quote.getSymbol());

				lbBid.setText(blankZero(quote.getBid()));
				try {
					lbBidVol.setText(BeanHolder.getInstance().getDataConverter().toString("Qty", quote.getBidVol()));
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}
				
				lbAsk.setText(blankZero(quote.getAsk()));
				try {
					lbAskVol.setText(BeanHolder.getInstance().getDataConverter().toString("Qty", quote.getAskVol()));
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}
				lbLast.setText(blankZero(quote.getLast()));
				try {
					lbLastVol.setText(BeanHolder.getInstance().getDataConverter().toString("Qty", quote.getLastVol()));
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}

				try {
					lbMktVol.setText(BeanHolder.getInstance().getDataConverter().toString("Qty", quote.getTotalVolume()));
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}

				lbHigh.setText(blankZero(quote.getHigh()));
				lbLow.setText(blankZero(quote.getLow()));
				lbOpen.setText(blankZero(quote.getOpen()));
				lbClose.setText(blankZero(quote.getClose()));
				
				if(!PriceUtils.isZero(quote.getClose()) && !PriceUtils.isZero(quote.getLast())) {
					String change = priceFormat.format(quote.getLast()-quote.getClose());
					String changePercent = priceFormat.format((quote.getLast()-quote.getClose()) * 100/quote.getClose()) + "%";
					lbChange.setText(change);
					lbChangePercent.setText(changePercent);
				} else {
					lbChange.setText("");
					lbChangePercent.setText("");
				}
				
				topComposite.layout();
				
				tableViewer.setInput(quote);
				tableViewer.refresh();
			}
		});
	}

	private void addSymbol(String symbol) {
		boolean found = false;
		for(String str: cbSymbol.getItems()) {
			if(str.equals(symbol)) {
				found = true;
				break;
			}
		}
		if(!found)
			cbSymbol.add(symbol, 0);
	}
	
	@Override
	public void onEvent(AsyncEvent e) {
		if (e instanceof ObjectSelectionEvent) {
			ObjectSelectionEvent event = (ObjectSelectionEvent)e;
			if (e instanceof MultiInstrumentStrategySelectionEvent)
				return;

			Map<String, Object> map = event.getData();
			String server = (String)map.get(OrderField.SERVER_ID.value());
			String symbol = (String)map.get(OrderField.SYMBOL.value());
			symbolServer.put(symbol, server);
			subscribeMD(symbol, server);
			addSymbol(symbol);

		} else if (e instanceof QuoteReplyEvent) {
			showQuote(((QuoteReplyEvent)e).getQuote());
		} else if (e instanceof QuoteEvent) {
			showQuote(((QuoteEvent)e).getQuote());
		} else {
			log.error("Unhandled event: " + e.getClass());
		}
	}

}
