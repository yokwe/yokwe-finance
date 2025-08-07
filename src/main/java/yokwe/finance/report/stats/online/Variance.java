package yokwe.finance.report.stats.online;

// Welford's online algorithm
//   https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm
public final class Variance implements OnlineDoubleUnaryOperator {
	private int    count = 0;
	private double m1    = 0;
	private double m2    = 0;
	
	@Override
	public void accept(double value) {
		count++;

		double delta = value - m1;
		m1 += delta / count;
		m2 += delta * (value - m1);
	}
	
	@Override
	public double getAsDouble() {
		return variance();
	}
	public double mean() {
		return (count == 0) ? Double.NaN : m1;
	}
	public double variance() {
		return (count == 0) ? Double.NaN : (m2 / (count - 1));
	}
	public double biasedVariance() {
		return (count == 0) ? Double.NaN : (m2 / count);
	}
	public double standardDeviation() {
		return Math.sqrt(variance());
	}

}

