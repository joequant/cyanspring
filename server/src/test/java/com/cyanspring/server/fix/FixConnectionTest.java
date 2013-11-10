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

import static org.junit.Assert.assertTrue;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

@ContextConfiguration(locations = { "classpath:META-INFO/spring/FixConnectionTest.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class FixConnectionTest {
	private static final Logger log = LoggerFactory.getLogger(FixConnectionTest.class);
	@Autowired
	private String aceptorSettings1;
	
	@Autowired
	private String initiatorSettings1;
	
	class App1 implements Application {
		public boolean logon;
		public boolean logout;
		@Override
		public void fromAdmin(Message arg0, SessionID arg1)
				throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
				RejectLogon {
			log.debug("fromAdmin: " + arg1 + ", " + arg0);
		}

		@Override
		public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound,
				IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
			log.debug("fromApp: " + arg1 + ", " + arg0);
		}

		@Override
		public void onCreate(SessionID arg0) {
			log.debug("onCreate: " + arg0);
		}

		@Override
		public void onLogon(SessionID arg0) {
			log.debug("onLogon: " + arg0);
			logon = true;
			synchronized(this) {
				this.notify();
			}
		}

		@Override
		public void onLogout(SessionID arg0) {
			log.debug("onLogout: " + arg0);
			logout = true;
			synchronized(this) {
				this.notify();
			}
		}

		@Override
		public void toAdmin(Message arg0, SessionID arg1) {
			log.debug("toAdmin: " + arg1 + ", " + arg0);
			
		}

		@Override
		public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
			log.debug("toApp: " + arg1 + ", " + arg0);
			
		}
		
	}
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
	}

	@Test
	public void test() throws ConfigError, FieldConvertError, InterruptedException {
		FixConnection con1 = new FixConnection(aceptorSettings1);
		App1 app1 = new App1();
		con1.setApp(app1);
		con1.init();
		con1.start();
		
		FixConnection con2 = new FixConnection(initiatorSettings1);
		con2.setApp(new App1());
		con2.init();
		con2.start();
		
		synchronized(app1) {
			app1.wait(5000);
		}
		assertTrue(app1.logon);
	}
}
