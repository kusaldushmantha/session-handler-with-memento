package com.kusalk.projects.session.handler.demo;

import java.io.Serializable;

public class Person implements Serializable {

    private String name;
    private int age;
    private int socialSecurityNumber;
    private String address;

    public Person( String name, int age, int socialSecurityNumber, String address ) {
        this.name = name;
        this.age = age;
        this.socialSecurityNumber = socialSecurityNumber;
        this.address = address;
    }

    public Person( ) {
    }

    public String getName( ) {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public int getAge( ) {
        return age;
    }

    public void setAge( int age ) {
        this.age = age;
    }

    public int getSocialSecurityNumber( ) {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber( int socialSecurityNumber ) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public String getAddress( ) {
        return address;
    }

    public void setAddress( String address ) {
        this.address = address;
    }

    @Override
    public String toString( ) {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", socialSecurityNumber=" + socialSecurityNumber +
                ", address='" + address + '\'' +
                '}';
    }
}
