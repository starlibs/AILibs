package ai.libs.jaicore.basic.sets;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.api4.java.common.attributedobjects.IListDecorator;

/**
 * This class solves the following problem: Sometimes you want to use objects of a concrete List class L
 * to be used in a context where some extension of the List interface L' is used, which is not implemented by L.
 * To use objects of type L in such a context, it is a bad idea to cast and copy the items of L into an
 * object of type L' but instead a decorator implementing L' should be used that forwards all operations to the
 * original instance.
 *
 * The ListDecorator must be told how to create elements of the decorated list in order to insert
 *
 * @author fmohr
 *
 * @param <L>
 * @param <E>
 */
public class ListDecorator<L extends List<E>, E, D extends ElementDecorator<E>> implements IListDecorator<L, E, D>, List<D> {

	private final L list;
	private final Class<E> typeOfDecoratedItems;
	private final Class<D> typeOfDecoratingItems;
	private final Constructor<D> constructorForDecoratedItems;

	@SuppressWarnings("unchecked")
	public ListDecorator(final L list) {
		super();
		try {
			this.list = list;
			Type[] genericTypes = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
			this.typeOfDecoratedItems = (Class<E>) this.getClassWithoutGenerics(genericTypes[1].getTypeName());
			this.typeOfDecoratingItems = (Class<D>) this.getClassWithoutGenerics(genericTypes[2].getTypeName());
			Constructor<D> vConstructorForDecoratedItems = null;
			vConstructorForDecoratedItems = this.typeOfDecoratingItems.getConstructor(this.typeOfDecoratedItems);
			this.constructorForDecoratedItems = vConstructorForDecoratedItems;
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not determin class without generics", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("The constructor of the list class could not be invoked.", e);
		}
	}

	@Override
	public Class<E> getTypeOfDecoratedItems() {
		return this.typeOfDecoratedItems;
	}

	@Override
	public Class<D> getTypeOfDecoratingItems() {
		return this.typeOfDecoratingItems;
	}

	@Override
	public Constructor<D> getConstructorForDecoratingItems() {
		return this.constructorForDecoratedItems;
	}

	@Override
	public L getList() {
		return this.list;
	}

	private Class<?> getClassWithoutGenerics(final String className) throws ClassNotFoundException {
		return Class.forName(className.replaceAll("(<[^>*]>)", ""));
	}
}
