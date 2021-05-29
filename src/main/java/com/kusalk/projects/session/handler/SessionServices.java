package com.kusalk.projects.session.handler;

import com.kusalk.projects.session.handler.util.SessionCode;
import com.kusalk.projects.session.handler.util.SessionMessages;
import com.kusalk.projects.session.handler.util.SessionResponse;

public class SessionServices {

    private final InternalSessionContainer internalSessionContainer;
    private final boolean loadSessionsFromExternalLoader;
    private ExternalSessionContainer externalSessionContainer;

    public SessionServices( ExternalSessionContainer sessionLoader ) {
        this.internalSessionContainer = InternalSessionContainer.getInstance( );
        this.externalSessionContainer = sessionLoader;
        loadSessionsFromExternalLoader = sessionLoader != null;
    }

    public SessionServices( ) {
        this.internalSessionContainer = InternalSessionContainer.getInstance( );
        this.loadSessionsFromExternalLoader = false;
    }

    public SessionResponse<String> createSession( String sessionClass, long timeout ) {
        return internalSessionContainer.createSession( sessionClass, timeout );
    }

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

    public SessionResponse<Session> loadContainerLocalSession( String sessionId ) {
        Session session = internalSessionContainer.loadSessionFromContainer( sessionId );
        if ( session == null ) {
            return new SessionResponse<>( "No session for session id : " + sessionId + " found in local container", SessionCode.ERROR, null );
        }
        return new SessionResponse<>( "Session for session id : " + sessionId + " found in local container", SessionCode.SUCCESS, session );
    }

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
