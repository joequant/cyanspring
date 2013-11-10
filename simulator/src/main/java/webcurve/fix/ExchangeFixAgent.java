package webcurve.fix;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RejectLogon;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.HandlInst;
import quickfix.field.LastPx;
import quickfix.field.LocateReqd;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TransactTime;
import webcurve.client.ClientOrder;
import webcurve.client.Execution;
import webcurve.client.ExecutionListener;
import webcurve.client.FixMsg;
import webcurve.client.MarketDepth;
import webcurve.client.MarketTrade;
import webcurve.common.ExchangeListener;
import webcurve.common.MultiSubscriptionManager;
import webcurve.util.FixUtil;
/**
 * @author dennis_d_chen@yahoo.com
 */
public class ExchangeFixAgent extends MessageCracker implements Application {
    private static Logger log = LoggerFactory.getLogger(ExchangeFixAgent.class);
    private SessionSettings settings;
    private Initiator initiator = null;
	private boolean initiatorStarted = false;   
	private SessionID primarySessionID = null;
	
	
	private Vector<ExecutionListener> listeners = new Vector<ExecutionListener>();
    public void addExecutionListener(ExecutionListener l)
    {
    	if (!listeners.contains(l))
    		listeners.add(l);
    }
    public void removeExecutionListener(ExecutionListener l)
    {
    	listeners.remove(l);
    }
    private void updateOrder(ClientOrder order, String info)
    {
    	for (ExecutionListener l: listeners)
    		l.OnOrder(order, info);
    }
    private void updateExecution(Execution exec, String info)
    {
    	for (ExecutionListener l: listeners)
    		l.OnExecution(exec, info);
    }
     
    private static InputStream getSettingsInputStream(String cfgFile) throws FileNotFoundException {
        InputStream inputStream = null;
        if (cfgFile == null || cfgFile.equals("")) {
        	log.info("loading config from resource");
            inputStream = ExchangeFixGateway.class.getResourceAsStream("FixClient.cfg");
        } else {
        	log.info("loading config from: " + cfgFile);
            inputStream = new FileInputStream(cfgFile);
        }
        
        if (inputStream == null) {
            log.error("missing configuration file: fixclient.cfg");
        }
        return inputStream;
    }	
    
	public boolean Open(String cfgFile)
	{
        try {
            InputStream inputStream = getSettingsInputStream(cfgFile);
            if (null != inputStream)
            	settings = new SessionSettings(inputStream);
            else
            {
                log.error("Cant load configuration");
            	return false;
            }
            inputStream.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }		
		      
        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "false")).booleanValue();
        
        MessageStoreFactory messageStoreFactory = new MemoryStoreFactory();
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();

        try {
			initiator = new SocketInitiator(this, messageStoreFactory, settings, logFactory,
			        messageFactory);
		} catch (ConfigError e) {
			e.printStackTrace();
			log.error(e.toString());
			return false;
		}
		
        if (!initiatorStarted ) {
            try {
                initiator.start();
                initiatorStarted = true;
            	primarySessionID = initiator.getSessions().get(0);
                
            } catch (Exception e) {
                log.error("Logon failed", e);
                return false;
            }
        } else {
        	if (initiator.getSessions().size() < 1)
        		return false;
            Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
            while (sessionIds.hasNext()) {
                SessionID sessionId = (SessionID) sessionIds.next();
                Session.lookupSession(sessionId).logon();
            }
        }
		
		return true;
	}
	
    private void sendMessage(quickfix.Message message, SessionID sessionID) {
        try {
        	log.info("Sending FIX message: " + message.toString());
            Session.sendToTarget(message, sessionID);
        } catch (SessionNotFound e) {
            System.out.println(e);
        }
    }	
	
    private void setExtraTagValues(Message message, Properties prop)
    {
    	if (null == prop)
    		return;
    	
		Set<Entry<Object, Object>> set = prop.entrySet();
		for(Entry<Object, Object> entry: set)
		{
			String tag = entry.getKey().toString();
			String value = entry.getValue().toString();
			message.setString(Integer.parseInt(tag), value);
		}
	}
    
	Hashtable<String, ClientOrder> orders = new Hashtable<String, ClientOrder>();
	public boolean enterOrder(ClientOrder order, Properties prop)
	{
		if (orders.get(order.getClientOrderID()) != null )
			return false;
		
		orders.put(order.getClientOrderID(), order);
		
        quickfix.fix42.NewOrderSingle nos;
		try {
			nos = new quickfix.fix42.NewOrderSingle(
			        new ClOrdID(order.getClientOrderID()), 
			        new HandlInst('1'), 
			        new Symbol(order.getCode()),
			        new Side(FixUtil.toFixClientOrderSide(order.getSide(), order.isShortSell())), 
			        new TransactTime(), 
			        new OrdType(FixUtil.toFixClientOrderType(order.getType()))
			    );
			if (order.isShortSell())
				nos.set(new LocateReqd(false));
			if (order.getType() == ClientOrder.TYPE.LIMIT)
				nos.set(new Price(order.getPrice()));
			
			nos.set(new OrderQty(new Double(order.getQuantity())));
			
			setExtraTagValues(nos, prop);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
        
        nos.set(new OrderQty(order.getQuantity()));
		order.addFixLog(new FixMsg(false, nos.toString()));
        sendMessage(nos, primarySessionID);
		return true;
		
	}
	
	public void amendOrder(ClientOrder order, ClientOrder newOrder, Properties prop)
	{
		if (orders.get(order.getClientOrderID()) == null)
		{
			log.warn("Cant find original order");
			return;
		}
		
		orders.put(newOrder.getClientOrderID(), newOrder);
        try {
			quickfix.fix42.OrderCancelReplaceRequest message = new quickfix.fix42.OrderCancelReplaceRequest(
			        new OrigClOrdID(order.getClientOrderID()), 
			        new ClOrdID(newOrder.getClientOrderID()), 
			        new HandlInst('1'),
			        new Symbol(order.getCode()), 
			        new Side(FixUtil.toFixClientOrderSide(order.getSide(), order.isShortSell())), 
			        new TransactTime(),
			        new OrdType(FixUtil.toFixClientOrderType(order.getType()))
			   );
			if (order.isShortSell())
				message.set(new LocateReqd(false));
			//if (order.getQuantity() != newOrder.getQuantity())
				message.set(new OrderQty(newOrder.getQuantity()));
			
			//if (order.getPrice() != newOrder.getPrice())
				message.set(new Price(newOrder.getPrice()));
				
			setExtraTagValues(message, prop);
			order.addFixLog(new FixMsg(false, message.toString()));
			sendMessage(message, primarySessionID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void cancelOrder(ClientOrder order, Properties prop)
	{
        String id = ClientOrder.nextOrderID();
        orders.put(id, order);
        quickfix.fix42.OrderCancelRequest message;
		try {
			message = new quickfix.fix42.OrderCancelRequest(
			        new OrigClOrdID(order.getClientOrderID()), 
			        new ClOrdID(id), 
			        new Symbol(order.getCode()),
			        new Side(FixUtil.toFixClientOrderSide(order.getSide(), order.isShortSell())), 
			        new TransactTime());
			message.set(new OrderQty(order.getQuantity()));
			order.addFixLog(new FixMsg(false, message.toString()));
			setExtraTagValues(message, prop);
			
			sendMessage(message, primarySessionID);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	//@Override
	public void fromAdmin(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			RejectLogon {
		
	}

	//@Override
	public void fromApp(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			UnsupportedMessageType {
        try {
    		MsgType msgType = new MsgType();
    		if (message.getHeader().getField(msgType).valueEquals("8")) {
                processExecutionReport42(message, sessionId);
            } else if (message.getHeader().getField(msgType).valueEquals("9")) {
                processCancelReject(message, sessionId);
            } else if (message.getHeader().getField(msgType).valueEquals("W"))
            {
                crack(message, sessionId);
            }  
            else {
            	log.warn("Fix message not processed: " + message.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }		
	}

	private void processCancelReject(Message message, SessionID sessionId) throws FieldNotFound
	{
        String clientOrderID = message.getField(new ClOrdID()).getValue();
        orders.remove(clientOrderID);
        String origClientOrderID = message.getField(new OrigClOrdID()).getValue();
    	ClientOrder origOrder = orders.get(origClientOrderID);
        orders.remove(clientOrderID);
        OrdStatus ordStatus = (OrdStatus)message.getField(new OrdStatus());
        origOrder.setStatus(FixUtil.fromFixOrdStatus(ordStatus));
        String info = "";
        if (message.isSetField(Text.FIELD))
        	info = message.getString(Text.FIELD);

        origOrder.addFixLog(new FixMsg(true, message.toString()));
        updateOrder(origOrder, info);
		
	}
	
	private void processExecutionReport42(Message message, SessionID sessionId) throws FieldNotFound
	{
        String clientOrderID = message.getField(new ClOrdID()).getValue();
        ClientOrder order = orders.get(clientOrderID);
        if (null == order)
        {
        	log.error("Cant find order for execution, clOrdID: " + clientOrderID);
        	return;
        }
        
        String info = "";
        if (message.isSetField(Text.FIELD))
        	info = message.getString(Text.FIELD);
        OrdStatus ordStatus = (OrdStatus)message.getField(new OrdStatus());
        ExecType execType = (ExecType)message.getField(new ExecType());
        if (execType.valueEquals(ExecType.REPLACE))
        {
        	String origClientOrderID = message.getField(new OrigClOrdID()).getValue();
        	ClientOrder origOrder = orders.get(origClientOrderID);
            orders.remove(clientOrderID);
            orders.remove(origClientOrderID);
            origOrder.setClientOrderID(clientOrderID);
            orders.put(clientOrderID, origOrder);
            if (message.isSetField(38))
            	origOrder.setQuantity(message.getInt(38));
        	if (message.isSetField(44))
        		origOrder.setPrice(message.getDouble(44));
        	
        	order = origOrder; // for later updateOrder() call;

        }
        //else if (ordStatus.valueEquals(OrdStatus.REJECTED)) 
        else if (execType.valueEquals(ExecType.REJECTED)) 
        {
//            orders.remove(clientOrderID);
        } 
        //else if (ordStatus.valueEquals(OrdStatus.CANCELED))
        else if (execType.valueEquals(ExecType.CANCELED))
		{
        	order.setClientOrderID(clientOrderID);
            if(message.isSetField(OrigClOrdID.FIELD))
            {
	        	String origClientOrderID = message.getField(new OrigClOrdID()).getValue();
	            orders.remove(origClientOrderID);
            }
		}
        else if (execType.valueEquals(ExecType.DONE_FOR_DAY)) 
        {
        } 
        else if (execType.valueEquals(ExecType.NEW)) {
        }
        else if ( execType.valueEquals(ExecType.PARTIAL_FILL) ||
        		execType.valueEquals(ExecType.FILL) )
        {
	        Execution exec = new Execution();
	        exec.setExecID(message.getField(new ExecID()).getValue());
	        exec.setClientOrderID(clientOrderID);
	        exec.setCode(order.getCode());
	        exec.setQuantity(message.getInt(32)); //LastShares
	        exec.setPrice(message.getField(new LastPx()).getValue());
	        order.getExecutions().add(exec);
	        order.setCumQty(message.getInt(14)); //CumQty;
	        order.setAvgPx(message.getField(new AvgPx()).getValue());
	        
	        updateExecution(exec, info);
        }
        order.setStatus(FixUtil.fromFixOrdStatus(ordStatus));
        order.addFixLog(new FixMsg(true, message.toString()));
        updateOrder(order, info);
        
	}
	
	static long nextMarketDataRequestId;
	static synchronized String getNextMarketDataRequestId() { return "MD-" + nextMarketDataRequestId++; };
	List<quickfix.fix42.MarketDataRequest> pendingMDRequest = new ArrayList<quickfix.fix42.MarketDataRequest>();
	MultiSubscriptionManager<String, MarketDepth> mdSubscriptionManager = new MultiSubscriptionManager<String, MarketDepth>();
	MultiSubscriptionManager<String, MarketTrade> mtSubscriptionManager = new MultiSubscriptionManager<String, MarketTrade>();
	public void subscribeMarketData(String symbol, ExchangeListener<MarketDepth> depthListener, ExchangeListener<MarketTrade> tradeListener)
	{
		quickfix.fix42.MarketDataRequest mdr;
		try {
			mdr = new quickfix.fix42.MarketDataRequest(new MDReqID(getNextMarketDataRequestId()), 
				  new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES), 
				  new quickfix.field.MarketDepth(0));
			
			mdr.set(new MDUpdateType(MDUpdateType.FULL_REFRESH));
			//quickfix.field.
			quickfix.fix42.MarketDataRequest.NoMDEntryTypes noMDEntryTypes = 
				new quickfix.fix42.MarketDataRequest.NoMDEntryTypes();

			noMDEntryTypes.set(new MDEntryType(MDEntryType.BID));
			mdr.addGroup(noMDEntryTypes);

			noMDEntryTypes.set(new MDEntryType(MDEntryType.OFFER));
			mdr.addGroup(noMDEntryTypes);
			
			noMDEntryTypes.set(new MDEntryType(MDEntryType.TRADE));
			mdr.addGroup(noMDEntryTypes);
			
			quickfix.fix42.MarketDataRequest.NoRelatedSym noRelatedSym =
				new quickfix.fix42.MarketDataRequest.NoRelatedSym();
			noRelatedSym.set(new Symbol(symbol));
			mdr.addGroup(noRelatedSym);
			mdSubscriptionManager.subscribe(symbol, MarketDepth.class, depthListener);
			mtSubscriptionManager.subscribe(symbol, MarketTrade.class, tradeListener);
			if (logon)
				sendMessage(mdr, primarySessionID);
			else
				pendingMDRequest.add(mdr);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
    public void onMessage(quickfix.fix42.MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue 
    {
    	try
    	{
	    	String symbol = message.getSymbol().getValue();
	    	
	    	quickfix.field.NoMDEntries noMDEntries = message.getNoMDEntries();
	
	    	quickfix.fix42.MarketDataSnapshotFullRefresh.NoMDEntries groupNoMDEntries 
	    		= new quickfix.fix42.MarketDataSnapshotFullRefresh.NoMDEntries();
	    	
	    	ArrayList<Double> bids = new ArrayList<Double>();
	    	ArrayList<Double> asks = new ArrayList<Double>();
	    	ArrayList<Long> bidVols = new ArrayList<Long>();
	    	ArrayList<Long> askVols = new ArrayList<Long>();
	    	
	    	MarketDepth marketDepth = new MarketDepth();
	    	marketDepth.setSymbol(symbol);
	    	MarketTrade marketTrade = null;
	    	for (int i=0; i<noMDEntries.getValue(); i++)
	    	{
	    		message.getGroup(i+1, groupNoMDEntries);
	    		if (groupNoMDEntries.getMDEntryType().getValue() == MDEntryType.BID)
	    		{
	    			bids.add(groupNoMDEntries.getMDEntryPx().getValue());
	    			bidVols.add((long)groupNoMDEntries.getMDEntrySize().getValue());
	    		}
	    		else if (groupNoMDEntries.getMDEntryType().getValue() == MDEntryType.OFFER)
	    		{
	    			asks.add(groupNoMDEntries.getMDEntryPx().getValue());
	    			askVols.add((long)groupNoMDEntries.getMDEntrySize().getValue());
	    		}
	    		else if (groupNoMDEntries.getMDEntryType().getValue() == MDEntryType.TRADE)
	    		{
	    	    	marketTrade = new MarketTrade();
	    			marketTrade.setSymbol(symbol);
	    			marketTrade.setVol((long)groupNoMDEntries.getMDEntrySize().getValue());
	    			marketTrade.setPrice(groupNoMDEntries.getMDEntryPx().getValue());
	    		}
	    	}
	    	
	    	if (bids.size()>0 || asks.size()>0)
	    	{
		    	marketDepth.setBids(bids);
		    	marketDepth.setAsks(asks);
		    	marketDepth.setBidVols(bidVols);
		    	marketDepth.setAskVols(askVols);
	//	    	marketDepth.setLastPrice(lastPrice);
	//	    	marketDepth.setLastVolume(lastVol);
	    	}
	    	mdSubscriptionManager.update(symbol, MarketDepth.class, marketDepth);
	    	
	    	if (null != marketTrade)
	    	{
	    		mtSubscriptionManager.update(symbol, MarketTrade.class, marketTrade);
	    	}
    	}
    	catch(Exception e)
    	{
    		log.error(e.getMessage());
    		e.printStackTrace();
    	}
    }

	//@Override
	public void onCreate(SessionID sessionId) {
	}

	private boolean logon = false;
	//@Override
	public void onLogon(SessionID sessionId) {
		logon = true;
		for(quickfix.fix42.MarketDataRequest request: pendingMDRequest)
			sendMessage(request, primarySessionID);
		pendingMDRequest.clear();
	}

	//@Override
	public void onLogout(SessionID sessionId) {
		pendingMDRequest.clear();
	}

	//@Override
	public void toAdmin(Message message, SessionID sessionId) {
		
	}

	//@Override
	public void toApp(Message message, SessionID sessionId) throws DoNotSend {
		
	}
}
