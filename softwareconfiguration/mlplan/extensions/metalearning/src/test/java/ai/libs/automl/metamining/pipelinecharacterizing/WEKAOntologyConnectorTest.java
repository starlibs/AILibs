package ai.libs.automl.metamining.pipelinecharacterizing;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import ai.libs.mlplan.metamining.pipelinecharacterizing.WEKAOntologyConnector;

@Disabled("This project is currently not maintained")
public class WEKAOntologyConnectorTest {

	private WEKAOntologyConnector connector;

	@Before
	public void initializeOntologyConnector() throws OWLOntologyCreationException {
		this.connector = new WEKAOntologyConnector();
	}

	@Test
	public void testGetAncestorsOfClassifiers() {
		this.connector.getAvailableClassifiers().forEach(classifier -> {
			List<String> ancestors = this.connector.getAncestorsOfAlgorithm(classifier);
			assertEquals(this.connector.getClassifierTopNode(), ancestors.get(0));
			assertEquals(classifier, ancestors.get(ancestors.size()-1));
		});
	}

	@Test
	public void testGetAncestorsOfEvaluators() {
		this.connector.getAvailableEvaluators().forEach(evaluator -> {
			List<String> ancestors = this.connector.getAncestorsOfAlgorithm(evaluator);
			assertEquals(this.connector.getEvaluatorTopNode(), ancestors.get(0));
			assertEquals(evaluator, ancestors.get(ancestors.size()-1));
		});
	}

	@Test
	public void testGetAncestorsOfSearchers() {
		this.connector.getAvailableSearchers().forEach(searcher -> {
			List<String> ancestors = this.connector.getAncestorsOfAlgorithm(searcher);
			assertEquals(this.connector.getSearcherTopNode(), ancestors.get(0));
			assertEquals(searcher, ancestors.get(ancestors.size()-1));
		});
	}

	@Test
	public void testGetAncestorsOfKernelFunctions() {
		this.connector.getAvailableKernelFunctions().forEach(searcher -> {
			List<String> ancestors = this.connector.getAncestorsOfAlgorithm(searcher);
			assertEquals(this.connector.getKernelFunctionTopNode(), ancestors.get(0));
			assertEquals(searcher, ancestors.get(ancestors.size()-1));
		});
	}
}
