package com.kusalk.projects.session.handler.demo;

import com.kusalk.projects.session.handler.session.Session;
import com.kusalk.projects.session.handler.session.SessionMemento;

/**
 * Demo entity session object class
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
public class PersonSession extends Session {

    private Person person;

    @Override
    public void restore( SessionMemento memento ) {
        PersonMemento personMemento = ( PersonMemento ) memento;
        this.person = personMemento.getPerson( );
    }

    @Override
    public SessionMemento createMemento( ) {
        PersonMemento memento = new PersonMemento( );
        memento.setPerson( person );
        return memento;
    }

    public Person getPerson( ) {
        return person;
    }

    public void setPerson( Person person ) {
        this.person = person;
    }
}
