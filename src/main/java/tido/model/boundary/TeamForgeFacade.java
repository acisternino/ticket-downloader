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

import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import tido.config.ConfigManager;
import tido.model.Ticket;

/**
 * Façade class mediating all access to the TeamForge server.
 *
 * @author Andrea Cisternino
 */
public class TeamForgeFacade
{
    private static final Logger log = Logger.getLogger( TeamForgeFacade.class.getName(), null );

    /** The JavaFX service used to download the tickets. */
    private TicketDownloadService tds;

    /** The JavaFX service used to download the attachments of all the tickets in the list. */
    private final AttachmentDownloadService ads;

    //---- Properties --------------------------------------------------------------

    /**
     * The list of Tickets. This is the model of the TableView.
     */
    private ListProperty<Ticket> list = new SimpleListProperty<>( FXCollections.<Ticket>observableArrayList() );
    public ListProperty<Ticket> listProperty() { return list; }
    public ObservableList<Ticket> getList() { return list.get(); }

    /**
     * Are we busy downloading?
     */
    private final BooleanProperty busy = new SimpleBooleanProperty( this, "busy", false );
    public BooleanProperty busyProperty() { return busy; }

    /**
     * The progress of the attachment download process. This number includes
     * all the attachments from all the tickets.
     */
    private final DoubleProperty progress = new SimpleDoubleProperty( this, "progress", 0.0d );
    public DoubleProperty progressProperty() { return progress; }

    //---- Lifecycle ---------------------------------------------------------------

    public TeamForgeFacade(ConfigManager config) {

        // create and configure TicketDownloadService
        tds = new TicketDownloadService( config );
        tds.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                list.addAll( tds.getValue() );
                log.info( "tickets downloaded" );
            }
        } );

        // create and configure AttachmentDownloadService
        ads = new AttachmentDownloadService( config );
        ads.setOnSucceeded( new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                log.info( "attachments downloaded" );
            }
        } );
        ads.setTickets( list );

        // we track only the progress of the attachments download
        progress.bind( ads.progressProperty() );

        // the global busy property
        busy.bind( Bindings.or( tds.runningProperty(), ads.runningProperty() ) );
    }

    //---- API ---------------------------------------------------------------------

    /**
     * Download a list of Tickets.
     * <br/>
     * The URL's have already been checked to be valid by the ViewModel.
     *
     * @param urls the URL's of the Tickets to download.
     */
    public void fetchTickets(List<String> urls) {
        log.info( Integer.toString( urls.size() ) );

        tds.setTicketUrls( urls );
        tds.restart();
    }

    /**
     * Download all the attachments of the tickets contained in the list.
     */
    public void downloadAttachments() {
        log.info( "called" );

        ads.restart();
    }

    /**
     * Called by the GUI when the base directory for tickets changes.
     *
     * @param path the new tickets directory.
     */
    public void setBaseDir(String path) {
        log.fine( path );

        ads.setBaseDir( Paths.get( path ) );
    }

}
