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
package tido.model;

import java.nio.file.Path;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import tido.config.ServerInfo;

/**
 * A TeamForge ticket.
 *
 * @author Andrea Cisternino
 */
public class Ticket
{
    /** The server containing this Ticket. */
    private final ServerInfo source;

    // TODO does it need to be observable?
    private ObservableList<AttachmentLink> attachments;

    //---- Lifecycle ---------------------------------------------------------------

    /**
     *
     * @param source
     */
    public Ticket(ServerInfo source) {

        this.source = source;

        attachments = FXCollections.observableArrayList();

        attachments.addListener( new ListChangeListener<AttachmentLink>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends AttachmentLink> change) {
                while ( change.next() ) {
                    if ( change.wasAdded() || change.wasRemoved() ) {
                        attachmentNum.set( attachments.size() );
                    }
                }
            }
        } );
    }

    //---- Properties --------------------------------------------------------------

    /**
     * The URL of the Ticket.
     */
    private final StringProperty url = new SimpleStringProperty( this, "url", "" );
    public StringProperty urlProperty() { return url; }
    public String getUrl() { return url.get(); }
    public void setUrl(String url) { this.url.set( url ); }

    /**
     * The artifact Id of the Ticket.
     */
    private final StringProperty id = new SimpleStringProperty( this, "id", "" );
    public StringProperty idProperty() { return id; }
    public String getId() { return id.get(); }
    public void setId(String id) { this.id.set( id ); }

    /**
     * The Ticket title.
     */
    private final StringProperty title = new SimpleStringProperty( this, "title", "" );
    public StringProperty titleProperty() { return title; }
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set( title ); }

    /**
     * The KPM number of the Ticket.
     */
    private final LongProperty kpm = new SimpleLongProperty( this, "kpm", 0L );
    public LongProperty kpmProperty() { return kpm; }
    public long getKpm() { return kpm.get(); }
    public void setKpm(long kpm) { this.kpm.set( kpm ); }

    /**
     * The Tracker containing the Ticket.
     */
    private final StringProperty tracker = new SimpleStringProperty( this, "tracker", "" );
    public StringProperty trackerProperty() { return tracker; }
    public String getTracker() { return tracker.get(); }
    public void setTracker(String tracker) { this.tracker.set( tracker ); }

    /**
     * The description of the Ticket.
     */
    private final StringProperty description = new SimpleStringProperty( this, "description", "" );
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set( description ); }

    /**
     * Analysis of the Ticket.
     */
    private final StringProperty analysis = new SimpleStringProperty( this, "analysis", "" );
    public StringProperty analysisProperty() { return analysis; }
    public String getAnalysis() { return analysis.get(); }
    public void setAnalysis(String analysis) { this.analysis.set( analysis ); }

    /**
     * Has this ticket been processed?
     */
    private final ObjectProperty<TicketState> processed = new SimpleObjectProperty<>( this, "processed", TicketState.NOT_PROCESSED );
    public ObjectProperty<TicketState> processedProperty() { return processed; }
    public TicketState isProcessed() { return processed.get(); }
    public void setProcessed(TicketState processed) { this.processed.set( processed ); }

    /**
     * The number of attachments of this ticket. Automatically calculated from the list of attachments.
     */
    private final IntegerProperty attachmentNum = new SimpleIntegerProperty( this, "attachmentNum", 0 );
    public IntegerProperty attachmentNumProperty() { return attachmentNum; }
    public int getAttachmentNum() { return attachmentNum.get(); }

    /**
     * The Path where this ticket was saved.
     */
    private final ObjectProperty<Path> path = new SimpleObjectProperty<>( this, "path", null );
    public ObjectProperty<Path> pathProperty() { return path; }
    public Path getPath() { return path.get(); }
    public void setPath(Path path) { this.path.set( path ); }

    //---- Accessors ---------------------------------------------------------------

    public ServerInfo getSource() {
        return source;
    }

    public ObservableList<AttachmentLink> getAttachments() {
        return attachments;
    }

    @Override
    public String toString() {
        return "Ticket{" + "url=" + url.get() + ", id=" + id.get() + ", title=" + title.get()
                + ", kpm=" + kpm.get() + ", tracker=" + tracker.get() + ", attachments=" + attachmentNum.get()
                + ", serverId=" + source.getId() + ", processed=" + processed.get()
                + ", path=" + path.get() + '}';
    }
}
