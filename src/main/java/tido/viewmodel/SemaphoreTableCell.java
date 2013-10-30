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

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import tido.model.Ticket;
import tido.model.TicketState;

/**
 * A simple TableCell that displays a colored icon depending on
 * the state of the processed ticket.
 *
 * @author Andrea Cisternino
 */
public class SemaphoreTableCell extends TableCell<Ticket, TicketState>
{
    private final ImageView greyImg = new ImageView();
    private final ImageView greenImg = new ImageView();
    private final ImageView redImg = new ImageView();

    public SemaphoreTableCell() {
        greyImg.setImage( new Image( "/img/circle_grey.png" ) );
        greenImg.setImage( new Image( "/img/circle_green.png" ) );
        redImg.setImage( new Image( "/img/circle_red.png" ) );
    }

    @Override
    protected void updateItem(TicketState state, boolean empty) {
        // very importatnt - do not skip this!
        super.updateItem( state, empty );

        if ( state != null ) {
            switch ( state ) {
                case PROCESSED_OK:
                    setGraphic( greenImg );
                    break;

                case PROCESSED_NOK:
                    setGraphic( redImg );
                    break;

                default:
                    setGraphic( greyImg );
                    break;
            }
        }
    }
}
