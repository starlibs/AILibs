package ai.libs.jaicore.math.probability.pl;

import java.util.HashMap;

public class PLSkillMap extends HashMap<Object, Double> {
	public Object getObjectWithHighestSkill() {
		return this.keySet().stream().max((k1,k2) -> Double.compare(this.get(k1), this.get(k2))).get();
	}
}
