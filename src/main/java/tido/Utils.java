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
package tido;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility methods.
 *
 * @author Andrea Cisternino
 */
public final class Utils
{
    private static final int BUF_SIZE = 8192;

    // taken from Apache commons-io 2.4

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedInputStream</code>.
     * <p>
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output  the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException if an I/O error occurs
     */
    public static long copyStream(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n = 0;
        byte[] buffer = new byte[BUF_SIZE];
        while ( -1 != ( n = input.read( buffer ) ) ) {
            output.write( buffer, 0, n );
            count += n;
        }
        return count;
    }

    /**
     * Capitalize the first character of the given string.
     *
     * @param original the original string.
     * @return the original string with the first letter upper-cased.
     */
    public static String capitalizeFirstLetter(final String original) {
        if ( original.length() == 0 ) {
            return original;
        }
        return original.substring( 0, 1 ).toUpperCase() + original.substring( 1 );
    }

    /**
     * Checks if a String is whitespace, empty ("") or null.
     *
     * @param string the String to check, may be null.
     * @return true if the String is null, empty or whitespace.
     */
    public static boolean isBlank(final String string) {
        return string == null || string.isEmpty() || string.trim().isEmpty();
    }
}
