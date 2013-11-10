package webcurve.util;

public class PriceUtils {

	static public boolean Equal(double x, double y)
	{
		return Math.abs(x-y) < 0.000001;
	}

	static public boolean GreaterThan(double x, double y)
	{
		return x-y > 0.000001;
	}

	static public boolean LessThan(double x, double y)
	{
		return y-x > 0.000001;
	}
	
	static public boolean EqualGreaterThan(double x, double y)
	{
		return Equal(x, y) || GreaterThan(x,y);
	}
	
	static public boolean EqualLessThan(double x, double y)
	{
		return Equal(x, y) || LessThan(x,y);
	}
	
	static public int Compare(double x, double y)
	{
		if (Equal(x, y))
			return 0;
		
		if (GreaterThan(x,y))
			return 1;
		else
			return -1;
	}
	
	static public boolean isZero(double x) {
		return Equal(x, 0);
	}
	
}
