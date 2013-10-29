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
package tido.viewmodel;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import tido.config.ConfigManager;
import tido.model.Ticket;
import tido.model.TicketState;
import tido.model.boundary.TeamForgeFacade;

/**
 * The ViewModel of the application.
 *
 * @author Andrea Cisternino
 */
public class TicketDownloaderViewModel implements Initializable {

    private static final Logger log = Logger.getLogger( TicketDownloaderViewModel.class.getName() );

    //---- FXML objects ------------------------------------------------------------

    @FXML
    private TableView<Ticket> ticketTable;
    @FXML
    private TableColumn<Ticket, TicketState> processedCol;
    @FXML
    private TableColumn<Ticket, String> serverNameCol;
    @FXML
    private TableColumn<Ticket, Integer> attchNumCol;

    @FXML
    private TextField baseDir;

    @FXML
    private Button baseDirButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button fetchButton;

    @FXML
    private ProgressBar progressBar;

    //---- End of FXML objects -----------------------------------------------------

    /** Class mediating all TeamForge interaction. */
    private TeamForgeFacade teamForge;

    /** The application configuration. */
    private ConfigManager config;

    //---- Lifecycle ---------------------------------------------------------------

    /*
     * See Initializable.initialize(URL url, ResourceBundle rb)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        log.log( Level.INFO, "from: {0}", url);

        // finish configuration of ticket table
        setupTable();

        // tooltips
        fetchButton.setTooltip( new Tooltip( "Downloads the content of all\nthe tickets in the list." ) );
        clearButton.setTooltip( new Tooltip( "Clears the list and readies the program\nfor other tickets." ) );
        baseDirButton.setTooltip( new Tooltip( "Selects the base directory\nwhere tickets will be downloaded." ) );
    }

    /**
     * Called by the application to perform the final initialization steps.
     *
     * @param teamForge the TeamForge façade.
     * @param config the application configuration.
     */
    public void postConstruct(TeamForgeFacade teamForge, ConfigManager config) {
        log.info( "called" );

        this.teamForge = teamForge;
        this.config = config;

        // configure all data bindings
        installBindings();

        // finish configuration of GUI
        baseDir.setText( config.config().getBaseDirectory() );
    }

    //---- GUI stuff ---------------------------------------------------------------

    private void installBindings() {
        ticketTable.setItems( teamForge.listProperty() );
        ticketTable.disableProperty().bind( teamForge.busyProperty() );
    }

    private void setupTable() {

        ticketTable.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );

        ticketTable.setPlaceholder( new Text( "Drop ticket URL's here" ) );

        serverNameCol.setCellValueFactory( new Callback<CellDataFeatures<Ticket, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Ticket, String> param) {
                return new SimpleStringProperty( param.getValue().getSource().getName() );
            }
        } );

        processedCol.setCellFactory( new Callback<TableColumn<Ticket, TicketState>, TableCell<Ticket, TicketState>>() {
            @Override
            public TableCell<Ticket, TicketState> call(TableColumn<Ticket, TicketState> param) {
                SemaphoreTableCell cell = new SemaphoreTableCell();
                // add style class to the cell
                cell.getStyleClass().add( "processedCell" );
                return cell;
            }
        } );

        // column alignment
        // PENDING use pure CSS solution when available in JavaFX
        attchNumCol.setCellFactory( new Callback<TableColumn<Ticket, Integer>, TableCell<Ticket, Integer>>() {
            @Override
            public TableCell<Ticket, Integer> call(TableColumn<Ticket, Integer> param) {
                TableCell<Ticket, Integer> cell = new TableCell<Ticket, Integer>() {
                    @Override
                    public void updateItem(Integer item, boolean empty) {
                        if ( item != null ) {
                            setText( item.toString() );
                        }
                    }
                };
                // add style class to the cell
                cell.getStyleClass().add( "attchNumCell" );
                return cell;
            }
        } );

    }

    //---- Actions -----------------------------------------------------------------

    // Handler for Button[fx:id="baseDirButton"] onAction
    @FXML
    public void chooseBaseDir(ActionEvent event) {
        log.info( "button pressed" );

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle( "Base directory for tickets" );

        File file = directoryChooser.showDialog( null );

        if ( file != null ) {
            String path = file.getPath();
            baseDir.setText( path );
            config.config().setBaseDirectory( path );
            teamForge.setBaseDir( path );
        }
    }

    // Handler for Button[fx:id="clearButton"] onAction
    @FXML
    public void clearList(ActionEvent event) {
        log.info( "button pressed" );

        progressBar.progressProperty().unbind();
        progressBar.progressProperty().set( 0.0d );
        teamForge.listProperty().clear();
    }

    // Handler for Button[fx:id="fetchButton"] onAction
    @FXML
    public void fetchAttachments(ActionEvent event) {
        log.info( "button pressed" );

        if ( teamForge.listProperty().size() > 0 ) {
            progressBar.progressProperty().bind( teamForge.progressProperty() );
            teamForge.downloadAttachments();
        }
        else {
            log.info( "list empty, nothing to download" );
        }
    }

    // Handler for TableView[fx:id="ticketList"] onKeyReleased
    private final KeyCombination delKey = new KeyCodeCombination( KeyCode.DELETE );

    @FXML
    void tableKeyReleased(KeyEvent event) {
        if ( delKey.match( event ) ) {
            // this copy is needed because of a bug in JavaFX 2.2
            List<Ticket> selectedItems = new ArrayList<>( ticketTable.getSelectionModel().getSelectedItems() );

            log.log( Level.FINE, "removing {0} items", selectedItems.size() );
            teamForge.listProperty().removeAll( selectedItems );

            progressBar.progressProperty().unbind();
            progressBar.progressProperty().set( 0.0d );
            ticketTable.getSelectionModel().clearSelection();
        }
    }

    //---- Drag & Drop -------------------------------------------------------------

    // Handler for TableView[fx:id="ticketList"] onDragDropped
    @FXML
    public void onDragDropped(DragEvent event) {

        // data dropped - if there is a string data on dragboard, read it and use it
        Dragboard db = event.getDragboard();
        boolean success = false;

        String dropped = null;
        DataFormat format = null;

        if ( db.hasUrl() ) {
            dropped = db.getUrl();
            format = DataFormat.URL;
            log.fine( "type: URL" );
        }
        else if ( db.hasString() ) {
            dropped = db.getString().trim();
            format = DataFormat.PLAIN_TEXT;
            log.fine( "type: Text" );
        }

        if ( dropped != null ) {
            droppedData( dropped, format );
            success = true;
        }

        // let the source know whether the string was successfully transferred and used
        event.setDropCompleted( success );
        event.consume();
    }

    // Handler for TableView[fx:id="ticketList"] onDragEntered
    @FXML
    public void onDragEntered(DragEvent event) {

        // the drag-and-drop gesture entered the target, add graphical cues
        if ( event.getGestureSource() != ticketTable
                && ( event.getDragboard().hasString() || event.getDragboard().hasUrl() ) ) {
            log.fine( event.toString() );
            // TODO provide visual clue
        }

        event.consume();
    }

    // Handler for TableView[fx:id="ticketList"] onDragExited
    @FXML
    public void onDragExited(DragEvent event) {

        // mouse moved away, remove the graphical cues
        // TODO remove visual clues
        log.fine( event.toString() );

        event.consume();
    }

    // Handler for TableView[fx:id="ticketList"] onDragOver
    @FXML
    public void onDragOver(DragEvent event) {

        // data is dragged over the target
        // accept it only if it is not dragged from the same node and if it has proper data
        if ( event.getGestureSource() != ticketTable
                && ( event.getDragboard().hasString() || event.getDragboard().hasUrl() ) ) {
            event.acceptTransferModes( TransferMode.COPY );
        }

        event.consume();
    }

    //---- Support methods ---------------------------------------------------------

    /**
     * Converts a String into a valid URL removing the query string and the fragment.
     *
     * @param dropped the input string.
     * @return a string representing a valid URL without queries or fragments.
     * @throws MalformedURLException if the incoming String is not a well formed URL.
     */
    String cleanUrl(String dropped) throws MalformedURLException {
        URL url = new URL( dropped );
        return url.getProtocol() + "://" + url.getAuthority() + url.getPath();
    }

    /**
     * Processes the dropped data.
     * <br/>
     * This method accepts a single URL directly dragged from the browser address bar
     * or one or more lines of text, each containing the URL of a ticket.
     * <br/>
     * All of these inputs are provided as a single String that must be processed and
     * converted into one or more Strings containing valid URL's.
     *
     * @param dropped the dropped data.
     * @param format the format of the data.
     * @return the number of URL's to download.
     */
    int droppedData(String dropped, DataFormat format) {

        int sentItems = 0;

        if ( format == DataFormat.URL ) {
            // dropped argument is a String but we know that it is a single valid URL
            try {
                String url = cleanUrl( dropped );
                log.info( url );

                // call the model/repository and add this url
                teamForge.fetchTickets( Collections.singletonList( url ) );
                sentItems++;

            } catch ( MalformedURLException ex ) {
            }

        } else if ( format == DataFormat.PLAIN_TEXT ) {
            // dropped argument can be any string or sequence of
            String[] urls = dropped.split( "\\r?\\n" );

            // build list of URL's
            List<String> goodUrls = new ArrayList<>( urls.length );

            for ( int i = 0; i < urls.length; i++ ) {
                try {
                    String url = cleanUrl( urls[i] );
                    log.log( Level.INFO, "[{0,number,00}] {1}", new Object[] { i, url } );

                    goodUrls.add( url );
                    sentItems++;

                } catch ( MalformedURLException ex ) {
                    log.log( Level.WARNING, "[{0,number,00}] malformed URL: \"{1}\"", new Object[] { i, urls[i] } );
                }
            }

            // finally download the tickets
            teamForge.fetchTickets( goodUrls );

        } else {
            log.log( Level.WARNING, "wrong format: {0}", format.toString() );
        }
        return sentItems;
    }

}
