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
package tido.model;

import java.net.URL;

/**
 * This class contains all the information needed to download an attachment from TeamForge.
 *
 * @author Andrea Cisternino
 */
public class DownloadableLink
{
    private URL url;
    private String name;
    private String cookies;

    public static DownloadableLink create(URL url, String name, String cookies) {
        return new DownloadableLink( url, name, cookies );
    }

    private DownloadableLink(URL url, String name, String cookies) {
        this.url = url;
        this.name = name;
        this.cookies = cookies;
    }

    public URL getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getCookies() {
        return cookies;
    }

}
