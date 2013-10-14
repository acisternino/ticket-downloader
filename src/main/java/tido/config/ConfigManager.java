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
package tido.config;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

import javafx.scene.control.Dialogs;
import javafx.stage.Stage;

import tido.Utils;

/**
 * Manages the configuration of the application.
 *
 * @author Andrea Cisternino
 */
public class ConfigManager
{
    private static final Logger log = Logger.getLogger( ConfigManager.class.getName() );

    private static final String CONFIG_DIR_WIN = "TiDoFx";
    private static final String CONFIG_DIR_UNIX = "." + CONFIG_DIR_WIN.toLowerCase();

    private static final String CONFIG_FILE = "config.xml";
    private static final String SERVERS_FILE = "servers.xml";
    private static final String JS_NAMER_FILE = "dir-namer.js";

    /** The directory that contains all configuration files. */
    private final Path configDir;

    /** The initial directory where tickets will be stored. */
    private final Path ticketsBaseDir;

    /** The list of servers we can download tickets from. */
    private ServerList servers;

    /** General application configuration. */
    private ConfigData config;

    /** The JavaScript naming script as a String. */
    private String jsNamingScript;

    /** The main Stage of the Application. Used to display error dialogs. */
    private final Stage stage;

    //---- Lifecycle ---------------------------------------------------------------

    public ConfigManager(Stage stage) {

        this.stage = stage;

        final FileSystem dfs = FileSystems.getDefault();

        if ( System.getProperty( "os.name" ).startsWith( "Windows" ) ) {
            // Windows
            configDir = dfs.getPath( System.getenv( "APPDATA" ), CONFIG_DIR_WIN);
            ticketsBaseDir = dfs.getPath( System.getenv( "USERPROFILE" ), "Documents", "tickets" );
        } else {
            // Linux (and... ?)
            configDir = dfs.getPath( System.getProperty( "user.home" ), CONFIG_DIR_UNIX );
            ticketsBaseDir = dfs.getPath( System.getProperty( "user.home" ), "tickets" );
        }
    }

    /**
     * Completely initializes the instance.
     *
     * @return this to allow a fluent interface.
     */
    public ConfigManager init() {

        // config directory
        try {
            // eventually create the configuration directory
            if ( Files.notExists( configDir ) ) {
                Files.createDirectory( configDir );
                log.log( Level.INFO, "config dir created: {0}", configDir.toString() );
            }
        } catch ( IOException ex ) {
            log.log( Level.SEVERE, "creating config dir:", ex );
        }

        // create and/or load JavaScript naming file
        loadNamingScript();

        loadServers( stage );
        loadConfigData();

        return this;
    }

    //---- API ---------------------------------------------------------------------

    public ServerList servers() {
        return servers;
    }

    public ConfigData config() {
        return config;
    }

    public String namingScript() {
        return jsNamingScript;
    }

    /**
     * Saves the configuration when quitting the application.
     */
    public void saveConfig() {
        log.log( Level.INFO, "path: {0}", config.getBaseDirectory() );

        JAXB.marshal( config, configDir.resolve( CONFIG_FILE ).toFile() );
    }

    //---- Support methods ---------------------------------------------------------

    /**
     * Load the file with the TF servers info. Display a dialog in case of missing file.
     *
     * @param stage the {@link Stage} used to display dialogs.
     */
    private void loadServers(Stage stage) {

        Path serversPath = configDir.resolve( SERVERS_FILE );

        log.log( Level.INFO, "from {0}", serversPath );

        try {
            servers = JAXB.unmarshal( serversPath.toUri(), ServerList.class );
        } catch ( DataBindingException ex ) {

            JAXBException rootEx = (JAXBException) ex.getCause();

            if ( rootEx.getCause() instanceof FileNotFoundException ) {
                log.warning( "servers file not found" );
                Dialogs.showErrorDialog( stage,
                        "Servers configuration file not found!\n"
                        + "Please create the file manually in\n"
                        + configDir.toString() + "\n"
                        + "and restart the aplication.",
                        "Application configuration error", "Ticket Downloader" );
                System.exit( 1 );
            } else {
                // this is not OK
                log.log( Level.SEVERE, "loading servers configuration file:", rootEx );
                // TODO handle exception
            }
        }
    }

    private void loadConfigData() {

        Path configPath = configDir.resolve( CONFIG_FILE );

        log.log( Level.INFO, "from {0}", configPath );

        try {
            config = JAXB.unmarshal( configPath.toUri(), ConfigData.class );
        } catch ( DataBindingException ex ) {
            // this is OK, use default values
            log.warning( "config file not found, using defaults" );

            config = new ConfigData();
            config.setBaseDirectory( ticketsBaseDir.toString() );
        }
    }

    private void loadNamingScript() {

        Path scriptPath = configDir.resolve( JS_NAMER_FILE );

        String content = null;

        try {
            if ( Files.notExists( scriptPath ) ) {

                log.log( Level.INFO, "not found: {0}", scriptPath.toString() );

                // load default one from classpath and copy in config dir
                InputStream in = this.getClass().getResourceAsStream( "/js/" + JS_NAMER_FILE );
                ByteArrayOutputStream out = new ByteArrayOutputStream( 2048 );

                Utils.copyStream( in, out );
                Files.write( scriptPath, out.toByteArray() );

                content = new String( out.toByteArray(), StandardCharsets.UTF_8 );

                log.info( "file created" );

            } else {
                log.info( scriptPath.toString() );

                content = new String( Files.readAllBytes( scriptPath ), StandardCharsets.UTF_8 );
            }

        } catch ( IOException ex ) {
            log.log( Level.SEVERE, "loading naming script:", ex );
        }

        jsNamingScript = content;
    }

}
