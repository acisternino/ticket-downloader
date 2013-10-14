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
package tido.naming;

import mockit.Mocked;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tido.config.ConfigManager;
import tido.model.Ticket;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Andrea Cisternino
 */
public class JsTicketDirectoryNamerTest
{
    @Mocked
    private ConfigManager config;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getTicketPath method, of class JsTicketDirectoryNamer.
     */
    @Test
    public void testGetTicketPath() {
        System.out.println( "**** testGetTicketPath" );

        Ticket ticket = new Ticket( null );
        ticket.setId( "artf12345" );
        ticket.setTitle( " [KPM] [TV]: gets active/i äè ");

        // TODO reactivate when situation is more stable
//        JsTicketDirectoryNamer namer = new JsTicketDirectoryNamer( config );
//        namer.setBaseDir( "D:\\baseDir" );
//
//        String result = namer.getTicketPath( ticket ).toString();
//
//        System.out.println( "  path: " + result );
//        assertEquals( "D:\\baseDir\\artf12345_kpm_tv_gets_activei_äè", result.toString() );
    }

}
