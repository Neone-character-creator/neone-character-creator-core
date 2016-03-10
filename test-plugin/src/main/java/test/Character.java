package test;

import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;

import java.math.BigInteger;

/**
 * Created by Damien on 1/12/2016.
 */
public class Character extends io.github.thisisnozaku.charactercreator.plugins.Character {
    private String firstName;
    private String secondName;
    private Address address;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
