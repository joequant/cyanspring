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
package com.cyanspring.common.transport;

public interface IObjectTransportService extends ITransportService {
	void createReceiver(String subject, IObjectListener listener) throws Exception;
	void createSubscriber(String subject, IObjectListener listener) throws Exception;
	void removeSubscriber(String subject, IObjectListener listener) throws Exception;
	void sendMessage(String subject, Object obj) throws Exception;
	void publishMessage(String subject, Object obj) throws Exception;

	IObjectSender createObjectSender(String subject) throws Exception;
	IObjectSender createObjectPublisher(String subject) throws Exception;

}
