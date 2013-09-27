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

import org.junit.Before;
import org.junit.Test;
import tido.config.ServerInfo;

import static org.junit.Assert.*;

/**
 *
 * @author Andrea Cisternino
 */
public class PageParserTest {

    private static final String TITLE_W_KPM = "TeamForge : artf73126: [KPM] DAB:PTY falsch angezeigt über Ensemble reconfig [5898943]";
    private static final String TITLE_WO_KPM = "TeamForge : artf73126: [KPM] DAB:PTY falsch angezeigt über Ensemble reconfig";

    private EbPageParser instance;

    //---- Lifecycle ---------------------------------------------------------------

    @Before
    public void setUp() {
        ServerInfo si = new ServerInfo();
        si.setId( "EB" );
        instance = (EbPageParser) BasePageParser.create( si );
    }

    //---- Artifact ID ----------------

    @Test
    public void testExtractArtifactId() {
        String expResult = "artf73126";
        String result = instance.extractArtifactId( TITLE_W_KPM );

        assertEquals( expResult, result );
    }

    //---- KPM number -----------------

    @Test
    public void testExtractKpmWithKpm() {
        long expResult = 5898943L;
        long result = instance.extractKpm( TITLE_W_KPM );

        assertEquals( expResult, result );
    }

    @Test
    public void testExtractKpmWithoutKpm() {
        long expResult = 0L;
        long result = instance.extractKpm( TITLE_WO_KPM );

        assertEquals( expResult, result );
    }

    //---- Title ----------------------

    @Test
    public void testExtractTitleWithKpm() {
        String expResult = "[KPM] DAB:PTY falsch angezeigt über Ensemble reconfig";
        String result = instance.extractTitle( TITLE_W_KPM );

        assertEquals( expResult, result );
    }

    @Test
    public void testExtractTitleWithoutKpm() {
        String expResult = "[KPM] DAB:PTY falsch angezeigt über Ensemble reconfig";
        String result = instance.extractTitle( TITLE_WO_KPM );

        assertEquals( expResult, result );
    }

    /**
     * Test of parse method, of class PageParser.
    @Test
    public void testParse() {
        System.out.println( "parse" );
        PageParser instance = null;
        Ticket expResult = null;
        Ticket result = instance.parse();
        assertEquals( expResult, result );
        // TODO review the generated test code and remove the default call to fail.
        fail( "The test case is a prototype." );
    }
     */

}