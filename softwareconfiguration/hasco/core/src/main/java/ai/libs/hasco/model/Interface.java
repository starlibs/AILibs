package ai.libs.hasco.model;

public class Interface {
    private final String id;
    private final String name;
    private final Integer min;
    private final Integer max;

    /* Unused
    @JsonCreator
    public Interface(@JsonProperty("id") final String id,
                     @JsonProperty("name") final String name,
                     @JsonProperty("optional") final Boolean optional,
                     @JsonProperty("min") final Integer min,
                     @JsonProperty("max") final Integer max) {
        this.id = id;
        this.name = name;

        if(optional == null && (min != null && max != null)) {
            this.min = min;
            this.max = max;
        } else if (optional != null && (min == null && max == null)) {
            this.min = 0;
            this.max = 1;
        } else { // optional == null && (min == null && max == null)
            this.min = 1;
            this.max = 1;
        }
    }
    */

    public Interface(final String id, final String name, final Integer min, final Integer max) {
        this.id = id;
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }
}
