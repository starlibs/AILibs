package jaicore.planning.graphgenerators.pddl;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import org.junit.Test;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.hsp.Node;
import fr.uga.pddl4j.util.BitState;
import jaicore.search.algorithms.standard.astar.AStar;
import jaicore.search.algorithms.standard.core.ParentDiscarding;
import jaicore.search.structure.core.GraphGenerator;

public class PDDLGraphGeneratorTester {

	@Test
	public void test() {
		File domain = null;
		File problem = null;
		
//		JFileChooser chooser = new JFileChooser();
//		chooser.setDialogTitle(" Open a Domain-File");
//		chooser.showOpenDialog(null);
//		domain = chooser.getSelectedFile();
		domain = new File("F:/Desktop/pddl4j/pddl/blocksworld/domain.pddl");
		System.out.println(domain.getAbsolutePath());
		
//		chooser.setDialogTitle("Open a Problme-File");
//		chooser.showOpenDialog(null);
//		problem = chooser.getSelectedFile();
		problem = new File("F:/Desktop/pddl4j/pddl/blocksworld/p15.pddl");
		
		if(domain.exists() && problem.exists()) {
			PDDLGraphGenerator gen = new PDDLGraphGenerator(domain, problem);
		
			//create a Astar-serch with the heuristic given pddl4j
			AStar<Node,String> search = new AStar<>(gen, (s1,s2)-> 1.0, state ->(double)gen.getHeuristic().estimate(state.getPoint(),gen.getProblem().getGoal()),ParentDiscarding.ALL);
		
			
			List<Node> solution = search.nextSolution();
			assertNotNull(solution);
		}
		else
			System.out.println("Either the domain file was not found or the problem file");
			assertTrue(true);
	}

}
