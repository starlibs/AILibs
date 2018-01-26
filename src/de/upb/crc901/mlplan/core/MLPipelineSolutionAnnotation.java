package de.upb.crc901.mlplan.core;

public class MLPipelineSolutionAnnotation<V> {
	final V f;
	final int fTime;

	public MLPipelineSolutionAnnotation(V f, int fTime) {
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
