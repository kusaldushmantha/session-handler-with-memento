package com.kusalk.projects.session.handler.util;

public class SessionResponse<T> {

    private final String message;
    private final SessionCode code;
    private final T data;

    public SessionResponse( String message, SessionCode code, T data ) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public boolean isError( ) {
        return SessionCode.ERROR == getCode( );
    }

    public boolean isSuccess( ) {
        return SessionCode.SUCCESS == getCode( );
    }

    public String getMessage( ) {
        return message;
    }

    public SessionCode getCode( ) {
        return code;
    }

    public T getData( ) {
        return data;
    }
}
