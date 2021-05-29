package com.kusalk.projects.session.handler.containers;

import com.kusalk.projects.session.handler.external.sources.ExternalSessionSource;
import com.kusalk.projects.session.handler.session.SessionMemento;
import com.kusalk.projects.session.handler.util.SessionCode;
import com.kusalk.projects.session.handler.util.SessionResponse;

/**
 * This class handles the basic implementations to deal with the {@link ExternalSessionSource} external source
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
public class ExternalSessionContainer {

    private final ExternalSessionSource externalSessionSource;

    public ExternalSessionContainer( ExternalSessionSource externalSessionSource ) {
        this.externalSessionSource = externalSessionSource;
    }

    /**
     * Loads the session from the external source
     *
     * @param sessionId session id
     * @return {@link SessionResponse<SessionMemento>} response object
     */
    public SessionResponse<SessionMemento> loadSession( String sessionId ) {
        SessionResponse<SessionMemento> mementoSessionResponse = externalSessionSource.readSessionMemento( sessionId );
        if ( mementoSessionResponse.isSuccess( ) ) {
            return mementoSessionResponse;
        }
        return new SessionResponse<>( "Error loading session from external source : " + sessionId, SessionCode.ERROR, null );
    }

    /**
     * Writes the memento object to the external source
     *
     * @param sessionId session id
     * @param memento   memento object
     * @return {@link SessionResponse<Boolean>} response object with {@code true} if success
     */
    public SessionResponse<Boolean> saveSession( String sessionId, SessionMemento memento ) {
        return externalSessionSource.writeSessionMemento( sessionId, memento );
    }

}
