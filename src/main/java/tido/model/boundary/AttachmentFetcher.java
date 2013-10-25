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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import tido.model.AttachmentLink;
import tido.model.DownloadResult;
import tido.naming.TicketDirectoryNamer;

/**
 * Fetches a URL and save the content into a file.
 *
 * @author Andrea Cisternino
 */
public class AttachmentFetcher
{
    private static final Logger log = Logger.getLogger( AttachmentFetcher.class.getName() );

    // we fake Firefox
    private static final String HTTP_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";

    private final TicketDirectoryNamer namer;

    //---- Lifecycle ---------------------------------------------------------------

    public AttachmentFetcher(TicketDirectoryNamer namer) {
        this.namer = namer;
    }

    //---- API ---------------------------------------------------------------------

    /**
     *
     * @param link
     * @return
     * @throws Exception
     */
    public DownloadResult fetch(AttachmentLink link) throws Exception {

        DownloadResult result = new DownloadResult();
        long length;

        try {
            Path ticketDir = namer.getTicketPath( link.getTicket() );   // throws InvalidPathException

            HttpURLConnection conn = prepareConnection( link );         // throws IOException

            log.log( Level.INFO, "fetching url: {0}", conn.getURL().toExternalForm() );

            // execute the HTTP transaction
            conn.connect();                     // throws SocketTimeoutException, IOException

            int responseCode = conn.getResponseCode();
            log.log( Level.INFO, "response code: {0}", responseCode );

            result.httpResult = responseCode;
            if ( responseCode != HttpURLConnection.HTTP_OK ) {
                return result;
            }

            String fname = extractFilename( conn.getHeaderField( "Content-Disposition" ) );
            log.log( Level.FINE, "received filename: {0}", fname );

            long expectedLength = conn.getHeaderFieldLong( "Content-Length", 0 );
            log.log( Level.FINE, "expected length: {0}", expectedLength );

            // create complete path without exceptions
            Files.createDirectories( ticketDir );

            try ( BufferedInputStream in = new BufferedInputStream( conn.getInputStream(), (int) expectedLength ) ) {

                Path an = ticketDir.resolve( fname );
                length = Files.copy( in, an, StandardCopyOption.REPLACE_EXISTING );     // throws IOException, InvalidPathException, SecurityException

                log.log( Level.FINE, "saved file: {0}", an.toString() );
            }

            log.log( Level.FINE, "saved length: {0}", expectedLength );

        } catch ( IOException | InvalidPathException ex ) {
            log.log( Level.WARNING, null, ex );
            result.error = ex;
            return result;
        }

        result.size = length;
        return result;
    }

    //---- Support methods ---------------------------------------------------------

    /**
     * Prepares the {@link HttpURLConnection} needed to fetch the attachment.
     * <br/>
     * The connection is <b>not</b> established by this method.
     *
     * @param link the attachment to download.
     * @return the set up HttpURLConnection.
     * @throws IOException
     */
    private HttpURLConnection prepareConnection(AttachmentLink link) throws IOException {

        URL url;
        HttpURLConnection conn;

        try {
            url = new URL( link.getUrl() );                         // throws MalformedURLException

            conn = (HttpURLConnection) url.openConnection();        // throws IOException

            conn.setRequestMethod( "GET" );                         // throws ProtocolException
            conn.setAllowUserInteraction( false );
            conn.setUseCaches( false );
            conn.setRequestProperty( "User-Agent", HTTP_USER_AGENT );

            String cookies = link.getTicket().getSource().getCookiesHeader();
            conn.setRequestProperty( "Cookie", cookies );
            log.log( Level.FINE, "cookies: {0}", cookies );

        } catch ( MalformedURLException | ProtocolException ex ) {
            log.log( Level.WARNING, null, ex );
            throw new IOException( ex );
        }
        return conn;
    }

    /**
     * Extracts the filename from the Content-Disposition response header.
     *
     * @param contentDisp
     * @return
     */
    private String extractFilename(String contentDisp) {
        String[] cdParts = contentDisp.split( "; " );
        for ( String cdPart : cdParts ) {
            String[] keyVal = cdPart.split( "=" );
            if ( keyVal[0].equals( "filename" ) ) {
                return keyVal[1].replace( '"', ' ' ).trim();
            }
        }
        return "";
    }
}
