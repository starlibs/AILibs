package de.upb.crc901.automl.metamining.pipelinecharacterizing;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.upb.crc901.mlplan.metamining.pipelinecharacterizing.WEKAOntologyConnector;

public class WEKAOntologyConnectorTest {
	
	private WEKAOntologyConnector connector;
	
	@Before
	public void initializeOntologyConnector() throws OWLOntologyCreationException {
		connector = new WEKAOntologyConnector();
	}
	
	@Test
	public void testGetAncestorsOfClassifiers() {
		connector.getAvailableClassifiers().forEach(classifier -> {
			List<String> ancestors = connector.getAncestorsOfAlgorithm(classifier);
			assertEquals(ancestors.get(0), connector.getClassifierTopNode());
			assertEquals(ancestors.get(ancestors.size()-1), classifier);
		});
	}
	
	@Test
	public void testGetAncestorsOfEvaluators() {
		connector.getAvailableEvaluators().forEach(evaluator -> {
			List<String> ancestors = connector.getAncestorsOfAlgorithm(evaluator);
			assertEquals(ancestors.get(0), connector.getEvaluatorTopNode());
			assertEquals(ancestors.get(ancestors.size()-1), evaluator);
		});
	}
	
	@Test
	public void testGetAncestorsOfSearchers() {
		connector.getAvailableSearchers().forEach(searcher -> {
			List<String> ancestors = connector.getAncestorsOfAlgorithm(searcher);
			assertEquals(ancestors.get(0), connector.getSearcherTopNode());
			assertEquals(ancestors.get(ancestors.size()-1), searcher);
		});
	}
}
