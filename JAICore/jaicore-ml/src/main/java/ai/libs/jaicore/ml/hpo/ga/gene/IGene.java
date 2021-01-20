package ai.libs.jaicore.ml.hpo.ga.gene;

public interface IGene {

	public Object getValue();

	public String getValueAsString();

	public void setValue(Object value);

	public IGene copy();

}
