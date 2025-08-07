package yokwe.finance.report.stats.online;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

import yokwe.util.UnexpectedException;

public final class Util {
	static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	///////////////////////////////////////////////////////////////////////////
	// check index consistency with array
	///////////////////////////////////////////////////////////////////////////
	//
	// one array
	//
	private static void checkindex(int length, int startIndex, int stopIndexPlusOne) {
		if (length == 0 && startIndex == 0 && stopIndexPlusOne == 0) return;
		
		if (!(0 <= startIndex && startIndex < length)) {
			logger.error("  alength           {}", length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
		if (!(startIndex < stopIndexPlusOne && stopIndexPlusOne <= length)) {
			logger.error("  length            {}", length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
	}
	// object array
	public static <T> void checkIndex(T[] array, int startIndex, int stopIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		checkindex(array.length, startIndex, stopIndexPlusOne);
	}
	// double array
	public static void checkIndex(double[] array, int startIndex, int stopIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		checkindex(array.length, startIndex, stopIndexPlusOne);
	}
	public static void checkIndex(double[] array) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
	}
	//
	// two array
	//
	// object array  object array
	public static <T, U> void checkIndex(T[] a, U[] b) {
		// length of array a and b must be same
		if (a == null) {
			throw new UnexpectedException("array a is null");
		}
		if (b == null) {
			throw new UnexpectedException("array b is null");
		}
		if (a.length != b.length) {
			logger.error("  a.length          {}", a.length);
			logger.error("  b.length          {}", b.length);
			throw new UnexpectedException("array length is different");
		}
	}
	
	public static <T, U> void checkIndex(T[] a, U[] b, int startIndex, int stopIndexPlusOne) {
		checkIndex(a, b);
		checkIndex(a, startIndex, stopIndexPlusOne);
		checkIndex(b, startIndex, stopIndexPlusOne);
	}
	// double array  double array
	public static void checkIndex(double[] a, double[] b) {
		// length of array a and b must be same
		if (a == null) {
			throw new UnexpectedException("array a is null");
		}
		if (b == null) {
			throw new UnexpectedException("array b is null");
		}
		if (a.length != b.length) {
			logger.error("  a.length          {}", a.length);
			logger.error("  b.length          {}", b.length);
			throw new UnexpectedException("array length is different");
		}
	}
	public static void checkIndex(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		checkIndex(a, b);
		checkIndex(a, startIndex, stopIndexPlusOne);
		checkIndex(b, startIndex, stopIndexPlusOne);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// Lambda to create object array
	///////////////////////////////////////////////////////////////////////////
	public static final class Generator<R> implements IntFunction<R[]> {
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

}
