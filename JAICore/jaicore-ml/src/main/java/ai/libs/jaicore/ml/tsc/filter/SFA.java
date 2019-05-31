package ai.libs.jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen Beierling
 *         c.f. p. 1511 p. 1510 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schäfer
 *         This class combines the MCB of finding the bins for a given set of DFT coefficients and SFA
 *         which selects the right letter for a DFT coefficient.
 */
public class SFA implements IFilter {

	private double[] alphabet;

	private boolean meanCorrected;

	private boolean fitted = false;
	private boolean fittedMatrix = false;

	private TimeSeriesDataset dFTDataset = null;
	private double[][] dftMatrix = null;

	private int numberOfDesieredDFTCoefficients;

	private DFT dftFilter = null;
	private DFT dftFilterMatrix = null;

	private ArrayList<double[][]> lookupTable = new ArrayList<double[][]>();
	private double[][] lookUpTableMatrix = null;

	private ArrayList<double[][]> sfaDataset = new ArrayList<double[][]>();
	private double[][] sfaMatrix = null;

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

	public SFA(final double[] alphabet, final int wordLength, final boolean meanCorrected) {
		this.alphabet = alphabet;

		// The wordlength must be even
		this.numberOfDesieredDFTCoefficients = wordLength / 2;
	}

	@Override
	public TimeSeriesDataset transform(final TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {

		if (input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if (!this.fitted) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}

		this.sfaDataset = new ArrayList<double[][]>();
		// calculate SFA words for every instance and its DFT coefficients
		for (int matrix = 0; matrix < this.dFTDataset.getNumberOfVariables(); matrix++) {
			double[][] sfaWords = new double[this.dFTDataset.getNumberOfInstances()][this.numberOfDesieredDFTCoefficients * 2];
			for (int instance = 0; instance < this.dFTDataset.getNumberOfInstances(); instance++) {
				for (int entry = 0; entry < this.numberOfDesieredDFTCoefficients * 2; entry++) {
					double elem = this.dFTDataset.getValues(matrix)[instance][entry];
					// get the lookup table for DFT values of the instance
					double lookup[] = this.lookupTable.get(matrix)[entry];

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
			this.sfaDataset.add(sfaWords);
		}
		TimeSeriesDataset output = new TimeSeriesDataset(this.sfaDataset, null, null);
		return output;
	}

	@Override
	public void fit(final TimeSeriesDataset input) {
		// TODO Auto-generated method stub

		if (input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}

		if (this.alphabet.length == 0) {
			throw new IllegalArgumentException("The alphabet size can not be zero.");
		}

		this.lookupTable.clear();

		this.dftFilter = new DFT();
		this.dftFilter.setMeanCorrected(this.meanCorrected);

		try {
			// calculates the number of DFT coefficents with wordlength as number of desired DFT coefficients
			this.dftFilter.setNumberOfDisieredCoefficients(this.numberOfDesieredDFTCoefficients);

			if (!this.rekursiv) {
				this.dFTDataset = this.dftFilter.fitTransform(input);
			} else {
				// Only works for sliding windows. However it is normally used for SFA.
				this.dFTDataset = this.dftFilter.rekursivDFT(input);
			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public TimeSeriesDataset fitTransform(final TimeSeriesDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		this.fit(input);
		return this.transform(input);
	}

	@Override
	public double[] transform(final double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {

		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}

	@Override
	public void fit(final double[] input) throws IllegalArgumentException {

		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}

	@Override
	public double[] fitTransform(final double[] input) throws IllegalArgumentException, NoneFittedFilterExeception {

		throw new UnsupportedOperationException("To build a SFA word the full dataset has to be considerd therefore it is not reasonable in this context to perform this operation on a single Instance.");
	}

	@Override
	public double[][] transform(final double[][] input) throws IllegalArgumentException, NoneFittedFilterExeception {
		if (input.length == 0) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if (!this.fittedMatrix) {
			throw new NoneFittedFilterExeception("The filter must be fitted before it can transform.");
		}

		this.sfaMatrix = new double[this.dftMatrix.length][this.numberOfDesieredDFTCoefficients * 2];
		for (int instance = 0; instance < this.dftMatrix.length; instance++) {
			for (int entry = 0; entry < this.numberOfDesieredDFTCoefficients * 2; entry++) {
				double elem = this.dftMatrix[instance][entry];
				// get the lookup table for DFT values of the instance
				double lookup[] = this.lookUpTableMatrix[entry];

				// if the DFT coefficient is smaller than the first or larger than the last
				// or it lays on the border give it first, last or second ,penultimate
				if (elem < lookup[0]) {
					this.sfaMatrix[instance][entry] = this.alphabet[0];
				}
				if (elem == lookup[0]) {
					this.sfaMatrix[instance][entry] = this.alphabet[1];
				}
				if (elem > lookup[this.alphabet.length - 2]) {
					this.sfaMatrix[instance][entry] = this.alphabet[this.alphabet.length - 1];
				}
				if (elem == lookup[this.alphabet.length - 2]) {
					this.sfaMatrix[instance][entry] = this.alphabet[this.alphabet.length - 1];
				}
				// get alphabet letter for every non extrem coefficient
				else {
					for (int i = 1; i < lookup.length - 2; i++) {
						if (elem > lookup[i]) {
							this.sfaMatrix[instance][entry] = this.alphabet[i];
						}
						if (elem == lookup[i]) {
							this.sfaMatrix[instance][entry] = this.alphabet[i + 1];
						}
					}
				}
			}
		}

		return this.sfaMatrix;
	}

	/*
	 * Can not be called in the fit dataset method because it needs its own DFT-
	 * Filter and for the dataset an overall Filter is needed.
	 */
	@Override
	public void fit(final double[][] input) throws IllegalArgumentException {
		if (input.length == 0) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}

		if (this.alphabet.length == 0) {
			throw new IllegalArgumentException("The alphabet size can not be zero.");
		}

		this.dftFilterMatrix = new DFT();

		try {
			// calculates the number of DFT coefficents with wordlength as number of desired DFT coefficients
			this.dftFilterMatrix.setNumberOfDisieredCoefficients(this.numberOfDesieredDFTCoefficients);
			if (!this.rekursiv) {
				this.dftMatrix = this.dftFilterMatrix.fitTransform(input);
			} else {
				this.dftMatrix = this.dftFilterMatrix.rekursivDFT(input);
			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
