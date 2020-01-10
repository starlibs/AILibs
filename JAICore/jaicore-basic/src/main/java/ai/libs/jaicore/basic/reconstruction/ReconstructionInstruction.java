package ai.libs.jaicore.basic.reconstruction;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.ReconstructionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReconstructionInstruction implements IReconstructionInstruction {

	/**
	 *
	 */
	private static final long serialVersionUID = -9034513607515486949L;
	private transient Logger logger = LoggerFactory.getLogger(ReconstructionInstruction.class);
	private final String clazzName;
	private final String methodName;
	private final Class<?>[] argumentTypes;
	private final transient Object[] arguments;

	public static boolean isArrayOfPrimitives(final Class<?> clazz) {
		return clazz.isArray() && (boolean[].class.isAssignableFrom(clazz) || byte[].class.isAssignableFrom(clazz) || short[].class.isAssignableFrom(clazz) || int[].class.isAssignableFrom(clazz) || long[].class.isAssignableFrom(clazz)
				|| float[].class.isAssignableFrom(clazz) || double[].class.isAssignableFrom(clazz) || char[].class.isAssignableFrom(clazz) || String[].class.isAssignableFrom(clazz));
	}

	@JsonCreator
	public ReconstructionInstruction(@JsonProperty("clazzName") final String clazzName, @JsonProperty("methodName") final String methodName, @JsonProperty("argumentTypes") final Class<?>[] argumentTypes,
			@JsonProperty("arguments") final Object[] arguments) {
		super();
		Objects.requireNonNull(clazzName);
		Objects.requireNonNull(methodName);
		this.clazzName = clazzName;
		this.methodName = methodName;
		this.argumentTypes = argumentTypes;
		this.arguments = arguments;
		int n = argumentTypes.length;

		/* */
		for (int i = 0; i < n; i++) {
			Class<?> requiredType = argumentTypes[i];
			boolean isThis = arguments[i].equals("this");
			this.logger.debug("ARG {}: {} (required type: {})", i, arguments[i], requiredType);

			/* if the required type is complex and requires serialization or deserialization, do this now */
			if (this.doesTypeRequireSerializationDeserialization(requiredType)) {
				try {
					if (arguments[i] instanceof String) { // suppose that the object is given in serialized form, try to deserialize it
						arguments[i] = requiredType.cast(new ObjectMapper().readValue(arguments[i].toString(), ReconstructionPlan.class).reconstructObject());
					} else if (arguments[i] instanceof IReconstructible) { // if this is a reconstructible, serialize it

						String reconstructionCommand = new ObjectMapper().writeValueAsString(((IReconstructible) arguments[i]).getConstructionPlan());
						arguments[i] = reconstructionCommand;
						continue; // if we are serializing the object, we can (in fact MUST) stop the processing of the parameter here
					} else {
						throw new IllegalArgumentException(
								"The " + i + "-th argument \"" + arguments[i] + "\" is neither a primitive (it's a " + arguments[i].getClass().getName() + ") nor a class object nor \"this\" and also not a reconstructible object.");
					}
				} catch (IOException | ReconstructionException e) {
					throw new IllegalArgumentException(e);
				}
			}

			/* check that correct type is transmitted */
			Class<?> givenType = arguments[i].getClass();
			if (!isThis && !TypeUtils.isAssignable(givenType, requiredType)) {

				/* if the required type is not a class, there is no excuse to deviate */
				if (requiredType != Class.class) {
					throw new IllegalArgumentException("Cannot create instruction. Required type for " + i + "-th argument is " + requiredType + ". But the given object is " + arguments[i] + " (type: " + arguments[i].getClass().getName() + ")");
				}

				/* here we know that the required type is a class. Then try to get a class object from the parameter */
				if (givenType != String.class) {
					throw new IllegalArgumentException("Cannot create instruction. " + i + "-th argument is required to be a class object (" + argumentTypes[i].getName() + "). The provided object is neither a Class nor a String and hence cannot be derived.");
				}

				/* now we know that the given param is a String. Try to find the class for it */
				try {
					String className = (String)arguments[i];
					if (className.startsWith("class:")) {
						className = className.substring("class:".length());
					}
					if (className.startsWith("class ")) {
						className = className.substring("class ".length());
					}
					arguments[i] = Class.forName(className);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Cannot create instruction. " + i + "-th argument is required to be a class object (" + argumentTypes[i].getName() + "). The provided object " + arguments[i] + " is a String that points to a class that does not exist! Cannot derive hence a class object.");
				}
			}		}
	}

	public ReconstructionInstruction(final Method method, final Object... arguments) {
		super();
		this.clazzName = method.getDeclaringClass().getName();
		this.methodName = method.getName();
		this.argumentTypes = method.getParameterTypes();
		this.arguments = arguments;
	}

	private boolean doesTypeRequireSerializationDeserialization(final Class<?> clazz) {
		if (clazz.isPrimitive() || clazz.equals(String.class)) {
			return false;
		}
		if (isArrayOfPrimitives(clazz)) {
			return false;
		}
		if (clazz.equals(Class.class)) {
			return false;
		}
		return !List.class.isAssignableFrom(clazz);
	}

	private Method getMethod() throws ClassNotFoundException, NoSuchMethodException  {

		String className = this.clazzName;
		if (className.equals("Instances")) {
			className = "weka.core.Instances";
		}
		Method m = MethodUtils.getMatchingAccessibleMethod(Class.forName(className), this.methodName, this.argumentTypes);
		if (m == null) {
			throw new NoSuchMethodException("Method " + this.methodName + " for class " + className + " not found!");
		}
		return m;
	}

	private Constructor<?> getConstructor() throws ClassNotFoundException, NoSuchMethodException  {

		String className = this.clazzName;
		if (className.equals("Instances")) {
			className = "weka.core.Instances";
		}
		Class<?> clazz = Class.forName(className);
		return this.argumentTypes.length == 0 ? clazz.getConstructor() : clazz.getConstructor(this.argumentTypes);
	}

	@Override
	public Object apply(final Object object) throws ReconstructionException {
		int n = this.arguments.length;
		Object[] replacedArguments = new Object[n];
		try {
			Method method = this.getMethod();
			for (int i = 0; i < n; i++) {
				Object val = this.arguments[i];

				/* first replace the encoding strings by their true value */
				if (val instanceof String) {
					if (val.equals("this")) {
						replacedArguments[i] = object;
					} else {
						String json = (String) val;
						replacedArguments[i] = new ObjectMapper().readValue(json, ReconstructionPlan.class).reconstructObject();
					}
				} else {
					replacedArguments[i] = this.arguments[i];
				}

				/* check whether the obtained argument value is compatible with the type required in the method */
				Class<?> type = replacedArguments[i].getClass();
				Class<?> requiredType = method.getParameterTypes()[i];
				if (!ClassUtils.isAssignable(type, requiredType, true)) {
					throw new IllegalStateException(
							"Error in reconstructing object via method " + this.clazzName + "." + this.methodName + ".\nCannot assign parameter of type " + type.getName() + " to required type " + requiredType.getName());
				}
			}
			int k = replacedArguments.length;
			this.logger.debug("{}.{}", this.getMethod().getDeclaringClass().getName(), this.getMethod().getName());
			for (int i = 0; i < k; i++) {
				this.logger.debug("{}: {}: {}", i, replacedArguments[i].getClass().getName(), replacedArguments[i]);
			}
			return method.invoke(null, replacedArguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | IOException e) {
			throw new ReconstructionException(e);
		}
	}

	@Override
	public Object applyToCreate() throws ReconstructionException {

		int m = this.arguments.length;
		if (!this.methodName.equals("__construct")) {
			Method method;
			try {
				method = this.getMethod();
			} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e1) {
				throw new ReconstructionException(e1);
			}
			this.logger.info("Creating new object via {}.{}", method.getDeclaringClass().getName(), method.getName());
			for (int i = 0; i < m; i++) {
				this.logger.debug("{}:  {}: {}", i, this.arguments[i].getClass().getName(), this.arguments[i]);
			}
			try {
				return method.invoke(null, this.arguments);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				this.logger.error("Error in invoking {}.{} with arguments: {} with class types {}.", this.clazzName, this.methodName, this.arguments, Arrays.stream(this.arguments).map(a -> a.getClass().getName()).collect(Collectors.toList()));
				throw new ReconstructionException(e);
			}
		}
		else {
			try {
				Constructor<?> c = this.getConstructor();
				return c.newInstance(this.arguments);
			} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ReconstructionException(e);
			}
		}
	}

	public String getClazzName() {
		return this.clazzName;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public Class<?>[] getArgumentTypes() {
		return this.argumentTypes;
	}

	public Object[] getArguments() {
		return this.arguments;
	}
}
