package sch.frog.frogjson;

import javafx.scene.control.TreeItem;
import sch.frog.frogjson.json.IJsonValueWriter;
import sch.frog.frogjson.json.JsonArray;
import sch.frog.frogjson.json.JsonObject;
import sch.frog.frogjson.json.JsonValue;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class TreeStructJsonWriter implements IJsonValueWriter {

    private final TreeItem<TreeNodeInfo> root = new TreeItem<>(new TreeNodeInfo(FrogJsonConstants.TREE_ROOT_NAME));

    private final Stack<TreeItem<TreeNodeInfo>> cursorParentStack = new Stack<>();

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
        TreeItem<TreeNodeInfo> parent;
        if(!cursorParentStack.isEmpty()){
            parent = cursorParentStack.peek();
        }else{
            parent = root;
        }
        Iterator<Map.Entry<String, JsonValue<?>>> iterator = jsonObject.getIterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonValue<?>> entry = iterator.next();
            TreeItem<TreeNodeInfo> node = new TreeItem<>();

            TreeNodeInfo treeNodeInfo = new TreeNodeInfo(entry.getKey());

            cursorParentStack.push(node);
            entry.getValue().write(this);
            cursorParentStack.pop();

            if(this.valueString != null){
                treeNodeInfo.setValue(this.valueString);
                this.valueString = null;
            }
            node.setValue(treeNodeInfo);
            parent.getChildren().add(node);
        }
    }

    @Override
    public void writeArray(JsonArray array) {
        TreeItem<TreeNodeInfo> parent = null;
        if(!cursorParentStack.isEmpty()){
            parent = cursorParentStack.peek();
        }else{
            parent = root;
        }
        Iterator<JsonValue<?>> iterator = array.getIterator();
        int index = 0;
        while (iterator.hasNext()) {
            JsonValue<?> value = iterator.next();
            TreeItem<TreeNodeInfo> node = new TreeItem<>();
            TreeNodeInfo treeNodeInfo = new TreeNodeInfo(String.valueOf(ARRAY_BEGIN) + index + ARRAY_END);
            treeNodeInfo.setArrayElement(true);
            cursorParentStack.push(node);
            value.write(this);
            cursorParentStack.pop();

            if(this.valueString != null){
                treeNodeInfo.setValue(this.valueString);
                this.valueString = null;
            }
            node.setValue(treeNodeInfo);
            parent.getChildren().add(node);
            index++;
        }
    }

    public TreeItem<TreeNodeInfo> getRoot(){
        return this.root;
    }

    @Override
    public String toJson() {
        throw new UnsupportedOperationException("this writer can't generate json.");
    }
}
