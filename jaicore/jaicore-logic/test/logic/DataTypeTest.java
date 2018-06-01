package jaicore.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import jaicore.logic.fol.structure.Type;
import jaicore.logic.fol.structure.TypeModule;

public class DataTypeTest {

	private Type rootType;
	private Type nonRootType;
	private Type parallelNonRootType;
	private Type greatestSubType;

	private TypeModule typeMod;

	@Before
	public void setUp() {
		this.typeMod = new TypeModule();

		rootType = typeMod.getType("Root");

		nonRootType = typeMod.getType("NonRoot");
		nonRootType.addSuperType(rootType);

	}

	@Test
	public void getNameTest() {
		assertEquals("getName()", "Root", rootType.getName());
	}

	@Test
	public void toStringTest() {
		assertEquals("Root;", rootType.toString());
		assertEquals("NonRoot;Root", nonRootType.toString());
	}

	@Test
	public void isRootTypeTest() {
		assertTrue(rootType.isRootType());
		assertFalse(nonRootType.isRootType());
	}

	@Test
	public void getParentTypesTest() {
		assertTrue(nonRootType.getDirectSuperTypes().indexOf(rootType) != -1);
		assertTrue(rootType.getDirectSuperTypes().indexOf(nonRootType) == -1);
	}

	@Test
	public void removeParentTypeTest() {
		nonRootType.removeSuperType(rootType);
		assertTrue(nonRootType.getDirectSuperTypes().indexOf(rootType) == -1);
		assertTrue(rootType.getDirectSubTypes().isEmpty());
	}

	@Test
	public void removeSubTypeTest() {
		rootType.removeSubType(nonRootType);
		assertTrue(rootType.getDirectSubTypes().isEmpty());
	}

	@Test
	public void isSubTypeOfTest() {
		assertFalse(rootType.isSubTypeOf(nonRootType));
		assertTrue(rootType.isSubTypeOf(rootType));
		assertTrue(nonRootType.isSubTypeOf(rootType));
	}

	@Test
	public void isSuperTypeOfTest() {
		assertFalse(nonRootType.isSuperTypeOf(rootType));
		assertTrue(nonRootType.isSuperTypeOf(nonRootType));
		assertTrue(rootType.isSuperTypeOf(nonRootType));
	}

	@Test
	public void getGreatestSubTypeHierarchyTest() {
		Type betweenType = typeMod.getType("IntermediateType1");
		betweenType.addSuperType(nonRootType);

		parallelNonRootType = typeMod.getType("ParallelNonRootType");
		parallelNonRootType.addSuperType(rootType);

		greatestSubType = typeMod.getType("GreatestSubType");
		greatestSubType.addSuperType(parallelNonRootType);
		greatestSubType.addSuperType(betweenType);

		betweenType = typeMod.getType("IntermediateType2");
		betweenType.addSuperType(nonRootType);

	}

	@Test
	public void getGreatestSubTypeSimpleTest() {
		assertEquals(nonRootType, Type.getGreatestSubType(rootType, nonRootType));
	}
}
