package yokwe.finance.report.stats.online;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;

public interface OnlineDoubleBinaryOperator extends DoubleBinaryOperator, DoubleSupplier, DoubleBiConsumer {
	@Override
	default double applyAsDouble(double a, double b) {
		accept(a, b);
		return getAsDouble();
	}
	
	default void accept(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		Util.checkIndex(a, b, startIndex, stopIndexPlusOne);
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			accept(a[i], b[i]);
		}
	}
	default void accept(double[] a, double[] b) {
		accept(a, b, 0, a.length);
	}
	
	default double applyAsDouble(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		accept(a, b, startIndex, stopIndexPlusOne);
		return getAsDouble();
	}
	default double applyAsDouble(double[] a, double[] b) {
		return applyAsDouble(a, b, 0, a.length);
	}

}
