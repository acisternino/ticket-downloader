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
package tido.scraping;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tido.config.ServerInfo;
import tido.model.AttachmentLink;
import tido.model.Ticket;

/**
 *
 * @author Andrea Cisternino
 */
public abstract class BasePageParser implements PageParser {

    private static final Logger log = Logger.getLogger( BasePageParser.class.getName() );

    protected ServerInfo server;

    //---- Abstract methods --------------------------------------------------------

    abstract String extractTracker(Document page);
    abstract String extractDescription(Document page);
    abstract String extractAnalysis(Document page);

    //---- Lifecycle ---------------------------------------------------------------

    /**
     * Protected constructor to avoid direct instantiation.
     *
     * @param server
     */
    protected BasePageParser(ServerInfo server) {
        this.server = server;
    }

    /**
     * Static factory method that creates instances of BaseParser customized
     * for a specific TeamForge server.
     *
     * @param server
     * @return
     */
    public static PageParser create( ServerInfo server ) {

        // TODO this is very BAD because forces to use a specific id in the file!!
        switch ( server.getId() ) {
            case "EB":
                return new EbPageParser( server );

            case "ESO":
                return new EsoPageParser( server );

            default:
                throw new IllegalArgumentException( "wrong server id: \"" + server.getId() + '"' );
        }
    }

    //---- API ---------------------------------------------------------------------

    @Override
    public Ticket parse(Document page) {

        if ( page == null ) {
            throw new IllegalArgumentException( "Document is null" );
        }

        Ticket t = new Ticket( server );

        // get main Ticket attributes parsing the title string
        String title = page.select( "head > title" ).first().ownText();
        log.log( Level.FINE, "original title: {0}", title );

        t.setId( extractArtifactId( title ) );
        t.setTitle( extractTitle( title ) );
        t.setKpm( extractKpm( title ) );

        // get other attributes parsing the page
        t.setTracker( extractTracker( page ) );
        log.log( Level.INFO, "tracker: \"{0}\"", t.getTracker() );

        t.setDescription( extractDescription( page ) );
        log.log( Level.INFO, "description length: {0}", t.getDescription().length() );

        t.setAnalysis( extractAnalysis( page) );
        log.log( Level.INFO, "analysys length: {0}", t.getAnalysis().length() );

        // add the attachments refs to the ticket
        addAttachments( t, page, server );

        return t;
    }

    //---- Contants ----------------------------------------------------------------

    /*
     * These values are common to all PageParsers.
     */
    private static final Pattern ARTF_ID_PATTERN = Pattern.compile( "^TeamForge : (artf\\d+):" );
    private static final Pattern TITLE_PATTERN = Pattern.compile( "artf\\d+:(.+)" );
    private static final Pattern KPM_PATTERN = Pattern.compile( "\\[(\\d+?)\\]$" );

    //---- Common methods ----------------------------------------------------------

    /**
     *
     * @param title
     * @return
     */
    String extractArtifactId(String title) {

        Matcher m = ARTF_ID_PATTERN.matcher( title );

        String artifactId = "";

        if ( m.find() ) {
            artifactId = m.group( 1 );
            log.log( Level.FINE, "\"{0}\"", artifactId );
        }
        return artifactId;
    }

    /**
     *
     * @param title
     * @return
     */
    long extractKpm(String title) {

        Matcher m = KPM_PATTERN.matcher( title );

        long kpm = 0;

        if ( m.find() ) {
            kpm = Long.parseLong( m.group( 1 ) );
            log.log( Level.FINE, "\"{0,number,#}\"", kpm );
        }
        return kpm;
    }

    /**
     *
     * @param title
     * @return
     */
    String extractTitle(String title) {

        String st = title;

        // try to remove final KPM number if there
        Matcher km = KPM_PATTERN.matcher( title );

        if ( km.find() ) {
            st = title.substring( 0, km.start() );
        }

        // extract title from remaining string
        Matcher tm = TITLE_PATTERN.matcher( st );

        String tt = "";

        if ( tm.find() ) {
            tt = tm.group( 1 ).trim();
            log.log( Level.FINE, "\"{0}\"", tt );
        }
        return tt;
    }

    //---- Attachments -------------------------------------------------------------

    private static final String ATTACHMENTS_PATH = "a[href*=/downloadAttachment/]:not(:has(img))";

    /**
     *
     * @param ticket
     * @param serverUrl
     */
    void addAttachments(Ticket ticket, Document page, ServerInfo server) {

        // extract all URL's of attachments
        Elements urls = page.select( ATTACHMENTS_PATH );
        log.log( Level.FINE, "found {0} attachment URL's", urls.size() );

        String serverUrl = server.getUrl();
        int c = 0;

        for ( Iterator<Element> it = urls.iterator(); it.hasNext(); ) {
            Element element = it.next();
            c++;

            log.log( Level.FINE, "url: {0}{1}", new String[] { serverUrl, element.attr( "href" ) } );
            log.log( Level.FINE, "name: {0}", element.ownText() );

            AttachmentLink ar = new AttachmentLink( ticket );

            ar.setUrl( serverUrl + element.attr( "href" ) );
            ar.setName( element.ownText() );

            ticket.getAttachments().add( ar );
        }
        log.log( Level.INFO, "added {0} attachments", c );
    }

}
