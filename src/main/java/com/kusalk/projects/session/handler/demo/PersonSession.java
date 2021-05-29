package com.kusalk.projects.session.handler.demo;

import com.kusalk.projects.session.handler.Session;
import com.kusalk.projects.session.handler.SessionMemento;

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
