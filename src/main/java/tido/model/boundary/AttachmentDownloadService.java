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
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import tido.config.ConfigManager;
import tido.model.AttachmentLink;
import tido.model.Ticket;
import tido.model.TicketState;
import tido.naming.TicketDirectoryNamer;

/**
 * A JavaFX {@link Service} that downloads all the attachments of a list of Tickets.
 *
 * @author Andrea Cisternino
 */
public class AttachmentDownloadService extends Service<Object>
{
    private static final Logger log = Logger.getLogger( AttachmentDownloadService.class.getName() );

    /** The Namer used to generate the folder name. */
    private final TicketDirectoryNamer namer;

    //---- Lifecycle ---------------------------------------------------------------

    public AttachmentDownloadService(ConfigManager config) {
        namer = new TicketDirectoryNamer( config );
    }

    //---- Properties --------------------------------------------------------------

    /**
     * The list of tickets containing the attachments to download.
     */
    private final ListProperty<Ticket> tickets = new SimpleListProperty<>();
    public ListProperty<Ticket> ticketsProperty() { return tickets; }
    public void setTickets(ObservableList<Ticket> tickets) { this.tickets.set( tickets ); }

    /**
     * Sets the name of the base directory for tickets.
     *
     * @param path the directory name.
     */
    public void setBaseDir(Path path) {
        // just forward to the namer
        namer.setBaseDir( path );
    }

    //---- Task --------------------------------------------------------------------

    @Override
    protected Task<Object> createTask() {
        return new AdsTask( tickets );
    }

    /**
     * The concrete Task implementation.
     */
    private class AdsTask extends Task<Object>
    {
        private final Logger log = Logger.getLogger( AdsTask.class.getName() );

        private final List<Ticket> origList;

        public AdsTask(List<Ticket> tickets) {
            origList = tickets;
        }

        @Override
        protected Object call() throws Exception {

            // extract downloadable tickets
            List<Ticket> tl = filterTickets();

            int attNum = countAttachments( tl );

            log.log( Level.INFO, "downloading {0} attachments", attNum);

            final AttachmentFetcher fetcher = new AttachmentFetcher( namer );

            int an = 1;

            for ( final Ticket ticket : tl ) {

                log.log( Level.FINE, "{0}: {1} attachments", new Object[] { ticket.getId(), ticket.getAttachmentNum() } );

                TicketState tempState = TicketState.PROCESSED_OK;          // default value

                // save attachments
                for ( AttachmentLink attachmentLink : ticket.getAttachments() ) {

                    log.log( Level.INFO, "downloading {0} {1}", new Object[] { new Integer( an ), attachmentLink } );

                    try {
                        int result = fetcher.fetch( attachmentLink );

                        // if result is NOK, record that something went wrong
                        if ( result != HttpURLConnection.HTTP_OK ) {
                            tempState = TicketState.PROCESSED_NOK;
                        }

                    } catch ( IOException | RuntimeException ex ) {
                        log.log( Level.SEVERE, "downloading ticket data:", ex );
                        tempState = TicketState.PROCESSED_NOK;
                    }

                    updateProgress( an++, attNum );
                }

                // save remaining fields
                try {
                    new TicketSaver( namer ).saveTicketFields( ticket );
                } catch ( IOException | RuntimeException ex ) {
                    log.log( Level.SEVERE, "saving ticket fields:", ex );
                }

                // update icon in table
                final TicketState ts = tempState;
                Platform.runLater( new Runnable() {
                    @Override
                    public void run() {
                        ticket.setProcessed( ts );
                    }
                } );
            }

            return null;
        }

        /**
         * Counts the attachments of all the downloadable tickets.
         *
         * @param tickets the list of processable tickets.
         * @return the total number of attachments.
         */
        private int countAttachments( List<Ticket> tickets ) {
            int num = 0;
            for ( Ticket ticket : tickets ) {
                num += ticket.getAttachments().size();
            }
            return num;
        }

        /**
         * Extracts all processable tickets from the main list.
         *
         * @return a list with workable tickets.
         */
        private List<Ticket> filterTickets() {
            List<Ticket> tl = new ArrayList<>();
            for ( Ticket ticket : origList ) {
                if ( ticket.isProcessed() != TicketState.NOT_PROCESSED ) {
                    continue;
                }
                tl.add( ticket );
            }
            return tl;
        }
    }
}
