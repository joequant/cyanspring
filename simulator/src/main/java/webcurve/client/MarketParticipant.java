package webcurve.client;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webcurve.common.BaseOrder;
import webcurve.common.HKexTickTable;
import webcurve.common.MarketMakerData;
import webcurve.common.Order;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;
import webcurve.util.Gaussian;
import webcurve.util.PriceUtils;

/**
 * @author dennis_d_chen@yahoo.com
 */
public class MarketParticipant extends Thread {
	private static final Logger log = LoggerFactory
			.getLogger(MarketParticipant.class);
	protected Exchange exchange;
	protected HKexTickTable tickTable = new HKexTickTable();
	protected MarketMakerData data;
	private boolean started = false;
	private boolean paused = false;
	public enum ALGO {RANDOM, GAUSSIAN, WAVE, RANGEBOUND};
	
	public MarketParticipant( Exchange exchange, MarketMakerData data)
	{
		super("MarketParticipant");
		this.exchange = exchange;
		this.data = data;
	}

	
	private class MarketOrderGenerator {
		Random ran = new Random();
		BaseOrder.SIDE getSide() {
			return ran.nextBoolean()? BaseOrder.SIDE.BID : BaseOrder.SIDE.ASK;
		}
		int getQuantity() {
			return (ran.nextInt(data.getMaxQuantity()-data.getMinQuantity()) + data.getMinQuantity())/ data.getLotSize() * data.getLotSize();
		}
		double getPrice(BaseOrder.SIDE side) {
			OrderBook book = exchange.getBook(data.getStock());
			double sign = ran.nextBoolean()?1.0:-1.0;
			double price = data.getBasePrice() + sign * ran.nextDouble() * data.getPriceVariant();
			price = tickTable.getRoundedPrice(price, sign>0?true:false);

			if (side == BaseOrder.SIDE.BID)
			{
				double bestAsk = book.getBestAsk();
				price = (PriceUtils.GreaterThan(bestAsk, 0.0) && price > bestAsk)?bestAsk:price;
			}
			else
			{
				double bestBid = book.getBestBid();
				price = (PriceUtils.GreaterThan(bestBid, 0.0) && price < bestBid)?bestBid:price;
			}
			return price;
		}
	}
	
	private class GaussianOrderGenerator extends MarketOrderGenerator {
		@Override
		double getPrice(BaseOrder.SIDE side) {
			double delta = Gaussian.getGaussianPriceDelta(data.getPriceVariant(), data.getStdFactor());
			
			return tickTable.getRoundedPrice(data.getBasePrice() + delta, false);
		}
	}
	
	@SuppressWarnings("unused")
	private class WaveOrderGenerator extends GaussianOrderGenerator {
		// run time variables
		private int establishCount = 20;
		private BaseOrder.SIDE waveSide;
		private int waveCount;
		private double waveShift;
		private int buyWaveCount;
		private int sellWaveCount;

		private BaseOrder.SIDE previousOrderSide = BaseOrder.SIDE.ASK;
		@Override
		BaseOrder.SIDE getSide() {
			BaseOrder.SIDE result;
			if(--establishCount >= 0) {
				result = previousOrderSide == BaseOrder.SIDE.ASK? BaseOrder.SIDE.BID : BaseOrder.SIDE.ASK;
				previousOrderSide = result;
				return result;
			} else {
				if(--waveCount < 0){
					waveCount = 5 + ran.nextInt(5);
					// we don't want the side generated to be too random
					if(buyWaveCount-sellWaveCount >= 2)
						waveSide = BaseOrder.SIDE.ASK;
					else if (sellWaveCount - buyWaveCount >= 2)
						waveSide = BaseOrder.SIDE.BID;
					else
						waveSide = super.getSide();
					
					if(waveSide == BaseOrder.SIDE.ASK)
						sellWaveCount++;
					else
						buyWaveCount++;
					waveShift = (waveSide == BaseOrder.SIDE.ASK?-1:1) * ran.nextDouble() * data.getPriceVariant() / 10;
				}
				return waveSide;
			}
		}

		@Override
		double getPrice(BaseOrder.SIDE side) {
			if(establishCount > 0) {
				double delta = Math.abs(Gaussian.getGaussianPriceDelta(data.getPriceVariant(), data.getStdFactor()));
				delta = (side == BaseOrder.SIDE.ASK)?delta:-delta; 
				return tickTable.getRoundedPrice(data.getBasePrice() + delta, side == BaseOrder.SIDE.ASK?true:false);
			} else {
				double delta = Gaussian.getGaussianPriceDelta(data.getPriceVariant(), data.getStdFactor());
				return tickTable.getRoundedPrice(data.getBasePrice() + waveShift + delta, side == BaseOrder.SIDE.ASK?false:true);
			}
		}

	}	
		private class RangeBoundOrderGenerator extends GaussianOrderGenerator {
			// run time variables
			private int establishCount = 10;
			private BaseOrder.SIDE waveSide = BaseOrder.SIDE.BID;
			private double targetPrice = data.getBasePrice();

			private BaseOrder.SIDE previousOrderSide = BaseOrder.SIDE.ASK;
			@Override
			BaseOrder.SIDE getSide() {
				BaseOrder.SIDE result;
				if(--establishCount >= 0) {
					result = previousOrderSide == BaseOrder.SIDE.ASK? BaseOrder.SIDE.BID : BaseOrder.SIDE.ASK;
					previousOrderSide = result;
					return result;
				} else {
					OrderBook book = exchange.getBook(data.getStock());
					if(PriceUtils.isZero(book.getBestBid())) {
						targetPrice = data.getBasePrice();
						return BaseOrder.SIDE.BID;
					} else if(PriceUtils.isZero(book.getBestAsk())) {
						targetPrice = data.getBasePrice();
						return BaseOrder.SIDE.ASK;
					} else {
						if((waveSide == BaseOrder.SIDE.BID && PriceUtils.EqualGreaterThan(book.getBestAsk(), targetPrice)) ||
						   (waveSide == BaseOrder.SIDE.ASK && PriceUtils.EqualLessThan(book.getBestBid(), targetPrice))	) {
							waveSide = waveSide == BaseOrder.SIDE.ASK? BaseOrder.SIDE.BID : BaseOrder.SIDE.ASK;
							if(waveSide == BaseOrder.SIDE.BID) {
								targetPrice = data.getBasePrice() + ran.nextDouble() * data.getPriceVariant();
							} else {
								targetPrice = data.getBasePrice() - ran.nextDouble() * data.getPriceVariant();
							}
							targetPrice = tickTable.getRoundedPrice(targetPrice, false);
							//System.out.println("Side is changed to: " + waveSide + ", Target price is changed to: " + targetPrice);
						}
						return waveSide;
					}
				}
			}
			
			@Override
			double getPrice(BaseOrder.SIDE side) {
				if(establishCount > 0) {
					double delta = Math.abs(Gaussian.getGaussianPriceDelta(data.getPriceVariant(), data.getStdFactor()));
					delta = (side == BaseOrder.SIDE.ASK)?delta:-delta; 
					return tickTable.getRoundedPrice(data.getBasePrice() + delta, side == BaseOrder.SIDE.ASK?true:false);
				} else {
					OrderBook book = exchange.getBook(data.getStock());
					if(PriceUtils.isZero(book.getBestBid()) && PriceUtils.isZero(book.getBestAsk())) {
						return data.getBasePrice();
					} else if(PriceUtils.isZero(book.getBestBid())) {
						return tickTable.tickDown(book.getBestAsk(), false);
					} else if(PriceUtils.isZero(book.getBestAsk())) {
						return tickTable.tickUp(book.getBestBid(), false);
					} else {
						if(side == BaseOrder.SIDE.BID) {
							double delta = Gaussian.getGaussianPriceDelta(data.getPriceVariant(), data.getStdFactor());
							return tickTable.getRoundedPrice(book.getBestAsk() + delta, true);
						} else {
							double delta = Gaussian.getGaussianPriceDelta(data.getPriceVariant(), data.getStdFactor());
							return tickTable.getRoundedPrice(book.getBestBid() + delta, false);
						}
					}
				}
			}
		
	}

	public void run()
	{
		MarketOrderGenerator orderGenerator = new RangeBoundOrderGenerator();
		Random ran = new Random();
		while (true)
		{		
			try {
	            synchronized(this) {
					while (paused)
							wait();
	            }
			} catch (InterruptedException e1) {
				log.error(e1.getMessage(), e1);
			}
			
			BaseOrder.SIDE side = orderGenerator.getSide();
			int quantity = orderGenerator.getQuantity();
			double price = orderGenerator.getPrice(side);
			exchange.enterOrder(data.getStock(), Order.TYPE.LIMIT, side, quantity, price, "MarketMaker", "");
		
			try {
				sleep(data.getTradingMinInterval() + ran.nextInt(data.getTradingMaxInterval()-data.getTradingMinInterval()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	synchronized public void start()
	{
		if (!started)
		{
			started = true;
			super.start();
		}
		
		if (paused)
		{
			notify();
			paused = false;
			return;		
		}
	}
	
	synchronized public void pause()
	{
		paused = true;
	}

	public MarketMakerData getData() {
		return data;
	}
	
	
}
