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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import tido.config.ConfigManager;
import tido.model.Ticket;

/**
 * A JavaFX {@link Service} that downloads a Ticket in a background thread.
 *
 * @author Andrea Cisternino
 */
public class TicketDownloadService extends Service<List<Ticket>> {

    private static final Logger log = Logger.getLogger( TicketDownloadService.class.getName() );

    /** The application configuration. */
    private final ConfigManager config;

    //---- Properties --------------------------------------------------------------

    /**
     * The URL's to download.
     */
    private List<String> ticketUrls;
    public void setTicketUrls(List<String> ticketUrls) { this.ticketUrls = ticketUrls; }
    public List<String> getTicketUrls() { return ticketUrls; }

    //---- Task --------------------------------------------------------------------

    public TicketDownloadService(ConfigManager config) {
        this.config = config;
    }

    //---- Task --------------------------------------------------------------------

    @Override
    protected Task<List<Ticket>> createTask() {

        final List<String> urls = getTicketUrls();

        return new Task<List<Ticket>>() {
            @Override
            protected List<Ticket> call() throws Exception {
                log.log( Level.INFO, "fetching {0} tickets", urls.size());

                List<Ticket> tickets = new ArrayList<>();
                TicketFetcher fetcher = new TicketFetcher( config );

                updateProgress( 0, urls.size() );

                int c = 1;
                for ( String url : urls ) {
                    Ticket ticket = fetcher.fetch( url );
                    tickets.add( ticket );

                    log.log( Level.INFO, "added ticket: {0}", ticket );

                    updateProgress( c++, urls.size() );
                }
                return tickets;
            }
        };
    }
}
