package jaicore.ml.evaluation.multilabel.databaseconnection;

import java.io.Serializable;

/**
 * Represents evaluation modes. A mode can either be test or validation.
 *
 * @author Helena Graf
 *
 */
public enum EvaluationMode implements Serializable {
	Test, Validation
}
