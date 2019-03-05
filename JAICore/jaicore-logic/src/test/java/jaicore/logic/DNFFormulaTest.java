package jaicore.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.DNFFormula;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;

public class DNFFormulaTest {

	@Test
	public void testMonomConstructor() {
		String monomString = ("P & Q(var1,var2) & R(var2, var3, 'const')");
		Monom m = new Monom(monomString);
		DNFFormula f = new DNFFormula(m);
		assertTrue(f.contains(new Monom(monomString)));
		assertEquals(1, f.size());
		assertEquals(3, f.getVariableParams().size());
		Set<String> varNames = f.getVariableParams().stream().map(p -> p.getName()).collect(Collectors.toSet());
		assertTrue(varNames.contains("var1"));
		assertTrue(varNames.contains("var2"));
		assertTrue(varNames.contains("var3"));
		Set<String> constantNames = f.getConstantParams().stream().map(p -> p.getName()).collect(Collectors.toSet());
		assertEquals(1, constantNames.size());
		assertTrue(constantNames.contains("const"));
	}

	@Test
	public void testClauseConstructor() {
		String clauseString = ("P | Q(var1,var2) | R(var2, var3, 'const')");
		Clause c = new Clause(clauseString);
		DNFFormula f = new DNFFormula(c);
		for (Literal l : c)
			assertTrue(f.contains(new Monom(l)));
		assertEquals(3, f.size());
		assertEquals(3, f.getVariableParams().size());
		Set<String> varNames = f.getVariableParams().stream().map(p -> p.getName()).collect(Collectors.toSet());
		assertTrue(varNames.contains("var1"));
		assertTrue(varNames.contains("var2"));
		assertTrue(varNames.contains("var3"));
		Set<String> constantNames = f.getConstantParams().stream().map(p -> p.getName()).collect(Collectors.toSet());
		assertEquals(1, constantNames.size());
		assertTrue(constantNames.contains("const"));
	}

	@Test
	public void testMonomCollectionConstructor() {
		String monom1String = ("P & Q(var1,var2) & R(var2, var3, 'const')");
		String monom2String = ("S & T(var3,'const2')");
		Monom m1 = new Monom(monom1String);
		Monom m2 = new Monom(monom2String);
		Collection<Monom> mc = new ArrayList<>();
		mc.add(m1);
		mc.add(m2);
		DNFFormula f = new DNFFormula(mc);
		assertTrue(f.contains(new Monom(monom1String)));
		assertTrue(f.contains(new Monom(monom2String)));
		assertEquals(2, f.size());
		assertEquals(3, f.getVariableParams().size());
		Set<String> varNames = f.getVariableParams().stream().map(p -> p.getName()).collect(Collectors.toSet());
		assertTrue(varNames.contains("var1"));
		assertTrue(varNames.contains("var2"));
		assertTrue(varNames.contains("var3"));
		Set<String> constantNames = f.getConstantParams().stream().map(p -> p.getName()).collect(Collectors.toSet());
		assertEquals(2, constantNames.size());
		assertTrue(constantNames.contains("const"));
		assertTrue(constantNames.contains("const2"));
	}

	@Test
	public void testEntailmentWithoutEquals() {
		Collection<Monom> mc = new ArrayList<>();
		{
			String monom1String = ("P & Q(var1,var2) & R(var2, var3, 'const')");
			String monom2String = ("S & T(var3,'const2')");
			Monom m1 = new Monom(monom1String);
			Monom m2 = new Monom(monom2String);
			mc.add(m1);
			mc.add(m2);
		}
		DNFFormula f = new DNFFormula(mc);
		
		/* check monoms */
		String checkMonom1String = ("P & Q(var1,var2)");
		assertFalse(f.entailedBy(new Monom(checkMonom1String)));
		String checkMonom2String = ("S & T(var3,'const2')");
		assertTrue(f.entailedBy(new Monom(checkMonom2String)));
	}
}
