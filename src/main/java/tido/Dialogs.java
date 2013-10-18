package tido;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.stage.Stage;

import static javafx.scene.control.Dialogs.*;

/**
 * Manages the application's dialogs.
 *
 * @author Andrea Cisternino
 */
public class Dialogs
{
    private static final Logger log = Logger.getLogger( Dialogs.class.getName() );

    private static final String MASTHEAD_CONFIG_ERROR = "Application configuration error.";
    private static final String MASTHEAD_AUTH_ERROR   = "Authentication error.";

    /** The stage used to display the dialogs. */
    private final Stage stage;

    private Set<DialogTypes> displayed = EnumSet.noneOf( DialogTypes.class );

    //---- Enums -------------------------------------------------------------------

    public static enum Wait {
        YES,
        NO
    }

    private static enum DialogTypes {
        CONFIG_DIR_ERROR,
        CONFIG_FILE_ERROR,
        SERVERS_FILE_MISSING,
        SERVERS_FILE_ERROR,
        FAILED_LOGIN_ERROR,
        JS_ENGINE_ERROR
    }

    //---- Lifecycle ---------------------------------------------------------------

    public Dialogs(Stage stage) {
        this.stage = stage;
    }

    //---- API ---------------------------------------------------------------------

    public DialogResponse configDirError(final String configDir, final Exception ex, Wait wait) {

        log.fine( "called" );
        FutureTask<DialogResponse> ft = new FutureTask<>( new Callable<DialogResponse>() {
            @Override
            public DialogResponse call() throws Exception {
                return showErrorDialog( stage,
                        "Error creating the application configuration directory:\n"
                        + configDir,
                        MASTHEAD_CONFIG_ERROR, App.FULL_NAME, ex );
            }
        } );

        return postDialog( ft, wait );
    }

    public DialogResponse configFileError(final Exception ex, Wait wait) {

        log.fine( "called" );
        FutureTask<DialogResponse> ft = new FutureTask<>( new Callable<DialogResponse>() {
            @Override
            public DialogResponse call() throws Exception {
                return showErrorDialog( stage, "Error loading application configuration file!\n"
                        + "A " + ex.getCause().getClass().getSimpleName()
                        + " was thrown while reading the file.\n"
                        + "Please fix the problem and restart the application.",
                        MASTHEAD_CONFIG_ERROR, App.FULL_NAME, ex );
            }
        } );

        return postDialog( ft, wait );
    }

    public DialogResponse serversFileMissing(final String configDir, Wait wait) {

        if ( displayed.contains( DialogTypes.SERVERS_FILE_MISSING ) ) {
            log.fine( "skip" );
            return DialogResponse.OK;
        } else {
            log.fine( "display" );
            displayed.add( DialogTypes.SERVERS_FILE_MISSING );
        }

        FutureTask<DialogResponse> ft = new FutureTask<>( new Callable<DialogResponse>() {
            @Override
            public DialogResponse call() throws Exception {
                return showErrorDialog( stage,
                        "Servers configuration file not found!\n"
                        + "Please create the file manually in\n"
                        + configDir.toString() + "\nand restart the aplication.",
                        MASTHEAD_CONFIG_ERROR, App.FULL_NAME );
            }
        } );

        return postDialog( ft, wait );
    }

    public DialogResponse serversFileError(final Exception ex, Wait wait) {

        if ( displayed.contains( DialogTypes.SERVERS_FILE_ERROR ) ) {
            log.fine( "skip" );
            return DialogResponse.OK;
        } else {
            log.fine( "display" );
            displayed.add( DialogTypes.SERVERS_FILE_ERROR );
        }

        FutureTask<DialogResponse> ft = new FutureTask<>( new Callable<DialogResponse>() {
            @Override
            public DialogResponse call() throws Exception {

                String exName;
                if ( ex.getCause() != null ) {
                    exName = ex.getCause().getClass().getSimpleName();
                } else {
                    exName = ex.getClass().getSimpleName();
                }

                return showErrorDialog( stage,
                        "Error reading the server configuration file!\n"
                        + "A " + exName + " was thrown while reading the file.\n"
                        + "Please fix the problem and restart the application.",
                        MASTHEAD_CONFIG_ERROR, App.FULL_NAME, ex );
            }
        } );

        return postDialog( ft, wait );
    }

    public String acceptPassword(final String serverName) {

        log.fine( "called" );
        FutureTask<String> ft = new FutureTask<>( new Callable<String>() {
            @Override
            public String call() throws Exception {
                return showInputDialog( stage,
                        "Please enter password for server \"" + serverName + "\":",
                        "Insert password.", App.FULL_NAME );
            }
        } );

        return postDialog( ft, Wait.YES );
    }

    public DialogResponse failedLoginError(final String serverName, Wait wait) {

        log.fine( "called" );
        FutureTask<DialogResponse> ft = new FutureTask<>( new Callable<DialogResponse>() {
            @Override
            public DialogResponse call() throws Exception {
                return showErrorDialog( stage,
                        "Error logging in to server \"" + serverName + '"',
                        MASTHEAD_AUTH_ERROR, App.FULL_NAME );
            }
        } );

        return postDialog( ft, wait );
    }

    public DialogResponse jsEngineError(final Exception ex, Wait wait) {

        log.fine( "called" );
        FutureTask<DialogResponse> ft = new FutureTask<>( new Callable<DialogResponse>() {
            @Override
            public DialogResponse call() throws Exception {
                return showErrorDialog( stage,
                        Utils.capitalizeFirstLetter( ex.getCause().getMessage() )
                        + "\n\nPlease correct the error and restart the application.",
                        "Error parsing JavaScript renamer script.", App.FULL_NAME, ex );
            }
        } );

        return postDialog( ft, wait );
    }

    //---- Support methods ---------------------------------------------------------

    /**
     * Post the Dialog to the proper thread.
     * It will also eventually wait for the return value.
     */
    private <V> V postDialog(FutureTask<V> dialogTask, Wait wait) {

        if ( Platform.isFxApplicationThread() ) {
            // run immediately in the GUI thread
            dialogTask.run();
            wait = Wait.YES;        // makes the method return the value
        } else {
            // run some time in the future in the GUI thread
            Platform.runLater( dialogTask );
        }

        try {
            if ( wait == Wait.YES ) {
                // this will block the calling thread waiting for completion
                return dialogTask.get();
            }
        } catch ( InterruptedException | ExecutionException ex ) {
            log.log( Level.SEVERE, "waiting for dialog:", ex );
        }

        return null;
    }

}
