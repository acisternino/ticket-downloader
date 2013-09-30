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

import tido.config.ConfigManager;
import tido.model.Ticket;

/**
 *
 * @author Andrea Cisternino
 */
public class JsTicketDirectoryNamer implements TicketDirectoryNamer
{
    private static final Logger log = Logger.getLogger( JsTicketDirectoryNamer.class.getName() );

    /** Name of the default JS naming script. Loaded from classpath. */
    private static final String JS_NAMER_FILE_DEFAULT = "dir-namer-default.js";

    private String baseDir = ".";

    /**
     * A simple cache for the generated directory names.
     */
    private final WeakHashMap<Ticket, Path> nameCache = new WeakHashMap<>();

    /**
     * The JavaScript engine used to run the name generator function.
     */
    private final ScriptEngine engine;

    //---- Lifecycle ---------------------------------------------------------------

    public JsTicketDirectoryNamer() {

        // JS engine
        engine = new ScriptEngineManager().getEngineByName( "JavaScript" );
        try {
            // inject some global variables
            engine.put( "log", Logger.getLogger( "tido.javascript" ) );
            engine.put( "separator", File.separator );

            // this is needed to support string.js
            engine.eval( "var window = this;");

            // string.js library from http://stringjs.com
            evalFromClasspath( "/js/string.min.js" );

            // load directory naming script
            String namingScript = ConfigManager.get().namingScript();

            if ( namingScript == null || namingScript.length() == 0 ) {
                // load default
                evalFromClasspath( "/js/" + JS_NAMER_FILE_DEFAULT );
            } else {
                // load custom version in config directory
                engine.eval( namingScript );
            }

        } catch ( ScriptException ex ) {
            log.log( Level.SEVERE, null, ex );
        }
    }

    //---- API ---------------------------------------------------------------------

    @Override
    public Path getTicketPath(Ticket ticket) {

        // test cache first
        Path tp;
        if ( nameCache.containsKey( ticket ) ) {
            tp = nameCache.get( ticket );
            if ( tp != null ) {
                log.log( Level.FINE, "path found in cache: {0}", tp.toString() );
                return tp;
            }
        }

        // use global JS generateName() function to generate the name
        String ticketDir = null;
        try {
            Invocable eng = (Invocable) engine;
            ticketDir = (String) eng.invokeFunction( "generateName", baseDir, ticket );
        } catch ( ScriptException | NoSuchMethodException ex ) {
            log.log( Level.SEVERE, null, ex );
        }

        if ( ticketDir == null ) {
            tp = backupName( ticket );
            log.log( Level.FINE, "js problems; backup name: {0}", tp.toString());
        }
        else {
            tp = Paths.get( ticketDir );
            log.log( Level.FINE, "generated: {0}", ticketDir);
        }

        nameCache.put( ticket, tp );

        return tp;
    }

    @Override
    public void setBaseDir(String dirName) {

        log.fine( dirName );

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
        InputStream tunerScript = this.getClass().getResourceAsStream( scriptName );
        return engine.eval( new InputStreamReader( tunerScript ) );
    }

    /**
     * Returns a backup name for the ticket directory in case something goes
     * wrong with the JavaScript side.
     *
     * @param ticket
     * @return a default Path for the Ticket directory.
     */
    private Path backupName(Ticket ticket) {
        return Paths.get( baseDir, ticket.getId() );
    }

}
