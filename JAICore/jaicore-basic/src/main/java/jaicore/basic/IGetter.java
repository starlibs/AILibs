package jaicore.basic;

/**
 * gets a property P of an object of class C
 */
public interface IGetter<C, P> {

	public P getPropertyOf(C obj);

}
