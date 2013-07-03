Hier Spezialitäten bzgl. Mapping im WSV iPlug:
    Geodatenkatalog der Wasser- und Schifffahrtsverwaltung (CSW DSC)
    /ingrid-group:iplug-csw-dsc-wsv 

MUSS bei Update des entsprechenden iPlugs BERÜCKSICHTIGT WERDEN !!!
s. auch in-Step Ticket:
    AF-00146 WSV-CSW Download Links anpassen
bzw. Jira:
    https://dev2.wemove.com/jira/browse/GEOPORTALWSV-39

Hier auch noch mal aus dem Ticket:

----------------------------------
Martin Maidhof, 20.03.2013 18:23

Wurde jetzt mit der 3.3 Version neu installiert und komplett umkonfiguriert, so dass nach dem normalen Mapping zusätzliche Mapper für die WSV eingebunden werden können, die hier z.B. die URLs prüfen und ersetzen. Dafür wurde das CSW-DSC iPlug entsprechend erweitert (in den Sourcen).

Suche nach allen Datensätzen bei denen die URL ersetzt wurde mit:

iplugs:"/ingrid-group:iplug-csw-dsc-wsv" t017_url_ref.url_link:"http://geokat.wsv.bvbs.bund.de"

Doku auch in:
https://itpedia.dlz-it.bvbs.bund.de/confluence/display/GDIGIS/Handbuch+-+GeoPortal.WSV+%28ingrid%29#Handbuch-GeoPortal.WSV%28ingrid%29-iPlugCSWDSC