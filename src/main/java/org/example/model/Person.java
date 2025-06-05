package org.example.model;

import io.ebean.annotation.NotNull;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class Person implements Serializable {
    @Id
    @Column(name = "personID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "personUsername")
    private String username;
    @NotNull
    @Column(name = "personPassword")
    private String password;
    @NotNull
    @Column(name = "personName")
    private String name;
    @NotNull
    @Column(name = "personAddress")
    private String address;
    @NotNull
    @Column(name = "personCategory")
    private String category;

    public Person() {
    }

    public Person(Long id, String username, String password, String name, String address, String category) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.address = address;
        this.category = category;
    }

    public Person(String username, String password, String name, String address, String category) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.address = address;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Username: " + this.getUsername()
                + "\nName: " + this.getName()
                + "\nAddress: " + this.getAddress();
    }


}
