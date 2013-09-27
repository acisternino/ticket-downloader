TiDoFx Ticket Downloader
========================

TiDoFx is a small JavaFX application that can automatically download all the
attachments of a TeamForge ticket into a user-definable directory.

TiDoFx require a recent Java 1.7 JRE to run (at least 1.7.0_u25).

> TiDoFx is still in Alpha state!!
> Please report bugs to [a.cisternino@gmail.com](mailto:a.cisternino@gmail.com)

![image](screenshot.png "TiDoFx screenshot")


Installation
------------

Until I implement a better solution, a manual step is required for the
application to work properly.

Credentials for the TeamForge servers known to the user must be manually
entered in a configuration file named `servers.xml`.

Here is an example of this file:

    <?xml version="1.0" encoding="UTF-8"?>
    <servers>
        <server>
            <!-- Simple string identifying the server -->
            <id>TF</id>

            <!-- Longer name for the server -->
            <name>Internal TF server</name>

            <!-- Root URL of the server -->
            <url>https://tf.example.com</url>

            <!-- Username -->
            <username>username</username>

            <!-- Password -->
            <password>password</password>
        </server>

        <!-- Add other server elements here -->
    </servers>

Once created, this file should be copied to the local application configuration
directory:

* `%APPDATA%\TiDoFx` on Windows (e.g. `C:\Users\JoeUser\AppData\Roaming\TiDoFx`).
* `~/.tidofx` on Linux.

In any case this directory must be created by hand.

Future versions of the application will streamline this entire process.


Installation and Usage
----------------------

1. Unzip the archive somewhere on your hard disk.
1. Start the application double clicking on _"Ticket Downloader.exe"_
1. Drag&drop ticket links from the TeamForge page onto the central list.
1. Select the base directory for the tickets clicking on the _Choose_ button.
1. Click on the _Fetch_ button.

All the attachments of the tickets in the list will be saved in a number of
directories located below the base directory chosen in step 2.

The current naming convention for this directory is:

> *"[artifact Id]_[title]"* in lower case

All punctuation and illegal characters are removed from the final name.


Build
-----

Tools needed:

* Java 1.7 JDK
* Gradle (not strictly necessary)

The project uses [Gradle] as build system but, to simplify development,
the Gradle Wrapper is supported. If you don't want to install Gradle, just use
the provided `gradlew` command like you would use the regular `gradle` command.

### Packaging ###

From the top of the source tree issue the following command:

    gradlew clean jfxDeploy

Once the process is terminated just zip the entire
`build/distributions/bundles/Ticket Downloader` directory.

### Eclipse support ###

I am sorry but I don't use Eclipse to develop TiDoFx. Patches and comments
are welcome.

[Gradle]: http://www.gradle.org/


License
-------

TiDoFx is licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
