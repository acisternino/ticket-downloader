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
package tido;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import tido.config.ConfigManager;
import tido.model.boundary.TeamForgeFacade;
import tido.viewmodel.TicketDownloaderViewModel;

/**
 * Main TiDoFx application class.
 *
 * @author Andrea Cisternino
 */
public class App extends Application {

    public static final String FULL_NAME = "Ticket Downloader";
    public static final String SHORT_NAME = "TiDoFx";

    private static final Logger log = Logger.getLogger( App.class.getName() );

    private static final String FXML_FILE = "TiDoFx.fxml";

    private static final double STAGE_MIN_WIDTH = 700d;
    private static final double STAGE_MIN_HEIGHT = 500d;

    // enable using system proxy if set
    static { System.setProperty( "java.net.useSystemProxies", "false" ); }

    //---- Main objects ------------------------------------------------------------

    /** The application configuration. */
    private ConfigManager config;

    /** The only ModelView of the application. */
    private TicketDownloaderViewModel ticketModelView;

    /** Class mediating all TeamForge interaction. */
    private TeamForgeFacade tforge;

    //---- Application -------------------------------------------------------------

    /*
     * @see Application.init()
     */
    @Override
    public void init() throws Exception {
        // disable SSL certificates check
        setTrustAllCerts();
    }

    /*
     * @see Application.start(Stage stage)
     */
    @Override
    public void start(Stage stage) throws Exception {

        // main application objects
        config = new ConfigManager( stage ).init();
        tforge = new TeamForgeFacade( config );
        ticketModelView = new TicketDownloaderViewModel( config, tforge );

        // user interface
        Parent page = loadGui();

        Scene scene = new Scene( page, Color.WHITESMOKE );

        stage.setTitle( FULL_NAME );
        stage.setMinHeight( STAGE_MIN_HEIGHT );
        stage.setMinWidth( STAGE_MIN_WIDTH );
        stage.setScene( scene );
        stage.sizeToScene();

        stage.getIcons().add( new Image( "/img/icon-16.png" ) );
        stage.getIcons().add( new Image( "/img/icon-32.png" ) );

        String uri = getClass().getResource( "TiDoFx.css" ).toExternalForm();
        scene.getStylesheets().add( uri );

        stage.show();
    }

    /*
     * @see Application.stop()
     */
    @Override
    public void stop() throws Exception {
        log.info( "quitting application");

        config.saveConfig();
    }

    //---- Support methods ---------------------------------------------------------

    private Parent loadGui() throws IOException {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation( getClass().getResource( FXML_FILE ) );
        loader.setControllerFactory( new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> p) {
                return ticketModelView;
            }
        });

        log.log( Level.FINE, "loading from {0}", loader.getLocation());

        Parent page;
        try ( InputStream is = getClass().getResourceAsStream( FXML_FILE ) )
        {
            page = (Parent) loader.load( is );
        }

        return page;
    }

    private void setTrustAllCerts() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        // install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance( "SSL" );
            sc.init( null, trustAllCerts, new SecureRandom() );
            HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory() );
            HttpsURLConnection.setDefaultHostnameVerifier( new HostnameVerifier() {
                @Override
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            } );
        } catch ( NoSuchAlgorithmException | KeyManagementException ex ) {
            // we can not recover from this exception
            log.log( Level.SEVERE, null, ex);
        }
    }

    //---- main() ------------------------------------------------------------------

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch( args );
    }

}
