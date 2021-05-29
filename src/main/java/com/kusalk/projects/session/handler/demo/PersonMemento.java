package com.kusalk.projects.session.handler.demo;

import com.kusalk.projects.session.handler.SessionMemento;

public class PersonMemento extends SessionMemento {

    private Person person;

    public Person getPerson( ) {
        return person;
    }

    public void setPerson( Person person ) {
        this.person = person;
    }

    @Override
    public void setSessionClass( ) {
        this.sessionClass = "com.kusalk.projects.session.handler.demo.PersonSession";
    }
}
