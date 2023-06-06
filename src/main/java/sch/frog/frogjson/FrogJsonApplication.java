package sch.frog.frogjson;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sch.frog.frogjson.config.GlobalInnerProperties;

import java.io.IOException;

public class FrogJsonApplication extends Application {

    public static FrogJsonApplication self;

    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        self = this;
        exceptionHandle();
        FXMLLoader fxmlLoader = new FXMLLoader(FrogJsonApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        scene.getStylesheets().add(FrogJsonApplication.class.getResource("css/json-assist.css").toExternalForm());
        scene.getStylesheets().add(FrogJsonApplication.class.getResource("css/common.css").toExternalForm());
        stage.setTitle("FrogJson " + GlobalInnerProperties.getProperty("application.version"));
        stage.setScene(scene);
        stage.getIcons().add(ImageResources.appIcon);
        stage.show();
        this.primaryStage = stage;
        stage.setOnCloseRequest(e -> {
            GlobalApplicationLifecycleUtil.stop();
        });
    }

    public static HostServices getAppHostServices(){
        return self.getHostServices();
    }

    private void exceptionHandle(){
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> GlobalExceptionThrower.INSTANCE.throwException(e));
    }

    public Stage getPrimaryStage(){
        return this.primaryStage;
    }
}