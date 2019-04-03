# smart-library-android

Add configuration.xml file and create directories for every flavor for building.

- configuration.xml -> /app/src/prodContentFull/configuration.xml
- configuration.xml -> /app/src/prodContentPart/configuration.xml
- configuration.xml -> /app/src/stageContentFull/configuration.xml
- configuration.xml -> /app/src/stageContentPart/configuration.xml

Example of configuration.xml file


    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="CLIENT_ID">client_id</string>
        <string name="CLIENT_SECRET">client_secret</string>
        <string name="USERNAME">username</string>
        <string name="PASSWORD">password</string>
        <string name="GRAND_TYPE">grand_type</string>
    </resources>

