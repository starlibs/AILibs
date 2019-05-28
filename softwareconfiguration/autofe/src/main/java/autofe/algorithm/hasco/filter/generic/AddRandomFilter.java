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
public class AddRandomFilter implements IFilter {

    private double[] randomValues = null;

    private Random random = new Random();

    @Override
    public DataSet applyFilter(DataSet inputData, boolean copy) {
        if (this.randomValues == null) {
            randomValues = new double[inputData.getInstances().numAttributes() - 1];
            for (int i = 0; i < randomValues.length; i++) {
                randomValues[i] = random.nextDouble() * 1000;
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
                if (att.isNumeric())
                    inst.setValue(att, inst.value(att) + this.randomValues[i]);
            }
        }

        return new DataSet(transformedInstances, inputData.getIntermediateInstances());
    }

    @Override
    public AddRandomFilter clone() throws CloneNotSupportedException {
        super.clone();
        return new AddRandomFilter();
    }
}
