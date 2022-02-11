package sch.frog.frogjson;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class HelpController {

    @FXML
    private Hyperlink githubLink;

    @FXML
    protected void gotoGithub(){
        FrogJsonApplication.getAppHostServices().showDocument(githubLink.getText());
    }

}
