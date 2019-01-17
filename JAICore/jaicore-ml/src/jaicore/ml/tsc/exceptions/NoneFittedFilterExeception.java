package jaicore.ml.tsc.exceptions;

import jaicore.ml.core.exception.CheckedJaicoreMLException;

public class NoneFittedFilterExeception extends CheckedJaicoreMLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    public NoneFittedFilterExeception(String message, Throwable cause) {
        super(message, cause);
    }

	public NoneFittedFilterExeception(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
