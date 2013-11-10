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

public interface ITransportService {
	void startBroker()throws Exception;
	void closeBroker() throws Exception;
	void startService() throws Exception;
	void closeService() throws Exception;
	ISender createSender(String subject) throws Exception;
	void createReceiver(String subject, IMessageListener listener) throws Exception;
	void removeReceiver(String subject) throws Exception;
	ISender createPublisher(String subject) throws Exception;
	void createSubscriber(String subject, IMessageListener listener) throws Exception;
	void removeSubscriber(String subject, IMessageListener listener) throws Exception;
	void sendMessage(String subject, String message) throws Exception;
	void publishMessage(String subject, String message) throws Exception;
}
