package es.pablomellado.pandealfacar.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Pablo Mellado on 19/4/17.
 */

@IgnoreExtraProperties
public class Client {
    private String phone;
    private String name;

    public Client() {
    }

    public Client(String phone, String name) {
        this.phone = phone;
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }
}
