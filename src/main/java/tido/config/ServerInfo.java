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
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Andrea Cisternino
 */
@XmlRootElement( name = "server" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ServerInfo {

    public static final String TF_AUTH_KEY = "sf_auth";

    private String id;
    private String name;
    private String url;
    private String username;
    private String password;

    @XmlTransient
    private Map<String, String> session;

    @XmlTransient
    private String cookiesHeader;

    //---- Accessors ---------------------------------------------------------------

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getSession() {
        return session;
    }
    public void setSession(Map<String, String> session) {
        this.session = session;
        cookiesHeader = joinCookies( this.session );
    }

    public String getCookiesHeader() {
        return cookiesHeader;
    }

    //---- Support methods ---------------------------------------------------------

    /**
     * @return true if the server has a valid session.
     */
    public boolean isAuthenticated() {
       return ( getSession() != null && getSession().containsKey( TF_AUTH_KEY ) );
    }

    /**
     * Creates a String representation of the authentication cookies to be used in the HTTP
     * Request header.
     *
     * @param cookies the session cookies.
     * @return the HTTP Request "Cookies" header.
     */
    private String joinCookies(Map<String, String> cookies)
    {
        if ( cookies == null ) {
            return null;
        }

        StringBuilder cks = new StringBuilder();

        Iterator<Map.Entry<String, String>> it = cookies.entrySet().iterator();
        while ( it.hasNext() ) {
            Map.Entry<String, String> c = it.next();
            cks.append( String.format( "%s=%s", c.getKey(), c.getValue() ) );
            if ( !it.hasNext() ) {
                break;
            }
            cks.append( "; " );
        }

        return cks.toString();
    }

    @Override
    public String toString() {
        return "ServerInfo{" + "id=" + id + ", name=" + name + ", url=" + url + ", username=" + username + ", password=" + password + '}';
    }
}
