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
package tido.config;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Andrea Cisternino
 */
@XmlRootElement( name = "servers" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ServerList
{
    @XmlElement( name = "server" )
    private List<ServerInfo> serverList;

    public List<ServerInfo> getServers() {
        return serverList;
    }
    public void setServers(List<ServerInfo> servers) {
        this.serverList = servers;
    }

    //---- Support methods ---------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( "ServerList{[" );
        Iterator<ServerInfo> it = serverList.iterator();
        while ( true ) {
            sb.append( it.next() );
            if ( !it.hasNext() ) {
                break;
            }
            sb.append( ", " );
        }
        return sb.append( "]}" ).toString();
    }
}
