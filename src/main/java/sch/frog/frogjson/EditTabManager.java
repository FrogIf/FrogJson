package sch.frog.frogjson;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import sch.frog.frogjson.controls.JsonEditor;

class EditTabManager {

    private static  int tabIndex = 0;

    private static String generateTitle(){
        return "   Tab " + (tabIndex++) + "   ";
    }

    public static void addTab(TabPane tabPane, MessageEmitter emitter){
        addTab(tabPane, null, emitter);
    }

    public static void addTab(TabPane tabPane, String title, MessageEmitter emitter){
        if(tabPane.getTabs().isEmpty()){
            tabIndex = 0;
        }
        if(title == null || title.length() == 0){
            title = EditTabManager.generateTitle();
        }
        Tab newTab = new Tab(title);
        JsonEditor jsonEditor = new JsonEditor(emitter);
        newTab.setContent(jsonEditor);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

}
