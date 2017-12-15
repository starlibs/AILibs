package de.upb.crc901.taskconfigurator.core;

import jaicore.search.algorithms.interfaces.solutionannotations.FTimeComputationAnnotation;
import jaicore.search.algorithms.interfaces.solutionannotations.NumberOfNodeAnnotation;
import jaicore.search.algorithms.interfaces.solutionannotations.SolutionAnnotation;
import jaicore.search.algorithms.interfaces.solutionannotations.TimeUntilFoundAnnotation;

public interface MLPipelineSolutionAnnotation<T,V extends Comparable<V>> extends SolutionAnnotation<T,V>, TimeUntilFoundAnnotation<T, V>, FTimeComputationAnnotation<T, V>, NumberOfNodeAnnotation<T, V> {
	public boolean isTunedSolution();
	public MLPipeline getPipeline();
}
