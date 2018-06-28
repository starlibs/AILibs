package autofe.algorithm.hasco.filter.generic;

import java.util.Enumeration;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class AddConstantFilter implements IFilter {

	private double constant = 1;

	@Override
	public DataSet applyFilter(DataSet inputData, final boolean copy) {
		System.out.println("Applying filter AddConstantFilter...");

		// TODO: Check for copy flag

		// Assume to deal with FastBitmap instances
//		List<Instance> transformedInstances = new ArrayList<>(inputData.getInstances().size());
		Instances transformedInstances = new Instances(inputData.getInstances());
		for (Instance inst : inputData.getInstances()) {
			Instance newInstance = new DenseInstance(inst);
			newInstance.setDataset(transformedInstances);
			Enumeration<Attribute> atts = newInstance.enumerateAttributes();
			while (atts.hasMoreElements()) {
				Attribute att = atts.nextElement();
				if (att.isNumeric())
					newInstance.setValue(att, newInstance.value(att) + this.constant);
			}
			transformedInstances.add(newInstance);

			// TODO: Set class attribute (maybe not necessary because of used dense
			// instances constructor)
		}

		System.out.println("Finished applying filter AddConstantFilter.");

		return new DataSet(transformedInstances, null);
	}

	public double getConstant() {
		return constant;
	}

	public void setConstant(double constant) {
		this.constant = constant;
	}
}
