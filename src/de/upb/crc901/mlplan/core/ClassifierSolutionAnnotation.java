package de.upb.crc901.mlplan.core;

import java.io.Serializable;

public class ClassifierSolutionAnnotation<V> implements Serializable {
	final V f;
	final int fTime;

	public ClassifierSolutionAnnotation(V f, int fTime) {
		super();
		this.f = f;
		this.fTime = fTime;
	}

	public V getF() {
		return f;
	}

	public int getFTime() {
		return fTime;
	}
}
