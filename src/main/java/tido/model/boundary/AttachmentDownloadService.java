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

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private  TicketDirectoryNamer namer;

    /** The application configuration. */
    private final ConfigManager config;

    /** Base directory where all ticket directories will be created. */
    private Path baseDir;

    //---- Lifecycle ---------------------------------------------------------------

    public AttachmentDownloadService(ConfigManager config) {
        this.config = config;

        namer = new TicketDirectoryNamer( config );
    }

    //---- Properties --------------------------------------------------------------

    /**
     * The list of tickets containing the attachments to download.
     */
    private final ListProperty<Ticket> tickets = new SimpleListProperty<>();
    public ListProperty<Ticket> ticketsProperty() { return tickets; }
    public void setTickets(ObservableList<Ticket> tickets) { this.tickets.set( tickets ); }
    public ObservableList<Ticket> getTickets() { return tickets.get(); }

    public void setBaseDir(Path path) {
        baseDir = path;
    }

    //---- Task --------------------------------------------------------------------

    @Override
    protected Task<Object> createTask() {

        final ObservableList<Ticket> tl = getTickets();
        final int ticketNum = tl.size();

        return new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                updateProgress( 0, ticketNum );

                int t = 0;
                final AttachmentFetcher fetcher = new AttachmentFetcher( namer );

                for ( Ticket ticket : tl ) {

                    for ( AttachmentLink attachmentLink : ticket.getAttachments() ) {
                        log.log( Level.INFO, "downloading {0}", attachmentLink );
                        fetcher.fetch( attachmentLink );
                        Thread.sleep( 500 );        // wait a bit to avoid swamping the server
                    }
                    // TODO handle return code from all attachments

                    new TicketSaver( namer ).saveTicketFields( ticket );

                    ticket.setProcessed( TicketState.PROCESSED_OK );
                    updateProgress( ++t, ticketNum );
                }
                return null;
            }
        };
    }

}
