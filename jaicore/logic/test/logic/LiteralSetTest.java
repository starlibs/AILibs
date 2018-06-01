package jaicore.logic;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.LiteralSet;
import jaicore.logic.fol.structure.Type;
import jaicore.logic.fol.structure.TypeModule;
import jaicore.logic.fol.structure.VariableParam;

/**
 * Test case for the LiteralSet class.
 * 
 * @author mbunse
 */
public class LiteralSetTest {

	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test for the implies() method.
	 */
	@Test
	public void testImplies() throws InterruptedException {
		
		TypeModule typeModule = new TypeModule();

		Type dummyType = typeModule.getType("http://dummy.type/");

		/* body := [Q(x, a), R(b), S(x)], where a, b variables, x constant. */
		LiteralSet body = new LiteralSet();

		List<LiteralParam> qParam = new LinkedList<>();
		qParam.add(new ConstantParam("x"));
		qParam.add(new VariableParam("a", dummyType));
		body.add(new Literal("Q", qParam));

		body.add(new Literal("R", new VariableParam("b", dummyType)));

		// assert that body =/> [T(a)], where a variable.
		LiteralSet noHead1 = new LiteralSet(new Literal("T", new VariableParam("a", dummyType)));

		assertTrue("noHead1 is implied!", !body.implies(noHead1));

		// assert that body => [R(c)], where c variable.
		LiteralSet head1 = new LiteralSet(new Literal("R", new VariableParam("c", dummyType)));

		assertTrue("[R(c)] is not implied!", body.implies(head1));

		// assert that body => [Q(x, c)], where c variable, x constant.
		List<LiteralParam> qParam2 = new LinkedList<>();
		qParam2.add(new ConstantParam("x")); // = x from above
		qParam2.add(new VariableParam("c", dummyType)); // substitutable by a
		LiteralSet head2 = new LiteralSet(new Literal("Q", qParam2));

		assertTrue("[Q(x, c)] is not implied!", body.implies(head2));

	} // testImplies

}
