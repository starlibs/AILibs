package ai.libs.jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen Beierling
 *         c.f. p. 1511 p. 1510 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schäfer
 *         This class combines the MCB of finding the bins for a given set of DFT coefficients and SFA
 *         which selects the right letter for a DFT coefficient.
 */
public class SFA implements IFilter {

	private static final String MSG_NOEMPTYDS = "This method can not work with an empty dataset.";
	private static final String MSG_NOSINGLEINSTANCE = "To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.";

	private double[] alphabet;

	private boolean meanCorrected;

	private boolean fitted = false;
	private boolean fittedMatrix = false;

	private TimeSeriesDataset dFTDataset = null;
	private double[][] dftMatrix = null;

	private int numberOfDesieredDFTCoefficients;

	private List<double[][]> lookupTable = new ArrayList<>();
	private double[][] lookUpTableMatrix = null;

	private boolean rekursiv;

	public void setNumberOfDesieredDFTCoefficients(final int numberOfDesieredDFTCoefficients) {
		this.numberOfDesieredDFTCoefficients = numberOfDesieredDFTCoefficients;
	}

	public void disableRekursiv() {
		this.rekursiv = false;
	}

	public void enableRekursiv() {
		this.rekursiv = true;
	}

	public SFA(final double[] alphabet, final int wordLength) {
		this.alphabet = alphabet;

		// The wordlength must be even
		this.numberOfDesieredDFTCoefficients = wordLength / 2;
	}

	@Override
	public TimeSeriesDataset transform(final TimeSeriesDataset input) {

		if (input.isEmpty()) {
			throw new IllegalArgumentException(MSG_NOEMPTYDS);
		}
		if (!this.fitted) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}

		List<double[][]> sfaDataset = new ArrayList<>();
		// calculate SFA words for every instance and its DFT coefficients
		for (int matrix = 0; matrix < this.dFTDataset.getNumberOfVariables(); matrix++) {
			double[][] sfaWords = new double[this.dFTDataset.getNumberOfInstances()][this.numberOfDesieredDFTCoefficients * 2];
			for (int instance = 0; instance < this.dFTDataset.getNumberOfInstances(); instance++) {
				for (int entry = 0; entry < this.numberOfDesieredDFTCoefficients * 2; entry++) {
					double elem = this.dFTDataset.getValues(matrix)[instance][entry];
					// get the lookup table for DFT values of the instance
					double[] lookup = this.lookupTable.get(matrix)[entry];

					// if the DFT coefficient is smaller than the first or larger than the last
					// or it lays on the border give it first, last or second ,penultimate
					if (elem < lookup[0]) {
						sfaWords[instance][entry] = this.alphabet[0];
					} else if (elem == lookup[0]) {
						sfaWords[instance][entry] = this.alphabet[1];
					} else if (elem > lookup[this.alphabet.length - 2]) {
						sfaWords[instance][entry] = this.alphabet[this.alphabet.length - 1];
					} else if (elem == lookup[this.alphabet.length - 2]) {
						sfaWords[instance][entry] = this.alphabet[this.alphabet.length - 1];
					}
					// get alphabet letter for every non extrem coefficient
					else {
						for (int i = 1; i < lookup.length; i++) {
							if (elem < lookup[i]) {
								sfaWords[instance][entry] = this.alphabet[i];
								break;
							}
							if (elem == lookup[i]) {
								sfaWords[instance][entry] = this.alphabet[i + 1];
								break;
							}
						}
					}
				}
			}
			sfaDataset.add(sfaWords);
		}
		return new TimeSeriesDataset(sfaDataset, null, null);
	}

	@Override
	public void fit(final TimeSeriesDataset input) {
		if (input.isEmpty()) {
			throw new IllegalArgumentException(MSG_NOEMPTYDS);
		}
		if (this.alphabet.length == 0) {
			throw new IllegalArgumentException("The alphabet size can not be zero.");
		}

		this.lookupTable.clear();

		DFT dftFilter = new DFT();
		dftFilter.setMeanCorrected(this.meanCorrected);

		// calculates the number of DFT coefficents with wordlength as number of desired DFT coefficients
		dftFilter.setNumberOfDisieredCoefficients(this.numberOfDesieredDFTCoefficients);

		if (!this.rekursiv) {
			this.dFTDataset = dftFilter.fitTransform(input);
		} else {
			// Only works for sliding windows. However it is normally used for SFA.
			this.dFTDataset = dftFilter.rekursivDFT(input);
		}

		for (int matrix = 0; matrix < this.dFTDataset.getNumberOfVariables(); matrix++) {
			// for each part of every coefficient calculate the bins for the alphabet (number of bins == number of letters)
			double[][] lookUpTable = new double[this.numberOfDesieredDFTCoefficients * 2][this.alphabet.length - 1];

			for (int coeficient = 0; coeficient < this.numberOfDesieredDFTCoefficients * 2; coeficient++) {
				// get the columns of the DFT dataset
				double[] toBin = new double[input.getNumberOfInstances()];
				for (int instances = 0; instances < this.dFTDataset.getNumberOfInstances(); instances++) {
					toBin[instances] = this.dFTDataset.getValues(matrix)[instances][coeficient];
				}

				// Sort ascending
				// If the number of instances is equal to the number of bins the breakpoints are set to this values
				Arrays.sort(toBin);
				if (toBin.length == this.alphabet.length - 1) {
					for (int alphabetLetter = 0; alphabetLetter < this.alphabet.length - 1; alphabetLetter++) {
						lookUpTable[coeficient][alphabetLetter] = toBin[alphabetLetter];
					}
				}
				// If the number of instances is greater than the number of bins then the breakpoints are set
				// in the way that all coefficients are spread equally over the bins
				else {
					int splitValue = (int) Math.round(toBin.length / (double) this.alphabet.length);
					for (int alphabetLetter = 1; alphabetLetter < this.alphabet.length; alphabetLetter++) {
						lookUpTable[coeficient][alphabetLetter - 1] = toBin[alphabetLetter * splitValue];
					}
				}

			}
			this.lookupTable.add(lookUpTable);
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
		throw new UnsupportedOperationException(MSG_NOSINGLEINSTANCE);
	}

	@Override
	public void fit(final double[] input) {

		throw new UnsupportedOperationException(MSG_NOSINGLEINSTANCE);
	}

	@Override
	public double[] fitTransform(final double[] input) {
		throw new UnsupportedOperationException(MSG_NOSINGLEINSTANCE);
	}

	@Override
	public double[][] transform(final double[][] input) {
		if (input.length == 0) {
			throw new IllegalArgumentException(MSG_NOEMPTYDS);
		}
		if (!this.fittedMatrix) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}

		double[][] sfaMatrix = new double[this.dftMatrix.length][this.numberOfDesieredDFTCoefficients * 2];
		for (int instance = 0; instance < this.dftMatrix.length; instance++) {
			for (int entry = 0; entry < this.numberOfDesieredDFTCoefficients * 2; entry++) {
				double elem = this.dftMatrix[instance][entry];
				// get the lookup table for DFT values of the instance
				double[] lookup = this.lookUpTableMatrix[entry];

				// if the DFT coefficient is smaller than the first or larger than the last
				// or it lays on the border give it first, last or second ,penultimate
				if (elem < lookup[0]) {
					sfaMatrix[instance][entry] = this.alphabet[0];
				}
				if (elem == lookup[0]) {
					sfaMatrix[instance][entry] = this.alphabet[1];
				}
				if (elem > lookup[this.alphabet.length - 2]) {
					sfaMatrix[instance][entry] = this.alphabet[this.alphabet.length - 1];
				}
				if (elem == lookup[this.alphabet.length - 2]) {
					sfaMatrix[instance][entry] = this.alphabet[this.alphabet.length - 1];
				}
				// get alphabet letter for every non extrem coefficient
				else {
					for (int i = 1; i < lookup.length - 2; i++) {
						if (elem > lookup[i]) {
							sfaMatrix[instance][entry] = this.alphabet[i];
						}
						if (elem == lookup[i]) {
							sfaMatrix[instance][entry] = this.alphabet[i + 1];
						}
					}
				}
			}
		}

		return sfaMatrix;
	}

	/*
	 * Can not be called in the fit dataset method because it needs its own DFT-
	 * Filter and for the dataset an overall Filter is needed.
	 */
	@Override
	public void fit(final double[][] input) {
		if (input.length == 0) {
			throw new IllegalArgumentException(MSG_NOEMPTYDS);
		}

		if (this.alphabet.length == 0) {
			throw new IllegalArgumentException("The alphabet size can not be zero.");
		}

		DFT dftFilterMatrix = new DFT();

		// calculates the number of DFT coefficents with wordlength as number of desired DFT coefficients
		dftFilterMatrix.setNumberOfDisieredCoefficients(this.numberOfDesieredDFTCoefficients);
		if (!this.rekursiv) {
			this.dftMatrix = dftFilterMatrix.fitTransform(input);
		} else {
			this.dftMatrix = dftFilterMatrix.rekursivDFT(input);
		}

		this.lookUpTableMatrix = new double[this.numberOfDesieredDFTCoefficients * 2][this.alphabet.length - 1];

		for (int coeficient = 0; coeficient < this.numberOfDesieredDFTCoefficients * 2; coeficient++) {
			// get the columns of the DFT dataset
			double[] toBin = new double[input.length];
			for (int instances = 0; instances < input.length; instances++) {
				toBin[instances] = this.dftMatrix[instances][coeficient];
			}

			// Sort ascending
			// If the number of instances is equal to the number of bins the breakpoints are set to this values
			Arrays.sort(toBin);
			if (toBin.length == this.alphabet.length - 1) {
				for (int alphabetLetter = 0; alphabetLetter < this.alphabet.length - 1; alphabetLetter++) {
					this.lookUpTableMatrix[coeficient][alphabetLetter] = toBin[alphabetLetter];
				}
			}
			// If the number of instances is greater than the number of bins then the breakpoints are set
			// in the way that all coefficients are spread equally over the bins
			else {
				int splitValue = (int) Math.round(toBin.length / (double) this.alphabet.length);
				for (int alphabetLetter = 0; alphabetLetter < this.alphabet.length - 1; alphabetLetter++) {
					this.lookUpTableMatrix[coeficient][alphabetLetter] = toBin[alphabetLetter + splitValue];
				}
			}

		}

		this.fittedMatrix = true;

	}

	@Override
	public double[][] fitTransform(final double[][] input) {
		this.fit(input);
		return this.transform(input);
	}

}
