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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

import javafx.application.Platform;
import javafx.stage.Stage;

import tido.App;
import tido.Dialogs;
import tido.Dialogs.Wait;
import tido.Utils;

/**
 * Manages the configuration of the application.
 *
 * @author Andrea Cisternino
 */
public class ConfigManager
{
    private static final Logger log = Logger.getLogger( ConfigManager.class.getName() );

    private static final String CONFIG_DIR_WIN  = "TiDoFx";
    private static final String CONFIG_DIR_UNIX = "." + CONFIG_DIR_WIN.toLowerCase();

    private static final String CONFIG_FILE   = "config.xml";
    private static final String SERVERS_FILE  = "servers.xml";
    private static final String JS_NAMER_FILE = "dir-namer.js";

    /** The main Stage of the Application. Used to display error dialogs. */
    private final Stage stage;

    /** The directory that contains all configuration files. */
    private final Path configDir;

    /** The initial directory where tickets will be stored. */
    private final Path ticketsBaseDir;

    /** Utility class to display dialogs over the main GUI. */
    private final Dialogs dialogs;

    //---- Configuration objects ---------------------------------------------------

    /** The list of servers we can download tickets from. */
    private ServerList servers;

    /** General application configuration. */
    private ConfigData config;

    /** The JavaScript naming script as a String. */
    private String jsNamingScript;

    //---- Lifecycle ---------------------------------------------------------------

    public ConfigManager(Stage stage, Dialogs dialogs) {

        this.stage = stage;
        this.dialogs = dialogs;

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
     * Initializes the instance. Must be run from the JavaFX GUI thread.
     *
     * @return the object being configured.
     */
    public ConfigManager postConstruct() {

        // configuration directory
        try {

            if ( Files.notExists( configDir ) ) {
                Files.createDirectory( configDir );
                log.log( Level.INFO, "config dir created: {0}", configDir.toString() );
            }

        } catch ( IOException ex ) {
            log.log( Level.SEVERE, "creating config dir:", ex );
            dialogs.configDirError( configDir.toString(), ex, Wait.YES );
            System.exit( 1 );
        }

        // configuration data
        loadConfigData();

        // server list
        loadServers( stage );

        return this;
    }

    //---- API ---------------------------------------------------------------------

    /**
     * @return the list of configured TeamForge servers.
     */
    public ServerList servers() {
        return servers;
    }

    /**
     * @return the general application configuration data.
     */
    public ConfigData config() {
        return config;
    }

    /**
     * @return the namer script as String.
     */
    public String namingScript() {
        if ( Utils.isBlank( jsNamingScript ) ) {
            loadNamingScript();
        }
        return jsNamingScript;
    }

    /**
     * Saves the application configuration.
     */
    public void saveConfig() {
        log.log( Level.INFO, "path: {0}", config.getBaseDirectory() );

        try {

            JAXB.marshal( config, configDir.resolve( CONFIG_FILE ).toFile() );

        } catch ( DataBindingException | InvalidPathException ex ) {
            log.log( Level.WARNING, "saving configuration:", ex );
        }
    }

    /**
     * @return the {@link Stage} of the application.
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * @return the {@link Dialogs} object used to display dialogs.
     */
    public Dialogs getDialogs() {
        return dialogs;
    }

    //---- Support methods ---------------------------------------------------------

    /**
     * Loads the general application configuration. This is not done lazily because
     * some values are needed to configure the GUI.
     */
    private void loadConfigData() {

        Path configPath = configDir.resolve( CONFIG_FILE );

        log.log( Level.INFO, "from {0}", configPath );

        try {

            config = JAXB.unmarshal( configPath.toUri(), ConfigData.class );

        } catch ( DataBindingException ex ) {

            JAXBException rootEx = (JAXBException) ex.getCause();

            if ( rootEx.getCause() instanceof FileNotFoundException ) {
                // this is OK, use default values
                log.warning( "config file not found, using defaults" );

                config = new ConfigData();
                config.setBaseDirectory( ticketsBaseDir.toString() );
                // TODO maybe better like this: config = new ConfigData().setDefaults() ?
            } else {
                // this is not OK
                log.log( Level.SEVERE, "loading configuration file:", rootEx );
                dialogs.configFileError( ex, Wait.NO );
            }
        }
    }

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

            final JAXBException rootEx = (JAXBException) ex.getCause();

            if ( rootEx.getCause() instanceof FileNotFoundException ) {
                log.warning( "servers file not found" );
                dialogs.serversFileMissing( configDir.toString(), Wait.NO );
            }
            else {
                log.log( Level.SEVERE, "loading servers configuration file:", rootEx );
                dialogs.serversFileError( ex, Wait.NO );
            }
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

            // FIXME handle error better
        }

        jsNamingScript = content;
    }
}
