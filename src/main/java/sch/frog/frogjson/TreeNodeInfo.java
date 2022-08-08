package sch.frog.frogjson;

public class TreeNodeInfo {

    private final String key;

    private String value;

    private boolean isArrayElement;

    public TreeNodeInfo(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isArrayElement() {
        return isArrayElement;
    }

    public void setArrayElement(boolean arrayElement) {
        isArrayElement = arrayElement;
    }

    @Override
    public String toString() {
        if(value == null){
            return key;
        }else{
            return key + " : " + value;
        }
    }
}
