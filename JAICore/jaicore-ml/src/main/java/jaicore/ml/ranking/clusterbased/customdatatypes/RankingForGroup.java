package jaicore.ml.ranking.clusterbased.customdatatypes;

import java.util.List;

/**
 * RankingForGroup.java - saves a solution ranking for a group identified by thier group
 *
 * @author Helen Beierling
 *
 * @param <C> The identifier of the group
 * @param <S> The solutions that are ranked best for a group of probleminstances
 */
public class RankingForGroup<C,S> extends Ranking<S>{
	private transient GroupIdentifier<C> identifierOfGroup;

	public RankingForGroup(final GroupIdentifier<C> identifier, final List<S> solutionsForGroup){
		super(solutionsForGroup);
		this.identifierOfGroup=identifier;
	}

	public GroupIdentifier<C> getIdentifierForGroup(){
		return this.identifierOfGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.identifierOfGroup == null) ? 0 : this.identifierOfGroup.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		RankingForGroup other = (RankingForGroup) obj;
		if (this.identifierOfGroup == null) {
			if (other.identifierOfGroup != null) {
				return false;
			}
		} else if (!this.identifierOfGroup.equals(other.identifierOfGroup)) {
			return false;
		}
		return true;
	}
}
