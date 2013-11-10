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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;

@ContextConfiguration(locations = { "classpath:conf/persistence.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class PersistenceTest {
	private static final Logger log = LoggerFactory
			.getLogger(PersistenceTest.class);
	
	@Autowired
	SessionFactory sessionFactory;
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
	}

	@Test
	public void testExecutionPersistence() throws Exception {
		Execution execution = new Execution("0005.HK", OrderSide.Sell, 2000, 68.2, "orderId2", "parentId", "strategyId", "execId");
		
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    session.save(execution);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
			assertTrue(false);
		    throw e;
		}
		finally {
			session.close();
		}
		
		session = sessionFactory.openSession();
	    Execution ex = (Execution)session.get(Execution.class, execution.getId());
	    
	    assertTrue(ex != null);
	    assertTrue(ex.getId().equals(execution.getId()));
	    assertTrue(ex.getSide().equals(execution.getSide()));
	    assertTrue(ex.getQuantity() == execution.getQuantity());
	    assertTrue(ex.getPrice() == execution.getPrice());
	    assertTrue(ex.getOrderId().equals(execution.getOrderId()));
	    assertTrue(ex.getParentOrderId().equals(execution.getParentOrderId()));
	    assertTrue(ex.getStrategyId().equals(execution.getStrategyId()));
	    assertTrue(ex.getExecId().equals(execution.getExecId()));
	    //DB returns java.sql.Timestamp which is a subclass of java.util.Date
	    assertTrue(ex.getModified().getTime() == execution.getModified().getTime());
	    assertTrue(ex.getCreated().getTime() == execution.getCreated().getTime());
	    assertTrue(ex.getSeqId().equals(execution.getSeqId()));
		
	    // clean up table
		try {
		    tx = session.beginTransaction();
	    	session.delete(ex);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
			assertTrue(false);
		    throw e;
		}
		finally {
			session.close();
		}

	}
	
	@Test
	public void testChildOrderPersistence() throws Exception {
		ChildOrder child1 = new ChildOrder("0005.HK", OrderSide.Sell, 4000, 68.3, ExchangeOrderType.LIMIT, "parentId", "strategyId");
		child1.setOrdStatus(OrdStatus.PARTIALLY_FILLED);
		child1.setCumQty(1000);
		child1.setAvgPx(68.5);
		
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    session.save(child1);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
			assertTrue(false);
		    throw e;
		}
		finally {
			session.close();
		}
		
		session = sessionFactory.openSession();
	    ChildOrder child2 = (ChildOrder)session.get(ChildOrder.class, child1.getId());
	    
	    assertTrue(child2 != null);
	    assertTrue(child1.getId().equals(child2.getId()));
	    assertTrue(child1.getSide().equals(child2.getSide()));
	    assertTrue(child1.getQuantity() == child2.getQuantity());
	    assertTrue(child1.getPrice() == child2.getPrice());
	    assertTrue(child1.getCumQty() == child2.getCumQty());
	    assertTrue(child1.getAvgPx() == child2.getAvgPx());
	    assertTrue(child1.getOrdStatus().equals(child2.getOrdStatus()));
	    assertTrue(child1.getType().equals(child2.getType()));
	    assertTrue(child1.getParentOrderId().equals(child2.getParentOrderId()));
	    assertTrue(child1.getStrategyId().equals(child2.getStrategyId()));
	    //DB returns java.sql.Timestamp which is a subclass of java.util.Date
	    assertTrue(child1.getModified().getTime() == child2.getModified().getTime());
	    assertTrue(child1.getCreated().getTime() == child2.getCreated().getTime());
	    assertTrue(child1.getSeqId().equals(child2.getSeqId()));
		
	    // clean up table
		try {
		    tx = session.beginTransaction();
	    	session.delete(child2);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
			assertTrue(false);
		    throw e;
		}
		finally {
			session.close();
		}


	}

	@Test
	public void testChildOrderAuditPersistence() throws Exception {
		ChildOrder c1 = new ChildOrder("0005.HK", OrderSide.Sell, 4000, 68.3, ExchangeOrderType.LIMIT, "parentId", "strategyId");
		c1.setOrdStatus(OrdStatus.PARTIALLY_FILLED);
		c1.setCumQty(1000);
		c1.setAvgPx(68.5);
		ChildOrderAudit child1 = new ChildOrderAudit(ExecType.PARTIALLY_FILLED, c1);
		
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    session.save(child1);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
			assertTrue(false);
		    throw e;
		}
		finally {
			session.close();
		}
		
		session = sessionFactory.openSession();
		ChildOrderAudit child2 = (ChildOrderAudit)session.get(ChildOrderAudit.class, child1.getAuditId());
	    
	    assertTrue(child2 != null);
	    assertTrue(child1.getId().equals(child2.getId()));
	    assertTrue(child1.getSide().equals(child2.getSide()));
	    assertTrue(child1.getQuantity() == child2.getQuantity());
	    assertTrue(child1.getPrice() == child2.getPrice());
	    assertTrue(child1.getCumQty() == child2.getCumQty());
	    assertTrue(child1.getAvgPx() == child2.getAvgPx());
	    assertTrue(child1.getOrdStatus().equals(child2.getOrdStatus()));
	    assertTrue(child1.getType().equals(child2.getType()));
	    assertTrue(child1.getParentOrderId().equals(child2.getParentOrderId()));
	    assertTrue(child1.getStrategyId().equals(child2.getStrategyId()));
	    assertTrue(child1.getExecType().equals(child2.getExecType()));
	    //DB returns java.sql.Timestamp which is a subclass of java.util.Date
	    assertTrue(child1.getModified().getTime() == child2.getModified().getTime());
	    assertTrue(child1.getCreated().getTime() == child2.getCreated().getTime());
	    assertTrue(child1.getSeqId().equals(child2.getSeqId()));
	    
	    // clean up table
		try {
		    tx = session.beginTransaction();
	    	session.delete(child2);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
			assertTrue(false);
		    throw e;
		}
		finally {
			session.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testTextObject() throws Exception {
		ParentOrder order = new ParentOrder("0005.HK", OrderSide.Sell, 4000, 68.3, OrderType.Limit);
		order.setAvgPx(68.3);
		order.setCumQty(3000);
		
		String xml1 = order.toXML();
		List<TextObject> list = TextObject.createTextObjects(order.getId(), xml1, 20);
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		try {
		    tx = session.beginTransaction();
		    for(TextObject obj: list)
		    	session.save(obj);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
			assertTrue(false);
		    throw e;
		}
		finally {
			session.close();
		}
		
		session = sessionFactory.openSession();
		list = (List<TextObject>)session.createCriteria(TextObject.class)
		    .add( Restrictions.eq("id", order.getId() ) )
		    .addOrder( Order.asc("line") )
		    .list();
		
		String xml2 = TextObject.assemble(list);
		assertTrue(xml1.equals(xml2));

	    // clean up table
		try {
		    tx = session.beginTransaction();
		    String hql = "delete from TextObject where id = :id";
	        Query query = session.createQuery(hql);
	        query.setString("id", order.getId());
	        int rowCount = query.executeUpdate();
	        log.debug("Records deleted: " + rowCount);
		    tx.commit();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		    if (tx!=null) 
		    	tx.rollback();
			assertTrue(false);
		    throw e;
		}
		finally {
			session.close();
		}

	}
}
