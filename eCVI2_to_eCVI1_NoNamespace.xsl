<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:v2="http://www.usaha.org/xmlns/ecvi2"
    version="1.0">
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
    
    <xsl:template match="v2:eCVI">
        <xsl:element name="eCVI">
            <xsl:apply-templates select="./@* | ./node()"/>
        </xsl:element>
    </xsl:template>

    <!-- Change namespace on consistent elements -->
    <xsl:template
        match="
            v2:Veterinarian | v2:MovementPurposes
            | v2:Origin | v2:Destination
            | v2:Accessions | v2:Accession | v2:Laboratory | v2:LabName
            | v2:PremId | v2:PremName | v2:Line1
            | v2:Line2 | v2:Town | v2:County | v2:State | v2:ZIP | v2:Country | v2:GeoPoint
            | v2:Person | v2:Name | v2:Phone | v2:Consignor | v2:Consignee">
        <xsl:element name="{local-name()}" >
            <xsl:apply-templates select="./@* | ./node()"/>
        </xsl:element>
    </xsl:template>

    <!-- Remove unsupported new attributes and elements -->
    <xsl:template match="@LicenseState"/>
    <xsl:template match="@CviNumberIssuedBy"/>

    <xsl:template match="v2:Email"/>
    <xsl:template match="v2:OtherReason"/>
    <xsl:template match="v2:Statements"/>
    <xsl:template match="v2:MiscAttribute"/>
    <xsl:template match="v2:Binary"/>
    <!-- Values pulled from referenced location -->

    <!-- Refactor remaining if possible -->
    <xsl:template match="v2:NameParts">
        <xsl:element  name="Name">
            <xsl:if test="v2:BusinessName">
                <xsl:value-of select="v2:BusinessName"/>
                <xsl:text>, </xsl:text>
            </xsl:if>
            <xsl:value-of select="v2:FirstName"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="v2:MiddleName"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="v2:LastName"/>
            <xsl:if test="v2:OtherName">
                <xsl:text>, </xsl:text>
                <xsl:value-of select="v2:OtherName"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <!-- Provide defaults for missing required elements. -->

    <xsl:template match="v2:Address">
        <xsl:element name="Address">
            <xsl:element  name="Line1">
                <xsl:choose>
                    <xsl:when test="./v2:Line1">
                        <xsl:value-of select="./v2:Line1"/>
                    </xsl:when>
                    <xsl:otherwise>No Line 1</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:if test="./v2:Line2">
                <xsl:element  name="Line2">
                    <xsl:value-of select="./v2:Line2"/>
                </xsl:element>
            </xsl:if>
            <xsl:element  name="Town">
                <xsl:choose>
                    <xsl:when test="./v2:Town">
                        <xsl:value-of select="./v2:Town"/>
                    </xsl:when>
                    <xsl:otherwise>No Town</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:if test="./v2:County">
                <xsl:element  name="County">
                    <xsl:value-of select="./v2:County"/>
                </xsl:element>
            </xsl:if>
            <xsl:element  name="State">
                <xsl:choose>
                    <xsl:when test="./v2:State">
                        <xsl:value-of select="./v2:State"/>
                    </xsl:when>
                    <xsl:otherwise>No State</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:element  name="ZIP">
                <xsl:choose>
                    <xsl:when test="./v2:ZIP">
                        <xsl:value-of select="./v2:ZIP"/>
                    </xsl:when>
                    <xsl:otherwise>00000</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:if test="./v2:Country">
                <xsl:element  name="Country">
                    <xsl:value-of select="./v2:Country"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="./v2:GeoPoint">
                <xsl:element  name="GeoPoint">
                    <xsl:attribute name="lat">
                        <xsl:value-of select="./v2:GeoPoint/@Latitude"/>
                    </xsl:attribute>
                    <xsl:attribute name="lng">
                        <xsl:value-of select="./v2:GeoPoint/@Longitude"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>


    <xsl:template match="v2:AddressBlock">
        
        <xsl:element  name="Address">
            <xsl:choose>
                <xsl:when test="/v2:eCVI/@CviNumberIssuedBy='AGV'">
                    <xsl:call-template name="agViewAddressBlock">
                        <xsl:with-param name="block" select="."/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="otherAddressBlock">
                        <xsl:with-param name="block" select="."/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:element>
     </xsl:template>
     
    <xsl:template name="agViewAddressBlock">
        <xsl:param name="block"/>
        <xsl:variable name="line1" select="substring-before($block, ', ')"/>
        <xsl:variable name="rest1" select="substring-after($block, ', ')"/>
        <xsl:variable name="town" select="substring-before($rest1, ', ')"/>
        <xsl:variable name="rest2" select="substring-after($rest1, ', ')"/>
        <xsl:variable name="state" select="substring-before($rest2, ', ')"/>
        <xsl:variable name="rest3" select="substring-after($rest2, ', ')"/>
        <xsl:variable name="rest4" select="substring-after($rest3, ', ')"/>
        <xsl:variable name="zip" select="$rest4"/>
        
        <xsl:element name="Line1"><xsl:value-of select="$line1"/></xsl:element>
        <xsl:element name="Town"><xsl:value-of select="$town"/></xsl:element>
        <xsl:element name="State"><xsl:value-of select="$state"/></xsl:element>
        <xsl:element name="Zip"><xsl:value-of select="$zip"/></xsl:element>
        
    </xsl:template>

    <xsl:template name="otherAddressBlock">
        <xsl:param name="block"/>
        <xsl:variable name="line1" select="substring-before($block, '&#xa;')"/>
        <xsl:variable name="line2" select="substring-after($block, '&#xa;')"/>
        <xsl:variable name="aline1" select="substring-before($block, ', ')"/>
        <xsl:variable name="aline2" select="substring-after($block, ', ')"/>
        <xsl:variable name="town" select="substring-before($line2, ',')"/>
        <xsl:variable name="rest" select="substring-after($line2, ', ')"/>
        <xsl:variable name="atown" select="substring-before($aline2, ',')"/>
        <xsl:variable name="arest" select="substring-after($aline2, ', ')"/>
        <xsl:variable name="state" select="substring($rest, 1, 2)"/>
        <xsl:variable name="zip" select="substring($block, string-length($block)-10)"/>
        <xsl:variable name="astate" select="substring($arest, 1, 2)"/>
        <xsl:variable name="azip" select="substring(substring-after($arest, ' '), 1, 5)"/>
        
        <xsl:element  name="Line1">
            <xsl:choose>
                <xsl:when test="contains($block, '&#xa;')">
                    <xsl:value-of select="substring(normalize-space($line1),1,50)"/>
                </xsl:when>
                <xsl:when test="contains($block, ',')">
                    <xsl:value-of select="substring(normalize-space($aline1),1,50)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring(normalize-space($block),1,50)"/>                        
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
        <xsl:element  name="Line2">
            <xsl:choose>
                <xsl:when test="contains($block, '&#xa;')">
                    <xsl:value-of select="substring(normalize-space($line2),1,50)"/>
                </xsl:when>
                <xsl:when test="contains($block, ', ')">
                    <xsl:value-of select="substring(normalize-space($aline2),1,50)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring(normalize-space($block),50,50)"/>                        
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
        <xsl:element  name="Town">
        </xsl:element>
        <xsl:element  name="State">AA</xsl:element>
        <xsl:element  name="ZIP">
            <xsl:choose>
                <xsl:when test="string(number(substring($zip,7,5))) = substring($zip,7,5)
                    and not(contains($zip,'-'))">
                    <xsl:value-of select="substring($zip,7,5)"/>
                </xsl:when>
                <xsl:otherwise>00000</xsl:otherwise>
            </xsl:choose>
        </xsl:element>
        
    </xsl:template>
    <xsl:template match="v2:MovementPurpose">
        <xsl:element  name="MovementPurpose">
            <xsl:call-template name="MovementPurpose">
                <xsl:with-param name="purpose" select="."/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@Latitude">
        <xsl:attribute name="lat">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="@Longitude">
        <xsl:attribute name="lng">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <!-- TODO Significant Translation -->
    <xsl:template match="v2:StateZoneOrAreaStatus"/>
    <xsl:template match="v2:HerdOrFlockStatus"/>
    <xsl:template match="v2:Carrier"/>
    <xsl:template match="v2:TransportMode"/>


    <xsl:template match="v2:Field">
        <xsl:element  name="Laboratory">
            <xsl:attribute name="AccessionDate">
                <xsl:value-of select="@AccessionDate"/>
            </xsl:attribute>
            <xsl:attribute name="AccessionNumber">InField</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="v2:Animal">
        <xsl:element  name="Animal">
            <xsl:if test="./@Age">
                <xsl:if test="./@Age">
                    <xsl:attribute name="Age">
                        <xsl:value-of select="./@Age"/>
                    </xsl:attribute>
                </xsl:if>
            </xsl:if>
            <xsl:attribute name="SpeciesCode">
                <xsl:choose>
                    <xsl:when test="v2:SpeciesCode/@Code">
                        <xsl:value-of select="v2:SpeciesCode/@Code"/>
                    </xsl:when>
                    <xsl:when test="v2:SpeciesOther/@Code">
                        <xsl:value-of select="v2:SpeciesOther/@Code"/>
                    </xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="Breed">
                <xsl:value-of select="normalize-space(translate(substring(@Breed, 1, 3), $lowercase, $uppercase))"/>
            </xsl:attribute>
            <xsl:attribute name="Sex">
                <xsl:call-template name="Sex">
                    <xsl:with-param name="sex" select="@Sex"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="InspectionDate">
                <xsl:value-of select="@InspectionDate"/>
            </xsl:attribute>
            <xsl:for-each
                select="
                    v2:AnimalTags/v2:AIN | v2:AnimalTags/v2:MfrRFID | v2:AnimalTags/v2:NUES9
                    | v2:AnimalTags/v2:NUES8 | v2:AnimalTags/v2:OtherOfficialID | v2:AnimalTags/v2:ManagementID">
                <xsl:variable name="type" select="name(.)"/>
                <xsl:variable name="number" select="./@Number"/>
                <xsl:element  name="AnimalTag">
                    <xsl:attribute name="Type">
                        <xsl:call-template name="TagType">
                            <xsl:with-param name="type" select="$type"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="Number">
                        <xsl:choose>
                            <xsl:when test="$number = ''">
                                <xsl:text>Not provided</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="./@Number"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="v2:AnimalTags/v2:BrandImage">
                <xsl:element  name="AnimalTag">
                    <xsl:attribute name="Type">BRAND-IMAGE</xsl:attribute>
                    <xsl:attribute name="Number">
                        <xsl:value-of select="./@Description"/>
                    </xsl:attribute>
                    <xsl:attribute name="BrandImage">
                        <xsl:variable name="IDRef" select="@BrandImageRef"/>
                        <xsl:value-of select="//v2:Binary[@ID = $IDRef]"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="v2:Test">
                <xsl:element  name="Test">
                    <xsl:attribute name="idref">
                        <xsl:value-of select="@AccessionRef"/>
                    </xsl:attribute>
                    <xsl:attribute name="TestCode">
                        <xsl:value-of select="@TestCode"/>
                    </xsl:attribute>
                    <xsl:for-each select="v2:Result">
                        <xsl:element  name="Result">
                            <xsl:attribute name="ResultName">
                                <xsl:value-of select="@ResultName"/>
                            </xsl:attribute>
                            <xsl:if test="./v2:ResultInteger">
                                <xsl:element  name="ResultInteger">
                                    <xsl:value-of select="./v2:ResultInteger"/>
                                </xsl:element>
                            </xsl:if>
                            <xsl:if test="./v2:ResultString">
                                <xsl:element  name="ResultString">
                                    <xsl:value-of select="./v2:ResultString"/>
                                </xsl:element>
                            </xsl:if>
                            <xsl:if test="./v2:ResultFloat">
                                <xsl:element  name="ResultFloat">
                                    <xsl:value-of select="./v2:ResultFloat"/>
                                </xsl:element>
                            </xsl:if>
                        </xsl:element>
                    </xsl:for-each>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template match="v2:GroupLot">
        <xsl:element  name="GroupLot">
            <xsl:attribute name="Quantity">
                <xsl:value-of select="@Quantity"/>
            </xsl:attribute>
            <xsl:attribute name="Unit">
                <xsl:value-of select="@Unit"/>
            </xsl:attribute>
            <xsl:attribute name="SpeciesCode">
                <xsl:choose>
                    <xsl:when test="v2:SpeciesCode/@Code">
                        <xsl:value-of select="v2:SpeciesCode/@Code"/>
                    </xsl:when>
                    <xsl:when test="v2:SpeciesOther/@Code">
                        <xsl:value-of select="v2:SpeciesOther/@Code"/>
                    </xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:if test="./@Age">
                <xsl:attribute name="Age">
                    <xsl:value-of select="./@Age"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:attribute name="Breed">
                <xsl:value-of select="normalize-space(translate(substring(@Breed, 1, 3), $lowercase, $uppercase))"/>
            </xsl:attribute>
            <xsl:attribute name="Sex">
                <xsl:call-template name="Sex">
                    <xsl:with-param name="sex" select="@Sex"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="Description">
                <xsl:value-of select="@Description"/>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="v2:Attachment">
        <xsl:variable name="ref" select="@AttachmentRef"/>
        <xsl:element  name="Attachment">
            <xsl:attribute name="DocType">
                <xsl:value-of select="@DocType"/>
            </xsl:attribute>
            <xsl:attribute name="MimeType">
                <xsl:choose>
                    <xsl:when test="//v2:Binary[@ID = $ref]/@MimeType">
                        <xsl:value-of select="//v2:Binary[@ID = $ref]/@MimeType"/>
                    </xsl:when>
                    <xsl:otherwise>application/octet-stream</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="Filename">
                <xsl:value-of select="@Filename"/>
            </xsl:attribute>
            <xsl:attribute name="Comment">
                <xsl:value-of select="@Comment"/>
            </xsl:attribute>
            <xsl:element  name="Payload">
                <xsl:value-of select="//v2:Binary[@ID = $ref]"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>


    <!-- Deal with Enumeration Changes -->
    <xsl:template name="MovementPurpose">
        <xsl:param name="purpose"/>
        <xsl:choose>
            <xsl:when test="$purpose = 'Racing'">race</xsl:when>
            <xsl:when test="$purpose = 'Exhibition/Show/Rodeo'">show</xsl:when>
            <xsl:when test="$purpose = 'Sale'">sale</xsl:when>
            <xsl:when test="$purpose = 'Slaughter'">slaughter</xsl:when>
            <xsl:when test="$purpose = 'Breeding'">breeding</xsl:when>
            <xsl:when test="$purpose = 'Medical Treatment'">medicalTreatment</xsl:when>
            <xsl:when test="$purpose = 'Companion Animal'">pet</xsl:when>
            <xsl:when test="$purpose = 'Competition'">other</xsl:when>
            <xsl:otherwise>other</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="TagType">
        <xsl:param name="type"/>
         <xsl:choose>
            <xsl:when test="$type = 'AMID'">AMID</xsl:when>
            <xsl:when test="$type = 'BT'">BT</xsl:when>
            <xsl:when test="$type = 'IMP'">IMP</xsl:when>
            <xsl:when test="$type = 'NAME'">NAME</xsl:when>
            <xsl:when test="$type = 'SGFLID'">SGFLID</xsl:when>
            <xsl:when test="$type = 'NPIN'">NPIN</xsl:when>
            <xsl:when test="$type = 'PINPLUS'">OTH</xsl:when>
            <xsl:when test="$type = 'TAT'">TAT</xsl:when>
            <xsl:when test="$type = 'OTHER'">OTH</xsl:when>
            <xsl:when test="$type = 'AIN'">N840RFID</xsl:when>
            <xsl:when test="$type = 'MfrRFID'">OTH</xsl:when>
            <xsl:when test="$type = 'NUES9'">NUES9</xsl:when>
            <xsl:when test="$type = 'NUES8'">NUES9</xsl:when>
            <xsl:when test="$type = 'OtherOfficialID'">OTH</xsl:when>
            <xsl:when test="$type = 'ManagementID'">MGT</xsl:when>
            <xsl:when test="$type = 'BrandImage'">BRAND-IMAGE</xsl:when>
            <xsl:when test="$type = 'EquineDescription'">OTH</xsl:when>
            <xsl:when test="$type = 'EquinePhotographs'">OTH</xsl:when>
            <xsl:otherwise>OTH</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="Sex">
        <xsl:param name="sex"/>
        <xsl:choose>
            <xsl:when test="$sex = 'Female'">Female</xsl:when>
            <xsl:when test="$sex = 'Male'">Male</xsl:when>
            <xsl:when test="$sex = 'Spayed Female'">Spayed Female</xsl:when>
            <xsl:when test="$sex = 'Neutered Male'">Neutered Male</xsl:when>
            <xsl:when test="$sex = 'True Hermaphrodite'">True Hermaphrodite</xsl:when>
            <xsl:when test="$sex = 'Mixed Group'">Other</xsl:when>
            <xsl:otherwise>Other</xsl:otherwise>
        </xsl:choose>
    </xsl:template>



</xsl:stylesheet>
