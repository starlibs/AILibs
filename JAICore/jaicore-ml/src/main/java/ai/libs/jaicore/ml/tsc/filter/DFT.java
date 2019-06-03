package ai.libs.jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen Beierling
 *         This class calculates the DFT coefficients for a given double vector or a set of them.
 *         The calculations are done iteratively or recursively.
 *
 *         Rafiei, D., and Mendelzon, A. Efficient retrieval of similar time sequences using DFT.
 *         (1998), pp. 249–257. (1)
 *
 *         Schäfer, P.: The BOSS is concerned with time series classification in the presence of noise. DMKD (2015)
 *         p.1510 p.1516 (2)
 */
public class DFT implements IFilter {

	private static final String MSG_EMPTYINPUT = "The input can not be empty";

	/**
	 * Is used to save the final DFT Coefficients matrices. Each entry in the list corresponds to one
	 * matrix in the original dataset.
	 */
	private List<double[][]> dftCoefficients = new ArrayList<>();

	private double[][] dftCoefficientsMatrix;

	private double[] dftCoefficientsInstance;
	/**
	 * default value for the computation of the DFT Coefficients normally set to the wordlength/2
	 */
	private int numberOfDisieredCoefficients = 10;

	/**
	 * tracks whether the fit method was called
	 */
	private boolean fittedInstance = false;
	private boolean fittedMatrix = false;
	private boolean fitted = false;

	private boolean meanCorrected = false;
	private int startingpoint = 0;

	private boolean rekursivFirstInstance;

	public void setNumberOfDisieredCoefficients(final int numberOfDisieredCoefficients) {
		this.numberOfDisieredCoefficients = numberOfDisieredCoefficients;
	}

	public void setMeanCorrected(final boolean meanCorrected) {
		this.meanCorrected = meanCorrected;

		if (this.meanCorrected) {
			this.startingpoint = 1;
			if (this.numberOfDisieredCoefficients == 1) {
				throw new IllegalArgumentException("The number of desiered dft coefficients would be zero.");
			}
		} else {
			this.startingpoint = 0;
		}
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.tsc.dataset.TimeSeriesDataset)
	 *
	 * Returns a new  DFT dataset according to the by fit calculated DFT coefficents.
	 *
	 */
	@Override
	public TimeSeriesDataset transform(final TimeSeriesDataset input) {

		if (input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}

		if (!this.fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method is called.");
		}
		// creates a new dataset out of the matrix vice Arraylist of the DFT coefficents calculated by fit
		return new TimeSeriesDataset(this.dftCoefficients, null, null);
	}

	// calculates the number of desired DFT coefficients for each matrix and therefore for each instance
	@Override
	public void fit(final TimeSeriesDataset input) {

		if (input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		this.dftCoefficients.clear();

		for (int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			this.fitTransform(input.getValues(matrix));
			this.fittedMatrix = false;
			this.dftCoefficients.add(this.dftCoefficientsMatrix);
		}

		this.fitted = true;
	}

	@Override
	public TimeSeriesDataset fitTransform(final TimeSeriesDataset input) {
		this.fit(input);
		return this.transform(input);
	}

	@Override
	public double[] transform(final double[] input) {
		if (!this.fittedInstance) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method.");
		}
		if (input.length == 0) {
			throw new IllegalArgumentException("The input can not be empty.");
		}
		return this.dftCoefficientsInstance;
	}

	@Override
	public void fit(final double[] input) {

		if (this.numberOfDisieredCoefficients > input.length) {
			throw new IllegalArgumentException("There cannot be more DFT coefficents calcualated than there entrys in the basis instance.");
		}

		if (input.length == 0) {
			throw new IllegalArgumentException("The to transform instance can not be of length zero.");
		}

		if (this.rekursivFirstInstance) {
			this.startingpoint = 0;
		}
		// The buffer for the calculated DFT coefficeients
		this.dftCoefficientsInstance = new double[this.numberOfDisieredCoefficients * 2 - (this.startingpoint * 2)];

		// Variable used to make steps of size two in a loop that makes setps of size one
		int loopcounter = 0;

		for (int coefficient = this.startingpoint; coefficient < this.numberOfDisieredCoefficients; coefficient++) {

			Complex result = new Complex(0.0, 0.0);

			for (int entry = 0; entry < input.length; entry++) {

				// calculates the real and imaginary part of the entry according to the desired coefficient
				// c.f. p. 1510 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schäfer
				double realpart = Math.cos(-(1.0 / input.length) * 2.0 * Math.PI * entry * coefficient);
				double imaginarypart = Math.sin(-(1.0 / input.length) * 2.0 * Math.PI * entry * coefficient);

				Complex tmp = new Complex(realpart, imaginarypart);
				tmp = tmp.multiply(input[entry]);

				result = result.add(tmp);
			}

			// saves the calculated coefficient in the buffer with first the real part and than the imaginary
			this.dftCoefficientsInstance[loopcounter] = result.getReal();
			this.dftCoefficientsInstance[loopcounter + 1] = result.getImaginary();
			loopcounter += 2;
		}
		if (this.rekursivFirstInstance && this.meanCorrected) {
			this.startingpoint = 1;
		}
		this.fittedInstance = true;
	}

	@Override
	public double[] fitTransform(final double[] input) {
		this.fit(input);
		return this.transform(input);
	}

	@Override
	public double[][] transform(final double[][] input) {
		if (!this.fittedMatrix) {
			throw new NoneFittedFilterExeception("The fit method must be called before transforming");
		}
		if (input.length == 0) {
			throw new IllegalArgumentException(MSG_EMPTYINPUT);
		}
		return this.dftCoefficientsMatrix;
	}

	@Override
	public void fit(final double[][] input) {

		this.dftCoefficientsMatrix = new double[input.length][this.numberOfDisieredCoefficients * 2 - (this.startingpoint * 2)];
		double[] dftCoefficientsOFInstance = null;
		for (int instance = 0; instance < input.length; instance++) {
			dftCoefficientsOFInstance = this.fitTransform(input[instance]);
			this.fittedInstance = false;
			this.dftCoefficientsMatrix[instance] = dftCoefficientsOFInstance;
		}
		this.fittedMatrix = true;
	}

	@Override
	public double[][] fitTransform(final double[][] input) {
		this.fit(input);
		return this.transform(input);
	}

	// It is required that the input is inform of the already sliced windows.
	// cf. p. 1516 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schäfer
	// Best explanation of the algorithm can be found here : "https://www.dsprelated.com/showarticle/776.php"

	public double[][] rekursivDFT(final double[][] input) {
		if (input.length == 0) {
			throw new IllegalArgumentException(MSG_EMPTYINPUT);
		}

		if (input[0].length < this.numberOfDisieredCoefficients) {
			throw new IllegalArgumentException("Can not compute more dft coefficents than the length of the input.");
		}

		if (this.numberOfDisieredCoefficients < 0) {
			throw new IllegalArgumentException("The number of desiered DFT coefficients can not be negativ.");
		}

		Complex[][] outputComplex = new Complex[input.length][this.numberOfDisieredCoefficients];

		for (int i = 0; i < input.length; i++) {
			if (i == 0) {
				this.rekursivFirstInstance = true;
				double[] tmp = this.fitTransform(input[i]);
				this.rekursivFirstInstance = false;
				Complex[] firstEntry = new Complex[this.numberOfDisieredCoefficients];
				for (int entry = 0; entry < tmp.length - 1; entry += 2) {
					firstEntry[entry / 2] = new Complex(tmp[entry], tmp[entry + 1]);
				}
				outputComplex[0] = firstEntry;
			} else {
				Complex[] coefficientsForInstance = new Complex[this.numberOfDisieredCoefficients];
				for (int j = 0; j < this.numberOfDisieredCoefficients; j++) {
					coefficientsForInstance[j] = this.vFormular(j, input[i].length).multiply((outputComplex[i - 1][j].subtract(new Complex(input[i - 1][0], 0).subtract(new Complex(input[i][input[i].length - 1], 0)))));
				}
				outputComplex[i] = coefficientsForInstance;
			}
		}

		return this.conversion(outputComplex);
	}

	private double[][] conversion(final Complex[][] input) {
		if (input.length == 0) {
			throw new IllegalArgumentException(MSG_EMPTYINPUT);
		}

		double[][] output = new double[input.length][input[0].length * 2 - (this.startingpoint * 2)];
		for (int i = 0; i < input.length; i++) {
			int loopcounter = this.startingpoint;
			for (int j = 0; j < output[i].length; j += 2) {
				output[i][j] = input[i][loopcounter].getReal();
				output[i][j + 1] = input[i][loopcounter].getImaginary();
				loopcounter++;
			}
		}
		return output;
	}

	private Complex vFormular(final int coefficient, final int legthOfinstance) {
		return new Complex(Math.cos(2 * Math.PI * coefficient / legthOfinstance), Math.sin(2 * Math.PI * coefficient / legthOfinstance));
	}

	public TimeSeriesDataset rekursivDFT(final TimeSeriesDataset input) {
		List<double[][]> tmp = new ArrayList<>();
		for (int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			tmp.add(this.rekursivDFT(input.getValues(matrix)));
		}
		return new TimeSeriesDataset(tmp, null, null);
	}
}
