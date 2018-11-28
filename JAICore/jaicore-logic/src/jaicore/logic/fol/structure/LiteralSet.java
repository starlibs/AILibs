package jaicore.logic.fol.structure;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.StringUtil;
import jaicore.basic.sets.SetUtil;

/**
 * A set of literals.
 * 
 * @author mbunse
 */

public class LiteralSet extends HashSet<Literal> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6767454041686262363L;
	private static Logger logger = LoggerFactory.getLogger(LiteralSet.class);
	
	/**
	 * Creates an empty literal set. Literals can be added later.
	 */
	public LiteralSet() {
		super();
	}
	
	public LiteralSet(String literals, String delimiter) {
		this(Arrays.asList(StringUtil.explode(literals, delimiter)).stream().map(s -> new Literal(s.trim())).collect(Collectors.toList()));
	}

	/**
	 * Creates a literal set that only contains the given literal. Other literals can be added later.
	 * 
	 * @param literal
	 *            A literal.
	 */
	public LiteralSet(Literal literal) {
		super();
		this.add(literal.clone());
	}

	/**
	 * Creates a copy of the given collection. The created literal set will contain all the elements from the given collection.
	 * 
	 * @param literals
	 *            A collection of literals.
	 */
	public LiteralSet(Collection<Literal> literals, boolean deep) {
		super();
		if (literals != null) {
			if (deep) {
				for (Literal l : literals) {
					this.add(l.clone());
				}
			}
			else {
				this.addAll(literals);
			}
		}
	}
	
	public LiteralSet(Collection<Literal> literals) {
		this(literals, true);
	}

	/**
	 * Creates a copy of the given collection under the given parameter mapping.
	 * 
	 * @param literals
	 *            A collection of literals.
	 * @param mapping
	 *            A mapping of literals.
	 */
	public LiteralSet(Collection<Literal> literals, Map<? extends LiteralParam, ? extends LiteralParam> mapping) {
		super();
		logger.debug("init with: " + literals);
		for (Literal l : literals) {
			logger.debug("call clone for literal {}, which is of class {}, on literal set {}", l, l.getClass(), this);
			this.add(new Literal(l, mapping));
			logger.debug("finished clone for literal {}, which is of class {}, on literal set {}", l, l.getClass(), this);
		}
	}

	/**
	 * @param conclusion
	 *            Another literal set that may be concluded by this literal set.
	 * @return True, if this literal set logically implies the conclusion literal set under any partial mapping.
	 */
	public boolean implies(LiteralSet conclusion) throws InterruptedException {
		if (this.containsAll(conclusion)) {
			return true;
		}
		// check all partial mappings for implication
		for (Map<VariableParam, VariableParam> mapping : SetUtil.allMappings(this.getVariableParams(), conclusion.getVariableParams(), false, false, false)) {
			if (new LiteralSet(this, mapping).containsAll(conclusion))
				return true; // implication mapping found
		}

		return false; // no implying mapping
	}

	public boolean isConsistent() {
		for (Literal l : this) {
			String prop = l.getProperty();
			String negProp = prop.startsWith("!") ? prop.substring(1) : ("!" + prop);
			if (this.contains(new Literal(negProp, l.getParameters())))
				return false;
		}
		return true;
	}

	public Map<VariableParam, VariableParam> getImplyingMappingThatMapsFromConclusionVarsToPremiseVars(LiteralSet conclusion) throws InterruptedException {
		for (Map<VariableParam, VariableParam> mapping : SetUtil.allMappings(conclusion.getVariableParams(), this.getVariableParams(), false, false, false))
			if (this.containsAll(new LiteralSet(conclusion, mapping)))
				return mapping; // implication mapping found

		return null; // no implying mapping
	}

	public LiteralSet getPositiveLiterals() {
		LiteralSet ls = new LiteralSet();
		for (Literal l : this) {
			if (l.isPositive())
				ls.add(l);
		}
		return ls;
	}
	
	public LiteralSet getNegativeLiterals() {
		LiteralSet ls = new LiteralSet();
		for (Literal l : this) {
			if (l.isNegated())
				ls.add(l);
		}
		return ls;
	}
	
	/**
	 * @return All the parameters (variable and constant) from the contained literals.
	 */
	public Set<LiteralParam> getParameters() {
		Set<LiteralParam> params = new HashSet<>();
		for (Literal literal : this)
			params.addAll(literal.getParameters());
		return params;
	}

	/**
	 * @return All the variable parameters from the contained literals.
	 */
	public Set<VariableParam> getVariableParams() {
		Set<VariableParam> vars = new HashSet<>();
		for (Literal literal : this)
			vars.addAll(literal.getVariableParams());
		return vars;
	}

	public Set<ConstantParam> getConstantParams() {
		Set<ConstantParam> constants = new HashSet<>();
		for (Literal literal : this)
			constants.addAll(literal.getConstantParams());
		return constants;
	}

	/**
	 * @return All interpreted literals that could be used as branching or looping condition
	 */
	public Set<InterpretedLiteral> getInterpretedLiterals() {
		Set<InterpretedLiteral> interpretedLiteralSet = new HashSet<>();

		for (Literal l : this) {
			if (l instanceof InterpretedLiteral) {
				interpretedLiteralSet.add((InterpretedLiteral) l);
			}
		}

		return interpretedLiteralSet;
	}

	/**
	 * This method converts the LiteralSet into a PropositionalSet meaning that the resulting set only contains properties of the literals contained in this LiteralSet.
	 * 
	 * @return A set of property name strings.
	 */
	public Set<String> toPropositionalSet() {
		Set<String> propositionalSet = new HashSet<>();
		for (Literal l : this) {
			propositionalSet.add(l.getProperty());
		}
		return propositionalSet;
	}

	public boolean containsPositiveAndNegativeVersionOfLiteral() {
		for (Literal l1 : this)
			for (Literal l2 : this)
				if (l1.isNegationOf(l2))
					return true;
		return false;
	}
	
	public boolean containsGroundEqualityPredicateThatEvaluatesTo(boolean eval) {
		for (Literal l : this) {
			if (l.getPropertyName().equals("=") && l.isGround()) {
				List<ConstantParam> params = l.getConstantParams();
				if ((l.isPositive() == params.get(0).equals(params.get(1))) == eval)
					return true;
			}
		}
		return false;
	}

	public boolean containsLiteralWithPredicatename(String predicateName) {
		for (Literal l : this)
			if (l.getPropertyName().equals(predicateName))
				return true;
		return false;
	}

	public boolean hasVariables() {
		return !this.getVariableParams().isEmpty();
	}

	public Set<Literal> getLiteralsWithPropertyName(String propertyName) {
		Set<Literal> literalsWithPropertyName = new HashSet<>();

		for (Literal lit : this)
			if (lit.getPropertyName().equals(propertyName))
				literalsWithPropertyName.add(lit);

		return literalsWithPropertyName;
	}
}
