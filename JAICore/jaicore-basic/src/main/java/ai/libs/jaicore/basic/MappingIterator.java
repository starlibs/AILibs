package ai.libs.jaicore.basic;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Turns an iterator over elements of type A into an iterator of elements of type B
 *
 * @author Felix Mohr
 *
 * @param <A>
 * @param <B>
 */
public class MappingIterator<A, B> implements Iterator<B> {

	private final Iterator<A> iterator;
	private final Function<A, B> map;

	public MappingIterator(final Iterator<A> iterator, final Function<A, B> map) {
		super();
		this.iterator = iterator;
		this.map = map;
	}

	@Override
	public boolean hasNext() {
		return this.iterator.hasNext();
	}

	@Override
	public B next() {
		return this.map.apply(this.iterator.next());
	}
}
