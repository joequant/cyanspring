package webcurve.util;

import quickfix.field.OrdStatus;
import webcurve.client.ClientOrder;
import webcurve.common.BaseOrder;
import webcurve.common.Order;

/**
 * @author dennis_d_chen@yahoo.com
 */
public class FixUtil {
	// Fix utils
	public static char toFixOrderSide(BaseOrder.SIDE side) throws Exception {
		if (side == BaseOrder.SIDE.BID)
			return '1';
		else if (side == BaseOrder.SIDE.ASK)
			return '2';
		else
			throw new Exception("toFixOrderSide: unknown side " + side);
	}

	public static char toFixClientOrderSide(ClientOrder.SIDE side,
			boolean shortSell) throws Exception {
		if (side == ClientOrder.SIDE.BID)
			return '1';
		else if (side == ClientOrder.SIDE.ASK) {
			if (shortSell)
				return '5';
			else
				return '2';
		} else
			throw new Exception("toFixClientOrderSide: unknown side " + side);
	}

	public static char toFixClientOrderType(ClientOrder.TYPE type)
			throws Exception {
		if (type == ClientOrder.TYPE.MARKET)
			return '1';
		else if (type == ClientOrder.TYPE.LIMIT)
			return '2';
		else
			throw new Exception("toFixClientOrderType: unknown type " + type);
	}

	public static BaseOrder.SIDE fromFixOrderSide(char side) throws Exception {
		if (side == '1')
			return Order.SIDE.BID;
		else if (side == '2' || side == '5')
			return Order.SIDE.ASK;
		else
			throw new Exception("fromFixOrderSide: unknown side " + side);
	}

	public static ClientOrder.STATUS fromFixOrdStatus(OrdStatus ordStatus) {
		if (ordStatus.valueEquals(OrdStatus.NEW))
			return ClientOrder.STATUS.NEW;
		if (ordStatus.valueEquals(OrdStatus.PARTIALLY_FILLED))
			return ClientOrder.STATUS.PARTIALLY_FILLED;
		if (ordStatus.valueEquals(OrdStatus.FILLED))
			return ClientOrder.STATUS.FILLED;
		if (ordStatus.valueEquals(OrdStatus.DONE_FOR_DAY))
			return ClientOrder.STATUS.DONE_FOR_DAY;
		if (ordStatus.valueEquals(OrdStatus.CANCELED))
			return ClientOrder.STATUS.CANCELED;
		if (ordStatus.valueEquals(OrdStatus.REPLACED))
			return ClientOrder.STATUS.REPLACED;
		if (ordStatus.valueEquals(OrdStatus.PENDING_CANCEL))
			return ClientOrder.STATUS.PENDING_CANCEL;
		if (ordStatus.valueEquals(OrdStatus.STOPPED))
			return ClientOrder.STATUS.STOPPED;
		if (ordStatus.valueEquals(OrdStatus.REJECTED))
			return ClientOrder.STATUS.REJECTED;
		if (ordStatus.valueEquals(OrdStatus.SUSPENDED))
			return ClientOrder.STATUS.SUSPENDED;
		if (ordStatus.valueEquals(OrdStatus.PENDING_NEW))
			return ClientOrder.STATUS.PENDING_NEW;
		if (ordStatus.valueEquals(OrdStatus.CALCULATED))
			return ClientOrder.STATUS.CALCULATED;
		if (ordStatus.valueEquals(OrdStatus.EXPIRED))
			return ClientOrder.STATUS.EXPIRED;
		if (ordStatus.valueEquals(OrdStatus.ACCEPTED_FOR_BIDDING))
			return ClientOrder.STATUS.ACCEPTED_FOR_BIDDING;
		if (ordStatus.valueEquals(OrdStatus.PENDING_REPLACE))
			return ClientOrder.STATUS.PENDING_REPLACE;

		return ClientOrder.STATUS.ERROR;
	}

}
