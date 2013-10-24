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
package tido.naming;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import tido.Dialogs.Wait;
import tido.config.ConfigManager;
import tido.model.Ticket;

/**
 *
 * @author Andrea Cisternino
 */
public class TicketDirectoryNamer
{
    private static final Logger log = Logger.getLogger( TicketDirectoryNamer.class.getName() );

    /** The application configuration. */
    private final ConfigManager config;

    /** Base ticket directory. */
    private Path baseDir;

    /** A simple cache for the generated directory names. */
    private final WeakHashMap<Ticket, Path> nameCache = new WeakHashMap<>();

    /** The JavaScript engine used to run the name generator function. */
    private final ScriptEngine engine;

    //---- Lifecycle ---------------------------------------------------------------

    /**
     * Creates an instance of the TicketDirectorynamer.
     *
     * @param config the configuration manager of the application.
     */
    public TicketDirectoryNamer(final ConfigManager config) {

        this.config = config;

        // set initial value of the base directory from configuration
        baseDir = Paths.get( config.config().getBaseDirectory() );

        // JS engine
        engine = new ScriptEngineManager().getEngineByName( "JavaScript" );

        try {
            // inject some global variables
            engine.put( "log", Logger.getAnonymousLogger() );
            engine.put( "separator", File.separator );

            // this is needed to support string.js
            engine.eval( "var window = this;");

            // load string.js library (see http://stringjs.com)
            evalFromClasspath( "/js/string.min.js" );

            // load directory naming script
            engine.eval( config.namingScript() );

        } catch ( final ScriptException ex ) {
            log.log( Level.SEVERE, "in constructor:", ex );
            config.getDialogs().jsEngineError( ex, Wait.NO );
        }
    }

    //---- API ---------------------------------------------------------------------

    public Path getTicketPath(Ticket ticket) {

        // test cache first
        Path tp;
        if ( nameCache.containsKey( ticket ) ) {
            tp = nameCache.get( ticket );
            if ( tp != null ) {
                log.log( Level.FINE, "found in cache: {0}", tp.toString() );
                return tp;
            }
        }

        // use global JS generateName() function to generate the name
        String ticketDir = null;
        try {
            Invocable eng = (Invocable) engine;
            ticketDir = (String) eng.invokeFunction( "generateName", ticket );
        } catch ( ScriptException | NoSuchMethodException ex ) {
            log.log( Level.SEVERE, null, ex );
        }

        if ( ticketDir == null ) {
            tp = backupName( ticket );
            log.log( Level.FINE, "js problems; backup name: {0}", tp.toString());
        }
        else {
            tp = baseDir.resolve( ticketDir );
            log.log( Level.FINE, "generated: {0}", ticketDir);
        }

        nameCache.put( ticket, tp );

        return tp;
    }

    /**
     * Sets the name of the base directory for the ticket folders.
     *
     * @param dirName the name of the directory.
     */
    public void setBaseDir(Path dirName) {
        log.fine( dirName.toString() );
        baseDir = dirName;
    }

    //---- Support methods ---------------------------------------------------------

    /**
     * Evaluate a script found on the classpath.
     *
     * @param scriptName the path of the script.
     * @return the value returned from the script.
     * @throws ScriptException if an error occurs while evaluating the script.
     */
    private Object evalFromClasspath(String scriptName) throws ScriptException {
        log.fine( scriptName );
        InputStream is = getClass().getResourceAsStream( scriptName );
        return engine.eval( new InputStreamReader( is ) );
    }

    /**
     * Returns a backup name for the ticket directory in case something goes
     * wrong with the JavaScript side.
     *
     * @param ticket the ticket being processed.
     * @return a default Path for the Ticket directory.
     */
    private Path backupName(Ticket ticket) {
        return baseDir.resolve( ticket.getId() );
    }

}
