package ai.libs.jaicore.basic.reconstruction;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ClassUtils;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.IReconstructionInstruction;
import org.api4.java.common.reconstruction.ReconstructionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReconstructionInstruction implements IReconstructionInstruction {

	/**
	 *
	 */
	private static final long serialVersionUID = -9034513607515486949L;
	private Logger logger = LoggerFactory.getLogger(ReconstructionInstruction.class);
	private final String clazzName;
	private final String methodName;
	private final Class<?>[] argumentTypes;
	//	private final Class<?>[] givenTypes;
	private final Object[] arguments;

	public static boolean isArrayOfPrimitives(final Class<?> clazz) {
		return clazz.isArray() && (boolean[].class.isAssignableFrom(clazz) || byte[].class.isAssignableFrom(clazz) || short[].class.isAssignableFrom(clazz) || int[].class.isAssignableFrom(clazz) || long[].class.isAssignableFrom(clazz) || float[].class.isAssignableFrom(clazz)  || double[].class.isAssignableFrom(clazz) || char[].class.isAssignableFrom(clazz) || String[].class.isAssignableFrom(clazz));
	}

	@JsonCreator
	public ReconstructionInstruction(@JsonProperty("clazzName") final String clazzName, @JsonProperty("methodName") final String methodName, @JsonProperty("argumentTypes") final Class<?>[] argumentTypes, @JsonProperty("arguments") final Object[] arguments) {
		super();
		Objects.requireNonNull(clazzName);
		Objects.requireNonNull(methodName);
		this.clazzName = clazzName;
		this.methodName = methodName;
		this.argumentTypes = argumentTypes;
		//		this.givenTypes = Arrays.stream(arguments).map(a -> a.getClass()).toArray(Class[]::new);
		this.arguments = arguments;
		int n = argumentTypes.length;
		for (int i = 0; i < n; i++) {
			Class<?> requiredType = argumentTypes[i];
			Class<?> givenType = arguments[i].getClass();
			boolean isThis = arguments[i].equals("this");
			this.logger.debug("ARG {}: {} (required type: {})", i, arguments[i], requiredType);

			/* check that the arguments are really of the required types */
			//			if (!isThis && !requiredType.isAssignableFrom(givenType) && requiredType.equals(long.class) && !givenType.equals(Long.class)) {
			//				throw new IllegalArgumentException("Cannot create instruction. " + i + "-th argument is of type " + arguments[i].getClass().getName() + " where the required type is " + argumentTypes[i].getName());
			//			}

			/* if the argument is a class definition, replace it */
			if (arguments[i] instanceof String && ((String) arguments[i]).startsWith("class:")) {
				try {
					Class<?> clazzNameEncodedInArgument = Class.forName(((String) arguments[i]).substring("class:".length()));
					arguments[i] = clazzNameEncodedInArgument;
				}
				catch (ClassNotFoundException e) {
					/* no class */
				}
			}
			else if (arguments[i] instanceof Class) {
				arguments[i] = "class:" + arguments[i];
			}

			/* check that the required class is a primitive, a Reconstructible, or the previous object */
			if (this.doesTypeRequireSerializationDeserialization(requiredType) && !isThis) {

				if (arguments[i] instanceof String) { // suppose that the object is given in serialized form, try to deserialize it
					this.logger.info("Object is already present in the form of an instruction.");
				}
				else if (arguments[i] instanceof IReconstructible) { // if this is a reconstructible, serialize it
					try {
						String reconstructionCommand = new ObjectMapper().writeValueAsString(((IReconstructible)arguments[i]).getConstructionPlan());
						arguments[i] = reconstructionCommand;
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				}
				else {
					throw new IllegalArgumentException("The " + i + "-th argument \"" + arguments[i] + "\" is neither a primitive (it's a " + arguments[i].getClass().getName() + ") nor a class object nor \"this\" and also not a reconstructible object.");
				}
			}
		}
	}

	public ReconstructionInstruction(final Method method, final Object... arguments) {
		super();
		this.clazzName = method.getDeclaringClass().getName();
		this.methodName = method.getName();
		this.argumentTypes = method.getParameterTypes();
		//		this.givenTypes = Arrays.stream(arguments).map(a -> a.getClass()).toArray(Class[]::new);
		this.arguments = arguments;
	}

	private boolean doesTypeRequireSerializationDeserialization(final Class<?> clazz) {
		return !clazz.isPrimitive() && !isArrayOfPrimitives(clazz) && !clazz.equals(Class.class) && !List.class.isAssignableFrom(clazz);
	}

	private Method getMethod() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		return Class.forName(this.clazzName).getMethod(this.methodName, this.argumentTypes);
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
				if (val != null && val instanceof String) {
					if (val.equals("this")) {
						replacedArguments[i] = object;
					}
					else {
						try {
							String json = (String)val;
							replacedArguments[i] = new ObjectMapper().readValue(json, ReconstructionPlan.class).reconstructObject();
						} catch (IOException e) {
							throw new ReconstructionException(e);
						}
					}
				}
				else {
					replacedArguments[i] = this.arguments[i];
				}

				/* check whether the obtained argument value is compatible with the type required in the method */
				Class<?> type = replacedArguments[i].getClass();
				Class<?> requiredType = method.getParameterTypes()[i];
				if (!ClassUtils.isAssignable(type, requiredType, true)) {

					/* check whether we can "repair" the type mismatch by converting from list to array or vice versa */
					//					if (type.isArray() && ClassUtils.isAssignable(requiredType, List.class)) {
					//						System.out.println("CONVERT");
					//					}
					//					else if (requiredType.isArray() && ClassUtils.isAssignable(type, List.class)) {
					//						replacedArguments[i] = ((List)replacedArguments[i]).toArray();
					//					}
					//					else {
					throw new IllegalStateException("Error in reconstructing object via method " + this.clazzName + "." + this.methodName + ".\nCannot assign parameter of type " + type.getName() + " to required type " + requiredType.getName());
					//					}
				}
			}
			int k = replacedArguments.length;
			this.logger.debug("{}.{}", this.getMethod().getDeclaringClass().getName(), this.getMethod().getName());
			for (int i = 0; i < k; i++) {
				this.logger.debug("{}: {}: {}", i, replacedArguments[i].getClass().getName(), replacedArguments[i]);
			}
			return method.invoke(null, replacedArguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new ReconstructionException(e);
		}
	}

	@Override
	public Object applyToCreate() throws ReconstructionException {
		try {
			int m = this.arguments.length;
			this.logger.info("Creating new object via {}.{}", this.getMethod().getDeclaringClass().getName(), this.getMethod().getName());
			for (int i = 0; i < m; i++) {
				this.logger.debug("{}:  {}: {}", i, this.arguments[i].getClass().getName(), this.arguments[i]);
			}

			return this.getMethod().invoke(null, this.arguments);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			this.logger.error("Error in invocation with arguments: {}", this.arguments);
			throw new ReconstructionException(e);
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
