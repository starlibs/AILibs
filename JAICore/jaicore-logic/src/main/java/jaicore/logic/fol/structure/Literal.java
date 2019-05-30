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
	private String property;
	protected List<LiteralParam> parameters;

	public Literal(final Literal l, final Map<? extends LiteralParam, ? extends LiteralParam> map) {
		this(l.getProperty());
		for (LiteralParam p : l.getParameters()) {
			this.parameters.add(map.containsKey(p) ? map.get(p) : p);
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
	public Literal(final String property, final LiteralParam parameter) {
		this(property);
		this.parameters.add(parameter);
	}

	/**
	 * Creates a literal with a list of parameters.
	 *
	 * @param property
	 *            The property defined by this literal.
	 * @param parameter
	 *            The parameters of this literal defined as a list.
	 */
	public Literal(final String property, final List<? extends LiteralParam> parameters) {
		this(property);
		this.parameters.addAll(parameters);
	}

	/**
	 * Protected helper constructor. Ensure the literal gets parameters!!
	 */
	public Literal(final String pPropertyWithParams) {
		super();
		this.parameters = new ArrayList<>();

		/* detect special predicates = or != */
		if (pPropertyWithParams.contains("=")) {
			String[] params = StringUtil.explode(pPropertyWithParams, "=");
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
			String propertyWithParams = "" +  pPropertyWithParams;
			if (pPropertyWithParams.startsWith("!")) {
				isPositive = false;
				propertyWithParams = pPropertyWithParams.substring(1);
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
		if (this.property == null) {
			throw new IllegalArgumentException("Given string \"" + pPropertyWithParams + "\" causes a NULL property!");
		}
	}

	public Literal(final String property2, final boolean isPositive) {
		this(property2);
		if (isPositive && this.isNegated() || !isPositive && this.isPositive()) {
			this.toggleNegation();
		}
	}

	public Literal(final String property2, final List<? extends LiteralParam> parameters, final boolean isPositive) {
		this(property2, parameters);
		if (isPositive && this.isNegated() || !isPositive && this.isPositive()) {
			this.toggleNegation();
		}
	}

	/**
	 * Returns a String representation of the property stated by this literal.
	 */
	public final String getProperty() {
		return (this.isNegated() ? "!" : "") + this.getPropertyName();
	}

	/**
	 * Returns only the property name of this literal.
	 */
	public final String getPropertyName() {
		return this.isNegated() ? this.property.substring(1) : this.property;
	}

	/**
	 * @return The parameters of this literal in an unmodifiable list.
	 */
	public final List<LiteralParam> getParameters() {
		return Collections.unmodifiableList(this.parameters);
	}

	public final boolean isNegated() {
		return this.property.startsWith("!");
	}

	public Literal toggleNegation() {
		this.property = this.isNegated() ? this.getPropertyName() : "!" + this.getPropertyName();
		return this;
	}

	/**
	 * @return The variable parameters of this literal in an unmodifiable list.
	 */
	public final List<VariableParam> getVariableParams() {
		List<VariableParam> vars = new LinkedList<>();
		for (LiteralParam param : this.parameters) {
			if (param instanceof VariableParam) {
				vars.add((VariableParam) param);
			}
		}
		return Collections.unmodifiableList(vars);
	}

	public final List<ConstantParam> getConstantParams() {
		List<ConstantParam> constants = new ArrayList<>();
		for (LiteralParam param : this.parameters) {
			if (param instanceof ConstantParam) {
				constants.add((ConstantParam) param);
			}
		}
		return Collections.unmodifiableList(constants);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		result = prime * result + ((this.property == null) ? 0 : this.property.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Literal other = (Literal) obj;
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		if (this.property == null) {
			if (other.property != null) {
				return false;
			}
		} else if (!this.property.equals(other.property)) {
			return false;
		}
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
	public Literal clone(final Map<? extends VariableParam, ? extends LiteralParam> mapping) {
		logger.debug("start cloning");
		Literal clone = new Literal(this.property);

		// add parameters corresponding to mapping
		for (LiteralParam v : this.getParameters()) {
			if (v instanceof VariableParam && mapping != null && mapping.containsKey(v)) {
				logger.trace("Params: {}", clone.parameters);
				if (mapping.get(v) == null) {
					throw new IllegalArgumentException("Mapping " + mapping + " assigns null to a parameter, which must not be the case!");
				}
				clone.parameters.add(mapping.get(v));
			} else {
				clone.parameters.add(v);
			}
		}
		logger.debug("finished cloning");
		return clone;
	}

	@Override
	public String toString() {
		return this.toString(true);

	}

	public String toString(final boolean printTypesOfParams) {

		StringBuilder sb = new StringBuilder();
		sb.append(this.property + "(");

		// iterate through parameter list
		int params = this.parameters.size();
		int i = 1;
		for (LiteralParam p : this.parameters) {
			sb.append(printTypesOfParams ? p.toString() : p.getName());
			if (i++ < params) {
				sb.append(", ");
			}
		}
		sb.append(")");

		return sb.toString();

	}

	public boolean isNegationOf(final Literal l) {
		return l.getPropertyName().equals(this.getPropertyName()) && l.getParameters().equals(this.parameters) && l.isNegated() != this.isNegated();
	}

	public boolean isPositive() {
		return !this.isNegated();
	}

	public boolean hasVariableParams() {
		return !this.getVariableParams().isEmpty();
	}

	public final boolean isGround() {
		return !this.hasVariableParams();
	}
}