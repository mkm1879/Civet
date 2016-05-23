<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns="http://www.usaha.org/xmlns/ecvi" xmlns:ecvi="http://www.usaha.org/xmlns/ecvi"
    xmlns:my="http://www.clemson.edu/public/lph/StdECVI" xmlns:xfa="http://www.xfa.org/schema/xfa-data/1.0/">
    <xsl:output indent="yes" method="xml"/>
    <xsl:strip-space elements="*"/>
    <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

    <xsl:template match="/xfa:data/StyleSheet">
        <xsl:element name="eCVI">
            <xsl:attribute name="CviNumber">
                <xsl:text>MovePermit</xsl:text>
                <xsl:value-of select="PermitNumber"/>
            </xsl:attribute>
            <xsl:attribute name="IssueDate">
                <xsl:value-of select="DateApproved"/>
            </xsl:attribute>
            <xsl:attribute name="ExpirationDate">
                <xsl:value-of select="ShipmentDate"/>
            </xsl:attribute>
            <xsl:if test="ShipmentDate and ShipmentDate != ''">
                <xsl:attribute name="ShipmentDate">
                    <xsl:value-of select="ShipmentDate"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="PermitNumber and PermitNumber != ''">
                <xsl:attribute name="EntryPermitNumber">
                    <xsl:value-of select="PermitNumber"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:element name="Veterinarian">
                <xsl:element name="Person">
                    <xsl:element name="Name">
                        <xsl:text>Julie Helm</xsl:text>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
            <xsl:element name="MovementPurposes">
                <xsl:element name="MovementPurpose">
                    <xsl:call-template name="PurposeMap">
                        <xsl:with-param name="purpose" select="PurposeofMvmnt"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:element>
            <xsl:element name="Origin">
                <xsl:if test="OriginPID and OriginPID != ''">
                    <xsl:element name="PremId">
                        <xsl:value-of select="translate(OriginPID, $smallcase, $uppercase)"/>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="OriginBusinessName and OriginBusinessName != ''">
                    <xsl:element name="PremName">
                        <xsl:value-of select="OriginBusinessName"/>
                    </xsl:element>
                </xsl:if>
                <xsl:element name="Address">
                    <xsl:element name="Line1">
                        <xsl:value-of select="OriginAddress"/>
                    </xsl:element>
                    <xsl:element name="Town">
                        <xsl:value-of select="OriginCity"/>
                    </xsl:element>
                    <xsl:element name="State">
                        <xsl:value-of select="OriginState"/>
                    </xsl:element>
                    <xsl:element name="ZIP">
                        <xsl:value-of select="OrginZipCode"/>
                    </xsl:element>
                    <xsl:element name="Country">
                        <xsl:text>USA</xsl:text>
                    </xsl:element>
                </xsl:element>
                <xsl:element name="Person">
                    <xsl:element name="Name">
                        <xsl:choose>
                            <xsl:when test="OriginLastName != '' and OriginFirstName != ''">
                                <xsl:value-of select="OriginLastName"/>
                                <xsl:text>, </xsl:text>
                                <xsl:value-of select="OriginFirstName"/>
                            </xsl:when>
                            <xsl:when test="OriginLastName != '' and OriginFirstName = ''">
                                <xsl:value-of select="OriginLastName"/>
                            </xsl:when>
                            <xsl:when test="OriginLastName = '' and OriginFirstName != ''">
                                <xsl:value-of select="OriginFirstName"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                    <xsl:if test="OriginPhone and OriginPhone != ''">
                        <xsl:element name="Phone">
                            <xsl:attribute name="Type">Unknown</xsl:attribute>
                            <xsl:attribute name="Number">
                                <xsl:value-of select="OriginPhone"/>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:if>
                </xsl:element>
            </xsl:element>
            <xsl:element name="Destination">
                <xsl:if test="DestPID and DestPID != ''">
                    <xsl:element name="PremId">
                        <xsl:value-of select="translate(DestPID, $smallcase, $uppercase)"/>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="DestBusinessName and DestBusinessName != ''">
                    <xsl:element name="PremName">
                        <xsl:value-of select="DestBusinessName"/>
                    </xsl:element>
                </xsl:if>
                <xsl:element name="Address">
                    <xsl:element name="Line1">
                        <xsl:value-of select="DestAddress"/>
                    </xsl:element>
                    <xsl:element name="Town">
                        <xsl:value-of select="DestCity"/>
                    </xsl:element>
                    <xsl:element name="State">
                        <xsl:value-of select="DestState"/>
                    </xsl:element>
                    <xsl:element name="ZIP">
                        <xsl:value-of select="DestZipCode"/>
                    </xsl:element>
                    <xsl:element name="Country">
                        <xsl:text>USA</xsl:text>
                    </xsl:element>
                </xsl:element>
                <xsl:element name="Person">
                    <xsl:element name="Name">
                        <xsl:choose>
                            <xsl:when test="DestLastName != '' and DestFirstName != ''">
                                <xsl:value-of select="DestLastName"/>
                                <xsl:text>, </xsl:text>
                                <xsl:value-of select="DestFirstName"/>
                            </xsl:when>
                            <xsl:when test="DestLastName != '' and DestFirstName = ''">
                                <xsl:value-of select="DestLastName"/>
                            </xsl:when>
                            <xsl:when test="DestLastName = '' and DestFirstName != ''">
                                <xsl:value-of select="DestFirstName"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                    <xsl:if test="DestPhone and DestPhone != ''">
                        <xsl:element name="Phone">
                            <xsl:attribute name="Type">Unknown</xsl:attribute>
                            <xsl:attribute name="Number">
                                <xsl:value-of select="DestPhone"/>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:if>
                </xsl:element>
            </xsl:element>
            <xsl:element name="Consignor">
                <xsl:if test="OriginBusinessName and OriginBusinessName != ''">
                    <xsl:element name="PremName">
                        <xsl:value-of select="OriginBusinessName"/>
                    </xsl:element>
                </xsl:if>
                <xsl:element name="Address">
                    <xsl:element name="Line1">
                        <xsl:choose>
                            <xsl:when test="ConsignorAddress and ConsignorAddress != ''">
                                <xsl:value-of select="ConsignorAddress"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="OriginAddress"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                    <xsl:element name="Town">
                        <xsl:value-of select="OriginCity"/>
                    </xsl:element>
                    <xsl:element name="State">
                        <xsl:value-of select="OriginState"/>
                    </xsl:element>
                    <xsl:element name="ZIP">
                        <xsl:value-of select="OrginZipCode"/>
                    </xsl:element>
                    <xsl:element name="Country">
                        <xsl:text>USA</xsl:text>
                    </xsl:element>
                </xsl:element>
                <xsl:element name="Person">
                    <xsl:element name="Name">
                        <xsl:choose>
                            <xsl:when test="OriginLastName != '' and OriginFirstName != ''">
                                <xsl:value-of select="OriginLastName"/>
                                <xsl:text>, </xsl:text>
                                <xsl:value-of select="OriginFirstName"/>
                            </xsl:when>
                            <xsl:when test="OriginLastName != '' and OriginFirstName = ''">
                                <xsl:value-of select="OriginLastName"/>
                            </xsl:when>
                            <xsl:when test="OriginLastName = '' and OriginFirstName != ''">
                                <xsl:value-of select="OriginFirstName"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                    <xsl:if test="OriginPhone and OriginPhone != ''">
                        <xsl:element name="Phone">
                            <xsl:attribute name="Type">Unknown</xsl:attribute>
                            <xsl:attribute name="Number">
                                <xsl:value-of select="OriginPhone"/>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:if>
                </xsl:element>
            </xsl:element>

            <xsl:element name="Consignee">
                <xsl:if test="DestBusinessName and DestBusinessName != ''">
                    <xsl:element name="PremName">
                        <xsl:value-of select="DestBusinessName"/>
                    </xsl:element>
                </xsl:if>
                <xsl:element name="Address">
                    <xsl:element name="Line1">
                        <xsl:choose>
                            <xsl:when test="ConsigneeAddress and ConsigneeAddress != ''">
                                <xsl:value-of select="ConsigneeAddress"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="DestAddress"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                    <xsl:element name="Town">
                        <xsl:value-of select="DestCity"/>
                    </xsl:element>
                    <xsl:element name="State">
                        <xsl:value-of select="DestState"/>
                    </xsl:element>
                    <xsl:element name="ZIP">
                        <xsl:value-of select="DestZipCode"/>
                    </xsl:element>
                    <xsl:element name="Country">
                        <xsl:text>USA</xsl:text>
                    </xsl:element>
                </xsl:element>
                <xsl:element name="Person">
                    <xsl:element name="Name">
                        <xsl:choose>
                            <xsl:when test="DestLastName != '' and DestFirstName != ''">
                                <xsl:value-of select="DestLastName"/>
                                <xsl:text>, </xsl:text>
                                <xsl:value-of select="DestFirstName"/>
                            </xsl:when>
                            <xsl:when test="DestLastName != '' and DestFirstName = ''">
                                <xsl:value-of select="DestLastName"/>
                            </xsl:when>
                            <xsl:when test="DestLastName = '' and DestFirstName != ''">
                                <xsl:value-of select="DestFirstName"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                    <xsl:if test="DestPhone and DestPhone != ''">
                        <xsl:element name="Phone">
                            <xsl:attribute name="Type">Unknown</xsl:attribute>
                            <xsl:attribute name="Number">
                                <xsl:value-of select="DestPhone"/>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:if>
                </xsl:element>
            </xsl:element>
            <xsl:element name="Accessions">
                <xsl:if test="DateTest1 and DateTest1 != ''">
                    <xsl:element name="Accession">
                        <xsl:attribute name="id">AIPCR1</xsl:attribute>
                        <xsl:attribute name="InfieldTest">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:value-of select="DateTest1"/>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="AccNumberT1"/>
                            </xsl:attribute>
                            <xsl:element name="LabName">
                                <xsl:value-of select="LabT1"/>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="DateTest2 and DateTest2 != '' and AccNumberT2 != AccNumberT1">
                    <xsl:element name="Accession">
                        <xsl:attribute name="id">AIPCR2</xsl:attribute>
                        <xsl:attribute name="InfieldTest">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:value-of select="DateTest2"/>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="AccNumberT2"/>
                            </xsl:attribute>
                            <xsl:element name="LabName">
                                <xsl:value-of select="LabT2"/>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="DateTest3 and DateTest3 != '' and AccNumberT3 != AccNumberT1 and AccNumberT3 != AccNumberT2">
                    <xsl:element name="Accession">
                        <xsl:attribute name="id">AIPCR3</xsl:attribute>
                        <xsl:attribute name="InfieldTest">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:value-of select="DateTest2"/>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="AccNumberT2"/>
                            </xsl:attribute>
                            <xsl:element name="LabName">
                                <xsl:value-of select="LabT2"/>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
            </xsl:element>
            <xsl:element name="GroupLot">
                <!-- Element Test would go here but standard does not include tests on groups -->
                <xsl:attribute name="Quantity">
                    <xsl:value-of select="NumbBirds"/>
                </xsl:attribute>
                <xsl:attribute name="SpeciesCode">
                    <xsl:call-template name="SpeciesMap">
                        <xsl:with-param name="species" select="BirdType"/>
                    </xsl:call-template>
                </xsl:attribute>
                <xsl:attribute name="Description">
                    <xsl:value-of select="BirdType"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="PurposeofMvmnt"/>
                </xsl:attribute>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="SpeciesMap">
        <xsl:param name="species"/>
        <xsl:choose>
            <xsl:when test="
                    substring(translate($species, $smallcase, $uppercase), 1, 6) =
                    translate('Turkey', $smallcase, $uppercase)">TUR</xsl:when>
            <xsl:when test="
                    substring(translate($species, $smallcase, $uppercase), 1, 4) =
                    translate('Duck', $smallcase, $uppercase)">DUC</xsl:when>
            <xsl:otherwise>CHI</xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="PurposeMap">
        <xsl:param name="purpose"/>
        <xsl:choose>
            <xsl:when test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('To Breeding Farm', $smallcase, $uppercase)">breeding</xsl:when>
            <xsl:when test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('To Commercial Farm', $smallcase, $uppercase)">feeding</xsl:when>
            <xsl:when test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('To Processing Plant', $smallcase, $uppercase)">slaughter</xsl:when>
            <xsl:when test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('To Distribution or Market', $smallcase, $uppercase)">sale</xsl:when>
            <xsl:otherwise>other</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
