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
package com.cyanspring.server.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.SingleInstrumentStrategyUpdateEvent;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.core.event.AsyncEventProcessor;

public class PersistenceManager {
	private static final Logger log = LoggerFactory
			.getLogger(PersistenceManager.class);
	
	@Autowired
	private IAsyncEventManager eventManager;
	
	@Autowired
	SessionFactory sessionFactory;
	
	private int textSize = 4000;
	private boolean cleanStart;
	private boolean todayOnly;
	private boolean deleteTerminated = true;
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(UpdateParentOrderEvent.class, null);
			subscribeToEvent(ChildOrderUpdateEvent.class, null);
			subscribeToEvent(SingleInstrumentStrategyUpdateEvent.class, null);
			subscribeToEvent(MultiInstrumentStrategyUpdateEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	public PersistenceManager() {
	}
	
	public void init() throws Exception {
		if(cleanStart)
			truncateData(new Date());
		else if (todayOnly)
			truncateData(TimeUtil.getOnlyDate(new Date()));

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("PersistenceManager");
	}

	public void uninit() {
		eventProcessor.uninit();
	}
	
	private void truncateData(Date date) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    String hql = "delete from TextObject where timeStamp < :timeStamp and serverId = :serverId";
	        Query query = session.createQuery(hql);
	        query.setParameter("timeStamp", date);
	        query.setParameter("serverId", IdGenerator.getInstance().getSystemId());
	        int rowCount = query.executeUpdate();
	        log.debug("TextObject Records deleted: " + rowCount);
	        
	        hql = "delete from ChildOrder where created < :created and serverId = :serverId";
	        query = session.createQuery(hql);
	        query.setParameter("created", date);
	        query.setParameter("serverId", IdGenerator.getInstance().getSystemId());
	        rowCount = query.executeUpdate();
	        log.debug("ChildOrder Records deleted: " + rowCount);

	        hql = "delete from ChildOrderAudit where created < :created and serverId = :serverId";
	        query = session.createQuery(hql);
	        query.setParameter("created", date);
	        query.setParameter("serverId", IdGenerator.getInstance().getSystemId());
	        rowCount = query.executeUpdate();
	        log.debug("ChildOrderAudit Records deleted: " + rowCount);

	        hql = "delete from Execution where created < :created and serverId = :serverId";
	        query = session.createQuery(hql);
	        query.setParameter("created", date);
	        query.setParameter("serverId", IdGenerator.getInstance().getSystemId());
	        rowCount = query.executeUpdate();
	        log.debug("Execution Records deleted: " + rowCount);

	        tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}

	private void persistXml(String id, String xml) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    @SuppressWarnings("unchecked")
			List<TextObject> list1 = (List<TextObject>)session.createCriteria(TextObject.class)
			    .add( Restrictions.eq("id", id ) )
		    .list();
			
		    for(TextObject obj: list1) {
		    	session.delete(obj);
		    }
	        
	        List<TextObject> list2 = TextObject.createTextObjects(id, xml, textSize);
		    for(TextObject obj: list2) {
		    	session.save(obj);
		    }
	        
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
		
		
	}
	
	public void processUpdateParentOrderEvent(UpdateParentOrderEvent event) {
		ParentOrder order = event.getParent();
		persistXml(order.getId(), order.toCompactXML());
	}

	public void processMultiInstrumentStrategyUpdateEvent(MultiInstrumentStrategyUpdateEvent event) {
		MultiInstrumentStrategyData data = event.getStrategyData();
		persistXml(data.getId(), data.toCompactXML());
	}

	public void processSingleInstrumentStrategyUpdateEvent(SingleInstrumentStrategyUpdateEvent event) {
		Instrument data = event.getInstrument();
		persistXml(data.getId(), data.toCompactXML());
	}

	public void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		Session session = sessionFactory.openSession();
		ChildOrder order = event.getOrder();
//		log.debug(">>>>>> ChildOrderUpdateEvent: " + order.getId() + "," + event.getExecType() + "," + order.getOrdStatus());
		ChildOrderAudit audit = new ChildOrderAudit(event.getExecType(), order);
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    if(order.getOrdStatus().isCompleted()) {
		    	session.delete(order);
		    } else {
		    	session.saveOrUpdate(order);
		    }
	    	session.save(audit);
	    	
	    	if(event.getExecution() != null) {
	    		session.save(event.getExecution());
	    	}
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
		}
		finally {
			session.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Execution> recoverExecutions() {
		Session session = sessionFactory.openSession();
		List<Execution> result = new ArrayList<Execution>();
		try {
			result = (List<Execution>)session.createCriteria(Execution.class)
				.add( Restrictions.eq("serverId", IdGenerator.getInstance().getSystemId()))
				.add( Restrictions.gt("created", todayOnly?TimeUtil.getOnlyDate(new Date()):new Date(0)))
				.list();
		} catch (HibernateException e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			session.close();
		}
		return result;
	}

	private void addStrategy(List<DataObject> result, List<String> toBeRemoved, DataObject dataObject) {
		result.add(dataObject);
		if(deleteTerminated) {
			StrategyState state = dataObject.get(StrategyState.class, OrderField.STATE.value());
			if(null == state || state.equals(StrategyState.Terminated))
				toBeRemoved.add(dataObject.get(String.class, OrderField.ID.value()));
		}
	}
	
	public List<DataObject> recoverStrategies() {
		Session session = sessionFactory.openSession();
		
		List<String> toBeRemoved = new ArrayList<String>();
		List<DataObject> result = new ArrayList<DataObject>();
		try {
			@SuppressWarnings("unchecked")
			List<TextObject> list = (List<TextObject>)session.createCriteria(TextObject.class)
			.add( Restrictions.eq("serverId", IdGenerator.getInstance().getSystemId()))
			.add( Restrictions.gt("timeStamp", todayOnly?TimeUtil.getOnlyDate(new Date()):new Date(0)))
			.addOrder( Order.asc("id") ) 
			.addOrder( Order.asc("line") ) 
			.list();

			String currentId = "";
			StringBuilder xml = new StringBuilder();
			for(TextObject obj: list) {
				if(!currentId.equals(obj.getId())) {
					if(xml.length() != 0) {
						DataObject dataObject = DataObject.fromString(DataObject.class, xml.toString());
						addStrategy(result, toBeRemoved, dataObject);
					}
					currentId = obj.getId();
					xml.setLength(0);
				}
				xml.append(obj.getXml());
			}
			if(xml.length() != 0) {
				DataObject dataObject = DataObject.fromString(DataObject.class, xml.toString());
				addStrategy(result, toBeRemoved, dataObject);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
		
		log.info("Deleting " + toBeRemoved.size() + " terminated items");
		session = sessionFactory.openSession();
		try {
			for(String id: toBeRemoved) {
				Transaction tx = session.beginTransaction();
			    @SuppressWarnings("unchecked")
				List<TextObject> list = (List<TextObject>)session.createCriteria(TextObject.class)
				    .add( Restrictions.eq("id", id ) )
			    .list();
				
			    for(TextObject obj: list) {
			    	session.delete(obj);
			    }
			    tx.commit();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			session.close();
		}
		
		return result;
	}

	// getters and setters
	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	public boolean isCleanStart() {
		return cleanStart;
	}

	public void setCleanStart(boolean cleanStart) {
		this.cleanStart = cleanStart;
	}

	public boolean isTodayOnly() {
		return todayOnly;
	}

	public void setTodayOnly(boolean todayOnly) {
		this.todayOnly = todayOnly;
	}

	public boolean isDeleteTerminated() {
		return deleteTerminated;
	}

	public void setDeleteTerminated(boolean deleteTerminated) {
		this.deleteTerminated = deleteTerminated;
	}

}
