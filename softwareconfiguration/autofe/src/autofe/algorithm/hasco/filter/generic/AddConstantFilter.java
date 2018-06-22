package autofe.algorithm.hasco.filter.generic;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class AddConstantFilter implements IFilter<Instance> {

	private double constant;

	@Override
	public DataSet<Instance> applyFilter(DataSet<Instance> inputData, final boolean copy) {
		System.out.println("Applying filter AddConstantFilter...");

		// TODO: Check for copy flag

		// Assume to deal with FastBitmap instances
		List<Instance> transformedInstances = new ArrayList<>(inputData.getInstances().size());
		for (Instance inst : inputData.getInstances()) {
			Instance newInstance = new DenseInstance(inst);
			Enumeration<Attribute> atts = newInstance.enumerateAttributes();
			while (atts.hasMoreElements()) {
				Attribute att = atts.nextElement();
				if (att.isNumeric())
					newInstance.setValue(att, newInstance.value(att) + 1);
			}
			transformedInstances.add(newInstance);

			// TODO: Set class attribute (maybe not necessary because of used dense
			// instances constructor)
		}

		System.out.println("Finished applying filter AddConstantFilter.");

		return new DataSet<Instance>(transformedInstances, null);
	}

	public double getConstant() {
		return constant;
	}

	public void setConstant(double constant) {
		this.constant = constant;
	}
}
