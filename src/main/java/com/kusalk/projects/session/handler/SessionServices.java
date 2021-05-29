package com.kusalk.projects.session.handler;

import com.kusalk.projects.session.handler.containers.ExternalSessionContainer;
import com.kusalk.projects.session.handler.containers.InternalSessionContainer;
import com.kusalk.projects.session.handler.session.Session;
import com.kusalk.projects.session.handler.session.SessionMemento;
import com.kusalk.projects.session.handler.util.SessionCode;
import com.kusalk.projects.session.handler.util.SessionMessages;
import com.kusalk.projects.session.handler.util.SessionResponse;

/**
 * This is the main API-implementation for using the session handler within an application
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
public class SessionServices {

    private final InternalSessionContainer internalSessionContainer;
    private final boolean loadSessionsFromExternalLoader;
    private ExternalSessionContainer externalSessionContainer;

    /**
     * Creates an instance of session services object with an external session source handler
     *
     * @param sessionLoader external session container handler object
     */
    public SessionServices( ExternalSessionContainer sessionLoader ) {
        this.internalSessionContainer = InternalSessionContainer.getInstance( );
        this.externalSessionContainer = sessionLoader;
        loadSessionsFromExternalLoader = sessionLoader != null;
    }

    /**
     * Creates an instance of session services object without an external session source handler
     */
    public SessionServices( ) {
        this.internalSessionContainer = InternalSessionContainer.getInstance( );
        this.loadSessionsFromExternalLoader = false;
    }

    /**
     * Creates a session from the provided session class with the provided timeout
     *
     * @param sessionClass session class
     * @param timeout      timeout in seconds
     * @return {@link SessionResponse<String>} session response
     */
    public SessionResponse<String> createSession( String sessionClass, long timeout ) {
        return internalSessionContainer.createSession( sessionClass, timeout );
    }

    /**
     * Loads the session object. This loads the session object from the {@link InternalSessionContainer} object
     * and if the session is not found and an external source is provided, then the session will try to retrieve
     * from the {@link ExternalSessionContainer} object
     *
     * @param sessionId session id
     * @return {@link SessionResponse< Session >} session response
     */
    public SessionResponse<Session> loadSession( String sessionId ) {
        SessionResponse<Session> session = loadContainerLocalSession( sessionId );
        if ( session.isError( ) && loadSessionsFromExternalLoader ) {
            session = loadExternalSourceSession( sessionId );
            if ( session.isError( ) ) {
                return new SessionResponse<>( SessionMessages.SESSION_EXPIRED, SessionCode.ERROR, null );
            }
        }
        return session;
    }

    /**
     * Saves the session to the configured external source
     *
     * @param session {@link Session} session object to be saved
     * @return {@link SessionResponse<Boolean>} session response
     */
    public SessionResponse<Boolean> saveSessionToExternalSource( Session session ) {
        try {
            session.startWritingSession( );

            SessionMemento memento = session.createMemento( );
            memento.setSessionClass( );
            memento.setSessionTimeoutInSeconds( session.getTimeoutInSeconds( ) );
            SessionResponse<Boolean> booleanSessionResponse = externalSessionContainer.saveSession( session.getSessionId( ), memento );
            if ( booleanSessionResponse.isSuccess( ) ) {
                return internalSessionContainer.removeSession( session.getSessionId( ) );
            } else {
                return new SessionResponse<Boolean>( "Session save to external source failed for session : " + session.getSessionId( ), SessionCode.ERROR, false );
            }
        } finally {
            session.endWritingSession( );
        }
    }

    /**
     * Loads the session from the {@link InternalSessionContainer} internal container
     *
     * @param sessionId session id to be loaded
     * @return {@link SessionResponse<Session>} session response
     */
    public SessionResponse<Session> loadContainerLocalSession( String sessionId ) {
        Session session = internalSessionContainer.loadSessionFromContainer( sessionId );
        if ( session == null ) {
            return new SessionResponse<>( "No session for session id : " + sessionId + " found in local container", SessionCode.ERROR, null );
        }
        return new SessionResponse<>( "Session for session id : " + sessionId + " found in local container", SessionCode.SUCCESS, session );
    }

    /**
     * Loads the session from the {@link ExternalSessionContainer} external container from the provided external source.
     * If the session can be loaded from the external source, then the application will provide a new session id for the loaded
     * session and will also add this session to the {@link InternalSessionContainer} application sessions map.
     *
     * @param sessionId session id to be loaded
     * @return {@link SessionResponse<Session>} session response
     */
    public SessionResponse<Session> loadExternalSourceSession( String sessionId ) {
        SessionResponse<SessionMemento> mementoSessionResponse = externalSessionContainer.loadSession( sessionId );
        if ( mementoSessionResponse.isSuccess( ) ) {
            SessionMemento memento = mementoSessionResponse.getData( );
            SessionResponse<String> sessionCreateResponse = internalSessionContainer.createSession( memento.getSessionClass( ), memento.getSessionTimeoutInSeconds( ) );
            if ( sessionCreateResponse.isSuccess( ) ) {
                Session createdSession = internalSessionContainer.loadSessionFromContainer( sessionCreateResponse.getData( ) );
                createdSession.restore( memento );
                return new SessionResponse<>( "Session load success from external source", SessionCode.SUCCESS, createdSession );
            }
        }
        return new SessionResponse<>( mementoSessionResponse.getMessage( ), SessionCode.ERROR, null );
    }
}
