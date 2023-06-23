package sch.frog.frogjson.controls;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import sch.frog.frogjson.ClipboardUtil;
import sch.frog.frogjson.MessageEmitter;
import sch.frog.frogjson.TreeNodeInfo;

import java.util.Iterator;
import java.util.Stack;

public class JsonTreeBox extends BorderPane {

    private final TreeView<TreeNodeInfo> treeView;

    private final SearchBox treeSearchBox;

    private final TreeSearchForeachActionForNext treeSearchForeachActionForNext;

    private final TreeSearchForeachActionForPrevious treeSearchForeachActionForPrevious;

    public JsonTreeBox(MessageEmitter messageEmitter, JsonEditor parent) {
        treeView = new TreeView<>();
        treeSearchForeachActionForNext = new TreeSearchForeachActionForNext(this.treeView, messageEmitter);
        treeSearchForeachActionForPrevious = new TreeSearchForeachActionForPrevious(this.treeView, messageEmitter);
        treeView.setContextMenu(initContextMenu(treeView));

        StackPane treeContainer = new StackPane();
        treeContainer.getChildren().add(treeView);

        Button closeBtn = new Button("×");
//        closeBtn.prefHeight(12);
//        closeBtn.maxHeight(12);
//        closeBtn.prefWidth(12);
//        closeBtn.maxWidth(12);
        closeBtn.setStyle("-fx-font-size: 10; -fx-cursor: hand;-fx-background-radius:10;-fx-border-radius:10;-fx-background-color: #9b9b9b;");
        closeBtn.setOnMouseClicked(mouseEvent -> {
            parent.closeTree();
        });
        StackPane.setMargin(closeBtn, new Insets(5, 30, 0, 0));
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        treeContainer.getChildren().add(closeBtn);

//        Circle circle = new Circle();
//        Label close = new Label("×");
//        close.setPadding(new Insets(0, 10, 0, 10));
//        close.setStyle("-fx-font-size: 16; -fx-cursor: hand;");
//        close.setOnMouseClicked(mouseEvent -> {
//            parent.closeTree();
//        });
//        Pane p = new Pane();
//        p.prefHeight(10);
//        p.prefWidth(10);
//        p.setStyle("-fx-background-radius:4;-fx-border-radius:4;-fx-background-color: #9b9b9b;");
//        p.getChildren().add(close);
//        StackPane.setAlignment(p, Pos.TOP_RIGHT);
//        treeContainer.getChildren().add(p);

        this.setCenter(treeContainer);
        treeSearchBox = new SearchBox(this, (text, searchOverviewFetcher) -> {
            messageEmitter.clear();
            searchNextForTree(text, messageEmitter);
        }, (text, searchOverviewFetcher) -> {
            messageEmitter.clear();
            searchPreviousForTree(text, messageEmitter);
        });
        this.setOnKeyPressed(keyEvent -> {
            if(keyEvent.isControlDown()){
                if(keyEvent.getCode() == KeyCode.F){
                    this.setTop(treeSearchBox);
                    treeSearchBox.focusSearch();
                }else if(keyEvent.getCode() == KeyCode.C){
                    this.copySelectContent();
                }
            }
        });
        treeSearchBox.onClose(treeView::requestFocus);
    }

    private ContextMenu initContextMenu(final TreeView<TreeNodeInfo> treeView) {
        ContextMenu treeContextMenu = new ContextMenu();

        Menu menu = new Menu("Copy");
        MenuItem copyValue = new MenuItem("Copy Value");
        copyValue.setOnAction(event -> {
            this.copySelectValue();
        });
        MenuItem copyKey = new MenuItem("Copy Key");
        copyKey.setOnAction(event -> {
            this.copySelectKey();
        });
        MenuItem copyAll = new MenuItem("Copy All");
        copyAll.setOnAction(actionEvent -> {
            this.copySelectContent();
        });
        menu.getItems().addAll(copyValue, copyKey, copyAll);

        MenuItem collapse = new MenuItem("Collapse All");
        collapse.setOnAction(actionEvent -> {
            TreeItem<TreeNodeInfo> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if(selectedItem != null){
                collapseOrExpand(selectedItem, false);
            }
        });
        MenuItem expand = new MenuItem("Expand All");
        expand.setOnAction(actionEvent -> {
            TreeItem<TreeNodeInfo> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if(selectedItem != null){
                collapseOrExpand(selectedItem, true);
            }
        });
        ObservableList<MenuItem> items = treeContextMenu.getItems();
        items.add(menu);
        items.add(expand);
        items.add(collapse);
        return treeContextMenu;
    }

    private void copySelectValue(){
        TreeItem<TreeNodeInfo> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TreeNodeInfo value = selectedItem.getValue();
            ClipboardUtil.putToClipboard(value.getValue());
        }
    }

    private void copySelectKey(){
        TreeItem<TreeNodeInfo> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TreeNodeInfo value = selectedItem.getValue();
            ClipboardUtil.putToClipboard(value.getKey());
        }
    }

    private void copySelectContent(){
        TreeItem<TreeNodeInfo> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TreeNodeInfo value = selectedItem.getValue();
            ClipboardUtil.putToClipboard(value.toString());
        }
    }

    private void collapseOrExpand(TreeItem<TreeNodeInfo> treeItem, boolean expand){
        foreachTree(treeItem, node -> {
            node.setExpanded(expand);
            return true;
        });
    }

    private void foreachTree(TreeItem<TreeNodeInfo> start, ForeachAction action){
        if(!action.doSomething(start)){
            return;
        }
        ObservableList<TreeItem<TreeNodeInfo>> items = start.getChildren();
        if(items != null && !items.isEmpty()){
            Stack<Iterator<TreeItem<TreeNodeInfo>>> rubberBand = new Stack<>();
            rubberBand.push(items.iterator());
            while(!rubberBand.isEmpty()){
                while(rubberBand.peek().hasNext()){
                    TreeItem<TreeNodeInfo> item = rubberBand.peek().next();
                    if(!action.doSomething(item)){
                        return;
                    }
                    ObservableList<TreeItem<TreeNodeInfo>> children = item.getChildren();
                    if(children != null && !children.isEmpty()){
                        rubberBand.push(children.iterator());
                    }
                }
                rubberBand.pop();
            }
        }
        action.searchFinish();
    }

    public void setTreeRoot(TreeItem<TreeNodeInfo> root) {
        this.treeView.setRoot(root);
    }

    private interface ForeachAction{
        boolean doSomething(TreeItem<TreeNodeInfo> node);
        default void searchFinish(){ /* do nothing */ }
    }

    private void searchNextForTree(String text, MessageEmitter emitter){
        if(text == null || text.isEmpty()){
            emitter.emitWarn("please input search keyword");
            return;
        }
        TreeItem<TreeNodeInfo> start = treeView.getSelectionModel().getSelectedItem();
        TreeItem<TreeNodeInfo> root = treeView.getRoot();
        if(start == null){
            start = root;
        }
        if(start != null){
            treeSearchForeachActionForNext.prepareSearch(start, text);
            foreachTree(root, treeSearchForeachActionForNext);
        }
    }

    private void searchPreviousForTree(String text, MessageEmitter emitter){
        if(text == null || text.isEmpty()){
            emitter.emitWarn("please input search keyword");
            return;
        }
        TreeItem<TreeNodeInfo> start = treeView.getSelectionModel().getSelectedItem();
        TreeItem<TreeNodeInfo> root = treeView.getRoot();
        if(start == null){
            start = root;
        }
        if(start != null){
            treeSearchForeachActionForPrevious.prepareSearch(start, text);
            foreachTree(root, treeSearchForeachActionForPrevious);
        }
    }

    private static class TreeSearchForeachActionForNext implements ForeachAction{

        private TreeItem<TreeNodeInfo> start;

        private final TreeView<TreeNodeInfo> tree;

        private String searchText;

        private final MessageEmitter emitter;

        private boolean reachBottom = false;

        public TreeSearchForeachActionForNext(TreeView<TreeNodeInfo> treeView, MessageEmitter emitter) {
            this.tree = treeView;
            this.emitter = emitter;
        }

        boolean matchStart = false;

        @Override
        public boolean doSomething(TreeItem<TreeNodeInfo> node) {
            if(start == node){
                matchStart = true;
            }else if(matchStart){
                TreeNodeInfo value = node.getValue();
                if(value != null && value.toString().contains(searchText)){
                    tree.getSelectionModel().select(node);
                    tree.scrollTo(tree.getRow(node));
                    return false;
                }
            }
            return true;
        }

        @Override
        public void searchFinish() {
            emitter.emitWarn("search reach bottom");
            this.reachBottom = true;
        }

        public void prepareSearch(TreeItem<TreeNodeInfo> start, String text){
            this.start = start;
            if(this.searchText != null && this.searchText.equals(text) && this.reachBottom){
                this.start = tree.getRoot();
            }
            this.searchText = text;
            this.matchStart = false;
            this.reachBottom = false;
        }
    }

    private static class TreeSearchForeachActionForPrevious implements ForeachAction{

        TreeItem<TreeNodeInfo> preMatch;

        TreeItem<TreeNodeInfo> start;

        TreeView<TreeNodeInfo> tree;

        String searchText;

        MessageEmitter emitter;

        boolean reachTop;

        public TreeSearchForeachActionForPrevious(TreeView<TreeNodeInfo> treeView, MessageEmitter emitter) {
            this.tree = treeView;
            this.emitter = emitter;
        }

        @Override
        public boolean doSomething(TreeItem<TreeNodeInfo> node) {
            TreeNodeInfo value = node.getValue();
            if(start != node && value != null && value.toString().contains(searchText)){
                preMatch = node;
            }
            if(start == node){
                if(preMatch != null){
                    tree.getSelectionModel().select(preMatch);
                    tree.scrollTo(tree.getRow(preMatch));
                }else{
                    this.reachTop = true;
                    emitter.emitWarn("search reach top");
                }
                return false;
            }else{
                return true;
            }
        }

        @Override
        public void searchFinish() {
            if(start == null && this.preMatch != null){ // 说明从最后一个开始向前搜索
                tree.getSelectionModel().select(preMatch);
                tree.scrollTo(tree.getRow(preMatch));
            }
        }

        public void prepareSearch(TreeItem<TreeNodeInfo> start, String text){
            this.start = start;
            if(this.searchText != null && this.searchText.equals(text) && this.reachTop){
                this.start = null;
            }
            this.searchText = text;
            this.preMatch = null;
            this.reachTop = false;
        }
    }
}
