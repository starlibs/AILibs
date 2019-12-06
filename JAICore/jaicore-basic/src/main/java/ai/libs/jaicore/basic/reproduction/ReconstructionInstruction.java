package ai.libs.jaicore.basic.reproduction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.ReconstructionException;

public class ReconstructionInstruction implements IReconstructionInstruction {

	/**
	 *
	 */
	private static final long serialVersionUID = -9034513607515486949L;
	private final String clazzName;
	private final String methodName;
	private final Class<?>[] argumentTypes;
	private final Object[] arguments;

	public ReconstructionInstruction(final Method method, final Object... arguments) {
		super();
		this.clazzName = method.getDeclaringClass().getName();
		this.methodName = method.getName();
		this.argumentTypes = method.getParameterTypes();
		this.arguments = arguments;
		if (!this.getMethod().equals(method)) {
			throw new IllegalArgumentException();
		}
	}

	private Method getMethod() {
		try {
			return Class.forName(this.clazzName).getMethod(this.methodName, this.argumentTypes);
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public Object apply(final Object object) throws ReconstructionException {
		int n = this.arguments.length;
		Object[] replacedArguments = new Object[n];
		for (int i = 0; i < n; i++) {
			Object val = this.arguments[i];
			if (val != null && val.equals("this")) {
				replacedArguments[i] = object;
			}
			else {
				replacedArguments[i] = this.arguments[i];
			}
		}
		try {
			return this.getMethod().invoke(null, replacedArguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ReconstructionException(e);
		}
	}

	@Override
	public Object applyToCreate() throws ReconstructionException {
		try {
			return this.getMethod().invoke(null, this.arguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ReconstructionException(e);
		}
	}

}
