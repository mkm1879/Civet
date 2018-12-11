<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns="http://www.usaha.org/xmlns/ecvi2"
    xmlns:v1="http://www.usaha.org/xmlns/ecvi" version="1.0"
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

    <xsl:template match="v1:eCVI">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="eCVI">
            <xsl:apply-templates select="./@* | ./node()"/>
            <xsl:for-each select="v1:Attachment">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Attachment">
                    <xsl:attribute name="AttachmentRef">A<xsl:number format="0000" level="any"/>
                    </xsl:attribute>
                    <xsl:attribute name="DocType">
                        <xsl:value-of select="@DocType"/>
                    </xsl:attribute>
                    <xsl:attribute name="Filename">
                        <xsl:value-of select="@Filename"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:for-each>
            <xsl:for-each select="v1:Attachment">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Binary">
                    <xsl:attribute name="ID">A<xsl:number format="0000" level="any"/>
                    </xsl:attribute>
                    <xsl:attribute name="MimeType">
                        <xsl:value-of select="@MimeType"/>
                    </xsl:attribute>
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Payload">
                        <xsl:value-of select="v1:Payload"/>
                    </xsl:element>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <!-- Change namespace on consistent elements -->
    <xsl:template
        match="
            v1:Veterinarian | v1:MovementPurposes
            | v1:Origin | v1:Destination
            | v1:PremId | v1:PremName | v1:Line1
            | v1:Line2 | v1:Town | v1:County | v1:State | v1:ZIP | v1:Country | v1:GeoPoint
            | v1:Person | v1:Name | v1:Phone | v1:Consignor | v1:Consignee">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="{local-name()}">
            <xsl:apply-templates select="./@* | ./node()"/>
        </xsl:element>
    </xsl:template>

    <!-- Remove unsupported new attributes and elements -->
    <xsl:template match="@LicenseIssueState"/>
    <xsl:template match="@CviNumberIssuedBy"/>
    <xsl:template match="@SpeciesCode"/>
    <!-- Consume the automatic apply templates since we handle these in for-each above. -->
    <xsl:template match="v1:Attachment"/>

    <!-- Values pulled from referenced location -->
    <!-- Provide defaults for missing required elements. -->

    <xsl:template match="v1:MovementPurpose">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="MovementPurpose">
            <xsl:call-template name="MovementPurpose">
                <xsl:with-param name="purpose" select="."/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template match="v1:Origin/v1:Address">
        <xsl:call-template name="FullAddress">
            <xsl:with-param name="parent">
                <xsl:value-of select="."/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="v1:Destination/v1:Address">
        <xsl:call-template name="FullAddress">
            <xsl:with-param name="parent">
                <xsl:value-of select="."/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="v1:Consignor/v1:Address">
        <xsl:call-template name="BlockedAddress">
            <xsl:with-param name="parent">
                <xsl:value-of select="."/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="v1:Consignee/v1:Address">
        <xsl:call-template name="BlockedAddress">
            <xsl:with-param name="parent">
                <xsl:value-of select="."/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template match="v1:Veterinarian/v1:Address">
        <xsl:call-template name="BlockedAddress">
            <xsl:with-param name="parent">
                <xsl:value-of select="."/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="FullAddress">
        <xsl:param name="parent"/>
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Address">
            <xsl:if test="exsl:node-set(exsl:node-set($parent))/v1:Line1">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Line1">
                    <xsl:value-of select="exsl:node-set(exsl:node-set($parent))/v1:Line1"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="exsl:node-set(exsl:node-set($parent))/v1:Line2">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Line2">
                    <xsl:value-of select="exsl:node-set(exsl:node-set($parent))/v1:Line2"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="exsl:node-set($parent)/v1:Town">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Line2">
                    <xsl:value-of select="exsl:node-set($parent)/v1:Town"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="exsl:node-set($parent)/v1:County">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="County">
                    <xsl:value-of select="exsl:node-set($parent)/v1:County"/>
                </xsl:element>
            </xsl:if>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="State">
                <xsl:choose>
                    <xsl:when test="exsl:node-set($parent)/v1:State">
                        <xsl:value-of select="exsl:node-set($parent)/v1:State"/>
                    </xsl:when>
                    <xsl:otherwise>AA</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:if test="exsl:node-set($parent)/v1:ZIP">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Line2">
                    <xsl:value-of select="exsl:node-set($parent)/v1:ZIP"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="exsl:node-set($parent)/v1:Country">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Country">
                    <xsl:value-of select="exsl:node-set($parent)/v1:Country"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="exsl:node-set($parent)/v1:GeoPoint">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="GeoPoint">
                    <xsl:attribute name="Latitude">
                        <xsl:value-of select="exsl:node-set($parent)/v1:GeoPoint/@lat"/>
                    </xsl:attribute>
                    <xsl:attribute name="Longitude">
                        <xsl:value-of select="exsl:node-set($parent)/v1:GeoPoint/@lng"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template name="BlockedAddress">
        <xsl:param name="parent"/>
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="AddressBlock">
             <xsl:if test="exsl:node-set($parent)/v1:Line1">
                <xsl:value-of select="exsl:node-set($parent)/v1:Line1"/>
            </xsl:if>
            <xsl:text>, </xsl:text>
            <xsl:if test="exsl:node-set($parent)/v1:Line2">
                <xsl:value-of select="exsl:node-set($parent)/v1:Line2"/>
            </xsl:if>
            <xsl:text>, </xsl:text>
            <xsl:choose>
                <xsl:when test="exsl:node-set($parent)/v1:Town">
                    <xsl:value-of select="exsl:node-set($parent)/v1:Town"/>
                </xsl:when>
                <xsl:otherwise>No Town</xsl:otherwise>
            </xsl:choose>
            <xsl:text>, </xsl:text>
            <xsl:if test="exsl:node-set($parent)/v1:County">
                <xsl:value-of select="exsl:node-set($parent)/v1:County"/>
            </xsl:if>
            <xsl:text>, </xsl:text>
            <xsl:choose>
                <xsl:when test="exsl:node-set($parent)/v1:State">
                    <xsl:value-of select="exsl:node-set($parent)/v1:State"/>
                </xsl:when>
                <xsl:otherwise>No State</xsl:otherwise>
            </xsl:choose>
            <xsl:text>, </xsl:text>
            <xsl:choose>
                <xsl:when test="exsl:node-set($parent)/v1:ZIP">
                    <xsl:value-of select="exsl:node-set($parent)/v1:ZIP"/>
                </xsl:when>
                <xsl:otherwise>00000</xsl:otherwise>
            </xsl:choose>
            <xsl:if test="exsl:node-set($parent)/v1:Country">
                <xsl:text>, </xsl:text>
                <xsl:value-of select="exsl:node-set($parent)/v1:Country"/>
            </xsl:if>
            <xsl:if test="exsl:node-set($parent)/v1:GeoPoint">
                <xsl:text>, </xsl:text>
                <xsl:value-of select="exsl:node-set($parent)/v1:GeoPoint/@lat"/>
                <xsl:text>, </xsl:text>
                <xsl:value-of select="exsl:node-set($parent)/v1:GeoPoint/@lng"/>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@lat">
        <xsl:attribute name="Latitude">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="@lng">
        <xsl:attribute name="Longitude">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
    <!-- TODO Significant Translation -->
    <xsl:template match="v1:StateZoneOrAreaStatus"/>
    <xsl:template match="v1:HerdOrFlockStatus"/>
    <xsl:template match="v1:Carrier"/>
    <xsl:template match="v1:TransportMode"/>

    <xsl:template match="v1:Accessions">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Accessions">
            <xsl:for-each select="v1:Accession">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Accession">
                    <xsl:choose>
                        <xsl:when test="@InfieldTest = 'false'">
                            <xsl:attribute name="InfieldTest">false</xsl:attribute>
                            <xsl:attribute name="id">
                                <xsl:value-of select="@id"/>
                            </xsl:attribute>
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Laboratory">
                                <xsl:attribute name="AccessionDate">
                                    <xsl:value-of select="v1:Laboratory/@AccessionDate"/>
                                </xsl:attribute>
                                <xsl:attribute name="AccessionNumber">
                                    <xsl:choose>
                                        <xsl:when test="v1:Laboratory/@AccessionDate">
                                            <xsl:value-of select="v1:Laboratory/@AccessionDate"/>
                                        </xsl:when>
                                        <xsl:otherwise>NotProvided</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="LabName">
                                    <xsl:choose>
                                        <xsl:when test="v1:Laboratory/v1:LabName">
                                            <xsl:value-of select="v1:Laboratory/v1:LabName"/>
                                        </xsl:when>
                                        <xsl:otherwise>NotProvided</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:element>
                                <xsl:if test="v1:Laboratory/v1:PremId">
                                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremId">
                                        <xsl:value-of select="v1:Laboratory/v1:PremId"/>
                                    </xsl:element>
                                </xsl:if>
                                 <xsl:call-template name="BlockedAddress">
                                    <xsl:with-param name="parent" select="v1:Laboratory/v1:Address"/>
                                </xsl:call-template>
                            </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="InfieldTest">true</xsl:attribute>
                            <xsl:attribute name="id">
                                <xsl:value-of select="@id"/>
                            </xsl:attribute>
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Field">
                                <xsl:attribute name="AccessionDate">
                                    <xsl:value-of select="v1:Laboratory/@AccessionDate"/>
                                </xsl:attribute>
                                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremId">
                                    <xsl:value-of select="v1:Laboratory/v1:PremId"/>
                                </xsl:element>
                                <xsl:call-template name="BlockedAddress">
                                    <xsl:with-param name="parent" select="v1:Laboratory/v1:Address"/>
                                </xsl:call-template>
                            </xsl:element>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template match="v1:Animal">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Animal">
            <xsl:if test="./@Age">
                <xsl:if test="./@Age">
                    <xsl:attribute name="Age">
                        <xsl:value-of select="./@Age"/>
                    </xsl:attribute>
                </xsl:if>
            </xsl:if>
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
            <xsl:choose>
                <xsl:when
                    test="./@SpeciesCode = 'AQU' or ./@SpeciesCode =  'BEF' or ./@SpeciesCode =  'BIS' or ./@SpeciesCode =  'BOV' or ./@SpeciesCode =  'CAM' or ./@SpeciesCode =  'CAN' or ./@SpeciesCode =  'CAP' or ./@SpeciesCode =  'CER' or ./@SpeciesCode =  'CHI' or ./@SpeciesCode =  'DAI' or ./@SpeciesCode =  'EQU' or ./@SpeciesCode =  'FEL' or ./@SpeciesCode =  'OVI' or ./@SpeciesCode =  'POR' or ./@SpeciesCode =  'TUR'">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="SpeciesCode">
                        <xsl:attribute name="Code">
                            <xsl:value-of select="./@SpeciesCode"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="SpeciesOther">
                        <xsl:attribute name="Code">
                            <xsl:value-of select="./@SpeciesCode"/>
                        </xsl:attribute>
                        <xsl:attribute name="Text">
                            <!-- ToDo: create list of reasonable text for other species in Civet -->
                            <xsl:value-of select="./@SpeciesCode"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:otherwise>
            </xsl:choose>
            <!-- End SpeciesCode vs. Other -->
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="AnimalTags">
                <xsl:for-each select="v1:AnimalTag">
                    <xsl:choose>
                        <xsl:when test="./@Type = 'AIN'">
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="AIN">
                                <xsl:attribute name="Number">
                                    <xsl:value-of select="./@Number"/>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:when>
                        <xsl:when test="./@Type = 'N840RFID'">
                            <xsl:choose>
                                <xsl:when test="starts-with(@Number, '840')">
                                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="AIN">
                                        <xsl:attribute name="Number">
                                            <xsl:value-of select="./@Number"/>
                                        </xsl:attribute>
                                    </xsl:element>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="MfrRFID">
                                        <xsl:attribute name="Number">
                                            <xsl:value-of select="./@Number"/>
                                        </xsl:attribute>
                                    </xsl:element>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="./@Type = 'NUES9'">
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="NUES9">
                                <xsl:attribute name="Number">
                                    <xsl:value-of select="./@Number"/>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:when>
                        <xsl:when test="./@Type = 'NUES8'">
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="NUES8">
                                <xsl:attribute name="Number">
                                    <xsl:value-of select="./@Number"/>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="OtherOfficialID">
                                <xsl:attribute name="Number">
                                    <xsl:value-of select="./@Number"/>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
                <!-- End each tag -->
            </xsl:element>
            <!-- End AnimalTags -->

            <xsl:for-each select="v1:Test">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Test">
                    <xsl:attribute name="AccessionRef">
                        <xsl:value-of select="./@idref"/>
                    </xsl:attribute>
                    <xsl:attribute name="TestCode">
                        <xsl:value-of select="./@TestCode"/>
                    </xsl:attribute>
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Result">
                        <xsl:attribute name="ResultName">
                            <xsl:value-of select="./v1:Result/@ResultName"/>
                        </xsl:attribute>
                        <xsl:choose>
                            <xsl:when test="v1:Result/v1:ResultInteger">
                                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="ResultInteger">
                                    <xsl:value-of select="v1:Result/v1:ResultInteger"/>
                                </xsl:element>
                            </xsl:when>
                            <xsl:when test="v1:Result/v1:ResultFloat">
                                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="ResultFloat">
                                    <xsl:value-of select="v1:Result/v1:ResultFloat"/>
                                </xsl:element>
                            </xsl:when>
                            <xsl:when test="v1:Result/v1:ResultString">
                                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="ResultString">
                                    <xsl:value-of select="v1:Result/v1:ResultString"/>
                                </xsl:element>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:element>
                </xsl:element>
            </xsl:for-each>
            <!-- End for each Test -->

        </xsl:element>
        <!-- End Animal -->
    </xsl:template>

    <xsl:template match="v1:GroupLot">
       <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="GroupLot">
            <xsl:attribute name="Quantity">
                <xsl:value-of select="@Quantity"/>
            </xsl:attribute>
            <xsl:attribute name="Unit">
                <xsl:value-of select="@Unit"/>
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
            <xsl:choose>
                <xsl:when
                    test="./@SpeciesCode = 'AQU' or ./@SpeciesCode =  'BEF' or ./@SpeciesCode =  'BIS' or ./@SpeciesCode =  'BOV' or ./@SpeciesCode =  'CAM' or ./@SpeciesCode =  'CAN' or ./@SpeciesCode =  'CAP' or ./@SpeciesCode =  'CER' or ./@SpeciesCode =  'CHI' or ./@SpeciesCode =  'DAI' or ./@SpeciesCode =  'EQU' or ./@SpeciesCode =  'FEL' or ./@SpeciesCode =  'OVI' or ./@SpeciesCode =  'POR' or ./@SpeciesCode =  'TUR'">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="SpeciesCode">
                        <xsl:attribute name="Code">
                            <xsl:value-of select="./@SpeciesCode"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="SpeciesOther">
                        <xsl:attribute name="Code">
                            <xsl:value-of select="./@SpeciesCode"/>
                        </xsl:attribute>
                        <xsl:attribute name="Text">
                            <!-- ToDo: create list of reasonable text for other species in Civet -->
                            <xsl:value-of select="./@SpeciesCode"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <!-- Deal with Enumeration Changes -->
    <xsl:template name="MovementPurpose">
        <xsl:param name="purpose"/>
        <xsl:choose>
            <xsl:when test="$purpose = 'race'">Racing</xsl:when>
            <xsl:when test="$purpose = 'show'">Exhibition/Show/Rodeo</xsl:when>
            <xsl:when test="$purpose = 'sale'">Sale</xsl:when>
            <xsl:when test="$purpose = 'slaughter'">Slaughter</xsl:when>
            <xsl:when test="$purpose = 'breeding'">Breeding</xsl:when>
            <xsl:when test="$purpose = 'medicalTreatment'">Medical Treatment</xsl:when>
            <xsl:when test="$purpose = 'pet'">Companion Animal</xsl:when>
            <xsl:otherwise>Other</xsl:otherwise>
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
