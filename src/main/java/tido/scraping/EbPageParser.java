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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tido.config.ServerInfo;

/**
 *
 * @author Andrea Cisternino
 */
class EbPageParser extends BasePageParser {

    //---- Lifecycle ---------------------------------------------------------------

    EbPageParser(ServerInfo server) {
        super( server );
    }

    //---- Abstract methods --------------------------------------------------------

    private static final String TRACKER_PATH = "div#main tr.artifactTrackerRow > td.ItemDetailValue";

    @Override
    String extractTracker(Document page) {
        return page.select( TRACKER_PATH ).first().text().trim();
    }

    private static final String DESCRIPTION_PATH = "div#main tr.artifactDescriptionRow > td.ItemDetailValue";

    @Override
    String extractDescription(Document page) {
        return page.select( DESCRIPTION_PATH ).first().text().trim();
    }

    @Override
    String extractAnalysis(Document page) {
        
        Elements rows = page.select( "table#fieldsColumn1 tr" );
        
        for ( Element row : rows ) {
        
            Elements columns = row.select( "td" );
            
            if ( ! columns.first().ownText().toLowerCase().contains( "analysis" ) ) {
                continue;
            }
            
            return columns.get( 2 ).text();
        }
        
        return "";
    }

}
