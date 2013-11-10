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
package com.cyanspring.server.downstream.fix;

import quickfix.SessionID;

import com.cyanspring.server.fix.FixAdaptor;
import com.cyanspring.server.fix.FixException;
import com.cyanspring.server.fix.IFixDownStreamConnection;

public class FixDownStreamAdaptor extends FixAdaptor<IFixDownStreamConnection> {

	public FixDownStreamAdaptor(String fixSettings) throws FixException {
		super(fixSettings);
	}

	@Override
	protected IFixDownStreamConnection createFixSession(SessionID session) {
		return new FixDownStreamConnection(session);
	}

	@Override
	public void uninit() {
	}

}
