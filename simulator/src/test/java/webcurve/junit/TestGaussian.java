package webcurve.junit;

import junit.framework.TestCase;
import webcurve.util.Gaussian;

public class TestGaussian extends TestCase{
	public void testIntGaussian() {
		int times = 100000;
		int total = 0;
		int band = 10;
		double dis[] = new double[band*2+1];
		for(int i=0; i<times; i++) {
			int value = Gaussian.getGaussianInRange(band);

			dis[value+band]++;
			total++;
		}
		for(int i=0; i<dis.length; i++) {
			System.out.println((i-band) + ", " + dis[i]*1.0/total);
		}
		
	}
}
