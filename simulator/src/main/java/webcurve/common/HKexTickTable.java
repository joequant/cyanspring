package webcurve.common;

import webcurve.util.PriceUtils;

public class HKexTickTable implements ITickTable {
	private final static double tickTable[][] = { 
		{0.01,		0.25,		0.001},
		{0.25,		0.50,		0.005},
		{0.50,		10.00,		0.010},
		{10.00,		20.00,		0.020},
		{20.00,		100.00,		0.050},
		{100.00,	200.00,		0.100},
		{200.00,	500.00,		0.200},
		{500.00,	1000.00,	0.500},
		{1000.00,	2000.00,	1.000},
		{2000.00,	5000.00,	2.000},
		{5000.00,	9995.00,	5.000}
	};

	//private final static double minPrice = 0.01;
	private final static double maxPrice = 9995;
	private final int scale = 1000;
	private final double delta = 0.000001;
	
	private double roundPrice(double price) {
		return ((int)((price + delta) * scale))/(double)scale;
	}
	
	@Override
	public double tickUp(double price, boolean roundUp) {
		return tickUp(price, 1, roundUp);
	}

	@Override
	public double tickDown(double price, boolean roundUp) {
		return tickDown(price, 1, roundUp);
	}

	@Override
	public double getRoundedPrice(double price, boolean up) {	
		if (PriceUtils.GreaterThan(price, maxPrice)) {
			return maxPrice;
		}
		double rounded = 0;
		for(double[] band: tickTable) {
			//find the right range
			if(PriceUtils.EqualGreaterThan(price, band[0]) && 
					PriceUtils.LessThan(price, band[1])) {
				int lprice = (int)(price * scale);
				int lbase = (int)(band[0] * scale);
				int ltick = (int)(band[2] * scale);
				int ticks = (lprice - lbase) / ltick;
				rounded = ((double)(lbase + ticks * ltick))/scale;
				if(up && PriceUtils.GreaterThan(price, rounded))
					rounded += band[2];
				
				break;
			}
		}
		return roundPrice(rounded);
	}
	
	@Override
	public double tickUp(double price, int ticks, boolean roundUp) {
		price = getRoundedPrice(price, roundUp);
		for(double[] band: tickTable) {
			if(PriceUtils.EqualGreaterThan(price, band[1]))
				continue;

			int llow = (int)(price * scale);
			int lhigh = (int)(band[1] * scale);
			int ltick = (int)(band[2] * scale);
			int totalTicks = (lhigh - llow) / ltick;
			if(totalTicks >= ticks) {
				return roundPrice(price + ticks * band[2]);
			} else {
				price = band[1];
				ticks -= totalTicks;
			}
		}
		return roundPrice(price);
	}

	@Override
	public double tickDown(double price, int ticks, boolean roundUp) {
		price = getRoundedPrice(price, roundUp);
		for(int i=tickTable.length; i>0; i--) {
			double[] band = tickTable[i-1];
			if(PriceUtils.EqualLessThan(price, band[0]))
				continue;

			int llow = (int)(band[0] * scale);
			int lhigh = (int)(price * scale);
			int ltick = (int)(band[2] * scale);
			int totalTicks = (lhigh - llow) / ltick;
			if(totalTicks >= ticks) {
				return roundPrice(price - ticks * band[2]);
			} else {
				price = band[0];
				ticks -= totalTicks;
			}
		}
		return roundPrice(price);
	}

	@Override
	public boolean validPrice(double price) {
		return PriceUtils.GreaterThan(price, 0) && PriceUtils.LessThan(price, 10000);
	}

}
