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
package com.cyanspring.common.event.order;

import com.cyanspring.common.event.RemoteAsyncEvent;

public final class CancelParentOrderEvent extends RemoteAsyncEvent {
	private String orderId;
	private String txId;
	public CancelParentOrderEvent(String key, String receiver, String orderId,
			String txId) {
		super(key, receiver);
		this.orderId = orderId;
		this.txId = txId;
	}
	public String getOrderId() {
		return orderId;
	}
	public String getTxId() {
		return txId;
	}
}
