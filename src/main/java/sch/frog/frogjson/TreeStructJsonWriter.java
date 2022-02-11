package sch.frog.frogjson;

import javafx.scene.control.TreeItem;
import sch.frog.frogjson.json.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class TreeStructJsonWriter implements IJsonValueWriter {

    private final TreeItem<String> root = new TreeItem<>("tree");

    private final Stack<TreeItem<String>> cursorParentStack = new Stack<>();

    private final static char MAP_CHAR = ':';

    private final static char SPACE = ' ';

    private final static char QUOTATION = '\"';

    private final static char ARRAY_BEGIN = '[';

    private final static char ARRAY_END = ']';

    private String valueString = null;

    @Override
    public void writeString(String str) {
        this.valueString = QUOTATION + str + QUOTATION;
    }

    @Override
    public void writeOrigin(String origin) {
        this.valueString = origin;
    }

    @Override
    public void writeObject(JsonObject jsonObject) {
        TreeItem<String> parent;
        if(!cursorParentStack.isEmpty()){
            parent = cursorParentStack.peek();
        }else{
            parent = root;
        }
        Iterator<Map.Entry<String, JsonValue<?>>> iterator = jsonObject.getIterator();
        StringBuilder itemText = new StringBuilder();
        while (iterator.hasNext()) {
            itemText.delete(0, itemText.length());
            Map.Entry<String, JsonValue<?>> entry = iterator.next();
            TreeItem<String> node = new TreeItem<>();
            itemText.append(entry.getKey());

            cursorParentStack.push(node);
            entry.getValue().write(this);
            cursorParentStack.pop();

            if(this.valueString != null){
                itemText.append(SPACE).append(MAP_CHAR).append(SPACE).append(this.valueString);
                this.valueString = null;
            }
            node.setValue(itemText.toString());
            parent.getChildren().add(node);
        }
    }

    @Override
    public void writeArray(JsonArray array) {
        TreeItem<String> parent = null;
        if(!cursorParentStack.isEmpty()){
            parent = cursorParentStack.peek();
        }else{
            parent = root;
        }
        Iterator<JsonValue<?>> iterator = array.getIterator();
        StringBuilder itemText = new StringBuilder();
        int index = 0;
        while (iterator.hasNext()) {
            itemText.delete(0, itemText.length());
            JsonValue<?> value = iterator.next();
            TreeItem<String> node = new TreeItem<>();
            itemText.append(ARRAY_BEGIN).append(index).append(ARRAY_END);
            cursorParentStack.push(node);
            value.write(this);
            cursorParentStack.pop();

            if(this.valueString != null){
                itemText.append(MAP_CHAR).append(this.valueString);
                this.valueString = null;
            }
            node.setValue(itemText.toString());
            parent.getChildren().add(node);
            index++;
        }
    }

    public TreeItem<String> getRoot(){
        return this.root;
    }

    @Override
    public String toJson() {
        throw new UnsupportedOperationException("this writer can't generate json.");
    }
}
