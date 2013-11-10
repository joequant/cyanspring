package webcurve.test;

import org.junit.Ignore;

import webcurve.common.Order;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;
/**
 * @author dennis_d_chen@yahoo.com
 */

@Ignore
public class Test1 {
	public static void showBook(OrderBook book)
	{
		System.out.printf("%n                    %s                      %n", book.getCode());
		System.out.printf("--------------------------------------------%n");
		System.out.printf("         Buy                  Sell          %n");
		System.out.printf("--------------------------------------------%n");
		int i = 0;
		while(i< Math.max(book.getBidOrders().size(),book.getAskOrders().size()))
		{
			if (i < book.getBidOrders().size())
			{
				Order order = book.getBidOrders().get(i);
				System.out.printf("% 10d% 10.3f", order.getQuantity(), order.getPrice());
			}
			else
				System.out.printf("                    ");
			System.out.printf(" | ");
			if (i < book.getAskOrders().size())
			{
				Order order = book.getAskOrders().get(i);
				System.out.printf("%-10.3f% 10d", order.getPrice(), order.getQuantity());
			}
			System.out.printf("%n");
			i++;
		}
		System.out.printf("--------------------------------------------%n%n");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Exchange exchange = new Exchange();
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.BID, 3200, 68.50, "7689", "");
		showBook(exchange.getBook("0005.HK"));
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 1200, 68.60, "8675", "");
		showBook(exchange.getBook("0005.HK"));
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 1200, 68.50, "8675", "");
		showBook(exchange.getBook("0005.HK"));
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.BID, 3600, 68.40, "7689", "");
		showBook(exchange.getBook("0005.HK"));
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 1600, 68.70, "8675", "");
		showBook(exchange.getBook("0005.HK"));
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.BID, 4000, 68.30, "7689", "");
		showBook(exchange.getBook("0005.HK"));
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 2000, 68.80, "8675", "");
		showBook(exchange.getBook("0005.HK"));
		
	}

}
