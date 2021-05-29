package com.kusalk.projects.session.handler;

import com.kusalk.projects.session.handler.util.SessionCode;
import com.kusalk.projects.session.handler.util.SessionResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InternalSessionContainer {

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

    public static InternalSessionContainer getInstance( ) {
        return ResourceHolder.LOCAL_SESSION_CONTAINER;
    }

    public Session loadSessionFromContainer( String sessionId ) {
        try {
            readLock.lock( );
            if ( sessionContainer.containsKey( sessionId ) ) {
                Session session = sessionContainer.get( sessionId );
                session.setLastLoadedTimestamp( System.currentTimeMillis( ) );
                return sessionContainer.get( sessionId );
            }
        } catch ( Exception e ) {
            e.printStackTrace( );
        } finally {
            readLock.unlock( );
        }
        return null;
    }

    public SessionResponse<String> createSession( String sessionClass, long timeoutInSeconds ) {
        try {
            writeLock.lock( );

            String sessionId;
            int retryCounter = 0;
            do {
                sessionId = UUID.randomUUID( ).toString( );
                retryCounter++;
            } while ( sessionContainer.containsKey( sessionId ) && retryCounter <= SESSION_GENERATE_LIMIT );

            Session sessionObject = ( Session ) Class.forName( sessionClass ).getDeclaredConstructor( ).newInstance( );
            sessionObject.setSessionId( sessionId );
            sessionObject.setTimeoutInSeconds( timeoutInSeconds );
            sessionObject.setLastLoadedTimestamp( System.currentTimeMillis( ) );

            sessionContainer.put( sessionId, sessionObject );
            boolean addToQueue = sessionQueue.add( sessionId );
            if ( addToQueue ) {
                return new SessionResponse<>( "Session created successfully and added to local container", SessionCode.SUCCESS, sessionId );
            } else {
                removeSessionFromLocalContainer( sessionId );
                return new SessionResponse<>( "Session could not add to local session queue", SessionCode.ERROR, sessionId );
            }

        } catch ( InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e ) {
            return new SessionResponse<>( "Error creating session : " + e.getCause( ).getMessage( ), SessionCode.ERROR, null );
        } finally {
            writeLock.unlock( );
        }
    }

    public SessionResponse<Boolean> removeSession( String sessionId ) {
        try {
            writeLock.lock( );
            Session remove = removeSessionFromLocalContainer( sessionId );
            if ( remove != null ) {
                return new SessionResponse<>( "Session removed for id : " + sessionId, SessionCode.SUCCESS, true );
            }
        } catch ( Exception e ) {
            e.printStackTrace( );
        } finally {
            writeLock.unlock( );
        }
        return new SessionResponse<>( "Session failed to removed for id : " + sessionId, SessionCode.ERROR, false );
    }

    private Session removeSessionFromLocalContainer( String sessionId ) {
        Session removedSession = sessionContainer.remove( sessionId );
        if ( removedSession != null ) {
            sessionQueue.remove( removedSession.getSessionId( ) );
        }
        return removedSession;
    }

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
                        e.printStackTrace( );
                    }
                }
            }
        } );
        sessionRemover.setDaemon( true );
        sessionRemover.setName( "session-remover-thread" );
        sessionRemover.setUncaughtExceptionHandler( ( t, e ) -> e.printStackTrace( ) );
        sessionRemover.start( );
    }

    private static class ResourceHolder {
        private static final InternalSessionContainer LOCAL_SESSION_CONTAINER = new InternalSessionContainer( );
    }
}
