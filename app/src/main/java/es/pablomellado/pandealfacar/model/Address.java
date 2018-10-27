package es.pablomellado.pandealfacar.model;

import java.io.Serializable;

/**
 * Created by Pablo Mellado on 20/4/17.
 */

public class Address implements Serializable{
    private String key;
    private String contact;
    private String line1;
    private String line2;
    private String city;
    private String postalcode;

    public Address(String key, String contact, String line1, String line2, String city, String postalcode){
        this.key = key;
        this.contact = contact;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.postalcode = postalcode;
    }

    public Address(String contact, String line1, String line2, String city, String postalcode) {
        this.contact = contact;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.postalcode = postalcode;
    }

    public Address() {
    }

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key = key;
    }

    public String getContact(){
        return contact;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getPostalcode() {
        return postalcode;
    }

    @Override
    public String toString() {
        String result;
        result = contact + '\n' + line1 + '\n';
        if (line2.trim().length()>0){
            result += line2 + '\n';
        }
        result += postalcode + ' ' + city;

        return result;
    }
}
