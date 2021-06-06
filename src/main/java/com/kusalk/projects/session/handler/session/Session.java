package com.kusalk.projects.session.handler.session;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the parent class of all sessions within the application.
 * <p>
 * Created By : Kusal Kanakanamge
 * Created On : 5/29/2021
 */
public abstract class Session {

    private static final Logger LOGGER = Logger.getLogger( Session.class.getName( ) );

    private final ReentrantReadWriteLock sessionLock = new ReentrantReadWriteLock( );
    private final Lock writeLock = sessionLock.writeLock( );
    private final Lock readLock = sessionLock.readLock( );
    protected long timeoutInSeconds;
    protected long lastLoadedTimestamp;
    private String sessionId;

    /**
     * This is used to restore the session state from the memento object
     *
     * @param memento {@link SessionMemento} memento
     */
    public abstract void restore( SessionMemento memento );

    /**
     * This creates an returns a memento object from the data held in the session
     *
     * @return {@link SessionMemento} memento object
     */
    public abstract SessionMemento createMemento( );

    public String getSessionId( ) {
        return sessionId;
    }

    public void setSessionId( String sessionId ) {
        if ( this.sessionId == null || this.sessionId.isBlank( ) ) {
            this.sessionId = sessionId;
        }
    }

    public long getTimeoutInSeconds( ) {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds( long timeoutInSeconds ) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    /**
     * This method should be called before writing the session memento object to the external source
     */
    public void startWritingSession( ) {
        LOGGER.log( Level.FINE, "Started writing session {0} to external source", new Object[]{ sessionId } );
        writeLock.lock( );
    }

    /**
     * This method should be called after writing the session memento object to the external source
     */
    public void endWritingSession( ) {
        LOGGER.log( Level.FINE, "Finished writing session {0} to external source", new Object[]{ sessionId } );
        writeLock.unlock( );
    }

    /**
     * This method should be called immediately after loading the session to the session container before working with the session
     */
    public void startReadingSession( ) {
        LOGGER.log( Level.FINE, "Started reading session {0} to external source", new Object[]{ sessionId } );
        readLock.lock( );
    }

    /**
     * This method should be called after working with the session
     */
    public void endReadingSession( ) {
        LOGGER.log( Level.FINE, "Finished reading session {0} to external source", new Object[]{ sessionId } );
        readLock.unlock( );
    }

    public long getLastLoadedTimestamp( ) {
        return lastLoadedTimestamp;
    }

    public void setLastLoadedTimestamp( long lastLoadedTimestamp ) {
        this.lastLoadedTimestamp = lastLoadedTimestamp;
    }
}
