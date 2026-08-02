package yokwe.finance.report.stats.online;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface BigDecimalOnlineUnaryOperator extends UnaryOperator<BigDecimal>, Supplier<BigDecimal>, Consumer<BigDecimal> {
	@Override
	default BigDecimal apply(BigDecimal value) {
		accept(value);
		return get();
	}

	default void accept(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		Util.checkIndex(array, startIndex, stopIndexPlusOne);
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			accept(array[i]);
		}
	}
	default void accept(BigDecimal[] array) {
		accept(array, 0, array.length);
	}

	default BigDecimal apply(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		accept(array, startIndex, stopIndexPlusOne);
		return get();
	}
	default BigDecimal applye(BigDecimal[] array) {
		return apply(array, 0, array.length);
	}
}
