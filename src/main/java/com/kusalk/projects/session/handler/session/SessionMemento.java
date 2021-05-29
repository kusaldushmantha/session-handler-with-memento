package com.kusalk.projects.session.handler.session;

import java.io.Serializable;

/**
 * This is the parent class of all memento classes. Memento classes defines which data from the session objects should
 * be saved so that they can be retreived and restored at a later point.
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
public abstract class SessionMemento implements Serializable {

    protected String sessionClass;
    private long sessionTimeoutInSeconds;

    public String getSessionClass( ) {
        return sessionClass;
    }

    public abstract void setSessionClass( );

    public long getSessionTimeoutInSeconds( ) {
        return sessionTimeoutInSeconds;
    }

    public void setSessionTimeoutInSeconds( long sessionTimeoutInSeconds ) {
        this.sessionTimeoutInSeconds = sessionTimeoutInSeconds;
    }
}
