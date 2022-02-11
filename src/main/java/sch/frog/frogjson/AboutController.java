package sch.frog.frogjson;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class AboutController {

    @FXML
    private Hyperlink githubLink;

    @FXML
    protected void gotoGithub(){
        FrogJsonApplication.getAppHostServices().showDocument(githubLink.getText());
    }

}
