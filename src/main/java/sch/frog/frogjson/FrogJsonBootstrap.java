package sch.frog.frogjson;

import javafx.application.Application;

public class FrogJsonBootstrap {

    public static void main(String[] args){
        try{
            Application.launch(FrogJsonApplication.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
