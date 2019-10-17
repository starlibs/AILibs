package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.classification.IClassifierEvaluator;
import org.api4.java.ai.ml.classification.execution.ClassifierExecutionFailedException;
import org.api4.java.ai.ml.classification.execution.IClassifierMetric;
import org.api4.java.ai.ml.classification.execution.IClassifierRunReport;
import org.api4.java.ai.ml.classification.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.classification.execution.IFixedDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

public class ExecutionBasedClassifierEvaluator implements IClassifierEvaluator<IClassifier<?, ?>> {

	private class GenericEncapsulation<I extends ILabeledInstance, D extends ILabeledDataset<I>> {

		private final IFixedDatasetSplitSetGenerator<D> splitGenerator;
		private final ClassifierExecutor<I, D> executor = new ClassifierExecutor<>();
		private final IClassifierMetric metric;

		public Double evaluate(final IClassifier<I, D> classifier) throws InterruptedException, ObjectEvaluationFailedException {
			try {
				IDatasetSplitSet<D> splitSet = this.splitGenerator.nextSplitSet();
				if (splitSet.getNumberOfFoldsPerSplit() != 2) {
					throw new IllegalStateException("Number of folds for each split should be 2 but is " + splitSet.getNumberOfFoldsPerSplit() + "!");
				}
				int n = splitSet.getNumberOfSplits();
				List<IClassifierRunReport> reports = new ArrayList<>(n);
				for (int i = 0; i < n; i++) {
					List<D> folds = splitSet.getFolds(i);
					reports.add(this.executor.execute(classifier, folds.get(0), folds.get(1)));
				}
				return this.metric.evaluateToDouble(reports);
			} catch (ClassifierExecutionFailedException | SplitFailedException e) {
				throw new ObjectEvaluationFailedException(e);
			}
		}

		public GenericEncapsulation(final IFixedDatasetSplitSetGenerator<D> splitGenerator, final IClassifierMetric metric) {
			super();
			this.splitGenerator = splitGenerator;
			this.metric = metric;
		}
	}

	@SuppressWarnings("rawtypes")
	private final GenericEncapsulation encapsulation;

	public <I extends ILabeledInstance, D extends ILabeledDataset<I>> ExecutionBasedClassifierEvaluator(final IFixedDatasetSplitSetGenerator<D> splitGenerator, final IClassifierMetric metric) {
		super();
		this.encapsulation = new GenericEncapsulation<>(splitGenerator, metric);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Double evaluate(final IClassifier<?, ?> object) throws InterruptedException, ObjectEvaluationFailedException {
		return this.encapsulation.evaluate(object);
	}
}
