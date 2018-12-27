package jaicore.ml.core.optimizing.graddesc;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.optimizing.IGradientDescendableFunction;
import jaicore.ml.core.optimizing.IGradientFunction;

/**
 * Difference quotient based gradient estimation. This class will give a
 * black-box gradient estimation by simply calculating
 * 
 * (f(x + h) - f(x))/h
 * 
 * where x is the provided point and x' is a point that slightly differs
 * (specified by the parameter <code>precision</code>. (Obviously it holds that
 * in lim_{precision -> 0} this yields the exact gradient.)
 * 
 * If x is a vector (a_o, ..., a_n), then, instead we calculate each partial
 * derivative i by:
 * 
 * (f(a_o, ... a_i +h, ... , a_n) - f((a_o, ..., a_n)))/h
 * 
 * Obviously, this is an highly in efficient approach for estimating the
 * gradient (if we have n partial derivatives, we need 2 *n estimations).
 * 
 * @author Mirko Jürgens
 *
 */
public class BlackBoxGradient implements IGradientFunction {

	private final double precision;

	private final IGradientDescendableFunction function;

	public BlackBoxGradient(IGradientDescendableFunction underlyingFunction, double precision) {
		this.precision = precision;
		this.function = underlyingFunction;
	}

	@Override
	public Vector apply(Vector x_vec) {
		Vector gradient = new DenseDoubleVector(x_vec.length());
		double f_x = function.apply(x_vec);
		Vector x_prime = new DenseDoubleVector(x_vec.asArray());
		for (int i = 0; i < x_vec.length(); i++) {
			if (i > 0) {
				x_prime.setValue(i - 1, x_prime.getValue(i - 1) - precision);
			}
			x_prime.setValue(i, x_prime.getValue(i) + precision);
			// now compute f(x') - f(x)
			double f_x_prime = function.apply(x_prime);
			double partial = f_x_prime - f_x;
			gradient.setValue(i, partial);
		}
		return gradient;
	}

}
