package com.kusalk.projects.session.handler.containers;

import com.kusalk.projects.session.handler.session.Session;
import com.kusalk.projects.session.handler.util.SessionCode;
import com.kusalk.projects.session.handler.util.SessionResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains the implementation logic to handle sessions within a single server instance. Only one instance
 * of this class should be there for the entire app server as this class is responsible for handling all the sessions
 * within the application.
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
public class InternalSessionContainer {

    private static final Logger LOGGER = Logger.getLogger( InternalSessionContainer.class.getName( ) );

    public static final int SESSION_GENERATE_LIMIT = 3;
    public static final int SESSION_REMOVER_WAIT_TIME_MILLIS = 10000;

    private final ConcurrentHashMap<String, Session> sessionContainer = new ConcurrentHashMap<>( );
    private final ConcurrentLinkedQueue<String> sessionQueue = new ConcurrentLinkedQueue<>( );

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock( true );
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock( );
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock( );

    private InternalSessionContainer( ) {
        runSessionRemoverThread( );
    }

    /**
     * Single session container instance should be maintained throughout the application
     *
     * @return {@link InternalSessionContainer} instance
     */
    public static InternalSessionContainer getInstance( ) {
        LOGGER.log( Level.FINE, "Creating an internal session container" );
        return ResourceHolder.LOCAL_SESSION_CONTAINER;
    }

    /**
     * Loads the session from the session container object within the server.
     *
     * @param sessionId session id
     * @return {@link Session} session object
     */
    public Session loadSessionFromContainer( String sessionId ) {
        try {
            readLock.lock( );
            if ( sessionContainer.containsKey( sessionId ) ) {
                Session session = sessionContainer.get( sessionId );
                session.setLastLoadedTimestamp( System.currentTimeMillis( ) );
                return sessionContainer.get( sessionId );
            }
        } catch ( Exception e ) {
            LOGGER.log( Level.SEVERE, e, ( ) -> "Error occurred while loading session from internal container" );
        } finally {
            readLock.unlock( );
        }
        return null;
    }

    /**
     * Creates a session object based on the provided session-class with the provided timeout. This will add the
     * newly created session to the session container within the server
     *
     * @param sessionClass     session-class
     * @param timeoutInSeconds timeout in seconds
     * @return {@link SessionResponse<String>} session response
     */
    public SessionResponse<String> createSession( String sessionClass, long timeoutInSeconds ) {
        try {
            writeLock.lock( );

            String sessionId;
            int retryCounter = 0;
            boolean sessionContainedAlready;

            do {
                sessionId = UUID.randomUUID( ).toString( );
                sessionContainedAlready = sessionContainer.containsKey( sessionId );
                retryCounter++;
            } while ( sessionContainedAlready && retryCounter <= SESSION_GENERATE_LIMIT );

            if ( sessionContainedAlready ) {
                LOGGER.log( Level.WARNING, ( ) -> "Error creating session. Session store contains generated sessionIDs. Tried " + SESSION_GENERATE_LIMIT + " times" );
                return new SessionResponse<>( "Error creating session. Session store contains generated sessionIDs", SessionCode.ERROR, null );
            }

            Session sessionObject = ( Session ) Class.forName( sessionClass ).getDeclaredConstructor( ).newInstance( );
            sessionObject.setSessionId( sessionId );
            sessionObject.setTimeoutInSeconds( timeoutInSeconds );
            sessionObject.setLastLoadedTimestamp( System.currentTimeMillis( ) );

            sessionContainer.put( sessionId, sessionObject );
            boolean addToQueue = sessionQueue.add( sessionId );
            if ( addToQueue ) {
                return new SessionResponse<>( "Session created successfully and added to local container", SessionCode.SUCCESS, sessionId );
            } else {
                LOGGER.log( Level.WARNING, "Error adding sessionID for session queue. SessionID : {0}", sessionId );

                removeSessionFromLocalContainer( sessionId );
                return new SessionResponse<>( "Session could not add to local session queue", SessionCode.ERROR, sessionId );
            }

        } catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e ) {
            LOGGER.log( Level.SEVERE, e, ( ) -> "Error while creating a session" );
            return new SessionResponse<>( "Error creating session : " + e.getCause( ).getMessage( ), SessionCode.ERROR, null );
        } finally {
            writeLock.unlock( );
        }
    }

    /**
     * Removes the session from the local session-container. This can happen when a session gets timed out or when the session gets written
     * to an external source and no longer needed in the memory.
     *
     * @param sessionId session id
     * @return {@link SessionResponse<Boolean>} session response
     */
    public SessionResponse<Boolean> removeSession( String sessionId ) {
        try {
            writeLock.lock( );
            Session remove = removeSessionFromLocalContainer( sessionId );
            if ( remove != null ) {
                return new SessionResponse<>( "Session removed for id : " + sessionId, SessionCode.SUCCESS, true );
            }
        } catch ( Exception e ) {
            LOGGER.log( Level.SEVERE, e, ( ) -> "Error while removing th session from local container" );
        } finally {
            writeLock.unlock( );
        }
        return new SessionResponse<>( "Session failed to removed for id : " + sessionId, SessionCode.ERROR, false );
    }

    private Session removeSessionFromLocalContainer( String sessionId ) {
        Session removedSession = sessionContainer.remove( sessionId );
        if ( removedSession != null ) {
            LOGGER.log( Level.FINE, "Session removed from local container. SessionID : ", sessionId );
            sessionQueue.remove( removedSession.getSessionId( ) );
        }
        return removedSession;
    }

    /**
     * This is an infinitely running thread which checks the session container for timed-out sessions.
     * If this finds any timed out sessions, then they will be removed from the memory
     */
    private void runSessionRemoverThread( ) {
        Thread sessionRemover = new Thread( ( ) -> {
            while ( true ) {
                int sessionsCount = sessionQueue.size( );
                for ( int i = 0; i < sessionsCount; i++ ) {
                    String polledSession = sessionQueue.poll( );
                    if ( polledSession != null ) {
                        Session session = sessionContainer.get( polledSession );
                        if ( session != null ) {
                            long sessionTimeout = session.getTimeoutInSeconds( );
                            long sessionCreatedTimestamp = session.getLastLoadedTimestamp( );

                            if ( ( System.currentTimeMillis( ) - sessionCreatedTimestamp ) > sessionTimeout * 1000 ) {
                                removeSessionFromLocalContainer( session.getSessionId( ) );
                                System.out.println( "Session expired for session : " + session.getSessionId( ) );
                            }
                        }
                    }
                }
                synchronized ( this ) {
                    try {
                        wait( SESSION_REMOVER_WAIT_TIME_MILLIS );
                    } catch ( InterruptedException e ) {
                        LOGGER.log( Level.SEVERE, e.getMessage( ), e );
                    }
                }
            }
        } );
        sessionRemover.setDaemon( true );
        sessionRemover.setName( "session-remover-thread" );
        sessionRemover.setUncaughtExceptionHandler( ( t, e ) -> LOGGER.log( Level.SEVERE, e, ( ) -> "Error occurred within the session remover thread" ) );
        sessionRemover.start( );
    }

    private static class ResourceHolder {
        private static final InternalSessionContainer LOCAL_SESSION_CONTAINER = new InternalSessionContainer( );
    }
}
