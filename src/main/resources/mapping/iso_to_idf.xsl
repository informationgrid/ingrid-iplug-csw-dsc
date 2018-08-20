<?xml version="1.0"?>
<!--
  **************************************************-
  ingrid-iplug-csw-dsc:war
  ==================================================
  Copyright (C) 2014 - 2018 wemove digital solutions GmbH
  ==================================================
  Licensed under the EUPL, Version 1.1 or – as soon they will be
  approved by the European Commission - subsequent versions of the
  EUPL (the "Licence");
  
  You may not use this work except in compliance with the Licence.
  You may obtain a copy of the Licence at:
  
  http://ec.europa.eu/idabc/eupl5
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the Licence is distributed on an "AS IS" basis,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the Licence for the specific language governing permissions and
  limitations under the Licence.
  **************************************************#
  -->

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:idf="http://www.portalu.de/IDF/1.0"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gml="http://www.opengis.net/gml">
	<xsl:output method="xml" />

  <xsl:template match="/">
    <xsl:apply-templates select="@*|node()"/>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="gmd:MD_Metadata">
    <idf:idfMdMetadata>
      <xsl:apply-templates select="@*|node()" />
    </idf:idfMdMetadata>
  </xsl:template>

  <xsl:template match="gmd:CI_ResponsibleParty">
    <idf:idfResponsibleParty>
      <xsl:apply-templates select="@*|node()" />
    </idf:idfResponsibleParty>
  </xsl:template>

<!-- GeoKatalog.WSV: Remove gmd:CI_OnlineResource of special type "localZipDownload" -->
<!-- see https://redmine.wemove.com/issues/1745 / "AF-00448 GP4: GeoKatalog - iPlug - Download Link anzeigen" -->
  <xsl:template match="gmd:transferOptions[gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode[@codeListValue='localZipDownload']]"/>

</xsl:stylesheet>
