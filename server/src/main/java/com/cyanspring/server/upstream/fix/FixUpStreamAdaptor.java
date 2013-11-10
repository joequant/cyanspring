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
package com.cyanspring.server.upstream.fix;

import quickfix.SessionID;

import com.cyanspring.server.fix.FixAdaptor;
import com.cyanspring.server.fix.FixException;
import com.cyanspring.server.fix.FixParentOrderConverter;
import com.cyanspring.server.fix.IFixUpStreamConnection;

public class FixUpStreamAdaptor extends FixAdaptor<IFixUpStreamConnection> {
	private FixParentOrderConverter fixParentOrderConverter;

	public FixUpStreamAdaptor(String fixSettings, FixParentOrderConverter fixParentOrderConverter) throws FixException {
		super(fixSettings);
		this.fixParentOrderConverter = fixParentOrderConverter;
	}

	@Override
	protected IFixUpStreamConnection createFixSession(SessionID session) {
		return new FixUpStreamConnection(session, fixParentOrderConverter);
	}

	@Override
	public void uninit() {
	}

}
