package autofe.algorithm.hasco.filter.generic;

import java.util.Enumeration;
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
 *
 */
public class AddRandomFilter implements IFilter {

	@Override
	public DataSet applyFilter(DataSet inputData, boolean copy) {
		Random random = new Random();

		Instances transformedInstances;
		if (copy)
			transformedInstances = new Instances(inputData.getInstances());
		else
			transformedInstances = inputData.getInstances();
		for (Instance inst : transformedInstances) {
			Enumeration<Attribute> atts = inst.enumerateAttributes();
			while (atts.hasMoreElements()) {
				Attribute att = atts.nextElement();
				if (att.isNumeric())
					inst.setValue(att, inst.value(att) + random.nextInt(10000));
			}
		}

		return new DataSet(transformedInstances, inputData.getIntermediateInstances());
	}

}
