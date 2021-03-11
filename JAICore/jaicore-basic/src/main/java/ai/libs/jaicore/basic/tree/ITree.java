package ai.libs.jaicore.basic.tree;

import java.util.List;

import ai.libs.jaicore.basic.IAnnotatable;

public interface ITree extends IAnnotatable {

	public ITree getParent();

	public List<ITree> getLeaves();

	public boolean isLeaf();

	public List<ITree> getSuccessors();

	public void addSuccessor(ITree successor);

	public void acceptPrefix(ITreeVisitor visitor);

	public void acceptInfix(ITreeVisitor visitor);

	public void acceptPostfix(ITreeVisitor visitor);

	public String toString(ITreeDescriptor descriptor);

	public void setParent(final ITree parent);

}
