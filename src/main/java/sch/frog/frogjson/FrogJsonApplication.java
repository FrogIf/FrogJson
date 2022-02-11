package sch.frog.frogjson;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;

public class FrogJsonApplication extends Application {

    public static FrogJsonApplication self;

    @Override
    public void start(Stage stage) throws IOException {
        self = this;
        FXMLLoader fxmlLoader = new FXMLLoader(FrogJsonApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("FrogJson");
        stage.setScene(scene);
        stage.getIcons().add(ImageResources.appIcon);
        stage.show();
        this.onLoadSuccess(stage.getScene().getRoot());
    }

    private void onLoadSuccess(Parent root){
        ObservableList<Node> nodes = root.getChildrenUnmodifiable();
        for(Node node : nodes){
            if(node instanceof TabPane){
                EditTabManager.addTab((TabPane) node);
                break;
            }
        }
    }

    public static HostServices getAppHostServices(){
        return self.getHostServices();
    }

    public static void main(String[] args) {
        launch();
    }
}