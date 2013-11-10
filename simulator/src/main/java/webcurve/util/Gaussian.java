package webcurve.util;

import java.util.Random;

public class Gaussian {
	public static int getGaussianInRange(int band) {
		return getGaussianInRange(band, 3.0);
	}

	// range should be usually between 1-3, the wider the range
	// the result is more around 0
	public static int getGaussianInRange(int band, double range) {
		Random ran = new Random();
		double delta = 0.0001;
		double rd = ran.nextGaussian();
		while(rd < -1*(range-delta) || rd > (range-delta))
			rd = ran.nextGaussian();

		if(rd > 0) {
			return(int)Math.floor((rd/range) * band + 0.5);
		} else {
			return(int)Math.ceil((rd/range) * band - 0.5);
		}
	}
	
	public static double getGaussianPriceDelta(double band) {
		return getGaussianPriceDelta(band, 3.0);
	}
	
	public static double getGaussianPriceDelta(double band, double range) {
		Random ran = new Random();
		double delta = 0.0001;
		double rd = ran.nextGaussian();
		while(rd < -1*(range-delta) || rd > (range-delta))
			rd = ran.nextGaussian();

		if(rd > 0) {
			return rd/range * band;
		} else {
			return rd/range * band;
		}
	}
}
