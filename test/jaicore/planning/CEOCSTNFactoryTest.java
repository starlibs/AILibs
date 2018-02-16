package jaicore.planning;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;

public class CEOCSTNFactoryTest {

	@Test
	public void test() {
		Collection<String> init = Arrays.asList(new String[] {"A", "B", "C", "D"});
		CEOCSTNPlanningProblem problem = StandardProblemFactory.getNestedDichotomyCreationProblem("root", init, true, 0, 0);
		System.out.println("Methods\n---------------");
		problem.getDomain().getMethods().stream().forEach(m -> System.out.println(m));
		System.out.println("\nOperations\n---------------");
		problem.getDomain().getOperations().stream().forEach(o -> System.out.println(o));
	}

}
