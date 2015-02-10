CSW iPlug
========

The CSW-iPlug connects a Catalog Service Web Interface to the InGrid data space.

Features
--------

- harvests and indexes any Catalog Service Web Interface at a certain schedule
- flexible indexing functionality (script for indexing and detail data generation)
- provides search functionality on the indexed data
- GUI for easy administration


Requirements
-------------

- a running InGrid Software System

Installation
------------

Download from https://dev.informationgrid.eu/ingrid-distributions/ingrid-iplug-csw-dsc/
 
or

build from source with `mvn package assembly:single`.

Execute

```
java -jar ingrid-iplug-csw-dsc-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at http://www.ingrid-oss.eu/ (sorry only in German)


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-iplug-csw-dsc/issues
- Source Code: https://github.com/informationgrid/ingrid-iplug-csw-dsc
 
### Set up eclipse project

```
mvn eclipse:eclipse
```

and import project into eclipse.

### Debug under eclipse

- execute `mvn install` to expand the base web application
- set up a java application Run Configuration with start class `de.ingrid.iplug.csw.dsc.CswDscSearchPlug`
- add the VM argument `-Djetty.webapp=src/main/webapp` to the Run Configuration
- add src/main/resources to class path
- the admin gui starts per default on port 8082, change this with VM argument `-Djetty.port=8083`

Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
