/*
 * This function must return a string that represents the path
 * where the ticket artifacts will be saved.
 *
 * Globals:
 *      "S": the main string.js object with many utility methods to operate on strings.
 *      "separator": a string containing the platform directory separator character.
 */
var generateName = function( baseDir, ticket ) {

    log.info( "generating for id: " + ticket.id );
    log.info( "base directory: " + baseDir );

    // convert to JavaScript strings
    var base = "" + baseDir;
    var title = "" + ticket.title;
    var kpm = "" + ticket.kpm;

    // calculate name
    var cleaned = S( title ).trim().stripPunctuation().collapseWhitespace().replaceAll(' ', '_').s;
    cleaned = cleaned.toLowerCase();

    var ret = base + separator + ticket.id + "_" + cleaned;

    return ret;
}
