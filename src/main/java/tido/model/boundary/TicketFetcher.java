/*
 * Copyright 2013 Andrea Cisternino <a.cisternino@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tido.model.boundary;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.FailedLoginException;

import javafx.application.Platform;
import javafx.scene.control.Dialogs;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import tido.App;
import tido.Utils;
import tido.config.ConfigManager;
import tido.config.ServerInfo;
import tido.model.Ticket;
import tido.scraping.BasePageParser;

/**
 * This class fetches a Ticket page from TeamForge and parses the content.
 *
 * @author Andrea Cisternino
 */
public class TicketFetcher {

    private static final Logger log = Logger.getLogger( TicketFetcher.class.getName() );

    private static final String LOGIN_PATH = "/sf/sfmain/do/login";
    private static final int MAX_BODY_SIZE = 8 * 1024 * 1024;

    /** The application configuration. */
    private final ConfigManager config;

    //---- Lifecycle ---------------------------------------------------------------

    public TicketFetcher(ConfigManager config) {
        this.config = config;
    }

    //---- API ---------------------------------------------------------------------

    /**
     * Fetches a {@link Ticket} from TeamForge and parses the page.
     *
     * This class uses <a href="http://jsoup.org/">Jsoup</a> to parse the received
     * HTML page creating a DOM-like object tree that is later used to extract the
     * attributes of the Ticket.
     *
     * @param ticketUrl the URL of the Ticket.
     * @return the Ticket object.
     * @throws IOException if an I/O error occurs while connecting to the TeamForge server.
     * @throws FailedLoginException if the server refuses the authentication attempt.
     */
    public Ticket fetch(String ticketUrl) throws IOException, FailedLoginException {

        log.log( Level.INFO, "fetching {0}", ticketUrl );

        ServerInfo server = findServer( ticketUrl );
        if ( server == null ) {
            log.warning( "unknown TeamForge server" );
            throw new IllegalArgumentException( ticketUrl );
        }

        if ( ! server.isAuthenticated() ) {
            login( server );
        }

        Document ticketPage;
        try {
            ticketPage = Jsoup.connect( ticketUrl )
                    .cookies( server.getSession() )
                    .maxBodySize( MAX_BODY_SIZE )
                    .timeout( 4000 )
                    .get();

        } catch ( IOException ex ) {
            log.log( Level.WARNING, "error: {0}", ex.getClass().getName() );
            throw ex;
        }
        log.log( Level.FINE, "ticket page downloaded: \"{0}\"", ticketPage.title() );

        Ticket ticket = parseTicketPage( ticketPage, server );
        ticket.setUrl( ticketUrl );

        return ticket;
    }

    /**
     * Fetches a ticket with the given ID.
     *
     * @param ticketId the ticket ID.
     * @param server
     * @return the ticket if found, null otherwise.
     */
    public Ticket fetch(int ticketId, ServerInfo server) {
        return null;
    }

    //---- Server interaction ------------------------------------------------------

    /**
     * Login to the given TeamForge server.
     *
     * @param server the base URL of the TeamForge server.
     * @throws IOException if an I/O error occurs while connecting to the TeamForge server.
     * @throws FailedLoginException if the server refuses the authentication attempt.
     */
    private void login(ServerInfo server) throws IOException, FailedLoginException {

        log.log( Level.INFO, "logging in to {0}", server.getUrl() );

        Connection connection = Jsoup.connect( server.getUrl() + LOGIN_PATH );
        connection.data( "sfsubmit", "submit" );
        connection.data( "username", server.getUsername() );
        connection.data( "password", server.getPassword() );

        try {
            // errors and timeouts are notified with an exception, see Jsoup javadocs
            connection.post();
        } catch ( IOException ex ) {
            log.log( Level.WARNING, "error: {0}", ex.getClass().getName() );
            throw ex;
        }

        Map<String, String> sessionId = connection.response().cookies();

        if ( sessionId.containsKey( ServerInfo.TF_AUTH_KEY ) ) {
            log.info( "login successful" );
            server.setSession( sessionId );
        } else {
            log.warning( "failed" );
            server.setSession( null );
            // TODO display a dialog in the GUI thread, see below
            throw new FailedLoginException();
        }
    }

    //---- Support methods ---------------------------------------------------------

    /**
     * Parses a page returning a Ticket object.
     *
     * @param doc the downloaded {@link Document}.
     * @return the Ticket object.
    */
    private Ticket parseTicketPage(Document doc, ServerInfo server) {

        Ticket t = null;
        try {
            t = BasePageParser.create( server ).parse( doc );
        } catch ( final IllegalArgumentException ex ) {
            log.warning( ex.getMessage() );
            Platform.runLater( new Runnable() {
                @Override public void run() {
                    Dialogs.showWarningDialog( config.getStage(), Utils.capitalizeFirstLetter( ex.getMessage() )
                            + "\n\nPlease verify the server configuration file and restart the application",
                            "Problem parsing the ticket page.", App.FULL_NAME );
                }
            } );
        }
        return t;
    }

    /**
     * Finds the server of the given URL in the configuration server list.
     *
     * @param ticketUrl the URL of the ticket.
     * @return the {@link ServerInfo} for the ticket if found, null otherwise.
     */
    private ServerInfo findServer(String ticketUrl) {
        for ( ServerInfo server : config.servers().getServers() ) {
            if ( ticketUrl.startsWith( server.getUrl() ) ) {
                log.log( Level.INFO, "server found: {0}", server );
                return server;
            }
        }
        return null;
    }
}
