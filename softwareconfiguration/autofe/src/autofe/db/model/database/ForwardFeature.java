package autofe.db.model.database;

public class ForwardFeature extends AbstractFeature {

	public ForwardFeature(Attribute parent) {
		super(parent.getName(), parent);
	}

	public ForwardFeature(ForwardFeature toClone) {
		super(toClone.name, toClone.parent);
	}

	@Override
	public String toString() {
		return "ForwardFeature [name=" + name + ", parent=" + parent + "]";
	}

}
