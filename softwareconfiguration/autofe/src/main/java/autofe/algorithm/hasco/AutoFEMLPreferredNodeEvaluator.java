package autofe.algorithm.hasco;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.Util;
import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.model.travesaltree.Node;
import ai.libs.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import ai.libs.mlplan.multiclass.wekamlplan.weka.model.SupervisedFilterSelector;

public class AutoFEMLPreferredNodeEvaluator implements INodeEvaluator<TFDNode, Double> {

	private static final Logger logger = LoggerFactory.getLogger(AutoFEMLPreferredNodeEvaluator.class);

	protected static final double ATT_COUNT_PENALTY = 1;

	public static final double MAX_EVAL_VALUE = 20000d;

	private Collection<Component> components;
	private AutoFEWekaPipelineFactory factory;

	// Maximum size of a pipeline
	protected int maxPipelineSize;

	private List<String> classifiers;

	public AutoFEMLPreferredNodeEvaluator(final Collection<Component> components, final AutoFEWekaPipelineFactory factory, final int maxPipelineSize) throws IOException {
		this.components = components;
		this.maxPipelineSize = maxPipelineSize;
		this.factory = factory;
		this.classifiers = FileUtils.readLines(new File("model/weka/precedenceList.txt"), Charset.defaultCharset());
	}

	public AutoFEWekaPipeline getPipelineFromNode(final Node<TFDNode, ?> node) throws ComponentInstantiationFailedException {
		return this.factory.getComponentInstantiation(this.getComponentInstanceFromNode(node));
	}

	public AutoFEWekaPipeline getPipelineFromComponentInstance(final ComponentInstance ci) throws ComponentInstantiationFailedException {
		return this.factory.getComponentInstantiation(ci);
	}

	public ComponentInstance getComponentInstanceFromNode(final Node<TFDNode, ?> node) {
		if (this.components == null || this.factory == null) {
			throw new IllegalArgumentException("Collection of components and factory need to be set to make node evaluators work.");
		}

		return Util.getSolutionCompositionFromState(this.components, node.getPoint().getState(), true);
	}

	@Override
	public Double f(final Node<TFDNode, ?> node) throws NodeEvaluationException {
		if (node.getParent() == null) {
			return 0.0;
		}

		/* get partial component */
		ComponentInstance ci = Util.getSolutionCompositionFromState(this.components, node.getPoint().getState(), true);

		AutoFEWekaPipeline pipe;
		try {
			pipe = this.getPipelineFromComponentInstance(ci);
		} catch (ComponentInstantiationFailedException e) {
			throw new NodeEvaluationException(e, "Node evaluation failed due to error in pipeline construction.");
		}
		logger.trace("Todo has algorithm selection tasks. Calculate node evaluation for {}.", pipe);

		List<String> remainingASTasks = node.getPoint().getRemainingTasks().stream().map(Literal::getProperty).filter(x -> x.startsWith("1_")).collect(Collectors.toList());
		String appliedMethod = (node.getPoint().getAppliedMethodInstance() != null ? node.getPoint().getAppliedMethodInstance().getMethod().getName() : "");
		logger.trace("Remaining AS Tasks: {}. Applied method: {}", remainingASTasks, appliedMethod);
		boolean toDoHasAlgorithmSelection = node.getPoint().getRemainingTasks().stream().anyMatch(x -> x.getProperty().startsWith("1_"));

		if (toDoHasAlgorithmSelection) {
			if (pipe != null) {
				if (pipe.getMLPipeline() != null) {
					MLPipeline pipeline = (MLPipeline) pipe.getMLPipeline();
					if (!pipeline.getPreprocessors().isEmpty()) {
						for (SupervisedFilterSelector preprocessor : pipeline.getPreprocessors()) {
							String evaluator = preprocessor.getEvaluator().getClass().getName();
							String searcher = preprocessor.getSearcher().getClass().getName();

							boolean isSetEvaluator = evaluator.toLowerCase().matches(".*(relief|gainratio|principalcomponents|onerattributeeval|infogainattributeeval|correlationattributeeval|symmetricaluncertattributeeval).*");
							boolean isRanker = searcher.toLowerCase().contains("ranker");
							boolean isNonRankerEvaluator = evaluator.toLowerCase().matches(".*(cfssubseteval).*");

							if (isSetEvaluator && !isRanker || isNonRankerEvaluator && isRanker) {
								throw new IllegalArgumentException("The given combination of searcher and evaluator cannot be benchmarked since they are incompatible.");
							}

						}
					}
				}
				if (pipe.getFilterPipeline() != null && pipe.getFilterPipeline().getFilters() != null) {
					return this.calculateScoreForFilterPipeline(pipe);
				}

				if (ci != null && ci.getSatisfactionOfRequiredInterfaces().containsKey("mlPipeline")) {
					ComponentInstance mlPipeline = ci.getSatisfactionOfRequiredInterfaces().get("mlPipeline");
					String classifierName = "";
					double score = 0.0;
					if (mlPipeline.getComponent().getProvidedInterfaces().contains("MLPipeline") && mlPipeline.getSatisfactionOfRequiredInterfaces().containsKey("classifier")) {
						classifierName = mlPipeline.getSatisfactionOfRequiredInterfaces().get("classifier").getComponent().getName();
						score += this.classifiers.size() + 1;
					} else if (!mlPipeline.getComponent().getProvidedInterfaces().contains("MLPipeline")) {
						classifierName = mlPipeline.getComponent().getName();
					}

					if (classifierName.equals("")) {
						return 0.0;
					}

					int index = this.classifiers.indexOf(classifierName);
					if (index > 0) {
						score += index;
					} else {
						score += this.classifiers.size() + 1;
					}
					return score / 10000;

				}
			}
			logger.trace("Still in algorithm selection phase => do BFS");
			return 0.0;
		} else {
			logger.trace("Algorithm configuration stage => do random subsamples");
			return null;
		}
	}

	private double calculateScoreForFilterPipeline(final AutoFEWekaPipeline pipe) {
		if (pipe.getFilterPipeline().getFilters().getItems().size() > this.maxPipelineSize) {
			logger.debug("We exceed the maximum number of image filters, so return {}", MAX_EVAL_VALUE);
			return MAX_EVAL_VALUE;
		}

		double numFilters = pipe.getFilterPipeline().getFilters().getItems().size();
		String classifierName;
		boolean isMLPipeline = false;
		if (pipe.getMLPipeline() instanceof MLPipeline) {
			isMLPipeline = true;
			classifierName = ((MLPipeline) pipe.getMLPipeline()).getBaseClassifier().getClass().getName();
		} else if (pipe.getMLPipeline() != null) {
			classifierName = pipe.getMLPipeline().getClass().getName();
		} else {
			// No ML pipeline
			return 0.0;
		}

		double indexOfClassifierName = this.classifiers.indexOf(classifierName);
		if (indexOfClassifierName < 0) {
			indexOfClassifierName = this.classifiers.size() + 1d;
		}

		double score = indexOfClassifierName;
		if (isMLPipeline) {
			score += this.classifiers.size() + 1;
		}

		score += numFilters * (this.classifiers.size() + 1d * 2d);
		score /= 100000d;

		return score;
	}
}
