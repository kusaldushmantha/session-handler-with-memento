package com.kusalk.projects.session.handler.external.sources;

import com.kusalk.projects.session.handler.session.SessionMemento;
import com.kusalk.projects.session.handler.util.SessionCode;
import com.kusalk.projects.session.handler.util.SessionResponse;

import java.io.*;

/**
 * This is a simple implementation of an {@link ExternalSessionSource} where session memento objects
 * are serialized to a file.
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
public class SessionFileSource implements ExternalSessionSource {

    @Override
    public SessionResponse<SessionMemento> readSessionMemento( String sessionId ) {
        try ( BufferedInputStream bufferedInputStream = new BufferedInputStream( new FileInputStream( sessionId + ".ser" ) );
              ObjectInputStream objectInputStream = new ObjectInputStream( bufferedInputStream ) ) {

            SessionMemento memento = ( SessionMemento ) objectInputStream.readObject( );

            return new SessionResponse<>( "Successful reading session from file for session : " + sessionId, SessionCode.SUCCESS, memento );

        } catch ( Exception e ) {
            e.printStackTrace( );
        }
        return new SessionResponse<>( "Error reading session from file for session : " + sessionId, SessionCode.ERROR, null );
    }

    @Override
    public SessionResponse<Boolean> writeSessionMemento( String sessionId, SessionMemento memento ) {
        try ( BufferedOutputStream bufferedOutputStream = new BufferedOutputStream( new FileOutputStream( sessionId + ".ser" ) );
              ObjectOutputStream objectOutputStream = new ObjectOutputStream( bufferedOutputStream ) ) {
            objectOutputStream.writeObject( memento );
            objectOutputStream.flush( );

            return new SessionResponse<>( "Session successfully written to external source", SessionCode.SUCCESS, true );

        } catch ( IOException e ) {
            e.printStackTrace( );
        }

        return new SessionResponse<>( "Session writing to external source failed", SessionCode.ERROR, false );
    }
}
