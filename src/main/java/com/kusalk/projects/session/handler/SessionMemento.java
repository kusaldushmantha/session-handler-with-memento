package com.kusalk.projects.session.handler;

import java.io.Serializable;

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
