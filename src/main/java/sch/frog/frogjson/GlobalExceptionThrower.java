package sch.frog.frogjson;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.lang.reflect.InvocationTargetException;

public class GlobalExceptionThrower {

    public static final GlobalExceptionThrower INSTANCE = new GlobalExceptionThrower();

    private Stage exceptionStage = null;

    private final ExceptionView exceptionView = new ExceptionView();

    public void throwException(Throwable t){
        this.throwException(t, false);
    }

    public void throwException(Throwable t, boolean fromLazy){
        if(exceptionStage == null){
            try{
                exceptionStage = new Stage();
            }catch (IllegalStateException e){
                if(!fromLazy){
                    this.throwExceptionLazy(t);
                }else{
                    throw e;
                }
            }
            Scene secondScene = new Scene(exceptionView, 500, 400);
            exceptionStage.setScene(secondScene);
            exceptionStage.setTitle("An error occurred");
            exceptionStage.getIcons().add(ImageResources.appIcon);
        }

        Throwable cause = t.getCause();
        if(cause instanceof InvocationTargetException){
            t = ((InvocationTargetException) cause).getTargetException();
        }

        exceptionView.setException(t);
        exceptionStage.show();
        if (exceptionStage.isIconified()) {
            exceptionStage.setIconified(false);
        }else{
            exceptionStage.requestFocus();
        }
    }

    private void throwExceptionLazy(Throwable t){
        Platform.runLater(() -> {
            this.throwException(t, true);
        });
    }

}
