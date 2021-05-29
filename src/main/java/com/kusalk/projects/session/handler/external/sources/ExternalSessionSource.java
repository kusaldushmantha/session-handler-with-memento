package com.kusalk.projects.session.handler.external.sources;

import com.kusalk.projects.session.handler.SessionMemento;
import com.kusalk.projects.session.handler.util.SessionResponse;

public interface ExternalSessionSource {

    SessionResponse<SessionMemento> readSessionMemento( String sessionId );

    SessionResponse<Boolean> writeSessionMemento( String sessionId, SessionMemento memento );
}
