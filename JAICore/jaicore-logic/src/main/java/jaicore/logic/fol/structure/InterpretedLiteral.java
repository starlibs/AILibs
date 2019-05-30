package jaicore.logic.fol.structure;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class InterpretedLiteral extends Literal {
	private static final Logger logger = LoggerFactory.getLogger(InterpretedLiteral.class);

	/**
	 * Copy constructor for an existing literal under giving mapping.
	 *
	 * @param l
	 *            Literal to be copied
	 * @param map
	 *            Mapping for projecting existing literal to a new literal.
	 */
	public InterpretedLiteral(final Literal l, final Map<VariableParam, ? extends LiteralParam> map) {
		super(l, map);
	}

	/**
	 * Creates a monadic literal (with only one parameter).
	 *
	 * @param property
	 *            The property defined by this literal.
	 * @param parameter
	 *            The parameter of this literal.
	 */
	public InterpretedLiteral(final String property, final LiteralParam parameter) {
		super(property, parameter);
	}

	/**
	 * Creates a literal with a list of parameters.
	 *
	 * @param property
	 *            The property defined by this literal.
	 * @param parameter
	 *            The parameters of this literal defined as a list.
	 */
	public InterpretedLiteral(final String property, final List<LiteralParam> parameters) {
		super(property, parameters);
	}

	/**
	 * Protected helper constructor. Ensure the literal gets parameters!!
	 */
	public InterpretedLiteral(final String property) {
		super(property);
	}

	public InterpretedLiteral(final String predicateName, final List<LiteralParam> params, final boolean b) {
		super(predicateName, params, b);
	}

	@Override
	public Literal clone() {
		return new InterpretedLiteral(this.getProperty(), this.getParameters());
	}

	/**
	 * Creates a copy of this literal on which the given parameter mapping is
	 * applied.
	 *
	 * @param mapping
	 *            A mapping of parameters.
	 * @return A copy of this literal on which the given parameter mapping is
	 *         applied.
	 */
	@Override
	public Literal clone(final Map<? extends VariableParam, ? extends LiteralParam> mapping) {
		logger.debug("start cloning");
		Literal clone = new InterpretedLiteral(this.getProperty());

		// add parameters corresponding to mapping
		for (LiteralParam v : this.getParameters()) {
			if (v instanceof VariableParam) {
				if (mapping != null && mapping.containsKey(v)) {
					logger.trace("Params: {}", clone.parameters);
				}
				clone.parameters.add((mapping != null && mapping.containsKey(v)) ? mapping.get(v) : v);
			} else {
				clone.parameters.add(v);
			}
		}
		logger.debug("finished cloning");
		return clone;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("i*");
		sb.append(super.toString());
		return sb.toString();
	}

}
