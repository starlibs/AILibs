package ai.libs.hasco.core;

/**
 * Classes can implement this interface if they want to be informed about the HASCO instance in which they are used.
 *
 * @author fmohr
 *
 */
public interface IHascoAware {

	public void setHascoReference(HASCO<?, ?, ?, ?> hasco);

	public HASCO<?, ?, ?, ?> getHASCOReference();

}
