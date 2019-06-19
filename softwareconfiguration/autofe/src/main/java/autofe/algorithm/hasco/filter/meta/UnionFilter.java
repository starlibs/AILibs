package autofe.algorithm.hasco.filter.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import autofe.util.DataSet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Union filter which merges two given data sets by concatenating the features.
 *
 * @author Julian Lienen
 *
 */
public class UnionFilter implements IFilter, IAbstractFilter, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5008187554401629128L;

	@Override
	public DataSet applyFilter(final DataSet inputData, final boolean copy) {
		if (copy) {
			return inputData.copy();
		} else {
			return inputData;
		}
	}

	static DataSet union(final DataSet coll1, final DataSet coll2) {
		if (coll1 == null || coll2 == null) {
			throw new IllegalArgumentException("Parameters 'coll1' and 'coll2' must not be null!");
		}

		if (coll1.getIntermediateInstances() == null || coll2.getIntermediateInstances() == null) {
			// Merge Weka instances
			Instances instances1 = coll1.getInstances();
			Instances instances2 = coll2.getInstances();

			if (instances1.numInstances() != instances2.numInstances()) {
				throw new IllegalArgumentException("Data sets to be united must have the same amount of instances!");
			}

			ArrayList<Attribute> attributes = new ArrayList<>(
					coll1.getInstances().numAttributes() + coll2.getInstances().numAttributes() - 1);
			for (int i = 0; i < instances1.numAttributes() - 1; i++) {
				attributes.add(instances1.attribute(i).copy(instances1.attribute(i).name() + "u1"));
			}
			for (int i = 0; i < instances2.numAttributes() - 1; i++) {
				attributes.add(instances2.attribute(i).copy(instances2.attribute(i).name() + "u2"));
			}

			// Add class attribute
			List<String> classValues = IntStream.range(0, instances1.classAttribute().numValues()).asDoubleStream()
					.mapToObj(String::valueOf).collect(Collectors.toList());
			Attribute classAtt = new Attribute("classAtt", classValues);
			attributes.add(classAtt);

			Instances unitedInstances = new Instances("UnitedInstances", attributes, instances1.numInstances());
			unitedInstances.setClassIndex(unitedInstances.numAttributes() - 1);

			for (int i = 0; i < instances1.numInstances(); i++) {
				Instance instance = new DenseInstance(attributes.size());
				instance.setDataset(unitedInstances);

				// Copy values
				int runningIndex = 0;
				for (int j = 0; j < instances1.numAttributes() - 1; j++) {
					instance.setValue(runningIndex++, instances1.get(i).value(j));
				}
				for (int j = 0; j < instances2.numAttributes() - 1; j++) {
					instance.setValue(runningIndex++, instances2.get(i).value(j));
				}
				instance.setClassValue(instances1.get(i).classValue());

				unitedInstances.add(instance);
			}

			return new DataSet(unitedInstances, null);
		} else {
			if (coll1.getIntermediateInstances().isEmpty() || coll2.getIntermediateInstances().isEmpty()) {
				throw new IllegalArgumentException("There must be intermediate instances if the collection is set.");
			}

			// Merge intermediate instances
			List<INDArray> intermediateInsts1 = coll1.getIntermediateInstances();
			List<INDArray> intermediateInsts2 = coll2.getIntermediateInstances();

			List<INDArray> unitedIntermediateInsts = new ArrayList<>(
					(int) (intermediateInsts1.get(0).length() + intermediateInsts2.get(0).length()));
			for (int i = 0; i < intermediateInsts1.size(); i++) {
				INDArray intermediateInst = Nd4j.hstack(intermediateInsts1.get(i).ravel(),
						intermediateInsts2.get(i).ravel());
				unitedIntermediateInsts.add(intermediateInst);
			}

			return new DataSet(coll1.getInstances(), unitedIntermediateInsts);
		}
	}

	@Override
	public String toString() {
		return "UnionFilter []";
	}

	@Override
	public UnionFilter clone() throws CloneNotSupportedException {
		super.clone();
		return new UnionFilter();
	}
}
