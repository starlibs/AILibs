package de.upb.crc901.mlplan.core;

import java.io.Serializable;

public interface ISolutionEvaluatorFactory extends Serializable {
	public SolutionEvaluator getInstance();
}
