package yokwe.finance.report.stats;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;

import yokwe.finance.report.stats.online.Correlation;
import yokwe.finance.report.stats.online.Covariance;
import yokwe.finance.report.stats.online.Mean;
import yokwe.finance.report.stats.online.OnlineDoubleBinaryOperator;
import yokwe.finance.report.stats.online.OnlineDoubleUnaryOperator;
import yokwe.finance.report.stats.online.Sum;
import yokwe.finance.report.stats.online.Util;
import yokwe.finance.report.stats.online.Variance;
import yokwe.util.DoubleUtil;

public final class DoubleArray {
	// FIXME add test case for each methods
	
	
	///////////////////////////////////////////////////////////////////////////
	// T[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static <T> double[] toDoubleArray(T[] array, int startIndex, int stopIndexPlusOne, ToDoubleFunction<T> map) {
		Util.checkIndex(array, startIndex, stopIndexPlusOne);
//		return Arrays.stream(array, startIndex, stopIndexPlusOne).mapToDouble(map).toArray();
		
		int length = stopIndexPlusOne - startIndex;
		double[] result = new double[length];
		for(int i = 0, j = startIndex; i < length; i++, j++) {
			result[i] = map.applyAsDouble(array[j]);
		}
		return result;
	}
	///////////////////////////////////////////////////////////////////////////
	// BigDecima[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static double[] toDoubleArray(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return toDoubleArray(array, startIndex, stopIndexPlusOne, o -> o.doubleValue());
	}
	public static double[] toDoubleArray(BigDecimal[] array) {
		return toDoubleArray(array, 0, array.length);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] to R[]
	///////////////////////////////////////////////////////////////////////////
	private static final class Generator<R> implements IntFunction<R[]> {
		private final Class<R> clazz;
		
		public Generator(Class<R> clazz) {
			this.clazz = clazz;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public R[] apply(int value) {
			return (R[]) Array.newInstance(clazz, value);
		}
	}
	private static <R> R[] toArray(double[] array, int startIndex, int stopIndexPlusOne, DoubleFunction<R> map, Class<R> clazz) {
		Util.checkIndex(array, startIndex, stopIndexPlusOne);
		IntFunction<R[]> generator = new Generator<R>(clazz);
//		return Arrays.stream(array, startIndex, stopIndexPlusOne).mapToObj(map).toArray(generator);

		int length = stopIndexPlusOne - startIndex;
		R[] result = generator.apply(length);
		for(int i = 0, j = startIndex; i < length; i++, j++) {
			result[i] = map.apply(array[j]);
		}
		return result;
	}
	///////////////////////////////////////////////////////////////////////////
	// double[] to BigDecima[]
	///////////////////////////////////////////////////////////////////////////
	private static DoubleFunction<BigDecimal> doubleToBigDecimal = o -> DoubleUtil.toBigDecimal(o);
	public static BigDecimal[] toArray(double[] array, int startIndex, int stopIndexPlusOne) {
		return toArray(array, startIndex, stopIndexPlusOne, doubleToBigDecimal, BigDecimal.class);
	}
	public static BigDecimal[] toArray(double[] array) {
		return toArray(array, 0, array.length);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static double[] toDoubleArray(double[] array, int startIndex, int stopIndexPlusOne, DoubleUnaryOperator op) {
		Util.checkIndex(array, startIndex, stopIndexPlusOne);
//		return Arrays.stream(array, startIndex, stopIndexPlusOne).map(op).toArray();
		
		int length = stopIndexPlusOne - startIndex;
		double[] result = new double[length];
		for(int i = 0, j = startIndex; i < length; i++, j++) {
			result[i] = op.applyAsDouble(array[j]);
		}
		return result;
	}
	public static double[] toDoubleArray(double[] array, DoubleUnaryOperator op) {
		return toDoubleArray(array, 0, array.length, op);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] double[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static double[] toDoubleArray(double[] a, double[] b, int startIndex, int stopIndexPlusOne, DoubleBinaryOperator op) {
		Util.checkIndex(a, b, startIndex, stopIndexPlusOne);
		
		int length = stopIndexPlusOne - startIndex;
		double[] result = new double[length];
		for(int i = startIndex, j = 0; i < stopIndexPlusOne; i++, j++) {
			result[j] = op.applyAsDouble(a[i], b[i]);
		}
		return result;
	}
	public static double[] toDoubleArray(double[] a, double[] b, DoubleBinaryOperator op) {
		Util.checkIndex(a, b);
		return toDoubleArray(a, b, 0, a.length, op);
	}
	///////////////////////////////////////////////////////////////////////////
	// multiply
	///////////////////////////////////////////////////////////////////////////
	private static final DoubleBinaryOperator multiplyOp = (a, b) -> a * b;
	public static double[] multiply(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		return toDoubleArray(a, b, startIndex, stopIndexPlusOne, multiplyOp);
	}
	public static double[] multiply(double[] a, double[] b) {
		Util.checkIndex(a, b);
		return multiply(a, b, 0, a.length);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] double[] to double
	///////////////////////////////////////////////////////////////////////////
	public static double toDouble(double[] a, double[] b, int startIndex, int stopIndexPlusOne, OnlineDoubleBinaryOperator op) {
		Util.checkIndex(a, b, startIndex, stopIndexPlusOne);
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			op.accept(a[i], b[i]);
		}
		return op.getAsDouble();
	}
	///////////////////////////////////////////////////////////////////////////
	// covariance
	///////////////////////////////////////////////////////////////////////////
	public static double covariance(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		return toDouble(a, b, startIndex, stopIndexPlusOne, new Covariance());
	}
	public static double covariance(double[] a, double[] b) {
		Util.checkIndex(a, b);
		return covariance(a, b, 0, a.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// correlation
	///////////////////////////////////////////////////////////////////////////
	public static double correlation(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		return toDouble(a, b, startIndex, stopIndexPlusOne, new Correlation());
	}
	public static double correlation(double[] a, double[] b) {
		Util.checkIndex(a, b);
		return correlation(a, b, 0, a.length);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] to double
	///////////////////////////////////////////////////////////////////////////
	public static double toDouble(double[] array, int startIndex, int stopIndexPlusOne, OnlineDoubleUnaryOperator op) {
		Util.checkIndex(array, startIndex, stopIndexPlusOne);
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			op.accept(array[i]);
		}
		return op.getAsDouble();
	}
	///////////////////////////////////////////////////////////////////////////
	// sum
	///////////////////////////////////////////////////////////////////////////
	public static double sum(double[] array, int startIndex, int stopIndexPlusOne) {
		return toDouble(array, startIndex, stopIndexPlusOne, new Sum());
	}
	public static double sum(double[] array) {
		Util.checkIndex(array);
		return sum(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// man
	///////////////////////////////////////////////////////////////////////////
	public static double mean(double[] array, int startIndex, int stopIndexPlusOne) {
		return toDouble(array, startIndex, stopIndexPlusOne, new Mean());
	}
	public static double mean(double[] array) {
		return mean(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// variance
	///////////////////////////////////////////////////////////////////////////
	public static double variance(double[] array, int startIndex, int stopIndexPlusOne) {
		return toDouble(array, startIndex, stopIndexPlusOne, new Variance());
	}
	public static double variance(double[] array) {
		Util.checkIndex(array);
		return variance(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// standard deviation
	///////////////////////////////////////////////////////////////////////////
	public static double standardDeviation(double[] array, int startIndex, int stopIndexPlusOne) {
		return Math.sqrt(toDouble(array, startIndex, stopIndexPlusOne, new Variance()));
	}
	public static double standardDeviation(double[] array) {
		Util.checkIndex(array);
		return standardDeviation(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// standard measure
	///////////////////////////////////////////////////////////////////////////
	public static double[] standardMeasure(double[] array) {
		Util.checkIndex(array);
		var op = new Variance();
		op.accept(array);
		
		double sd   = op.standardDeviation();
		double mean = op.mean();
		return DoubleArray.toDoubleArray(array, o -> (o - mean) / sd);
	}
	
}
