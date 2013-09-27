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

    private static final String ANALYSIS_PATH = "div#main td.ItemDetailValue > textarea#fv_id_fild4882.inputfield";

    @Override
    String extractAnalysis(Document page) {
        return page.select( ANALYSIS_PATH ).text().trim();
    }

}
