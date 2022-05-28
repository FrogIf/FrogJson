package sch.frog.frogjson;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RenameTabController {

    private ConfirmCallback confirmCallback;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button confirmBtn;

    @FXML
    private TextField nameInput;

    @FXML
    protected void onConfirm() {
        if(confirmCallback != null){
            confirmCallback.confirm(nameInput.getText());
        }
        Stage stage = (Stage)confirmBtn.getScene().getWindow();
        stage.close();
    }


    @FXML
    protected void onCancel() {
        Stage stage = (Stage)cancelBtn.getScene().getWindow();
        stage.close();
    }

    public void setConfirmCallback(ConfirmCallback confirmCallback) {
        this.confirmCallback = confirmCallback;
    }

    public void setOriginTabName(String name){
        this.nameInput.setText(name);
    }

    public interface ConfirmCallback{
        void confirm(String name);
    }

}
