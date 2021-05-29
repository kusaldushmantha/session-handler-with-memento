package com.kusalk.projects.session.handler.external.sources;

import com.kusalk.projects.session.handler.session.SessionMemento;
import com.kusalk.projects.session.handler.util.SessionResponse;

/**
 * Session data can be stored to an external source so that they can be retrieved later and restored.
 * This interface contains the functionality any such external source should implement.
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
public interface ExternalSessionSource {

    /**
     * This method reads the session from the external source.
     *
     * @param sessionId session-id of the session to be read
     * @return {@link SessionResponse<SessionMemento>} memento object
     */
    SessionResponse<SessionMemento> readSessionMemento( String sessionId );

    /**
     * This method writes the session memento object to the external source along with the relevant
     * session id
     *
     * @param sessionId session id of the memento
     * @param memento   memento object
     * @return {@link SessionResponse<Boolean>} response with data set to {@code true} if success
     */
    SessionResponse<Boolean> writeSessionMemento( String sessionId, SessionMemento memento );
}
