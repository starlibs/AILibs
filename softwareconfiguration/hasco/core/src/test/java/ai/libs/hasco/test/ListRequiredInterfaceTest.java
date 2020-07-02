package ai.libs.hasco.test;

import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirst;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ListRequiredInterfaceTest {

    @Test
    public void test() throws IOException, InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {

        File componentFile = new File("testsrc/simpleproblem.json");
        String requiredInterface = "EntryPoint";

        IObjectEvaluator<ComponentInstance, Double> compositionEvaluator = new ExampleCompositionEvaluator();
        RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(componentFile, requiredInterface, compositionEvaluator);

        HASCOViaFDAndBestFirstFactory<Double> factory = new HASCOViaFDAndBestFirstFactory<>();
        factory.setNodeEvaluator(n -> 0.0);
        factory.setProblemInput(problem);
        factory.withDefaultAlgorithmConfig();
        HASCOViaFDAndBestFirst<Double> hasco = factory.getAlgorithm();

        int expected = 7;
        int seen = 0;

        while (hasco.hasNext()) {
            IAlgorithmEvent e = hasco.nextWithException();
            if (e instanceof HASCOSolutionEvent) {
                seen++;
            }
        }
        assertEquals(expected, seen);
    }

    static class ExampleCompositionEvaluator implements IObjectEvaluator<ComponentInstance, Double> {
        @Override
        public Double evaluate(ComponentInstance ci) {
            return 1.0;
        }
    }
}
