package top.sunbread.MCBingo.util;

import java.util.Objects;

public class VariablePair {

    private String name;
    private String value;

    public VariablePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public VariablePair() {
        this(null, null);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof VariablePair)) return false;
        VariablePair vp = (VariablePair) o;
        return Objects.equals(name, vp.name) && Objects.equals(value, vp.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name) ^ Objects.hashCode(value);
    }

}
