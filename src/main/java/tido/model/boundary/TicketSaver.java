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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import tido.model.Ticket;
import tido.naming.TicketDirectoryNamer;

/**
 *
 * @author Andrea Cisternino
 */
public class TicketSaver
{
    private static final Logger log = Logger.getLogger( TicketSaver.class.getName() );

    private final TicketDirectoryNamer namer;

    //---- Lifecycle ---------------------------------------------------------------

    TicketSaver(TicketDirectoryNamer namer) {
        this.namer = namer;
    }

    //---- API ---------------------------------------------------------------------

    /**
     * Saves the additional ticket fields to text files in the ticket directory.
     *
     * @param ticket the ticket whose fields must be saved.
     */
    public void saveTicketFields(Ticket ticket) {
        try {
            Path ticketDir = namer.getTicketPath( ticket );

            // description
            if ( ticket.getDescription().length() > 0 ) {
                log.fine( "description" );
                Files.copy( new ByteArrayInputStream( ticket.getDescription().getBytes( StandardCharsets.UTF_8 ) ),
                        ticketDir.resolve( "description.txt" ), StandardCopyOption.REPLACE_EXISTING );
            }

            // analysis
            if ( ticket.getAnalysis().length() > 0 ) {
                log.fine( "analysis" );
                Files.copy( new ByteArrayInputStream( ticket.getAnalysis().getBytes( StandardCharsets.UTF_8 ) ),
                        ticketDir.resolve( "analysis.txt" ), StandardCopyOption.REPLACE_EXISTING );
            }
        }
        catch ( IOException ex ) {
            log.log( Level.SEVERE, "saving ticket fields:", ex );
        }
    }
}
