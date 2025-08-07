package yokwe.finance.report.stats.online;

import java.util.Objects;

public interface DoubleBiConsumer {
	void accept(double a, double b);
	
    default DoubleBiConsumer andThen(DoubleBiConsumer after) {
        Objects.requireNonNull(after);
        return (double a, double b) -> { accept(a, b); after.accept(a, b); };
    }
}
