/*
 * dir-namer.js  -  (c) 2013 Andrea Cisternino
 *
 * This file must contain the definition of a function with the following
 * signature:
 *
 *    function generateName( ticket )
 *
 * The function must return a string that represents a valid directory name
 * where the ticket attachments will be saved.
 *
 * This function will be called by the application for each ticket whose
 * attachments must be downloaded.
 *
 * The "ticket" argument provided to the function by the application is
 * a Java object that contains many attributes of the ticket being processed.
 * These attributes can be used to generate a directory name according to
 * the user needs.
 *
 * Besides the argument provided to the function at each invocation, the
 * environment also contains the following global variables initialised by
 * the application at startup:
 *
 *      "S":          the String.js object containing many utility methods
 *                    to operate on strings.
 *
 *      "separator":  the platform directory separator character.
 *
 * The "S" variable comes from the String.js library, a JavaScript library
 * that provides useful extra methods to work on strings.
 *
 * The "separator" variable contains the platform directory separator
 * character. This character can be useful when creating directory names
 * that contain more than one directory level. Such names can be useful
 * for ordering ticket directories in the filesystem according to one of
 * the ticket object fields.
 *
 *
 * === The String.js library ===
 *
 * The String.js library is available at http://stringjs.com.
 *
 * It is loaded in the JavaScript interpreter at startup and can be accessed
 * using the global "S" object.
 *
 * The first step to use the library is creating a String.js object from a
 * native JavaScript string using the "S" variable:
 *
 *    var myString = S( 'I am a JavaScript string' );
 *
 * Once a String.js object is created, methods can be called on it using
 * a fluent interface. For example, to convert a native string to lowercase
 * and extract the first 4 chars, we can do:
 *
 *    var result = S( 'I am a JavaScript string' ).toLowerCase().left( 4 );  // 'i am'
 *
 * String.js objects imports all of the native JavaScript methods.
 * This is for convenience. The only difference is that the imported methods
 * return String.js objects instead of native JavaScript strings.
 *
 * An overview of the JavaScript String object and its methods can be found here:
 *
 * http://www.w3schools.com/jsref/jsref_obj_string.asp
 *
 * The use of String.js is entirely optional. If the regular native JavaScript
 * string methods are enough for generating your directory name, it's OK to
 * use only them and disregard entirely the "S" object.
 *
 * Don't forget to read the section "Java vs. JavaScript strings" though.
 *
 *
 * === The Ticket object ===
 *
 * The ticket object provided to the function is an instance of the
 * tido.model.Ticket Java class.
 *
 * The useful attributes of this class are:
 *
 *       url:  The complete artifact URL (e.g. "https://sf43.elektrobit.com/sf/go/artf74149").
 *        id:  The artifact id (e.g. "artf74149").
 *     title:  The artifact title (e.g. "[Screen] The buttons are not visible").
 *       kpm:  The KPM number (e.g. 50478039. The type of this field is long).
 *   tracker:  The Tracker that contains the ticket (e.g. "Internal Interface Tickets").
 *
 * As an example, the following function returns a directory name comprised
 * only of the artifact id:
 *
 *    function generateName( ticket ) {
 *        return ticket.id;
 *    }
 *
 *
 * === Java vs. JavaScript strings ===
 *
 * All String attributes of the Ticket object are of type java.lang.String
 * and not native JavaScript strings.
 *
 * While the JavaScript interpreter makes using these objects very easy
 * also from within a the JavaScript code, one must remember that the
 * methods available on one of these objects are those of the
 * java.lang.String class and not those of native JavaScript strings.
 *
 * For example this call uses the Java method replaceFirst() that is not
 * available on a native JavaScript string
 *
 *    ticket.id.replaceFirst( 'artf', '' );     // 'artf76749 ' -> '76749'
 *
 * To use the String.js library, these Java String objects must be converted
 * to native JavaScript strings to avoid exceptions at runtime.
 *
 * This conversion can be done with statements like this:
 *
 *    var title = "" + ticket.title;    // title is now a JavaScript string
 *
 * The concatenation of an empty native JavaScript string with a Java String
 * object, provided by the JVM, forces the creation of another native
 * JavaScript string that can afterwards be used in String.js methods.
 */
function generateName( ticket ) {

    // Convert from Java String to native JavaScript strings
    // Needed to operate using String.js functions

    var title = "" + ticket.title;

    // Generate directory name

    var cleanTitle = S( title )
            .trim()
            .stripPunctuation()
            .collapseWhitespace()
            .replaceAll( ' ', '_' )
            .toLowerCase()
            .s;

    return ticket.id + '_' + cleanTitle;
}
