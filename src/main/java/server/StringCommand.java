package server;

import java.util.ArrayList;

public class StringCommand {
    private String type;
    private ArrayList<String> key;
    private Object value;

    StringCommand(String type, ArrayList<String> key){
        this.type = type;
        this.key = key;
    }

    StringCommand(String type, ArrayList<String> key, Object value){
        this.type = type;
        this.key = key;
        this.value = value;
    }



    String getType(){
        return this.type;
    }

    ArrayList<String> getKey(){
        return this.key;
    }


    Object getValue(){
        return this.value;
    }
}
