package com.cyanspring.server.marketdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.marketdata.ITickDataWriter;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.TickDataException;
import com.cyanspring.core.event.AsyncEventProcessor;

public class TickDataManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(TickDataManager.class);
	
	@Autowired
	protected IAsyncEventManager eventManager;
	@Autowired
	protected ITickDataWriter tickDataWriter;
	
	private List<String> symbolList = new ArrayList<String>();
	private String symbolFile;
	private String directory;
	private String id = IdGenerator.getInstance().getNextID();
	private Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
	private boolean newFile;
	private boolean dated;
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(QuoteReplyEvent.class, null);
			subscribeToEvent(QuoteEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};

	private BufferedWriter getWriter(String symbol) throws IOException {
		BufferedWriter writer = writers.get(symbol);
		if(null == writer) {
			String sdate = dated?"-"+new SimpleDateFormat("yyyyMMdd").format(new Date()): "";
			String fileName = directory + "/" + symbol + sdate + ".txt";
			File file = new File(fileName);
			if(file.exists() && !newFile) {
				writer = new BufferedWriter(new FileWriter(file, true));
			} else {
				file.createNewFile();
				writer = new BufferedWriter(new FileWriter(file));
			}
			writers.put(symbol, writer);
		}
		return writer;
	}
	
	private void persistQuote(Quote quote) throws IOException {
		BufferedWriter writer = getWriter(quote.getSymbol());
		String line = tickDataWriter.quoteToString(quote);
		writer.write(line);
		writer.newLine();
		writer.flush();
	}
	
	public void processQuoteReplyEvent(QuoteReplyEvent event) throws IOException {
		persistQuote(event.getQuote());
	}
	
	public void processQuoteEvent(QuoteEvent event) throws IOException {
		persistQuote(event.getQuote());
	}
	
	@Override
	public void init() throws Exception {
		log.debug("Initialising TickDataManager....");
		String dirName = directory;
		File file = new File(dirName);
		if(!file.isDirectory()) {
			log.info("Creating tick directory: " + dirName);
			if(!file.mkdir()) {
				throw new TickDataException("Unable to create tick data directory: " + dirName);
			}
		} else {
			log.info("Existing tick directory: " + dirName);
		}
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("PersistenceManager");
		
		loadSymbolFile();
		for(String symbol: symbolList) {
			eventManager.sendEvent(new QuoteSubEvent(id, null, symbol));
		}
	}
	
	private void loadSymbolFile() throws IOException {
		if(null == symbolFile)
			return;
		
		if (null == symbolList)
			symbolList = new ArrayList<String>();
		
		log.info("Loading symbol file: " + symbolFile);
		File file = new File(symbolFile);
		if(!file.exists()) {
			log.error("Symbol file doesn't exist: " + symbolFile);
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while((line = reader.readLine()) != null) {
			symbolList.add(line.trim());
		}
	}
	
	@Override
	public void uninit() {
		log.info("uninitialising");
		eventProcessor.uninit();
		for(BufferedWriter writer: writers.values()) {
			try {
				writer.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	//////////////////////
	// getters and setters
	//////////////////////

	public List<String> getSymbolList() {
		return symbolList;
	}

	public void setSymbolList(List<String> symbolList) {
		this.symbolList = symbolList;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public ITickDataWriter getTickDataWriter() {
		return tickDataWriter;
	}

	public void setTickDataWriter(ITickDataWriter tickDataWriter) {
		this.tickDataWriter = tickDataWriter;
	}

	public boolean isNewFile() {
		return newFile;
	}

	public void setNewFile(boolean newFile) {
		this.newFile = newFile;
	}

	public boolean isDated() {
		return dated;
	}

	public void setDated(boolean dated) {
		this.dated = dated;
	}

	public String getSymbolFile() {
		return symbolFile;
	}

	public void setSymbolFile(String symbolFile) {
		this.symbolFile = symbolFile;
	}

}
