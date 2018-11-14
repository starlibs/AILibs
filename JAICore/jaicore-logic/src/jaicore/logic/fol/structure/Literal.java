package jaicore.logic.fol.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.StringUtil;
import jaicore.logic.fol.util.LogicUtil;


/**
 * A literal defines a property over parameters. Note that literals can be cloned using the clone() methods.
 * 
 * @author Felix Mohr
 */
@SuppressWarnings("serial")
public class Literal implements Serializable {

	private static Logger logger = LoggerFactory.getLogger(Literal.class);

	// private short getIntProperty(String property) {
	// boolean isNegated = property.startsWith("!");
	// String propertyName = !isNegated ? property : property.substring(1);
	// if (!ext2int.containsKey(propertyName)) {
	// short id = counter ++;
	// if (id < 0)
	// throw new IllegalArgumentException("No support for more than " + Short.MAX_VALUE + " predicates!");
	// ext2int.put(propertyName, id);
	// int2ext.put(id, propertyName);
	// }
	// return (short)(ext2int.get(propertyName) * (isNegated ? -1 : 1));
	// }

	private String property;
	protected List<LiteralParam> parameters;

	public Literal(Literal l, Map<? extends LiteralParam, ? extends LiteralParam> map) {
		this(l.getProperty());
		for (LiteralParam p : l.getParameters()) {
			parameters.add(map.containsKey(p) ? map.get(p) : p);
		}
	}

	/**
	 * Creates a monadic literal (with only one parameter).
	 * 
	 * @param property
	 *            The property defined by this literal.
	 * @param parameter
	 *            The parameter of this literal.
	 */
	public Literal(String property, LiteralParam parameter) {
		this(property);
		this.parameters.add(parameter);
	}

	/**
	 * Creates a monadic literal (with only one parameter).
	 * 
	 * @param property
	 *            The property defined by this literal.
	 * @param parameter
	 *            The parameter of this literal.
	 */
	// private Literal(short property, List<LiteralParam> parameters) {
	// this(property);
	// this.parameters.addAll(parameters);
	// }
	//
	// private Literal(short property) {
	// this.parameters = new ArrayList<>();
	// this.property = property;
	// }

	/**
	 * Creates a literal with a list of parameters.
	 * 
	 * @param property
	 *            The property defined by this literal.
	 * @param parameter
	 *            The parameters of this literal defined as a list.
	 */
	public Literal(String property, List<? extends LiteralParam> parameters) {
		this(property);
		if (parameters.contains(null))
			throw new IllegalArgumentException("Literal parameters must not be null!");
		this.parameters.addAll(parameters);
	}

	/**
	 * Protected helper constructor. Ensure the literal gets parameters!!
	 */
	public Literal(String propertyWithParams) {
		super();
		this.parameters = new ArrayList<>();

		/* detect special predicates = or != */
		if (propertyWithParams.contains("=")) {
			String[] params = StringUtil.explode(propertyWithParams, "=");
			boolean isNegated = params.length > 0 && params[0].endsWith("!");
			this.property = isNegated ? "!=" : "=";
			if (params.length == 2) {
				int p1Length = isNegated ? params[0].length() - 1 : params[0].length();
				this.parameters.add(LogicUtil.parseParamName(params[0].substring(0, p1Length).trim()));
				this.parameters.add(LogicUtil.parseParamName(params[1].trim()));
			}

		}

		/* otherwise, if this is a normal predicate */
		else {

			boolean isPositive = true;
			if (propertyWithParams.startsWith("!")) {
				isPositive = false;
				propertyWithParams = propertyWithParams.substring(1);
			}

			/* add parameters if given in the string */
			if (propertyWithParams.contains("(")) {
				if (propertyWithParams.contains(")")) {
					int index = propertyWithParams.indexOf('(');
					this.property = propertyWithParams.substring(0, index);
					if (index < propertyWithParams.length() - 2) {
						this.parameters.addAll(Arrays.asList(StringUtil.explode(propertyWithParams.substring(index + 1, propertyWithParams.length() - 1), ",")).stream().map(s -> {
							return LogicUtil.parseParamName(s.trim());
						}).collect(Collectors.toList()));
					}
				}
			} else {
				this.property = propertyWithParams;
			}
			if (!isPositive) {
				this.property = "!" + this.property;
			}
		}
	}

	public Literal(String property2, boolean isPositive) {
		this(property2);
		if (isPositive && isNegated()) {
			this.toggleNegation();
		} else if (!isPositive && isPositive()) {
			this.toggleNegation();
		}
	}

	public Literal(String property2, List<? extends LiteralParam> parameters, boolean isPositive) {
		this(property2, parameters);
		if (isPositive && isNegated()) {
			this.toggleNegation();
		} else if (!isPositive && isPositive()) {
			this.toggleNegation();
		}
	}

	/**
	 * Returns a String representation of the property stated by this literal.
	 */
	public final String getProperty() {
		return (isNegated() ? "!" : "") + getPropertyName();
	}

	/**
	 * Returns only the property name of this literal.
	 */
	public final String getPropertyName() {
		return isNegated() ? property.substring(1) : property;
	}

	/**
	 * @return The parameters of this literal in an unmodifiable list.
	 */
	public final List<LiteralParam> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public final boolean isNegated() {
		return property.startsWith("!");
	}

	public Literal toggleNegation() {
		property = isNegated() ? getPropertyName() : "!" + getPropertyName();
		return this;
	}

	/**
	 * @return The variable parameters of this literal in an unmodifiable list.
	 */
	public final List<VariableParam> getVariableParams() {
		List<VariableParam> vars = new LinkedList<>();
		for (LiteralParam param : parameters)
			if (param instanceof VariableParam)
				vars.add((VariableParam) param);
		return Collections.unmodifiableList(vars);
	}

	public final List<ConstantParam> getConstantParams() {
		List<ConstantParam> constants = new ArrayList<>();
		for (LiteralParam param : parameters)
			if (param instanceof ConstantParam)
				constants.add((ConstantParam) param);
		return Collections.unmodifiableList(constants);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Literal other = (Literal) obj;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		return true;
	}

	@Override
	public Literal clone() {
		return new Literal(this.property, this.parameters);
	}

	/**
	 * Creates a copy of this literal on which the given parameter mapping is applied.
	 * 
	 * @param mapping
	 *            A mapping of parameters.
	 * @return A copy of this literal on which the given parameter mapping is applied.
	 */
	public Literal clone(Map<? extends VariableParam, ? extends LiteralParam> mapping) {
		logger.debug("start cloning");
		Literal clone = new Literal(this.property);

		// add parameters corresponding to mapping
		for (LiteralParam v : this.getParameters()) {
			if (v instanceof VariableParam && mapping != null && mapping.containsKey(v)) {
				logger.trace("Params: {}", clone.parameters);
				if (mapping.get(v) == null)
					throw new IllegalArgumentException("Mapping " + mapping + " assigns null to a parameter, which must not be the case!");
				clone.parameters.add(mapping.get(v));
			} else
				clone.parameters.add(v);
		}
		logger.debug("finished cloning");
		return clone;
	}

	@Override
	public String toString() {
		return toString(true);

	}
	
	public String toString(boolean printTypesOfParams) {

		StringBuilder sb = new StringBuilder();
		sb.append(property + "(");

		// iterate through parameter list
		int params = this.parameters.size();
		int i = 1;
		for (LiteralParam p : this.parameters) {
			sb.append(printTypesOfParams ? p.toString() : p.getName());
			if (i++ < params)
				sb.append(", ");
		}
		sb.append(")");

		return sb.toString();

	}

	public boolean isNegationOf(Literal l) {
		return l.getPropertyName().equals(this.getPropertyName()) && l.getParameters().equals(this.parameters) && l.isNegated() != this.isNegated();
	}

	public boolean isPositive() {
		return !this.isNegated();
	}

	public boolean hasVariableParams() {
		return !this.getVariableParams().isEmpty();
	}

	public final boolean isGround() {
		return !hasVariableParams();
	}
}