package com.kusalk.projects.session.handler;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class Session {

    private final ReentrantReadWriteLock sessionLock = new ReentrantReadWriteLock( );
    private final Lock writeLock = sessionLock.writeLock( );
    private final Lock readLock = sessionLock.readLock( );
    private String sessionId;
    protected long timeoutInSeconds;
    protected long lastLoadedTimestamp;

    public abstract void restore( SessionMemento memento );

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

    public void startWritingSession( ) {
        writeLock.lock( );
    }

    public void endWritingSession( ) {
        writeLock.unlock( );
    }

    public void startReadingSession( ) {
        readLock.lock( );
    }

    public void endReadingSession( ) {
        readLock.unlock( );
    }

    public long getLastLoadedTimestamp( ) {
        return lastLoadedTimestamp;
    }

    public void setLastLoadedTimestamp( long lastLoadedTimestamp ) {
        this.lastLoadedTimestamp = lastLoadedTimestamp;
    }
}
