package yokwe.finance.report.stats.online;

public final class Covariance implements OnlineDoubleBinaryOperator {
	private int    count     = 0;
	private double lastMeanA = 0;
	private double lastMeanB = 0;
	private double lastC     = 0;
	
	@Override
	public void accept(double a, double b) {
		count++;
		
		double deltaX = a - lastMeanA;
		double meanA  = lastMeanA + deltaX / count;
		double meanB  = lastMeanB + (b - lastMeanB) / count;
//		c     = Math.fma(deltaX, y - meanY, lastC);
		double c      = lastC + deltaX * (b - meanB);
		
		// update for next iteration
		lastMeanA = meanA;
		lastMeanB = meanB;
		lastC     = c;
	}
	
	@Override
	public double getAsDouble() {
		return covariance();
	}
	
 	public double covariance() {
		return (count <= 1) ? Double.NaN : (lastC / (count - 1));
	}
 	public double biasedCovariance() {
		return (count == 0) ? Double.NaN : (lastC / count);
	}

}
