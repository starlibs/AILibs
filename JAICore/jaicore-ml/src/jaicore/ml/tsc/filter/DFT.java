package jaicore.ml.tsc.filter;

import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class DFT implements IFilter {

	private ArrayList<INDArray> DFTCoefficients = new ArrayList<INDArray>();
	// TODO sinvollen wert finden
	private int numberOfDisieredCoefficients = 10;

	private boolean variableSet = false;
	private boolean fitted = false;

	/**
	 * The variable is set to 1/sqrt(n) in paper "Efficient Retrieval of Similar
	 * Time Sequences Using DFT" by Davood Rafieidrafiei and Alberto Mendelzon but
	 * in the orignal "The BOSS is concerned with time series classification in the
	 * presence of noise" by Patrick Sch�fer it is set to 1/n. By default it is set
	 * to 1/n.
	 */
	private double paperSpecificVariable;

	public void setPaperSpecificVariable(double paperSpecificVariable) {
		this.paperSpecificVariable = paperSpecificVariable;
		variableSet = true;
	}

	public void setNumberOfDisieredCoefficients(int numberOfDisieredCoefficients) {
		this.numberOfDisieredCoefficients = numberOfDisieredCoefficients;
	}

	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Dataset empty ??
		if (!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		if (((TimeSeriesDataset) input).isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if (!fitted) {
			throw new NoneFittedFilterExeception(
					"The fit method must be called before the transform method is called.");
		}
		// First value is real part and second imaginary
		TimeSeriesDataset output = new TimeSeriesDataset(DFTCoefficients, null, null);
		return output;
	}

	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub

		if (!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}

		if (((TimeSeriesDataset) input).isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}

		double InstancesLength = ((TimeSeriesDataset) input).getValues(0).getRow(0).length();
		if (!variableSet) {
			paperSpecificVariable = (double) 1.0 / ((double) InstancesLength);
		}

		if (numberOfDisieredCoefficients > InstancesLength) {
			throw new IllegalArgumentException(
					"The number of desired coeficientes must be smaller than the number of data points of an instance.");
		}

		for (int matrix = 0; matrix < ((TimeSeriesDataset) input).getNumberOfVariables(); matrix++) {
			INDArray matrixDFTCoefficient = Nd4j.zeros(((TimeSeriesDataset) input).getNumberOfInstances(),
					numberOfDisieredCoefficients * 2);
			for (int instances = 0; instances < ((TimeSeriesDataset) input).getNumberOfInstances(); instances++) {
				int loopcounter = 0;
				for (int f = 0; f < numberOfDisieredCoefficients; f++) {
					Complex result = new Complex(0, 0);
					Complex c = null;
					for (int t = 0; t < InstancesLength; t++) {

						double entry = ((TimeSeriesDataset) input).getValues(matrix).getRow(instances).getDouble(t);

						// TODO can not find a exponential function for Nd4j that is free to use
						double realpart = Math
								.cos(-(1.0 / (double) InstancesLength) * 2.0 * Math.PI * (double) t * (double) f);
						double imaginarypart = Math
								.sin(-(1.0 / (double) InstancesLength) * 2.0 * Math.PI * (double) t * (double) f);
						c = new Complex(realpart, imaginarypart);
						c.exp();
						c = c.multiply(entry);
						result = result.add(c);

					}
					// TODO faster if special cases are catched T(x)= 0
					result = result.multiply(paperSpecificVariable);
					if (Math.abs(result.getImaginary()) < Math.pow(10, -15)) {
						result = new Complex(result.getReal(), 0);
					}
					if (Math.abs(result.getReal()) < Math.pow(10, -15)) {
						result = new Complex(0, result.getImaginary());
					}
					matrixDFTCoefficient.putScalar(new long[] { instances, loopcounter }, result.getReal());
					matrixDFTCoefficient.putScalar(new long[] { instances, loopcounter + 1 }, result.getImaginary());
					loopcounter = loopcounter + 2;
				}
			}
			DFTCoefficients.add(matrixDFTCoefficient);
		}
		fitted = true;
	}

	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		fit(input);
		return transform(input);
	}

}
