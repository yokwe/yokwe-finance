package yokwe.finance.report.stats.online;

import yokwe.util.UnexpectedException;

//
// calculate no-reinvested value
//
public final class NoReinvestedValue implements OnlineDoubleBinaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
	private double sumDiv = 0;
	private double lastNoReinvetedValue = Double.NaN;

	@Override
	public void accept(double price, double div) {
		// sanity check
		if (Double.isInfinite(price)) {
			logger.error("price is infinite");
			logger.error("  price {}", Double.toString(price));
			throw new UnexpectedException("price is infinite");
		}
		if (Double.isInfinite(div)) {
			logger.error("div is infinite");
			logger.error("  div {}", Double.toString(div));
			throw new UnexpectedException("div is infinite");
		}
		
		// update for next iteration
		sumDiv += div;
		
		lastNoReinvetedValue = price + sumDiv;
	}
	
	@Override
	public double getAsDouble() {
		return lastNoReinvetedValue;
	}
}