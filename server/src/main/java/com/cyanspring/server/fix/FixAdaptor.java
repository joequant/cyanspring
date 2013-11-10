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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

import com.cyanspring.common.stream.IStreamAdaptor;

public abstract class  FixAdaptor<T extends Application> implements Application, IStreamAdaptor<T> {
	private static final Logger log = LoggerFactory
			.getLogger(FixAdaptor.class);

	private FixConnection fixConnection;
	// each fix session is mapped to an IDownStreamConnection object
	private List<T> connections = new ArrayList<T>();
	private Map<SessionID, T> map = new HashMap<SessionID, T>();

	public FixAdaptor(String fixSettings) {
		this.fixConnection = new FixConnection(fixSettings);
	}

	abstract protected T createFixSession(SessionID session);
	
	@Override
	public void init() throws FixException {
		try {
			fixConnection.setApp(this);
			fixConnection.init();

//			List<SessionID> sessions = fixConnection.getSessions();
//			for (SessionID session : sessions) {
//				T connection = createFixSession(session);
//				connections.add(connection);
//				map.put(session, connection);
//			}
			fixConnection.start();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			throw new FixException(e.getMessage());
		}
	}

	@Override
	public List<T> getConnections() {
		return connections;
	}

	@Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		Application connection = map.get(arg1);
		if (null == connection) {
			log.warn("fromAdmin: Cant location connection for this SessionID: " + arg1);
			return;
		}
		connection.fromAdmin(arg0, arg1);
	}

	@Override
	public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		Application connection = map.get(arg1);
		if (null == connection) {
			log.error("fromApp: Cant location connection for this SessionID: " + arg1);
			return;
		}
		connection.fromApp(arg0, arg1);
	}

	@Override
	public void onCreate(SessionID sessionID) {
		log.debug("Adding FIX session: " + sessionID.toString());
		T connection = createFixSession(sessionID);
		connections.add(connection);
		map.put(sessionID, connection);
	}

	@Override
	public void onLogon(SessionID arg0) {
		Application connection = map.get(arg0);
		if (null == connection) {
			log.error("onLogon: Cant location connection for this SessionID: " + arg0);
			return;
		}
		connection.onLogon(arg0);
	}

	@Override
	public void onLogout(SessionID arg0) {
		Application connection = map.get(arg0);
		if (null == connection) {
			log.error("onLogout: Cant location connection for this SessionID: " + arg0);
			return;
		}
		connection.onLogout(arg0);
	}

	@Override
	public void toAdmin(Message arg0, SessionID arg1) {
		Application connection = map.get(arg1);
		if (null == connection) {
			log.error("toAdmin: Cant location connection for this SessionID: " + arg1);
			return;
		}
		connection.toAdmin(arg0, arg1);
	}

	@Override
	public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
		Application connection = map.get(arg1);
		if (null == connection) {
			log.error("toApp: Cant location connection for this SessionID: " + arg1);
			return;
		}
		connection.toApp(arg0, arg1);
	}

}
