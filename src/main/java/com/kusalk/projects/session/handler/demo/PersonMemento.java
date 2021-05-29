package com.kusalk.projects.session.handler.demo;

import com.kusalk.projects.session.handler.session.SessionMemento;

/**
 * Demo entity memento class
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
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
