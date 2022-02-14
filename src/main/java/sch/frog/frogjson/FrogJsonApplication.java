package sch.frog.frogjson;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class FrogJsonApplication extends Application {

    public static FrogJsonApplication self;

    @Override
    public void start(Stage stage) throws IOException {
        self = this;
        exceptionHandle();
        FXMLLoader fxmlLoader = new FXMLLoader(FrogJsonApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("FrogJson");
        stage.setScene(scene);
        stage.getIcons().add(ImageResources.appIcon);
        stage.show();
    }

    public static HostServices getAppHostServices(){
        return self.getHostServices();
    }

    private Stage exceptionStage = null;

    private final ExceptionView exceptionView = new ExceptionView();

    private void exceptionHandle(){
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if(exceptionStage == null){
                    exceptionStage = new Stage();
                    Scene secondScene = new Scene(exceptionView, 500, 400);
                    exceptionStage.setScene(secondScene);
                    exceptionStage.setTitle("An error occurred");
                    exceptionStage.getIcons().add(ImageResources.appIcon);
                }

                Throwable cause = e.getCause();
                if(cause instanceof InvocationTargetException){
                    e = ((InvocationTargetException) cause).getTargetException();
                }

                exceptionView.setException(e);
                exceptionStage.show();
                if (exceptionStage.isIconified()) {
                    exceptionStage.setIconified(false);
                }else{
                    exceptionStage.requestFocus();
                }
            }
        });
    }
}