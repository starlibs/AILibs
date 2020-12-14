package ai.libs.jaicore.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.logic.fol.structure.Type;
import ai.libs.jaicore.logic.fol.structure.TypeModule;

public class DataTypeTest {

	private Type rootType;
	private Type nonRootType;
	private Type parallelNonRootType;
	private Type greatestSubType;

	private TypeModule typeMod;

	@BeforeEach
	public void setUp() {
		this.typeMod = new TypeModule();

		this.rootType = this.typeMod.getType("Root");

		this.nonRootType = this.typeMod.getType("NonRoot");
		this.nonRootType.addSuperType(this.rootType);

	}

	@Test
	public void getNameTest() {
		assertEquals("getName()", "Root", this.rootType.getName());
	}

	@Test
	public void toStringTest() {
		assertEquals("Root;", this.rootType.toString());
		assertEquals("NonRoot;Root", this.nonRootType.toString());
	}

	@Test
	public void isRootTypeTest() {
		assertTrue(this.rootType.isRootType());
		assertFalse(this.nonRootType.isRootType());
	}

	@Test
	public void getParentTypesTest() {
		assertTrue(this.nonRootType.getDirectSuperTypes().indexOf(this.rootType) != -1);
		assertTrue(this.rootType.getDirectSuperTypes().indexOf(this.nonRootType) == -1);
	}

	@Test
	public void removeParentTypeTest() {
		this.nonRootType.removeSuperType(this.rootType);
		assertTrue(this.nonRootType.getDirectSuperTypes().indexOf(this.rootType) == -1);
		assertTrue(this.rootType.getDirectSubTypes().isEmpty());
	}

	@Test
	public void removeSubTypeTest() {
		this.rootType.removeSubType(this.nonRootType);
		assertTrue(this.rootType.getDirectSubTypes().isEmpty());
	}

	@Test
	public void isSubTypeOfTest() {
		assertFalse(this.rootType.isSubTypeOf(this.nonRootType));
		assertTrue(this.rootType.isSubTypeOf(this.rootType));
		assertTrue(this.nonRootType.isSubTypeOf(this.rootType));
	}

	@Test
	public void isSuperTypeOfTest() {
		assertFalse(this.nonRootType.isSuperTypeOf(this.rootType));
		assertTrue(this.nonRootType.isSuperTypeOf(this.nonRootType));
		assertTrue(this.rootType.isSuperTypeOf(this.nonRootType));
	}

	@Test
	public void getGreatestSubTypeHierarchyTest() {
		Type betweenType = this.typeMod.getType("IntermediateType1");
		betweenType.addSuperType(this.nonRootType);

		this.parallelNonRootType = this.typeMod.getType("ParallelNonRootType");
		this.parallelNonRootType.addSuperType(this.rootType);

		this.greatestSubType = this.typeMod.getType("GreatestSubType");
		this.greatestSubType.addSuperType(this.parallelNonRootType);
		this.greatestSubType.addSuperType(betweenType);

		betweenType = this.typeMod.getType("IntermediateType2");
		betweenType.addSuperType(this.nonRootType);

	}

	@Test
	public void getGreatestSubTypeSimpleTest() {
		assertEquals(this.nonRootType, Type.getGreatestSubType(this.rootType, this.nonRootType));
	}
}
