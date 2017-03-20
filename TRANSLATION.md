## TRANSLATION

The original translation file is located [here](/web/src/main/java/conf/messages.properties). 

To test your translation file in serposcope, you must create a file named `conf/messages_en.properties` in the serposcope [data directory](https://serposcope.serphacker.com/doc/install.html#datadir). This file will temporary replace the original english translation for your tests.

Ubuntu/Debian the file should be here : 

`/var/lib/serposcope/conf/messages_en.properties`

Then starts serposcope, and be sure your lang is set to **en** (top right, Accounts > Preferences > lang).

Each time you edit this file, you must restart serposcope. 

To restart serposcope on Ubuntu/Debian : 

`sudo service serposcope restart`
