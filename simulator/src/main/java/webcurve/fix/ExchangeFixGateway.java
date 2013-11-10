package webcurve.fix;

import static quickfix.Acceptor.SETTING_ACCEPTOR_TEMPLATE;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS;
import static quickfix.Acceptor.SETTING_SOCKET_ACCEPT_PORT;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileLogFactory;
import quickfix.LogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider;
import quickfix.mina.acceptor.DynamicAcceptorSessionProvider.TemplateMapping;
import webcurve.exchange.Exchange;
/**
 * @author dennis_d_chen@yahoo.com
 */
public class ExchangeFixGateway {
    private final static Logger log = LoggerFactory.getLogger(ExchangeFixGateway.class);
    private SocketAcceptor accepter = null;
    private final Map<InetSocketAddress, List<TemplateMapping>> dynamicSessionMappings = new HashMap<InetSocketAddress, List<TemplateMapping>>();
    private Exchange exchange = null;
    private SessionSettings settings;
   
    public ExchangeFixGateway(Exchange exchange)
    {
    	super();
    	this.exchange = exchange;
    }

    private void configureDynamicSessions(SessionSettings settings, ExchangeFixManager application,
            MessageStoreFactory messageStoreFactory, LogFactory logFactory,
            MessageFactory messageFactory) throws ConfigError, FieldConvertError {
        //
        // If a session template is detected in the settings, then
        // set up a dynamic session provider.
        //

        Iterator<SessionID> sectionIterator = settings.sectionIterator();
        while (sectionIterator.hasNext()) {
            SessionID sessionID = sectionIterator.next();
            if (isSessionTemplate(settings, sessionID)) {
                InetSocketAddress address = getAcceptorSocketAddress(settings, sessionID);
                getMappings(address).add(new TemplateMapping(sessionID, sessionID));
            }
        }

        for (Map.Entry<InetSocketAddress, List<TemplateMapping>> entry : dynamicSessionMappings
                .entrySet()) {
            accepter.setSessionProvider(entry.getKey(), new DynamicAcceptorSessionProvider(
                    settings, entry.getValue(), application, messageStoreFactory, logFactory,
                    messageFactory));
        }
    }

    private List<TemplateMapping> getMappings(InetSocketAddress address) {
        List<TemplateMapping> mappings = dynamicSessionMappings.get(address);
        if (mappings == null) {
            mappings = new ArrayList<TemplateMapping>();
            dynamicSessionMappings.put(address, mappings);
        }
        return mappings;
    }

    private InetSocketAddress getAcceptorSocketAddress(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {
        String acceptorHost = "0.0.0.0";
        if (settings.isSetting(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS)) {
            acceptorHost = settings.getString(sessionID, SETTING_SOCKET_ACCEPT_ADDRESS);
        }
        int acceptorPort = (int) settings.getLong(sessionID, SETTING_SOCKET_ACCEPT_PORT);

        InetSocketAddress address = new InetSocketAddress(acceptorHost, acceptorPort);
        return address;
    }

    private boolean isSessionTemplate(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {
        return settings.isSetting(sessionID, SETTING_ACCEPTOR_TEMPLATE)
                && settings.getBool(sessionID, SETTING_ACCEPTOR_TEMPLATE);
    }

    private boolean init() {
        ExchangeFixManager application;
		try {
			application = new ExchangeFixManager(exchange);

			MessageStoreFactory messageStoreFactory = new MemoryStoreFactory();
//	        LogFactory logFactory = new ScreenLogFactory(true, true, true);
			FileLogFactory logFactory = new FileLogFactory(settings);
	        MessageFactory messageFactory = new DefaultMessageFactory();
	
	        accepter = new SocketAcceptor(application, messageStoreFactory, settings, logFactory,
	                messageFactory);
			configureDynamicSessions(settings, application, messageStoreFactory, logFactory,
			        messageFactory);

			accepter.start();
			return true;
		} catch (FieldConvertError e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (ConfigError e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
    	return false;
    }
    
    public boolean open(String strSettings) {
    	try {
			settings = new SessionSettings(new ByteArrayInputStream(strSettings.getBytes()));
		} catch (ConfigError e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return init();
    }
    
    public boolean openFile(String cfgFile) 
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
        return init();
    }

    public void close() {
        accepter.stop();
     }

    private static InputStream getSettingsInputStream(String cfgFile) throws FileNotFoundException {
        InputStream inputStream = null;
        if (cfgFile == null || cfgFile.equals("")) {
        	log.info("loading config from resource");
            inputStream = ExchangeFixGateway.class.getResourceAsStream("exchange.cfg");
        } else {
        	log.info("loading config from: " + cfgFile);
            inputStream = new FileInputStream(cfgFile);
        }
        
        if (inputStream == null) {
            log.error("missing configuration file: exchange.cfg");
        }
        return inputStream;
    }

}
