package jaicore.ml.core.optimizing.lbfgs;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;

/**
 * All configuration parameters of the LBFGS optimizer.
 * 
 * @author Mirko
 *
 */
public class LBFGSArrayUtils {
	
	private LBFGSArrayUtils() {
		// Intentionally left blank
	}

	public static double trimmedDotProduct(double[] a, double[] b) {
		int minLength = Math.min(a.length, b.length);
		double[] trimmedA = new double[minLength];
		System.arraycopy(a, 0, trimmedA, 0, minLength);
		double[] trimmedB = new double[minLength];
		System.arraycopy(b, 0, trimmedB, 0, minLength);
		return dotProduct(trimmedA, trimmedB);
	}

	public static double dotProduct(double[] a, double[] b) {
		Vector wrappedVector = new DenseDoubleVector(a);
		return wrappedVector.dotProduct(b);
	}

	static double vecdot(double[] a, double[] b) {
		DenseDoubleVector vec = new DenseDoubleVector(a);
		return vec.dotProduct(b);
	}

	static void veccpy(double[] y, double[] x, int n) {
		System.arraycopy(x, 0, y, 0, n);
	}

	static void vecadd(double[] y, final double[] x, final double c, final int n) {
		int i;

		for (i = 0; i < n; ++i) {
			y[i] += c * x[i];
		}
	}

	static void vecncpy(double[] y, double[] x, int n) {
		for (int i = 0; i < n; ++i) {
			y[i] = -x[i];
		}
	}

	static void vecset(double[] x, final double c) {
		for (int i = 0; i < x.length; i++) {
			x[i] = c;
		}
	}

	static void vecdiff(double[] z, final double[] x, final double[] y, final int n) {
		for (int i = 0; i < n; ++i) {
			z[i] = x[i] - y[i];
		}
	}

	static void vecscale(double[] y, final double c, final int n) {
		for (int i = 0; i < n; ++i) {
			y[i] *= c;
		}
	}

	static void vecmul(double[] y, final double[] x, final int n) {
		for (int i = 0; i < n; ++i) {
			y[i] *= x[i];
		}
	}

	static double vec2norm(final double[] x) {
		double s = vecdot(x, x);
		return Math.sqrt(s);
	}

	static double vec2norminv(final double[] x) {
		double s = vec2norm(x);
		return 1.0 / s;
	}

}
