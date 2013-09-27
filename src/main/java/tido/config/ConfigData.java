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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Andrea Cisternino
 */
@XmlRootElement( name = "config" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ConfigData {

    @XmlElement( name = "base-dir" )
    private String baseDirectory;

    //---- Acessors ----------------------------------------------------------------

    public String getBaseDirectory() {
        return baseDirectory;
    }
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    //---- Support methods ---------------------------------------------------------

    @Override
    public String toString() {
        return "ConfigData{" + "baseDirectory=" + baseDirectory + '}';
    }

}
