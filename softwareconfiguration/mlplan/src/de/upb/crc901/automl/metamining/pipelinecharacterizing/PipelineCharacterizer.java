package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import de.upb.crc901.automl.pipeline.basic.SupervisedFilterSelector;
import treeminer.FrequentSubtreeFinder;
import treeminer.TreeMiner;
import treeminer.TreeRepresentationUtils;

public class PipelineCharacterizer implements IPipelineCharacterizer {

	private FrequentSubtreeFinder treeMiner = new TreeMiner();
	private IOntologyConnector ontologyConnector = new WEKAOntologyConnector();
	private String[] patterns;
	private int minSupport;
	private String preprocessorSubTreeName = "Preprocessor";
	private String preprocessorsSubTreeName = "Preprocessors";
	private String pipelineTreeName = "Pipeline";

	@Override
	public void build(List<MLPipeline> pipelines) {
		// Convert the pipelines to String representations
		List<String> pipelineRepresentations = new ArrayList<String>();
		pipelines.forEach(pipeline -> {
			pipelineRepresentations.add(makeStringTreeRepresentation(pipeline));
		});

		// Use the tree miner to find patterns
		treeMiner.findFrequentSubtrees(pipelineRepresentations, minSupport);
	}

	@Override
	public double[] characterize(MLPipeline pipeline) {
		// Make tree representation from this pipeline
		String treeRepresentation = makeStringTreeRepresentation(pipeline);

		// Ask the treeMiner which of the patterns are included in this pipeline
		double[] pipelineCharacterization = new double[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			if (TreeRepresentationUtils.containsSubtree(treeRepresentation, patterns[i])) {
				pipelineCharacterization[i] = 1;
			} else {
				pipelineCharacterization[i] = 0;
			}
		}

		return pipelineCharacterization;
	}

	/**
	 * Converts the given MLPipeline to a String representation of its components
	 * using the ontology
	 * 
	 * @param pipeline
	 * @return
	 */
	protected String makeStringTreeRepresentation(MLPipeline pipeline) {
		// TODO add hyperparameters of the algorithms

		// Get annotations for preprocessors
		List<String> preprocessorsSubTree = new ArrayList<String>();
		List<SupervisedFilterSelector> preprocessors = pipeline.getPreprocessors();
		preprocessors.forEach(preprocessor -> {
			// Get searcher annotation
			String searcher = preprocessor.getSearcher().getClass().getName();
			List<String> searcherBranch = ontologyConnector.getParentsOfSearcher(searcher);
			String searcherBranchRepresentation = TreeRepresentationUtils.makeRepresentationForBranch(searcherBranch);

			// Get evaluator annotation
			String evaluator = preprocessor.getEvaluator().getClass().getName();
			List<String> evaluatorBranch = ontologyConnector.getParentsOfEvaluator(evaluator);
			String evaluatorBranchRepresentation = TreeRepresentationUtils.makeRepresentationForBranch(evaluatorBranch);

			// Merge both annotations
			String preprocessorSubTree = TreeRepresentationUtils.addChildrenToNode(preprocessorSubTreeName,
					Arrays.asList(searcherBranchRepresentation, evaluatorBranchRepresentation));
			preprocessorsSubTree.add(preprocessorSubTree);
		});
		// Merge preprocessors
		String preprocessorsSubTreeRepresentation = TreeRepresentationUtils.addChildrenToNode(preprocessorsSubTreeName,
				preprocessorsSubTree);

		// Get annotations for classifier
		String classifier = pipeline.getBaseClassifier().getClass().getName();
		List<String> classifierBranch = ontologyConnector.getParentsOfClassifier(classifier);
		String classifierBranchRepresentation = TreeRepresentationUtils.makeRepresentationForBranch(classifierBranch);

		// Merge preprocessors and classifiers
		return TreeRepresentationUtils.addChildrenToNode(pipelineTreeName,
				Arrays.asList(preprocessorsSubTreeRepresentation, classifierBranchRepresentation));
	}

	@Override
	public double[][] getCharacterizationsOfTrainingExamples() {
		// TODO Auto-generated method stub
		// TODO maybe adjust return parameter type here
		return null;
	}

	/**
	 * @return the ontologyConnector
	 */
	public IOntologyConnector getOntologyConnector() {
		return ontologyConnector;
	}

	/**
	 * @param ontologyConnector
	 *            the ontologyConnector to set
	 */
	public void setOntologyConnector(IOntologyConnector ontologyConnector) {
		this.ontologyConnector = ontologyConnector;
	}

	public int getMinSupport() {
		return minSupport;
	}

	public void setMinSupport(int minSupport) {
		this.minSupport = minSupport;
	}

}
