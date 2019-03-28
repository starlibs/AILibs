package jaicore.CustomDataTypes;

import java.util.ArrayList;

/**
 * RankingForGroup.java - saves a solution ranking for a group identified by thier group
 * 
 * @author Helen Beierling
 *
 * @param <C> The identifier of the group
 * @param <S> The solutions that are ranked best for a group of probleminstances
 */
public class RankingForGroup<C,S> extends Ranking<S>{
	private GroupIdentifier<C> identifierOfGroup;
	
	public RankingForGroup(GroupIdentifier<C> identifier, ArrayList<Solution<S>> solutionsForGroup){
		super(solutionsForGroup);
		this.identifierOfGroup=identifier;	
	}
	
	public GroupIdentifier<C> getIdentifierForGroup(){
		return identifierOfGroup;
	}
}
