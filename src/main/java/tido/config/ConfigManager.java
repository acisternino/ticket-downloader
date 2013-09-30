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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

/**
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
    private Path baseDir;

    /** The list of servers we can download tickets from. */
    private ServerList servers;

    /** General application configuration. */
    private ConfigData config;

    /** The JavaScript naming script as string. */
    private String jsNamingScript;

    /** The singleton instance of the application configuration. */
    private static ConfigManager instance;

    //---- Lifecycle ---------------------------------------------------------------

    protected ConfigManager() {

        final FileSystem dfs = FileSystems.getDefault();

        if ( System.getProperty( "os.name" ).startsWith( "Windows" ) ) {
            // Windows
            configDir = dfs.getPath( System.getenv( "APPDATA" ), CONFIG_DIR_WIN);
            baseDir = dfs.getPath( System.getenv( "USERPROFILE" ), "Documents", "tickets" );
        } else {
            // Linux (and... ?)
            configDir = dfs.getPath( System.getProperty( "user.home" ), CONFIG_DIR_UNIX );
            baseDir = dfs.getPath( System.getProperty( "user.home" ), "tickets" );
        }

        loadServers();
        loadConfigData();
        loadNamingScript();
    }

    //---- API ---------------------------------------------------------------------

    /**
     * @return the singleton instance of the application configuration.
     */
    public static ConfigManager get() {
        if ( instance == null ) {
            instance = new ConfigManager();
        }
        return instance;
    }

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

    private void loadServers() {

        Path serversPath = configDir.resolve( SERVERS_FILE);

        log.log( Level.INFO, "from {0}", serversPath );

        try {
            servers = JAXB.unmarshal( serversPath.toUri(), ServerList.class );
        } catch ( DataBindingException ex ) {
            // this is not OK
            log.warning( "servers file not found" );
            // TODO handle exception
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
            config.setBaseDirectory( baseDir.toString() );
        }
    }

    private void loadNamingScript() {

        Path namerPath = configDir.resolve( JS_NAMER_FILE );

        log.info( namerPath.toString() );

        String content;
        try {
            content = new String( Files.readAllBytes( namerPath ), StandardCharsets.UTF_8 );
        } catch ( NoSuchFileException ex ) {
            log.info( "js script not found, using default" );
            return;
        } catch ( IOException ex ) {
            log.log( Level.SEVERE, null, ex );
            return;
        }

        jsNamingScript = content;
    }

}
