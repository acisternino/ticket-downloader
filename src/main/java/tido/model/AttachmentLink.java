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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A reference to an Attachment of a {@link Ticket}.
 *
 * @author Andrea Cisternino
 */
public class AttachmentLink
{
    /** The Parent ticket. */
    private final Ticket ticket;

    //---- Lifecycle ---------------------------------------------------------------

    /**
     * Create an AttachmentRef linked to the given Ticket.
     *
     * @param ticket the {@link Ticket} containing this AttachmentRef.
     */
    public AttachmentLink(Ticket ticket) {
        this.ticket = ticket;
    }

    //---- Properties --------------------------------------------------------------

    /**
     * The URL of the attachment.
     */
    private final StringProperty url = new SimpleStringProperty( this, "url", "" );
    public StringProperty urlProperty() { return url; }
    public String getUrl() { return url.get(); }
    public void setUrl(String url) { this.url.set( url ); }

    /**
     * The filename of the attachment.
     */
    private final StringProperty name = new SimpleStringProperty( this, "name", "" );
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set( name ); }

    //---- Getters -----------------------------------------------------------------

    public Ticket getTicket() {
        return ticket;
    }

    //---- Support methods ---------------------------------------------------------

    @Override
    public String toString() {
        return "AttachmentLink{" + "url=" + url.get() + ", name=" + name.get()
                + ", ticket=" + ticket.getId() + '}';
    }
}
