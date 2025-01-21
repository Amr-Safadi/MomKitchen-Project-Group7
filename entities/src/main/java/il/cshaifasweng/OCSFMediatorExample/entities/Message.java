package il.cshaifasweng.OCSFMediatorExample.entities;

// universal message class to make it easier to communicate

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;


    private Object object;
    private String text;

    public Message(Object object, String text) {
        this.object = object;
        this.text = text;
    }

    public Message(String text) {
        this.text = text;
        this.object = null;
    }

    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
