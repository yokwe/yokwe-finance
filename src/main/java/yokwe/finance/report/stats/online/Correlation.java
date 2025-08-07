package yokwe.finance.report.stats.online;

public class Correlation implements OnlineDoubleBinaryOperator {
	private int  count = 0;
	
	private Mean eXY = new Mean();
	private Mean eX  = new Mean();
	private Mean eXX = new Mean();
	private Mean eY  = new Mean();
	private Mean eYY = new Mean();

	@Override
	public void accept(double x, double y) {
		eXY.accept(x * y);
		eX.accept(x);
		eXX.accept(x * x);
		eY.accept(y);
		eYY.accept(y * y);
		//
		count++;
	}
	
	@Override
	public double getAsDouble() {
		double EXY = eXY.getAsDouble();
		double EX  = eX.getAsDouble();
		double EXX = eXX.getAsDouble();
		double EY  = eY.getAsDouble();
		double EYY = eYY.getAsDouble();
		
		return (count == 0) ? Double.NaN : (EXY - (EX * EY)) / Math.sqrt((EXX - (EX * EX)) * (EYY - (EY * EY)));
	}

}
