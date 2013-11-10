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
package com.cyanspring.server.fix;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileLogFactory;
import quickfix.MemoryStoreFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;
import quickfix.SocketInitiator;
import quickfix.mina.SessionConnector;

public class FixConnection {
	 Logger log = LoggerFactory.getLogger(FixConnection.class);
	 
	private String settings;
	private Application app;
	private SessionConnector connection;
	
	public FixConnection(String settings) {
		super();
		this.settings = settings;
	}

	public void init() throws ConfigError, FieldConvertError {
		SessionSettings sessionSettings;
		sessionSettings = new SessionSettings(new ByteArrayInputStream(settings.getBytes()));
		
		if (settings == null) {
			log.error("settings are null");
			throw new ConfigError("settings are null");
		}
		if (app == null) {
			log.error("application  is null");
			throw new ConfigError("application  is null");
		}
		
		String connectionType = sessionSettings.getString("ConnectionType");
		FileLogFactory logFactory = new FileLogFactory(sessionSettings);
        MessageFactory messageFactory = new DefaultMessageFactory();
		MessageStoreFactory messageStoreFactory = new MemoryStoreFactory();

		if (connectionType.equals("acceptor")) {
			connection = new SocketAcceptor(app, messageStoreFactory, sessionSettings, logFactory,
	                messageFactory);
		} else if (connectionType.equals("initiator")) {
			connection = new SocketInitiator(app, messageStoreFactory, sessionSettings, logFactory,
					messageFactory);
		} else {
			throw new ConfigError("unknow connection type:" + connectionType);
		}
	}
	
	public void start() throws RuntimeError, ConfigError {
		connection.start();
	}
	
	public void close() {
		connection.stop();
	}

	public Application getApp() {
		return app;
	}

	public void setApp(Application app) {
		this.app = app;
	}

	public List<SessionID> getSessions() {
		return connection.getSessions();
	}

	
}
