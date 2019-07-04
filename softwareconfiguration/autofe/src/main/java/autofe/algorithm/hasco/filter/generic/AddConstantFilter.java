package autofe.algorithm.hasco.filter.generic;

import java.util.Enumeration;

import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Simple filter adding a constant value to each instance attribute value.
 *
 * @author Julian Lienen
 */
public class AddConstantFilter implements IFilter, Cloneable {

	private double constant = 1;

	public AddConstantFilter() {
		super();
	}

	public AddConstantFilter(final double constant) {
		super();
		this.constant = constant;
	}

	public AddConstantFilter(final AddConstantFilter objectToCopy) {
		this.constant = objectToCopy.constant;
	}

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) {

		Instances transformedInstances;
		if (copy) {
			transformedInstances = new Instances(inputData.getInstances());
		} else {
			transformedInstances = inputData.getInstances();
		}
		for (Instance inst : transformedInstances) {
			Enumeration<Attribute> atts = inst.enumerateAttributes();
			while (atts.hasMoreElements()) {
				Attribute att = atts.nextElement();
				if (att.isNumeric()) {
					inst.setValue(att, inst.value(att) + this.constant);
				}
			}
		}

		return new DataSet(transformedInstances, inputData.getIntermediateInstances());
	}

	public double getConstant() {
		return this.constant;
	}

	public void setConstant(final double constant) {
		this.constant = constant;
	}

	@Override
	public AddConstantFilter clone() {
		return new AddConstantFilter(this);
	}
}
