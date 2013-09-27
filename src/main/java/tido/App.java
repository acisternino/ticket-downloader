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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import tido.config.ConfigManager;
import tido.viewmodel.TicketDownloaderViewModel;

/**
 * Main TiDoFx application class.
 *
 * @author Andrea Cisternino
 */
public class App extends Application {

    private static final Logger log = Logger.getLogger( App.class.getName() );

    private static final String FXML_FILE = "TiDoFx.fxml";

    // enable using system proxy if set
    static { System.setProperty( "java.net.useSystemProxies", "true" ); }

    /** The only ModelView of the application. */
    private TicketDownloaderViewModel ticketModelView;

    //---- Application -------------------------------------------------------------

    /*
     * @see Application.init()
     */
    @Override
    public void init() throws Exception {

        // disable SSL certificates check
        setTrustAllCerts();

        // load configuration
        ConfigManager.get();
    }

    /*
     * @see Application.start(Stage stage)
     */
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader();

        loader.setLocation( getClass().getResource( FXML_FILE ) );
        log.log( Level.INFO, "loading from {0}", loader.getLocation());

        Parent page;
        try ( InputStream is = App.class.getResourceAsStream( FXML_FILE) )
        {
            page = (Parent) loader.load( is );
        }

        setupModelView( loader );

        Scene scene = new Scene( page, Color.WHITESMOKE );

        stage.setTitle( "TiDoFx - Lite" );
        stage.setScene( scene );
        stage.sizeToScene();

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

        ConfigManager.get().saveConfig();
    }

    //---- Support methods ---------------------------------------------------------

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

    private void setupModelView(FXMLLoader loader) {

        ticketModelView = loader.<TicketDownloaderViewModel>getController();

        // inject dependencies
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
