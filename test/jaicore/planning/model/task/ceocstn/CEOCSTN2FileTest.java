package jaicore.planning.model.task.ceocstn;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JFileChooser;

import org.junit.Test;

public class CEOCSTN2FileTest {

	@Test
	public void test() {
		String packageName = "";
		
		Collection<String> init = Arrays.asList(new String[] {"A", "B", "C", "D"});
		CEOCSTNPlanningProblem problem = StandardProblemFactory.getNestedDichotomyCreationProblem("root", init, true, 0,0);

		CEOCSTN2Shop2.print(problem);	
	}

}
