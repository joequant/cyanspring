package com.cyanspring.strategy.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.server.strategy.SingleOrderStrategyTest;

public class SingleOrderStartEndTimeTest extends SingleOrderStrategyTest {
	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	@Override
	protected ParentOrder createData() {
		ParentOrder order = new ParentOrder("0005.HK", OrderSide.Sell, 200000, 68.00, OrderType.Limit);
		order.put(OrderField.STRATEGY.value(), "SDMA");
		try {
			setNow(timeFormat.parse("14:19:00"));
			order.put(OrderField.START_TIME.value(), timeFormat.parse("14:20:00"));
			order.put(OrderField.END_TIME.value(), timeFormat.parse("14:21:00"));
		} catch (ParseException e) {
			assertTrue(false);
		}
		return order;
	}

	@Test
	public void test() throws ParseException{
		assertNoOfChildOrders(0);
		setNow(timeFormat.parse("14:20:00"));
		assertNoOfChildOrders(1);
		setNow(timeFormat.parse("14:21:00"));
		assertNoOfChildOrders(0);
	}
}
