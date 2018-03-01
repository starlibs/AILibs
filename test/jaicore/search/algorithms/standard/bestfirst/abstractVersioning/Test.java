package jaicore.search.algorithms.standard.bestfirst.abstractVersioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;

public class Test {

	@org.junit.Test
	public void test() {
		TestGraphGenerator gen = new TestGraphGenerator();
		
		BestFirst<TestNode,String> bf = new BestFirst<>(gen, n->(double)Math.round(Math.random()*50));
		
//		new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(n-> String.valueOf(n.getInternalLabel()));
		
		//set node numbering to false
		gen.setNodeNumbering(false);
		
		/*find the solution*/
		List<TestNode> solutionPath = bf.nextSolution();
		solutionPath.stream().forEach(n-> {
			assertEquals(n.getId(),-1);
		});
		
		
		/*second test now with numbering.
		 */
		gen.reset();
		bf = new BestFirst<>(gen, n->(double)Math.round(Math.random()*50));
//		new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(n-> String.valueOf(n.getInternalLabel()));
		gen.setNodeNumbering(true);
		List<TestNode> solutionPath2 = bf.nextSolution();
		Set<Integer> ids = new HashSet<Integer>();
		
		solutionPath2.stream().forEach(n ->{
			assertTrue(n.getId() > 0);
			assertFalse(ids.contains(n.getId()));
			
			ids.add(n.getId());
		});
		
		
	}

}
