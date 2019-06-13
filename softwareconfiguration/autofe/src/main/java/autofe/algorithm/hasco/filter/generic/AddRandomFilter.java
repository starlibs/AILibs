package autofe.algorithm.hasco.filter.generic;

import java.util.Random;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Simple filter adding noise to each instance value (to deteriorate results).
 *
 * @author Julian Lienen
 */
public class AddRandomFilter implements IFilter, Cloneable {

	private double[] randomValues = null;

	private Random random = new Random();

	public AddRandomFilter() {

		/* just assume default values */
	}

	public AddRandomFilter(final AddRandomFilter objectToCopy) {
		this.randomValues = objectToCopy.randomValues;
	}

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) {
		if (this.randomValues == null) {
			this.randomValues = new double[inputData.getInstances().numAttributes() - 1];
			for (int i = 0; i < this.randomValues.length; i++) {
				this.randomValues[i] = this.random.nextDouble() * 1000;
			}
		}

		Instances transformedInstances;
		if (copy) {
			transformedInstances = new Instances(inputData.getInstances());
		} else {
			transformedInstances = inputData.getInstances();
		}
		for (Instance inst : transformedInstances) {
			for (int i = 0; i < inst.numAttributes() - 1; i++) {
				Attribute att = inst.attribute(i);
				if (att.isNumeric()) {
					inst.setValue(att, inst.value(att) + this.randomValues[i]);
				}
			}
		}

		return new DataSet(transformedInstances, inputData.getIntermediateInstances());
	}

	@Override
	public AddRandomFilter clone() {
		return new AddRandomFilter(this);
	}
}
