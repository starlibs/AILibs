package jaicore.basic.kvstore;

/**
 * Enum for the outcomes for a significance test.
 * If superior: the considered sample is significantly better.
 * If inferior: the considered sample is significantly worse.
 * If tie: there is no significant difference between the two samples.
 * 
 * @author mwever
 */
public enum ESignificanceTestResult {
	SUPERIOR, TIE, INFERIOR;
}
