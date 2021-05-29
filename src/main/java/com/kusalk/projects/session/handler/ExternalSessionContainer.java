package com.kusalk.projects.session.handler;

import com.kusalk.projects.session.handler.external.sources.ExternalSessionSource;
import com.kusalk.projects.session.handler.util.SessionCode;
import com.kusalk.projects.session.handler.util.SessionResponse;

public class ExternalSessionContainer {

    private final ExternalSessionSource externalSessionSource;

    public ExternalSessionContainer( ExternalSessionSource externalSessionSource ) {
        this.externalSessionSource = externalSessionSource;
    }

    public SessionResponse<SessionMemento> loadSession( String sessionId ) {
        SessionResponse<SessionMemento> mementoSessionResponse = externalSessionSource.readSessionMemento( sessionId );
        if ( mementoSessionResponse.isSuccess( ) ) {
            return mementoSessionResponse;
        }
        return new SessionResponse<>( "Error loading session from external source : " + sessionId, SessionCode.ERROR, null );
    }

    public SessionResponse<Boolean> saveSession( String sessionId, SessionMemento memento ) {
        return externalSessionSource.writeSessionMemento( sessionId, memento );
    }

}
