package jaicore.ml.ranking.clusterbased.customdatatypes;

/**
 * @author Helen Beierling
 *
 * @param <C> An identifier of a group
 */
public class GroupIdentifier<C> {

	private C identifier;

	public GroupIdentifier(final C id){
		this.identifier = id;
	}
	public C getIdentifier(){
		return this.identifier;
	}

}
