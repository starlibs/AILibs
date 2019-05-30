package jaicore.planning.graphgenerators.pddl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import org.junit.Test;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.hsp.HSP;
import fr.uga.pddl4j.util.Plan;
import fr.uga.pddl4j.util.SequentialPlan;
import jaicore.search.algorithms.standard.astar.AStar;

public class PDDLGraphGeneratorTester {

	@Test
	public void test() throws InterruptedException {
		File domain = null;
		File problem = null;

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(" Open a Domain-File");
		chooser.showOpenDialog(null);
		domain = chooser.getSelectedFile();
		// domain = new File("/home/jkoepe/git/pddl4j/pddl/blocksworld/domain.pddl");

		chooser.setDialogTitle("Open a Problme-File");
		chooser.showOpenDialog(null);
		problem = chooser.getSelectedFile();
		// problem = new File("/home/jkoepe/git/pddl4j/pddl/blocksworld/p15.pddl");

		if (domain.exists() && problem.exists()) {
			PDDLGraphGenerator gen = new PDDLGraphGenerator(domain, problem);
			gen.setNodeNumbering(true);

			// create a Astar-serch with the heuristic given pddl4j
			AStar<PDDLNode, String> search = new AStar<>(gen, (s1, s2) -> 1.0, state -> (double) gen.getHeuristic().estimate(state.getPoint().getNode(), gen.getProblem().getGoal()));

			if (gen.getProblem().isSolvable()) {
				List<PDDLNode> solution = search.nextSolution();
				assertNotNull(solution);

				Plan plan = gen.extractPlan(solution);
				assertNotNull(plan);

				plan.actions().stream().forEach(n -> System.out.println(n.getName()));

				ProblemFactory factory = new ProblemFactory();
				try {
					factory.parse(domain, problem);

					CodedProblem enc = factory.encode();

					HSP hspComp = new HSP();

					SequentialPlan hspPlan = hspComp.search(enc);

					// assertNotNull(hspPlan);

				} catch (IOException e) {
					System.out.println("The comparrison with HSP did not work");
					e.printStackTrace();
				}
			} else
				fail("problem not solvable");

		} else
			System.out.println("Either the domain file was not found or the problem file");
		assertTrue(true);
	}

}
