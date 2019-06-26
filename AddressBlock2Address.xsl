<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns="http://www.usaha.org/xmlns/ecvi2"
    xmlns:v2="http://www.usaha.org/xmlns/ecvi2" version="1.0"
    xmlns:exsl="http://exslt.org/common" >
    <!-- Copyright (C) 2018 Michael K Martin
        
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/. -->
    
    <xsl:output method="xml" indent="yes"/>
    <!-- Avoid tons of whitespace -->
    <xsl:strip-space elements="*"/>
    <!-- Simple case conversion stuff. Plus ignore digits and punctuation by converting to X -->
    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz0123456789.,;:'"/>
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZXXXXXXXXXXXXXX'"/>
    
    <!-- Start with generic identity logic.  Then follow with changes. -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="//v2:AddressBlock">
        <xsl:element name="Address">
            <xsl:element name="Line1">
                <xsl:value-of select="substring-before(., ',')"/>
            </xsl:element>
            <xsl:variable name="l2" select="substring-after(.,',')"/>
            <xsl:element name="Town">
                <xsl:value-of select="substring-before($l2, ',')"/>
            </xsl:element>
            <xsl:variable name="l3" select="translate(substring-after($l2,','), ' \t&#10;&#13;', '')"/>
            <xsl:variable name="countyOrstate" select="translate(substring-before($l3, ','), ' \t&#10;&#13;', '')"/>
            <xsl:choose>
                <xsl:when test="string-length($countyOrstate) = 2">
                    <xsl:element name="State">
                        <xsl:value-of select="$countyOrstate"/>
                    </xsl:element>
                    <xsl:variable name="l4" select="translate(substring-after($l3,','), ' \t&#10;&#13;', '')"/>
                    <xsl:variable name="l5" select="translate(substring-after($l4,','), ' \t&#10;&#13;', '')"/>
                    <xsl:element name="ZIP">
                        <xsl:value-of select="translate($l5, ' \t', '')"/>
                    </xsl:element>
                    <xsl:element name="Country">
                        <xsl:value-of select="translate(substring-before($l4, ','), ' \t&#10;&#13;', '')"/>
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element name="County">
                        <xsl:value-of select="$countyOrstate"/>
                    </xsl:element>
                    <xsl:variable name="l3b" select="translate(substring-after($l3,','), ' \t&#10;&#13;', '')"/>
                    <xsl:element name="State">
                        <xsl:value-of select="translate(substring-before($l3b,','), ' \t&#10;&#13;', '')"/>
                    </xsl:element>
                    <xsl:variable name="l4b" select="translate(substring-after($l3b,','), ' \t&#10;&#13;', '')"/>
                    <xsl:variable name="l5b" select="translate(substring-after($l4b,','), ' \t&#10;&#13;', '')"/>
                    <xsl:element name="ZIP">
                        <xsl:value-of select="translate($l5b, ' \t', '')"/>
                    </xsl:element>
                    <xsl:element name="Country">
                        <xsl:value-of select="translate(substring-before($l4b, ','), ' \t&#10;&#13;', '')"/>
                    </xsl:element>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>