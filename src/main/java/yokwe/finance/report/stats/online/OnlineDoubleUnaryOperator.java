package yokwe.finance.report.stats.online;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

public interface OnlineDoubleUnaryOperator extends DoubleUnaryOperator, DoubleSupplier, DoubleConsumer {
	@Override
	default double applyAsDouble(double value) {
		accept(value);
		return getAsDouble();
	}
	
	default void accept(double[] array, int startIndex, int stopIndexPlusOne) {
		Util.checkIndex(array, startIndex, stopIndexPlusOne);
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			accept(array[i]);
		}
	}
	default void accept(double[] array) {
		accept(array, 0, array.length);
	}
	
	default double applyAsDouble(double[] array, int startIndex, int stopIndexPlusOne) {
		accept(array, startIndex, stopIndexPlusOne);
		return getAsDouble();
	}
	default double applyAsDouble(double[] array) {
		return applyAsDouble(array, 0, array.length);
	}
}
