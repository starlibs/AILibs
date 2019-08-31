package ai.libs.jaicore.basic.metric;

import org.api4.java.common.metric.IScalarDistance;

/**
 * ScalarDistanceUtil
 */
public class ScalarDistanceUtil {

	private ScalarDistanceUtil() {
		/* no instantiation desired */
	}

	public static IScalarDistance getAbsoluteDistance() {
		return (x, y) -> Math.abs(x - y);
	}

	public static IScalarDistance getSquaredDistance() {
		return (x, y) -> (x - y) * (x - y);
	}
}