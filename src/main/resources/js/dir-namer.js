/*
 * This function must return a string that represents the path
 * where the ticket artifacts will be saved.
 *
 * Global variables:
 *      "S": the main String.js object with many utility methods to operate on strings.
 *      "separator": the platform directory separator character.
 *
 * Arguments:
 *      "ticket": the tido.model.Ticket java object containing the ticket data.
 *
 * For more information on String.js see: http://stringjs.com
 *
 * For JavaScript Strings see: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String
 */
function generateName( ticket ) {

    // convert from Java to JavaScript strings
    // this needed to operate on string using String.js functions
    var title = "" + ticket.title;

    // calculate name
    var cleaned = S( title ).trim().stripPunctuation().collapseWhitespace().replaceAll(' ', '_').s;
    cleaned = cleaned.toLowerCase();

    var ret = ticket.id + "_" + cleaned;

    return ret;
}
