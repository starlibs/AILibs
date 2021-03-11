package ai.libs.jaicore.basic.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Tree implements ITree {

	private final Map<String, Object> annotations = new HashMap<>();

	private ITree parent = null;
	private List<ITree> successors;
	private List<ITree> leafCache = null;

	public Tree() {
		this.successors = new ArrayList<>();
	}

	public Tree(final List<? extends ITree> successors) {
		successors.stream().forEach(x -> x.setParent(this));
		this.successors = new ArrayList<>(successors);
	}

	@Override
	public void addSuccessor(final ITree successor) {
		successor.setParent(this);
		this.successors.add(successor);
	}

	@Override
	public List<ITree> getSuccessors() {
		return this.successors;
	}

	@Override
	public boolean isLeaf() {
		return this.successors.isEmpty();
	}

	@Override
	public List<ITree> getLeaves() {
		if (this.leafCache == null) {
			List<ITree> leaves = new ArrayList<>();
			if (this.isLeaf()) {
				leaves.add(this);
			} else {
				for (ITree succ : this.successors) {
					leaves.addAll(succ.getLeaves());
				}
			}
			this.leafCache = leaves;
		}
		return this.leafCache;
	}

	@Override
	public Map<String, Object> getAnnotations() {
		return this.annotations;
	}

	@Override
	public String toString(final ITreeDescriptor nodeDescriptor) {
		StringBuilder sb = new StringBuilder();
		sb.append(nodeDescriptor.getTreeDescription(this));
		if (!this.isLeaf()) {
			sb.append("(").append(this.successors.stream().map(x -> x.toString(nodeDescriptor)).collect(Collectors.joining(","))).append(")");
		}
		return sb.toString();
	}

	@Override
	public void acceptPrefix(final ITreeVisitor visitor) {
		visitor.accept(this);
		for (ITree successor : this.successors) {
			successor.acceptPrefix(visitor);
		}
	}

	@Override
	public void acceptInfix(final ITreeVisitor visitor) {
		if (!this.successors.isEmpty()) {
			this.successors.get(0).acceptInfix(visitor);
		}
		visitor.accept(this);
		for (int i = 1; i < this.successors.size(); i++) {
			this.successors.get(i).acceptInfix(visitor);
		}
	}

	@Override
	public void acceptPostfix(final ITreeVisitor visitor) {
		for (ITree successor : this.successors) {
			successor.acceptPrefix(visitor);
		}
		visitor.accept(this);
	}

	@Override
	public ITree getParent() {
		return this.parent;
	}

	@Override
	public void setParent(final ITree parent) {
		this.parent = parent;
	}

}
