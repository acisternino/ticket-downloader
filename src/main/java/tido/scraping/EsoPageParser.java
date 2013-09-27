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
class EsoPageParser extends BasePageParser {

    //---- Lifecycle ---------------------------------------------------------------

    EsoPageParser(ServerInfo server) {
        super( server );
    }

    //---- Abstract methods --------------------------------------------------------

    private static final String TRACKER_PATH = "div#main tr#artifactTrackerRow > td.ItemDetailValue";

    @Override
    String extractTracker(Document page) {
        return page.select( TRACKER_PATH ).first().text().trim();
    }

    private static final String DESCRIPTION_PATH = "div#main tr#artifactDescriptionRow > td.ItemDetailValue";

    @Override
    String extractDescription(Document page) {
        return page.select( DESCRIPTION_PATH ).first().text().trim();
    }

    private static final String ANALYSIS_PATH = "div#main td.ItemDetailValue > textarea.inputfield";
    // /html/body/div[3]/div[3]/table[3]/tbody/tr[2]/td/form/table/tbody/tr/td/table/tbody/tr[17]/td[2]/textarea

    @Override
    String extractAnalysis(Document page) {
        return page.select( ANALYSIS_PATH ).first().text().trim();
    }

}
