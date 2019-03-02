package jaicore.basic.algorithm;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AlgorithmTestProblemSet<I, O> {
	private final String name;
//	private final AlgorithmProblemTransformer<P, I> getProblemReducer();

	public AlgorithmTestProblemSet(String name) {
		super();
		this.name = name;
	}
	
	public abstract I getSimpleProblemInputForGeneralTestPurposes() throws Exception;

	public abstract I getDifficultProblemInputForGeneralTestPurposes() throws Exception; // runtime at least 10 seconds

	public Class<I> getInputClass() {
		Type[] mvcPatternClasses = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
		try {
			return ((Class<I>) Class.forName(getClassNameWithoutGenerics(mvcPatternClasses[0].getTypeName())));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Class<O> getOutputClass() {
		Type[] mvcPatternClasses = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
		try {
			return ((Class<O>) Class.forName(getClassNameWithoutGenerics(mvcPatternClasses[1].getTypeName())));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	private String getClassNameWithoutGenerics(String className) {
		return className.replaceAll("(<.*>)", "");
	}
}
