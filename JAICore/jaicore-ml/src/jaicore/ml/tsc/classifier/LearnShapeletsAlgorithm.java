package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;

/**
 * Generalized Shapelets Learning implementation for
 * <code>LearnShapeletsClassifier</code> published in "J. Grabocka, N.
 * Schilling, M. Wistuba, L. Schmidt-Thieme: Learning Time-Series Shapelets"
 * (https://www.ismll.uni-hildesheim.de/pub/pdfs/grabocka2014e-kdd.pdf)
 * 
 * @author Julian Lienen
 *
 */
public class LearnShapeletsAlgorithm implements IAlgorithm<IDataset, LearnShapeletsClassifier> {

	private int K;
	private double learningRate;
	private double regularization;
	private int scaleR;
	private int minShapeLength;
	private int maxIter;

	private int I;
	private int Q;
	private int C;

	private double alpha = -100d;

	public LearnShapeletsAlgorithm(final int K, final double learningRate, final double regularization,
			final int scaleR, final int minShapeLength, final int maxIter) {
		this.K = K;
		this.learningRate = learningRate;
		this.regularization = regularization;
		this.scaleR = scaleR;
		this.minShapeLength = minShapeLength;
		this.maxIter = maxIter;

	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlgorithmEvent next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LearnShapeletsClassifier call() throws Exception {
		// TODO Auto-generated method stub
		// Training

		LearnShapeletsClassifier classifier = new LearnShapeletsClassifier();

		IDataset data = this.getInput();
		INDArray dataMatrix = null; // TODO

		this.I = data.getNumberOfAttributes(); // I
		this.Q = data.size(); // Q
		this.C = 0; // TODO: C

		// TODO: Prepare binary classes
		INDArray Y = Nd4j.create(this.I, this.C);

		// TODO: Initialization
		List<INDArray> S = new ArrayList<>();
		List<INDArray> D = new ArrayList<>();
		List<INDArray> Xi = new ArrayList<>();
		List<INDArray> Phi = new ArrayList<>();
		for (int r = 0; r < this.scaleR; r++) {
			final int numberOfSegments = getNumberOfSegments(r);
			S.add(Nd4j.create(K, r * this.minShapeLength));
			D.add(Nd4j.create(this.I, this.K, numberOfSegments));
			Xi.add(Nd4j.create(this.I, this.K, numberOfSegments));
			Phi.add(Nd4j.create(this.I, this.K, numberOfSegments));
		}

		// TODO: Check correct order of shape parameters of W => Current version is the
		// paper's version but doesn't match with the allocated matrix's shape
		INDArray W = Nd4j.create(this.C, this.scaleR, this.K);
		INDArray W_0 = Nd4j.create(this.C);

		INDArray Psi = Nd4j.create(this.scaleR, this.I, this.K);
		INDArray M_hat = Nd4j.create(this.scaleR, this.I, this.K);
		INDArray Theta = Nd4j.create(this.I, this.C);

		for (int it = 0; it < this.maxIter; it++) {
			for (int i = 0; i < this.Q; i++) {
				// Pre-compute terms
				for (int r = 0; r < this.scaleR; r++) {
					for (int k = 0; k < this.K; k++) {
						for (int j = 0; j < getNumberOfSegments(r); j++) {
							double newDValue = 0;
							for (int l = 0; l < r * this.minShapeLength; l++) {
								newDValue += Math.pow(dataMatrix.getDouble(i, j + l - 1) - S.get(r).getDouble(k, l), 2);
							}
							newDValue /= r * this.minShapeLength;

							D.get(r).putScalar(new int[] { i, k, j }, newDValue);
							Xi.get(r).putScalar(new int[] { i, k, j }, Math.exp(this.alpha * newDValue));
						}
						double newPsiValue = 0;
						double newMHatValue = 0;
						for (int j = 0; j < getNumberOfSegments(r); j++) {
							newPsiValue += Xi.get(r).getDouble(i, k, j);
							newMHatValue += D.get(r).getDouble(i, k, j) * Xi.get(r).getDouble(i, k, j);
						}
						Psi.putScalar(new int[] { r, i, k }, newPsiValue);

						newMHatValue /= Psi.getDouble(r, i, k);
						M_hat.putScalar(new int[] { r, i, k }, newMHatValue);
					}
				}

				for (int c = 0; c < this.C; c++) {
					double newThetaValue = 0;
					for (int r = 0; r < this.scaleR; r++) {
						for (int k = 0; k < this.K; k++) {

							newThetaValue += M_hat.getDouble(r, i, k) * W.getDouble(c, r, k);
						}
					}
					newThetaValue = Y.getDouble(i, c) - sigmoid(newThetaValue);
					Theta.putScalar(new int[] { i, c }, newThetaValue);
				}

				// Learn shapelets and classification weights
				for (int c = 0; c < this.C; c++) {
					for (int r = 0; r < this.scaleR; r++) {
						for (int k = 0; k < this.K; k++) {
							double wStep = Theta.getDouble(i, c) * M_hat.getDouble(r, i, k)
									- 2 * this.regularization / (this.I * this.C) * W.getDouble(c, r, k);

							W.putScalar(new int[] { c, r, k }, W.getDouble(c, r, k) + this.learningRate * wStep);

							for (int j = 0; j < getNumberOfSegments(r); j++) {
								double newPhiValue = 2 * Xi.get(r).getDouble(i, k, j)
										* (1 + this.alpha * (D.get(r).getDouble(i, k, j) - M_hat.getDouble(r, i, k)));
								newPhiValue /= r * this.minShapeLength * Psi.getDouble(r, i, k);
								Phi.get(r).putScalar(new int[] { i, k, j }, newPhiValue);

								for (int l = 0; l < r * this.scaleR; l++) {
									double sStep = Theta.getDouble(i, c) * Phi.get(r).getDouble(i, k, j)
											* (S.get(r).getDouble(k, l) - dataMatrix.getDouble(i, j + l - 1))
											* W.getDouble(c, r, k);
									S.get(r).putScalar(new int[] { k, l },
											S.get(r).getDouble(k, l) + this.learningRate * sStep);
								}
							}
						}
					}
					W_0.putScalar(c, W_0.getDouble(c) + this.learningRate * Theta.getDouble(i, c));
				}
			}
		}

		return classifier;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public IDataset getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerListener(Object listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(TimeOut timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public TimeOut getTimeout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private int getNumberOfSegments(final int r) {
		return this.Q - r * this.minShapeLength + 1;
	}

	// TODO: Maybe move to utility? Or use library?
	private static double sigmoid(final double z) {
		return 1 / (1 + Math.exp((-1) * z));
	}
}
