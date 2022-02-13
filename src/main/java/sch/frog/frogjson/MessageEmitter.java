package sch.frog.frogjson;

import javafx.scene.control.Label;

public class MessageEmitter {

    private final Label messageLabel;

    public MessageEmitter(Label messageLabel) {
        this.messageLabel = messageLabel;
    }

    public void emitInfo(String msg){
        messageLabel.setText(msg);
    }

    public void emitWarn(String msg){
        messageLabel.setText(msg);
    }

    public void emitError(String msg){
        messageLabel.setText(msg);
    }

    public void clear(){
        messageLabel.setText("");
    }

}
