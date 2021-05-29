package com.kusalk.projects.session.handler.demo;

import com.kusalk.projects.session.handler.SessionServices;
import com.kusalk.projects.session.handler.containers.ExternalSessionContainer;
import com.kusalk.projects.session.handler.external.sources.SessionFileSource;
import com.kusalk.projects.session.handler.session.Session;
import com.kusalk.projects.session.handler.util.SessionResponse;

/**
 * This class contains some demo code for the project.
 * <p>
 * Created By : Kusal Kankanamge
 * Created On : 5/29/2021
 */
public class Demo {

    public static void main( String[] args ) {
        writeToExternalSource( );
        readSession( );
    }

    public static void writeToExternalSource( ) {
        SessionServices sessionServices = new SessionServices( new ExternalSessionContainer( new SessionFileSource( ) ) );
        SessionResponse<String> session = sessionServices.createSession( "com.kusalk.projects.session.handler.demo.PersonSession", 1500 );
        if ( session.isSuccess( ) ) {
            SessionResponse<Session> sessionSessionResponse = sessionServices.loadSession( session.getData( ) );
            if ( sessionSessionResponse.isSuccess( ) ) {
                PersonSession personSessionData = null;
                try {
                    Session sessionData = sessionSessionResponse.getData( );
                    personSessionData = ( PersonSession ) sessionData;

                    personSessionData.startReadingSession( );
                    Person person = new Person( "Kusal", 27, 123456789, "Matara" );
                    personSessionData.setPerson( person );

                } finally {
                    if ( personSessionData != null ) {
                        personSessionData.endReadingSession( );
                        sessionServices.saveSessionToExternalSource( personSessionData );
                    }

                }
            }
        }
    }

    public static void readSession( ) {
        SessionServices sessionServices = new SessionServices( new ExternalSessionContainer( new SessionFileSource( ) ) );
        SessionResponse<Session> sessionSessionResponse = sessionServices.loadSession( "d1e0b149-057b-4bae-84d8-73a0ded227d4" );
        if ( sessionSessionResponse.isSuccess( ) ) {
            Session sessionData = sessionSessionResponse.getData( );
            PersonSession personSessionData = null;
            try {
                personSessionData = ( PersonSession ) sessionData;

                personSessionData.startReadingSession( );
                System.out.println( personSessionData );

            } finally {
                if ( personSessionData != null ) {
                    personSessionData.endReadingSession( );
                }
            }
        }

    }
}
