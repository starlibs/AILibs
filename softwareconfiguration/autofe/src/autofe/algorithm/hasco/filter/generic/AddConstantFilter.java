package autofe.algorithm.hasco.filter.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import autofe.algorithm.hasco.filter.meta.IFilter;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class AddConstantFilter implements IFilter<Instance> {

	private double constant;

	@Override
	public Collection<Instance> applyFilter(Collection<Instance> inputData, final boolean copy) {
		System.out.println("Applying filter AddConstantFilter...");

		// TODO: Check for copy flag

		// Assume to deal with FastBitmap instances
		List<Instance> transformedInstances = new ArrayList<>(inputData.size());
		for (Instance inst : (Collection<Instance>) inputData) {
			Instance newInstance = new DenseInstance(inst);
			Enumeration<Attribute> atts = newInstance.enumerateAttributes();
			while (atts.hasMoreElements()) {
				Attribute att = atts.nextElement();
				if (att.isNumeric())
					newInstance.setValue(att, newInstance.value(att) + 1);
			}
			transformedInstances.add(newInstance);
		}

		System.out.println("Finished applying filter AddConstantFilter.");

		return transformedInstances;
	}

	public double getConstant() {
		return constant;
	}

	public void setConstant(double constant) {
		this.constant = constant;
	}
}
