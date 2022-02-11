package sch.frog.frogjson;

import javafx.scene.image.Image;

import java.util.Objects;

public class ImageResources {

    public static Image appIcon = new Image(Objects.requireNonNull(FrogJsonApplication.class.getResourceAsStream("/logo.png")));

}
