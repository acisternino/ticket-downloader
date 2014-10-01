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
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import tido.Utils;
import tido.model.AttachmentLink;
import tido.naming.TicketDirectoryNamer;

/**
 * Fetches an URL and save the content into a file.
 *
 * @author Andrea Cisternino
 */
public class AttachmentFetcher
{
    private static final Logger log = Logger.getLogger( AttachmentFetcher.class.getName() );

    private static final Set<String> DOUBLE_EXTS = new HashSet<>( Arrays.asList( "gz", "bz2", "xz" ) );

    // we fake Firefox
    private static final String HTTP_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0";

    private final TicketDirectoryNamer namer;

    //---- Lifecycle ---------------------------------------------------------------

    public AttachmentFetcher(TicketDirectoryNamer namer) {
        this.namer = namer;
    }

    //---- API ---------------------------------------------------------------------

    /**
     * Fetches and saves the ticket attachment identified by the link.
     *
     * @param link the attachment to be downloaded.
     * @return the HTTP return code of the transaction.
     * @throws IOException in case of errors while downloading or saving the attachment.
     */
    public int fetch(AttachmentLink link) throws IOException {

        Path ticketDir = namer.getTicketPath( link.getTicket() );   // throws InvalidPathException

        HttpURLConnection conn = prepareConnection( link );         // throws IOException

        log.log( Level.INFO, "fetching url: {0}", conn.getURL().toExternalForm() );

        // execute the HTTP transaction
        conn.connect();                     // throws SocketTimeoutException, IOException

        // process result
        int responseCode = conn.getResponseCode();
        log.log( Level.INFO, "response code: {0}", responseCode );

        if ( responseCode != HttpURLConnection.HTTP_OK ) {
            // the transaction failed, no reason to continue
            return responseCode;
        }

        String fname = extractFilename( conn.getHeaderField( "Content-Disposition" ) );
        if ( Utils.isBlank( fname ) ) {
            log.log( Level.INFO, "received filename empty, retrieving from page", fname );
            fname = link.getName();
        }
        log.log( Level.FINE, "filename: {0}", fname );

        long expectedLength = conn.getHeaderFieldLong( "Content-Length", 0 );
        log.log( Level.FINE, "expected length: {0}", expectedLength );

        // create complete path without exceptions
        Files.createDirectories( ticketDir );

        long length;

        // save attachment
        try ( BufferedInputStream in = new BufferedInputStream( conn.getInputStream(), (int) expectedLength ) ) {

            Path an = ticketDir.resolve( fname );

            int copyNum = 0;
            do {
                try {
                    length = Files.copy( in, an );  // throws IOException, InvalidPathException, SecurityException
                    break;
                } catch ( FileAlreadyExistsException ex ) {
                    copyNum++;
                    an = ticketDir.resolve( deDupName( fname, copyNum ) );
                }
            } while ( true );

            log.log( Level.FINE, "saved file: {0}", an.toString() );
        }

        log.log( Level.FINE, "expected length: {0}", expectedLength );
        log.log( Level.FINE, "saved length: {0}", length );

        return responseCode;
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

        url = new URL( link.getUrl() );                         // throws MalformedURLException

        conn = (HttpURLConnection) url.openConnection();        // throws IOException

        conn.setRequestMethod( "GET" );                         // throws ProtocolException
        conn.setAllowUserInteraction( false );
        conn.setUseCaches( false );
        conn.setRequestProperty( "User-Agent", HTTP_USER_AGENT );

        String cookies = link.getTicket().getSource().getCookiesHeader();
        conn.setRequestProperty( "Cookie", cookies );
        log.log( Level.FINE, "cookies: {0}", cookies );

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

    /**
     * De-duplicates the name of a file that is already existing on the filesystem.
     *
     * @param fname the name of the duplicate fie.
     * @param copyNum number of copy.
     * @return the de-duplicated name.
     */
    private String deDupName(String fname, int copyNum) {

        String newName;

        // position of extension
        int extIdx = fname.lastIndexOf( '.' );

        if ( extIdx > 0 ) {
            // extension found

            // check for special extensions
            if ( DOUBLE_EXTS.contains( fname.substring( extIdx + 1 ) ) ) {
                extIdx = fname.lastIndexOf( '.', extIdx - 1 );
            }

            String ext = fname.substring( extIdx + 1 );
            String basename = fname.substring( 0, extIdx );

            newName = basename + "(" + copyNum + ")." + ext;

        } else {
            // no extension

            newName = fname + "(" + copyNum + ")";
        }

        log.log( Level.FINE, "new name: {0}", newName );

        return newName;
    }
}
