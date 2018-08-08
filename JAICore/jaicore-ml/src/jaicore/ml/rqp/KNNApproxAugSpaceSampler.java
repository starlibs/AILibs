package jaicore.ml.rqp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import be.tarsos.lsh.LSH;
import be.tarsos.lsh.Vector;
import be.tarsos.lsh.families.EuclidianHashFamily;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Samples interval-valued data from a dataset of precise points.
 * First chooses one point uniformly at random and then generates 
 * @author michael
 *
 */
public class KNNApproxAugSpaceSampler extends AbstractAugmentedSpaceSampler {
	
	// Number of hashes. 
	// Default value recommended by author of TarsosLSH is 4.
	private static final int N_HASHES = 4;
	// Number of hash tables, each with N_HASHES.
	// Default value recommended by author of TarsosLSH is 4.
	private static final int N_TABLES = 4;
	
	private int k;
	private LSH lsh;
	
	/**
	 * @param preciseInsts
	 * @param rng
	 * @param k
	 * @param w		Parameter for LSH hash family: 
	 */
	public KNNApproxAugSpaceSampler(Instances preciseInsts, Random rng, int k, int w) {
		super(preciseInsts, rng);
		this.k = k;
		
		List<Vector> instsVectors = new ArrayList<Vector>(preciseInsts.size());
		for (Instance inst : preciseInsts) {
			InstanceVectorLSH instVector = new InstanceVectorLSH(inst);
			instsVectors.add(instVector);
		}
		
		int dimensions = preciseInsts.numAttributes() - 1;
		this.lsh = new LSH(instsVectors, new EuclidianHashFamily(w, dimensions));
		this.lsh.buildIndex(N_HASHES, N_TABLES);
	}

	@Override
	public Instance augSpaceSample() {
		Instances preciseInsts = this.getPreciseInsts();
		int numInsts = preciseInsts.size();
		
		InstanceVectorLSH x = new InstanceVectorLSH(preciseInsts.get(this.getRng().nextInt(numInsts)));
		List<Vector> kNNsVectors = lsh.query(x, k);
		List<Instance> kNNs = new ArrayList<Instance>(k);
		for (Vector vec : kNNsVectors) {
			InstanceVectorLSH instVec = (InstanceVectorLSH) vec;
			kNNs.add(instVec.getInst());
		}
		
		return generateAugPoint(kNNs);
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

}
