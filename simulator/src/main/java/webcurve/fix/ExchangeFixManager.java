package webcurve.fix;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DataDictionaryProvider;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.MessageUtils;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.field.ApplVerID;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.NoMDEntries;
import quickfix.field.NoRelatedSym;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import webcurve.common.ArrayListMap;
import webcurve.common.BaseOrder;
import webcurve.common.ExchangeListener;
import webcurve.common.Order;
import webcurve.common.Trade;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;
import webcurve.util.FixUtil;
import webcurve.util.PriceUtils;
/**
 * @author dennis_d_chen@yahoo.com
 */
public class ExchangeFixManager extends MessageCracker 
					implements Application
{

	
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private Exchange exchange;
    private Hashtable<String, SessionID> sessions = new Hashtable<String, SessionID>();
    private Hashtable<String, Order> orders = new Hashtable<String, Order>();
    
    public ExchangeFixManager(Exchange exchange) throws ConfigError, FieldConvertError {
    	this.exchange = exchange;
    	exchange.orderListenerKeeper.addExchangeListener(exchangeEventListener);
    	exchange.orderBookListenerKeeper.addExchangeListener(orderBookEventListener);
    	exchange.tradeListenerKeeper.addExchangeListener(tradeEventListener);
    }

    public void onCreate(SessionID sessionID) {
    	log.debug("onCreate: " + sessionID.toString());
    	sessions.put(sessionID.getTargetCompID(), sessionID);
    }

    public void onLogon(SessionID sessionID) {
    	log.debug("logon: " + sessionID.toString());
    	sessions.put(sessionID.getTargetCompID(), sessionID);
    }

    public void onLogout(SessionID sessionID) {
    	log.debug("logout: " + sessionID.toString());
    	sessions.remove(sessionID.getTargetCompID());
   }

    public void toAdmin(quickfix.Message message, SessionID sessionID) {
    }

    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
    }

    public void fromAdmin(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, RejectLogon {
     }

    public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionID);
    }


    private void sendMessage(SessionID sessionID, Message message) {
        try {
            Session session = Session.lookupSession(sessionID);
            if (session == null) {
                throw new SessionNotFound(sessionID.toString());
            }
            
            if (!session.isEnabled())
            {
            	log.info("Client not connected, discard: " + message);
            	return;
            }
            
            DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
            if (dataDictionaryProvider != null) {
                try {
//                  dataDictionaryProvider.getApplicationDataDictionary(
//                  getApplVerID(session, message), null).validate(message, true);
                  dataDictionaryProvider.getApplicationDataDictionary(
                  getApplVerID(session, message)).validate(message, true);
                } catch (Exception e) {
                    log.error("Outgoing message failed validation: " + message);
                    LogUtil.logThrowable(sessionID, e.getMessage(), e);
                    return;
                }
            }
            
            synchronized (session) {
            	session.send(message);
            }
        } catch (SessionNotFound e) {
            log.error(e.getMessage(), e);
        }
    }

    private ApplVerID getApplVerID(Session session, Message message) {
        String beginString = session.getSessionID().getBeginString();
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            return new ApplVerID(ApplVerID.FIX50);
        } else {
            return MessageUtils.toApplVerID(beginString);
        }
    }

	private void sendNewOrderReject(Message message, SessionID sessionID, String error) {
		try {
			char side = message.getField(new Side()).getValue();
			String symbol = message.getField(new Symbol()).getValue();
			
			quickfix.fix42.ExecutionReport er = new quickfix.fix42.ExecutionReport(
				new OrderID("NONE"), 
				new ExecID(""+exchange.getNextTranID()),
				new ExecTransType(ExecTransType.NEW), 
				new ExecType(ExecType.REJECTED), 
				new OrdStatus(OrdStatus.REJECTED), 
				new Symbol(symbol), 
				new Side(side),
				new LeavesQty(), 
				new CumQty(), 
				new AvgPx() );

			if(message.isSetField(new ClOrdID())) {
				er.setField(message.getField(new ClOrdID()));
			}
			er.setField(new Text(error));
			
			Session.sendToTarget(er, sessionID);
		} catch (FieldNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (SessionNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

    public void onMessage(quickfix.fix42.NewOrderSingle message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue 
    {
        try 
        {
	    	Side side = message.getSide();
	    	Order.SIDE orderSide;
	    	if (side.getValue() == Side.BUY)
	    		orderSide = Order.SIDE.BID;
	    	else if (side.getValue() == Side.SELL || side.getValue() == Side.SELL_SHORT)
	    		orderSide = Order.SIDE.ASK;
	    	else
	    	{
	    		sendNewOrderReject(message, sessionID, "Order SIDE not supported");
	    		log.warn("Order SIDE not supported: " + message);
	    		return;
	    	}
	    	
	    	OrdType type = message.getOrdType();
	    	TimeInForce tif = null;
	    	if (message.isSetField(TimeInForce.FIELD))
	    		tif = message.getTimeInForce();
	    	Order.TYPE orderType;
	    	if(type.getValue() == OrdType.LIMIT)
	    	{
	    		if (tif != null && tif.getValue() == TimeInForce.IMMEDIATE_OR_CANCEL)
	    			orderType = Order.TYPE.FAK;
	    		else
	    			orderType = Order.TYPE.LIMIT;
	    	}
	    	else if (type.getValue() == OrdType.MARKET )
	    		orderType = Order.TYPE.MARKET;
	    	else
	    	{
	    		sendNewOrderReject(message, sessionID, "Order TYPE not supported");
	    		log.warn("Order TYPE not supported: " + message);
	    		return;
	    	}
	    	
	    	if( null == exchange.enterOrder(message.getSymbol().getValue(), orderType, 
	    						orderSide, message.getInt(38), // 38 order quantity
	    						message.getPrice().getValue(), sessionID.getTargetCompID(), 
	    						message.getClOrdID().getValue())) {
	    		sendNewOrderReject(message, sessionID, "Enter order failed");
	    		log.error("Enter order failed: " + message);
	    	}
    
        } catch (RuntimeException e) {
                LogUtil.logThrowable(sessionID, e.getMessage(), e);
        }
    	
    }

    private char orderStatusToFix(Order order) {
    	Order.STATUS status = order.getStatus();
    	// NONE, NEW, FILLING, AMENDED, CANCELLED, REJECTED, DONE 
    	switch(status) {
    		case NEW:
    			return quickfix.field.OrdStatus.NEW;
    		case FILLING:
    			return quickfix.field.OrdStatus.PARTIALLY_FILLED;
    		case AMENDED:
    			return quickfix.field.OrdStatus.REPLACED;
    		case CANCELLED:
    			return quickfix.field.OrdStatus.CANCELED;
    		case  REJECTED:
    			return quickfix.field.OrdStatus.REJECTED;
    		case DONE:
				return quickfix.field.OrdStatus.FILLED;
    	}
    	log.error("Unable to map to FIX OrdStatus");
    	return quickfix.field.OrdStatus.EXPIRED;
    }
    

    public void onMessage(quickfix.fix42.OrderCancelReplaceRequest message, SessionID sessionID)
    throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		
		Order order = orders.get(sessionID.getTargetCompID() + "-" + message.getOrigClOrdID().getValue());
			
		//construct cancelreject here
		quickfix.fix42.OrderCancelReject reject = new quickfix.fix42.OrderCancelReject(
	    		new quickfix.field.OrderID("NONE"),
	            message.getClOrdID(), 
	            message.getOrigClOrdID(),
	            new quickfix.field.OrdStatus(OrdStatus.REJECTED),
	            new quickfix.field.CxlRejResponseTo(quickfix.field.CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST));

		if (null == order)	{
			reject.set(new quickfix.field.Text("Cant find this order(OrigClOrdId): " + message.getOrigClOrdID()));
			sendMessage(sessionID, reject);
			return;
		} else {
			reject.set(new quickfix.field.OrdStatus(orderStatusToFix(order)));
		}
   	
		BaseOrder.SIDE side;
		try {
			side = FixUtil.fromFixOrderSide(message.getSide().getValue());
		} catch (Exception e) {
			reject.set(new quickfix.field.Text("Side not supported"));
			sendMessage(sessionID, reject);
			return;
		}
    	String code = message.getSymbol().getValue();
    	
    	int quantity = order.getQuantity();  	
    	// by FIX protocol, quantity change is relative to original order quantity
    	if (message.isSetOrderQty())
    	{
    		quantity = message.getInt(38); // order quantity
    		if (quantity == 0)
    		{
    			reject.set(new quickfix.field.Text("cant amend quantity to 0"));
    			sendMessage(sessionID, reject);
    			return;
    		}
    		
    		if (quantity> order.getOriginalQuantity())
    		{
    			reject.set(new quickfix.field.Text("can only amend down quantity"));
    			sendMessage(sessionID, reject);
    			return;
    		}
    		
    		int filledQuantity = order.getOriginalQuantity() - order.getQuantity();
    		if (quantity <= order.getOriginalQuantity() - order.getQuantity())
    		{
    			reject.set(new quickfix.field.Text("Quantity is less than filled quantity" + filledQuantity));
    			sendMessage(sessionID, reject);
    			return;
    		}
    		
    		quantity = order.getQuantity()-(order.getOriginalQuantity()-quantity);
    	}   	
    	
    	double price = 0;
    	if (message.isSetPrice())
    		price = message.getPrice().getValue();
    	log.debug("amending order: " + message.getOrigClOrdID().getValue() + ", " + quantity + ", " + price );

    	if ( (!PriceUtils.Equal(price, 0.0)) && PriceUtils.Equal(price, order.getPrice()) && quantity == order.getQuantity()) {
        	log.warn("Price and quantity is the same as before nothing changed, just ack: " + message.getOrigClOrdID().getValue() + ", " + quantity + ", " + price );
	        quickfix.fix42.ExecutionReport er;
			try {
				er = createReportFromOrder(order, new OrdStatus(OrdStatus.REPLACED), new ExecType(ExecType.REPLACE));
		        sendMessage(sessionID, er);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
    	}
		if (!exchange.amendOrder(order.getOrderID(), code, side, 
				quantity, price, message.getClOrdID().getValue()))
		{
			log.error("Can't amend this order: " + message.getOrigClOrdID().getValue());
			reject.set(new quickfix.field.Text("Cant amend this order:"+order.getOrderID()));
			sendMessage(sessionID, reject);
		}
    	
    }

    public void onMessage(quickfix.fix42.OrderCancelRequest message, SessionID sessionID)
    throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
    	
		Order order = orders.get(sessionID.getTargetCompID() + "-" + message.getOrigClOrdID().getValue());
		
		quickfix.fix42.OrderCancelReject reject = new quickfix.fix42.OrderCancelReject(
	    		new quickfix.field.OrderID("NONE"),
	            message.getClOrdID(), 
	            message.getOrigClOrdID(),
	            new quickfix.field.OrdStatus(quickfix.field.OrdStatus.REJECTED),
	            new quickfix.field.CxlRejResponseTo(quickfix.field.CxlRejResponseTo.ORDER_CANCEL_REQUEST));

		if(null == order) {
			reject.set(new quickfix.field.Text("Cant find this order: " + message.getOrigClOrdID()));
			sendMessage(sessionID, reject);
			log.warn("OrderCancelRequest order not found: " + message.getOrigClOrdID().getValue());
			return;
		} else {
			reject.set(new quickfix.field.OrdStatus(orderStatusToFix(order)));
		}

		BaseOrder.SIDE side;
    	try {
			side = FixUtil.fromFixOrderSide(message.getSide().getValue());
		} catch (Exception e) {
			reject.set(new quickfix.field.Text("Side not supported"));
			sendMessage(sessionID, reject);
			return;
		}
    	String code = message.getSymbol().getValue();
    	log.debug("canceling order: " + message.getOrigClOrdID().getValue() + ", " + code + ", " + side );
		
		if (order == null || !exchange.cancelOrder(order.getOrderID(), code, side, message.getClOrdID().getValue())) {
			reject.set(new quickfix.field.Text("Exchange cant find this order: " + message.getOrigClOrdID()));
			log.warn("Exchange cant find this order: " + message.getOrigClOrdID());
			sendMessage(sessionID, reject);
		}

    }

    private void sendTradeUpdate(String mdReqID, SessionID sessionID, Trade trade)
    {
		quickfix.fix42.MarketDataSnapshotFullRefresh mdfr = 
			new quickfix.fix42.MarketDataSnapshotFullRefresh();
		
		
		try {
			if (null != mdReqID)
				mdfr.set(new MDReqID(mdReqID));
			
			mdfr.set(new Symbol(trade.getAskOrder().getCode()));
			
			//quickfix.field.
			quickfix.fix42.MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = 
				new quickfix.fix42.MarketDataSnapshotFullRefresh.NoMDEntries();

			noMDEntries.set(new MDEntryType(MDEntryType.TRADE));
			noMDEntries.set(new MDEntryPx(trade.getPrice()));
			noMDEntries.set(new MDEntrySize(trade.getQuantity()));
			
			mdfr.addGroup(noMDEntries);
			sendMessage(sessionID, mdfr);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}    	
    }

	ExchangeListener<OrderBook> orderBookEventListener = new ExchangeListener<OrderBook>()
	{

		@Override
		public void onChangeEvent(OrderBook book) {
			ArrayList<SessionID> sessionIDs = mdSubscription.getSessions(book.getCode());
			if (sessionIDs == null)
				return;
			for (SessionID sessionID: sessionIDs)
			{
				sendOrderBookUpdate(null, sessionID, book);
			}
			
		}
		
	};

	ExchangeListener<Trade> tradeEventListener = new ExchangeListener<Trade>()
	{

		@Override
		public void onChangeEvent(Trade trade) {
			ArrayList<SessionID> sessionIDs = mdSubscription.getSessions(trade.getAskOrder().getCode());
			if ( null == sessionIDs )
				return;
			for (SessionID sessionID: sessionIDs)
			{
				sendTradeUpdate(null, sessionID, trade);
			}
			
		}
		
	};
	
	ExchangeListener<Order> exchangeEventListener = new ExchangeListener<Order>()
	{
		//@Override
		public void onChangeEvent(Order order) {
			try
			{
				SessionID sessionID = sessions.get(order.getBroker());
				if (null == sessionID)
					return;
				
				ExecType execType;
				OrdStatus ordStatus;
				if (order.getStatus() == Order.STATUS.NEW)
				{
					execType = new ExecType(ExecType.NEW);
					ordStatus = new OrdStatus(OrdStatus.NEW);
					orders.put(order.getBroker() + "-" + order.getClOrderId(), order);
				}
				else if (order.getStatus() == Order.STATUS.AMENDED)
				{
					execType = new ExecType(ExecType.REPLACE);
					ordStatus = new OrdStatus(OrdStatus.REPLACED);
					orders.remove(order.getBroker() + "-" + order.getOrigClOrderId());
					orders.put(order.getBroker() + "-" + order.getClOrderId(), order);
				}
				else if (order.getStatus() == Order.STATUS.FILLING)
				{
					if (order.getQuantity() == 0)
					{
						orders.remove(order.getBroker() + "-" + order.getClOrderId());
						execType = new ExecType(ExecType.FILL);
						ordStatus = new OrdStatus(OrdStatus.FILLED);
					}
					else
					{
						execType = new ExecType(ExecType.PARTIAL_FILL);
						ordStatus = new OrdStatus(OrdStatus.PARTIALLY_FILLED);				
						orders.put(order.getBroker() + "-" + order.getClOrderId(), order);
					}
				}
				else if (order.getStatus() == Order.STATUS.CANCELLED)
				{
					orders.remove(order.getBroker() + "-" + order.getClOrderId());
					execType = new ExecType(ExecType.CANCELED);
					ordStatus = new OrdStatus(OrdStatus.CANCELED);
				}
				else if (order.getStatus() == Order.STATUS.DONE)
				{
					orders.remove(order.getBroker() + "-" + order.getClOrderId());
					execType = new ExecType(ExecType.FILL);
					ordStatus = new OrdStatus(OrdStatus.FILLED);
				}
				else if (order.getStatus() == Order.STATUS.REJECTED)
				{
					orders.remove(order.getBroker() + "-" + order.getClOrderId());
					execType = new ExecType(ExecType.REJECTED);
					ordStatus = new OrdStatus(OrdStatus.REJECTED);
				}
				else
				{
					log.error("Unknow order update type" + order.getStatus());
					return;
				}
				
		        quickfix.fix42.ExecutionReport er = createReportFromOrder(order, ordStatus, execType);
		        	        	
		        sendMessage(sessionID, er);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				log.error(e.toString());
			}
		}
	};

    class MarketDataSubscription 
    {
        ArrayListMap<String, SessionID> symbolSessionMap = new ArrayListMap<String, SessionID>();
        ArrayListMap<String, String> requestSymbolMap = new ArrayListMap<String, String>();
        Map<String, SessionID> requestSessionMap = Collections.synchronizedMap(new HashMap<String, SessionID>());
        
    	void add(String symbol, String requestId, SessionID sessionID)
    	{
    		symbolSessionMap.add(symbol, sessionID);
    		requestSymbolMap.add(requestId, symbol);
    		requestSessionMap.put(requestId, sessionID);
    	}
    	
    	public ArrayList<SessionID> getSessions(String symbol)
    	{
    		return symbolSessionMap.getAll(symbol);
    	}

    	
    	void remove(String requestID)
    	{
    		SessionID sessionID = requestSessionMap.remove(requestID);
    		ArrayList<String> symbols = requestSymbolMap.remove(requestID);
    		for ( String symbol: symbols)
    		{
    			symbolSessionMap.remove(symbol, sessionID);
    		}
    	}
    }
    
    MarketDataSubscription mdSubscription = new MarketDataSubscription();
    
    private void sendOrderBookUpdate(String mdReqID, SessionID sessionID, OrderBook book)
    {
    	List<Order> bidOrders = book.getSumBidOrders();
    	List<Order> askOrders = book.getSumAskOrders();
    	
		quickfix.fix42.MarketDataSnapshotFullRefresh mdfr = 
			new quickfix.fix42.MarketDataSnapshotFullRefresh();
		
		
		try {
			if (null != mdReqID)
				mdfr.set(new MDReqID(mdReqID));
			
			mdfr.set(new Symbol(book.getCode()));
			
			//quickfix.field.
			quickfix.fix42.MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = 
				new quickfix.fix42.MarketDataSnapshotFullRefresh.NoMDEntries();

			mdfr.set(new NoMDEntries());
			for (Order order: bidOrders)
			{
    			noMDEntries.set(new MDEntryType(MDEntryType.BID));
    			noMDEntries.set(new MDEntryPx(order.getPrice()));
    			noMDEntries.set(new MDEntrySize(order.getQuantity()));
    			mdfr.addGroup(noMDEntries);
			}
			
			for (Order order: askOrders)
			{
    			noMDEntries.set(new MDEntryType(MDEntryType.OFFER));
    			noMDEntries.set(new MDEntryPx(order.getPrice()));
    			noMDEntries.set(new MDEntrySize(order.getQuantity()));
    			mdfr.addGroup(noMDEntries);
			}
			
//			noMDEntries.set(new MDEntryType(MDEntryType.TRADE));
//			noMDEntries.set(new MDEntryPx(book.getLast()));
//			noMDEntries.set(new MDEntrySize(book.getLastVol()));
			
//			mdfr.addGroup(noMDEntries);
			
			sendMessage(sessionID, mdfr);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}    	
    }

    public void onMessage(quickfix.fix42.MarketDataRequest message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue 
    {
    	
    	MDReqID mdReqID = message.getMDReqID();

    	quickfix.field.NoRelatedSym noRelatedSym = message.get(new NoRelatedSym());
    	int symbolCount = noRelatedSym.getValue();

    	//boolean aggregated = true;
//    	if (message.isSet(new AggregatedBook()))
//    	{
//    		AggregatedBook aggregatedBook = message.getAggregatedBook();
//    		if (aggregatedBook.getValue())
//    			aggregated = true;
//    		else
//    			aggregated = false;
//    	}
		quickfix.fix42.MarketDataRequest.NoRelatedSym groupNoRelatedSym =
			new quickfix.fix42.MarketDataRequest.NoRelatedSym();
		
		SubscriptionRequestType subType = message.getSubscriptionRequestType();
		if (SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST == subType.getValue())
		{
			mdSubscription.remove(mdReqID.getValue());
		}
		else
		{
	    	for (int i=0; i<symbolCount; i++)
	    	{
	        	quickfix.field.Symbol symbolField = new quickfix.field.Symbol();
	    		message.getGroup(i+1, groupNoRelatedSym);
	    		groupNoRelatedSym.get(symbolField);
	        	log.info("Symbols: " + symbolField.getObject());
	    		
	        	OrderBook book = exchange.getBook(symbolField.getValue());

	        	sendOrderBookUpdate(mdReqID.getValue(), sessionID, book );
				if (SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES == subType.getValue())
					mdSubscription.add(symbolField.getValue(), mdReqID.getValue(), sessionID);
	    		
	    	}
		}
    }


	quickfix.fix42.ExecutionReport createReportFromOrder(Order order, OrdStatus ordStatus, ExecType execType) throws Exception
	{
	    quickfix.fix42.ExecutionReport er = new quickfix.fix42.ExecutionReport(
				new OrderID(Long.toString(order.getOrderID())), 
				new ExecID(Long.toString(order.getTranSeqNo())),
				new ExecTransType(ExecTransType.NEW), 
				execType, 
				ordStatus, 
				new Symbol(order.getCode()), 
				new Side(FixUtil.toFixOrderSide(order.getSide())),
				new LeavesQty(order.getQuantity()), 
				new CumQty(order.getCumQty()), 
				new AvgPx(order.getAvgPrice()) );
		
		er.set(new ClOrdID(order.getClOrderId()));
		er.set(new OrderQty(order.getOriginalQuantity()));
		er.set(new Price(order.getPrice()));
		er.set(new LastShares(order.getLastQty()));
		er.set(new LastPx(order.getLastPx()));
		if (order.getOrigClOrderId() != null)
			er.set(new quickfix.field.OrigClOrdID(order.getOrigClOrderId()));
			        	
		return er;
	}
 
}
