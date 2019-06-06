<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns="http://www.usaha.org/xmlns/ecvi2" xmlns:ecvi="http://www.usaha.org/xmlns/ecvi"
    xmlns:my="http://www.clemson.edu/public/lph/StdECVI"
    xmlns:xfa="http://www.xfa.org/schema/xfa-data/1.0/">
    <xsl:output indent="yes" method="xml"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="eCVI">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="eCVI">
            <xsl:attribute name="CviNumber">
                <xsl:value-of select="certificate"/>
            </xsl:attribute>
            <xsl:attribute name="IssueDate">
                <xsl:value-of select="certDate"/>
            </xsl:attribute>
            <xsl:attribute name="ExpirationDate">
                <xsl:value-of select="certDate"/>
            </xsl:attribute>
            <xsl:if test="vetInspection/cviPG1/shipDate and vetInspection/cviPG1/shipDate != ''">
                <xsl:attribute name="ShipmentDate">
                    <xsl:value-of select="vetInspection/cviPG1/shipDate"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="entryPermit and entryPermit != ''">
                <xsl:attribute name="EntryPermitNumber">
                    <xsl:value-of select="entryPermit"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="vetInspection/vetCertification"/>
            <xsl:apply-templates select="vetInspection/cviPG1/carrier/purpose"/>
            <xsl:apply-templates select="vetInspection/cviPG1/consignor"/>
            <xsl:apply-templates select="vetInspection/cviPG1/consignee"/>
            <xsl:apply-templates select="vetInspection/cviPG1/consignor/ownerAdd"/>
            <xsl:apply-templates select="vetInspection/cviPG1/consignee/ownerAdd"/>
            <xsl:call-template name="Accessions"/>
            <xsl:if test="vetInspection/cviPG1/species/large/table/item/headCt != ''">
                <xsl:call-template name="LargeAnimal">
                    <xsl:with-param name="table" select="vetInspection/cviPG1/species/large/table"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="vetInspection/cviPG1/species/large/table/item/headCt != ''">
                <xsl:call-template name="LargeAnimalGroup">
                    <xsl:with-param name="table" select="vetInspection/cviPG1/species/large/table"/>
                </xsl:call-template>
            </xsl:if>
            <!-- Don't test for number, just assume one if not provided. -->
            <xsl:if
                test="
                    vetInspection/cviPG1/species/small/table/item/spp != ''
                    and vetInspection/cviPG1/species/small/table/item/rabiesTag != ''">
                <xsl:call-template name="SmallAnimal">
                    <xsl:with-param name="table" select="vetInspection/cviPG1/species/small/table"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if
                test="
                    vetInspection/cviPG1/species/small/table/item/headCt != ''
                    and vetInspection/cviPG1/species/small/table/item/rabiesTag = ''">
                <xsl:call-template name="SmallAnimalGroup">
                    <xsl:with-param name="table" select="vetInspection/cviPG1/species/small/table"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="eCVI/vetInspection/vetCertification">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Veterinarian">
            <xsl:if test="licenseNumber and licenseNumber != ''">
                <xsl:attribute name="LicenseNumber">
                    <xsl:value-of select="licenseNumber"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="usdaNumber and usdaNumber != ''">
                <xsl:attribute name="NationalAccreditationNumber">
                    <xsl:value-of select="usdaNumber"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                    <xsl:value-of select="../../printedName"/>
                </xsl:element>
                <xsl:if test="phoneNum and phoneNum != ''">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Phone">
                        <xsl:attribute name="Type">Unknown</xsl:attribute>
                        <xsl:attribute name="Number">
                            <xsl:value-of select="translate(phoneNum, ' ()-', '')"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
            </xsl:element>
            <xsl:call-template name="BlockedAddress">
                <xsl:with-param name="address" select="certAddress"/>
                <xsl:with-param name="city" select="certCity"/>
                <xsl:with-param name="state" select="certState"/>
                <xsl:with-param name="zip" select="certZip"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template match="eCVI/vetInspection/cviPG1/carrier/purpose">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="MovementPurposes">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="MovementPurpose">
                <xsl:call-template name="PurposeMap">
                    <xsl:with-param name="purpose" select="."/>
                </xsl:call-template>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="eCVI/vetInspection/cviPG1/consignor">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Origin">
            <xsl:call-template name="OriginDestination">
                <xsl:with-param name="data" select="."/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template match="eCVI/vetInspection/cviPG1/consignee">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Destination">
            <xsl:call-template name="OriginDestination">
                <xsl:with-param name="data" select="."/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template name="OriginDestination">
        <xsl:param name="data"/>
        <xsl:variable name="alphanumeric" select="'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'"/>
        <xsl:if test="$data/lid != '' and string-length($data/lid) &gt; 5 and string-length($data/lid) &lt; 9
            and string-length(translate($data/lid, $alphanumeric, '')) = 0 ">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremId">
                <xsl:value-of select="$data/lid"/>
            </xsl:element>
        </xsl:if>
        <xsl:if test="$data/busName and $data != ''">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremName">
                <xsl:value-of select="$data/busName"/>
            </xsl:element>
        </xsl:if>
        <xsl:call-template name="FullAddress">
            <xsl:with-param name="address" select="$data/address"/>
            <xsl:with-param name="city" select="$data/city"/>
            <xsl:with-param name="county" select="$data/county"/>
            <xsl:with-param name="state" select="$data/state"/>
            <xsl:with-param name="zip" select="$data/zipCode"/>
        </xsl:call-template>
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                <xsl:choose>
                    <xsl:when test="$data/lName != '' and $data/fName != ''">
                        <xsl:value-of select="$data/lName"/>
                        <xsl:text>, </xsl:text>
                        <xsl:value-of select="$data/fName"/>
                    </xsl:when>
                    <xsl:when test="$data/lName != '' and $data/fName = ''">
                        <xsl:value-of select="$data/lName"/>
                    </xsl:when>
                    <xsl:when test="$data/lName = '' and $data/fName != ''">
                        <xsl:value-of select="$data/fName"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:if test="$data/phoneNum and $data/phoneNum != ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Phone">
                    <xsl:attribute name="Type">Unknown</xsl:attribute>
                    <xsl:attribute name="Number">
                        <xsl:value-of select="translate($data/phoneNum, ' ()-', '')"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="eCVI/vetInspection/cviPG1/consignor/ownerAdd">
        <xsl:if test=". != ''">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Consignor">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                        <xsl:value-of select="."/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template match="eCVI/vetInspection/cviPG1/consignee/ownerAdd">
        <xsl:if test=". != ''">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Consignee">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                        <xsl:value-of select="."/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="LargeAnimal">
        <xsl:param name="table"/>
        <xsl:for-each select="$table/item">
            <xsl:variable name="head" select="headCt"/>
            <xsl:if test="number($head) = 1 and ./offID and ./offID != ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Animal">
                    <xsl:if test="./ageNum and ./ageNum != ''">
                        <xsl:attribute name="Age">
                            <xsl:call-template name="Age">
                                <xsl:with-param name="item" select="."/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="./breed and ./breed != ''">
                        <xsl:attribute name="Breed">
                            <xsl:call-template name="Breed">
                                <xsl:with-param name="species" select="spp"/>
                                <xsl:with-param name="breed" select="breed"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="Sex">
                        <xsl:call-template name="Sex">
                            <xsl:with-param name="sex" select="sex"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="InspectionDate">
                        <xsl:value-of select="../../../../inspDate"/>
                    </xsl:attribute>
                    <xsl:call-template name="AnimalSpecies">
                        <xsl:with-param name="item" select="."/>
                    </xsl:call-template>
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="AnimalTags">
                        <xsl:for-each select="./offID">
                            <xsl:choose>
                                <xsl:when test="starts-with(., '840')">
                                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2"
                                        name="AIN">
                                        <xsl:attribute name="Number">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                    </xsl:element>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2"
                                        name="OtherOfficialID">
                                        <xsl:attribute name="Number">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                    </xsl:element>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                        <!-- End each tag -->
                    </xsl:element>
                    <xsl:if test="./eiaResult != 'N/A' and ./eiaResult != ''">
                        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Test">
                            <xsl:attribute name="AccessionRef">EIA<xsl:value-of select="./itemIndex"
                                /></xsl:attribute>
                            <xsl:attribute name="TestCode">EIA</xsl:attribute>
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Result">
                                <xsl:attribute name="ResultName">RESULT</xsl:attribute>
                                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2"
                                    name="ResultString">
                                    <xsl:value-of select="./eiaResult"/>
                                </xsl:element>
                            </xsl:element>
                        </xsl:element>
                    </xsl:if>
                    <xsl:if test="./brucResult != 'N/A' and ./brucResult != ''">
                        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Test">
                            <xsl:attribute name="AccessionRef">BRUC<xsl:value-of
                                    select="./itemIndex"/></xsl:attribute>
                            <xsl:attribute name="TestCode">BRUC</xsl:attribute>
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Result">
                                <xsl:attribute name="ResultName">RESULT</xsl:attribute>
                                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2"
                                    name="ResultString">
                                    <xsl:value-of select="./brucResult"/>
                                </xsl:element>
                            </xsl:element>
                        </xsl:element>
                    </xsl:if>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="LargeAnimalGroup">
        <xsl:param name="table"/>
        <xsl:for-each select="$table/item">
            <xsl:variable name="head" select="headCt"/>
            <xsl:if test="number($head) > 1 or not(./offID) or ./offID = ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="GroupLot">
                    <xsl:attribute name="Quantity">
                        <xsl:value-of select="$head"/>
                    </xsl:attribute>
                    <xsl:if test="./ageNum and ./ageNum != ''">
                        <xsl:attribute name="Age">
                            <xsl:call-template name="Age">
                                <xsl:with-param name="item" select="."/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="Breed">
                        <xsl:call-template name="Breed">
                            <xsl:with-param name="species" select="spp"/>
                            <xsl:with-param name="breed" select="breed"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="Sex">
                        <xsl:call-template name="Sex">
                            <xsl:with-param name="sex" select="sex"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <!-- Why did we leave this out?
                    <xsl:attribute name="InspectionDate">
                        <xsl:value-of select="../../../../inspDate"/>
                    </xsl:attribute>
                    -->
                    <xsl:attribute name="Description">
                        <xsl:value-of select="description"/>
                    </xsl:attribute>
                    <xsl:call-template name="AnimalSpecies">
                        <xsl:with-param name="item" select="."/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="SmallAnimal">
        <xsl:param name="table"/>
        <xsl:for-each select="$table/item">
            <xsl:variable name="head" select="headCt"/>
            <xsl:if test="$head = '' or number($head) = 1">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Animal">
                    <!-- CO/KS Small Animal Age is unstructured so not reliably translateable -->
                    <xsl:attribute name="Sex">
                        <xsl:call-template name="Sex">
                            <xsl:with-param name="sex" select="sex"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="InspectionDate">
                        <xsl:value-of select="../../../../inspDate"/>
                    </xsl:attribute>
                    <xsl:call-template name="AnimalSpecies">
                        <xsl:with-param name="item" select="."/>
                    </xsl:call-template>
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="AnimalTags">
                        <xsl:call-template name="getMicrochip">
                            <xsl:with-param name="desc" select="description"/>
                        </xsl:call-template>
                        <xsl:if test="rabiesTag and not(rabiesTag = '')">
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2"
                                name="OtherOfficialID">
                                <xsl:attribute name="Number">
                                    <xsl:value-of select="rabiesTag"/>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:if>
                        <xsl:call-template name="getAllButMicrochip">
                            <xsl:with-param name="desc" select="description"/>
                        </xsl:call-template>
                        <xsl:call-template name="noSmallAnimalId">
                            <xsl:with-param name="rabiesTag" select="rabiesTag"/>
                            <xsl:with-param name="desc" select="description"/>
                        </xsl:call-template>
                    </xsl:element>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="SmallAnimalGroup">
        <xsl:param name="table"/>
        <xsl:for-each select="$table/item">
            <xsl:variable name="head" select="headCt"/>
            <xsl:if test="$head = '' or number($head) = 1">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="GroupLot">
                    <!-- CO/KS Small Animal Age is unstructured so not reliably translateable -->
                    <xsl:attribute name="Quantity">
                        <xsl:value-of select="translate($head, ',', '')"/>
                    </xsl:attribute>
                    <xsl:if test="sex and not(sex = '')">
                        <xsl:attribute name="Sex">
                            <xsl:call-template name="Sex">
                                <xsl:with-param name="sex" select="sex"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="Description">
                        <xsl:if test="not(description) or description = ''">No Description
                            Entered</xsl:if>
                        <xsl:value-of select="description"/>
                    </xsl:attribute>
                    <xsl:call-template name="AnimalSpecies">
                        <xsl:with-param name="item" select="."/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="noSmallAnimalId">
        <xsl:param name="desc"/>
        <xsl:param name="rabiesTag"/>
        <xsl:if test="(not($desc) or $desc = '') and (not($rabiesTag) or $rabiesTag = '')">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="ManagementID">
                    <xsl:attribute name="Number">CO/KS No ID</xsl:attribute>
                </xsl:element>
        </xsl:if>
        
    </xsl:template>

    <xsl:template name="getMicrochip">
        <xsl:param name="desc"/>
        <xsl:if test="$desc != '' and string-length($desc) >= 15">
            <xsl:variable name="last15" select="substring($desc, string-length($desc) - 14)"/>
            <xsl:variable name="first3" select="substring($last15,1,3)"/>
            <xsl:if test="string(number($last15)) != 'NaN' and (
                (substring($first3,1,1) = '9' and $first3 != '999') or $first3 = '124' or $first3 = '484' )"> 
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="MfrRFID">
                    <xsl:attribute name="Number">
                        <xsl:value-of select="$last15"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="getAllButMicrochip">
        <xsl:param name="desc"/>
        <xsl:if test="$desc != ''">
            <xsl:choose>
                <xsl:when test="string-length($desc) >= 15">
                    <xsl:variable name="last15" select="substring($desc, string-length($desc) - 15)"/>
                    <xsl:variable name="first3" select="substring($last15,1,3)"/>
                    <xsl:choose>
                        <xsl:when test="string(number($last15)) != 'NaN' and (
                            (substring($first3,1,1) = '9' and $first3 != '999') or $first3 = '124' or $first3 = '484' )">
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="ManagementID">
                                <xsl:attribute name="Number">
                                    <xsl:value-of select="substring($desc,1,string-length($desc) - 15)"/>
                                </xsl:attribute>
                            </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="ManagementID">
                                <xsl:attribute name="Number">
                                    <xsl:value-of select="$desc"/>
                                </xsl:attribute>
                            </xsl:element>                    
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="ManagementID">
                        <xsl:attribute name="Number">
                            <xsl:value-of select="$desc"/>
                        </xsl:attribute>
                    </xsl:element>                    
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template name="AnimalSpecies">
        <xsl:param name="item"/>
        <xsl:choose>
            <xsl:when
                test="
                    $item/spp = 'AQU'
                    or $item/spp = 'BEF'
                    or $item/spp = 'BIS'
                    or $item/spp = 'BOV'
                    or $item/spp = 'CAM'
                    or $item/spp = 'CAN'
                    or $item/spp = 'CAP'
                    or $item/spp = 'CER'
                    or $item/spp = 'CHI'
                    or $item/spp = 'DAI'
                    or $item/spp = 'EQU'
                    or $item/spp = 'FEL'
                    or $item/spp = 'OVI'
                    or $item/spp = 'POR'
                    or $item/spp = 'TUR'">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="SpeciesCode">
                    <xsl:attribute name="Code">
                        <xsl:call-template name="Species">
                            <xsl:with-param name="species" select="$item/spp"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="SpeciesOther">
                    <xsl:attribute name="Code">
                        <xsl:call-template name="Species">
                            <xsl:with-param name="species" select="$item/spp"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="Text">
                        <xsl:call-template name="SpeciesText">
                            <xsl:with-param name="species" select="$item/spp"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
        <!-- End SpeciesCode vs. Other -->

    </xsl:template>
    <xsl:template name="Age">
        <xsl:param name="item"/>
        <xsl:value-of select="$item/ageNum"/>
        <xsl:call-template name="AgeTime">
            <xsl:with-param name="ageTime" select="$item/ageTime"/>
        </xsl:call-template>
    </xsl:template>

    <!-- Translate from simple English time interval abbreviations to UCUM units -->
    <xsl:template name="AgeTime">
        <xsl:param name="ageTime"/>
        <xsl:choose>
            <xsl:when test="$ageTime = 'Y'">
                <xsl:text>a</xsl:text>
            </xsl:when>
            <xsl:when test="$ageTime = 'M'">
                <xsl:text>mo</xsl:text>
            </xsl:when>
            <xsl:when test="$ageTime = 'W'">
                <xsl:text>wk</xsl:text>
            </xsl:when>
            <xsl:when test="$ageTime = 'D'">
                <xsl:text>d</xsl:text>
            </xsl:when>
            <xsl:when test="$ageTime = 'Years'">
                <xsl:text>a</xsl:text>
            </xsl:when>
            <xsl:when test="$ageTime = 'Months'">
                <xsl:text>mo</xsl:text>
            </xsl:when>
            <xsl:when test="$ageTime = 'Weeks'">
                <xsl:text>wk</xsl:text>
            </xsl:when>
            <xsl:when test="$ageTime = 'Days'">
                <xsl:text>d</xsl:text>
            </xsl:when>
            <!-- Is this the appropriate way to react to no age units? -->
            <xsl:when test="$ageTime = null or $ageTime = ''">
                <xsl:text>a</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$ageTime"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Known bug.  Does not combine tests into accession if they share accession number and date. -->
    <xsl:template name="Accessions">
        <xsl:if
            test="
                /eCVI/vetInspection/cviPG1/species/large/table/item and (
                /eCVI/vetInspection/cviPG1/species/large/table/item[eiaTestDate != 'N/A' and eiaTestDate != 'n/a' and eiaTestDate != '']
                or /eCVI/vetInspection/cviPG1/species/large/table/item[brucTestDate != 'N/A' and brucTestDate != 'n/a' and brucTestDate != ''])">

            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Accessions">
                <xsl:for-each
                    select="/eCVI/vetInspection/cviPG1/species/large/table/item[eiaTestDate != 'N/A' and eiaTestDate != '']">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Accession">
                        <xsl:attribute name="id">EIA<xsl:value-of select="./itemIndex"
                            /></xsl:attribute>
                        <xsl:attribute name="InfieldTest">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:value-of select="./eiaTestDate"/>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="./other"/>
                            </xsl:attribute>
                            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="LabName">
                                <xsl:value-of select="./eiaLab"/>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:for-each>
                <xsl:for-each
                    select="/eCVI/vetInspection/cviPG1/species/large/table/item[brucTestDate != 'N/A' and brucTestDate != '']">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Accession">
                        <xsl:attribute name="id">BRUC<xsl:value-of select="./itemIndex"
                            /></xsl:attribute>
                        <xsl:attribute name="InfieldTest">
                            <xsl:text>false</xsl:text>
                        </xsl:attribute>
                        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:value-of select="./brucTestDate"/>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:text>Not provided</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:element>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
    </xsl:template>


    <xsl:template name="Sex">
        <xsl:param name="sex"/>
        <xsl:choose>
            <xsl:when test="$sex = 'F'">Female</xsl:when>
            <xsl:when test="$sex = 'M'">Male</xsl:when>
            <xsl:when test="$sex = 'C'">Neutered Male</xsl:when>
            <xsl:when test="$sex = 'S'">Spayed Female</xsl:when>
            <xsl:when test="$sex = 'X'">Other</xsl:when>
            <xsl:when test="$sex = 'U'">Gender Unknown</xsl:when>
            <xsl:otherwise>Gender Unknown</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="PurposeMap">
        <xsl:param name="purpose"/>
        <xsl:choose>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Backgrounding', $smallcase, $uppercase)"
                >other</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Breeding', $smallcase, $uppercase)"
                >Breeding</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Feeding', $smallcase, $uppercase)"
                >Feeding to slaughter</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Grazing', $smallcase, $uppercase)"
                >Grazing</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Medical Treatment', $smallcase, $uppercase)"
                >Medical Treatment:</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Other (specify)', $smallcase, $uppercase)"
                >Other</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Pet', $smallcase, $uppercase)"
                >Companion Animal</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Pet Movement', $smallcase, $uppercase)"
                >Companion Animal</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Production', $smallcase, $uppercase)"
                >other</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Race', $smallcase, $uppercase)"
                >Racing</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Recreational', $smallcase, $uppercase)"
                >Other</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Rodeo', $smallcase, $uppercase)"
                >Exhibition/Show/Rodeo</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Sale', $smallcase, $uppercase)"
                >Sale</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Show/Exhibition', $smallcase, $uppercase)"
                >Exhibition/Show/Rodeo</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Show', $smallcase, $uppercase)"
                >Exhibition/Show/Rodeo</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Show/Sale', $smallcase, $uppercase)"
                >Exhibition/Show/Rodeo</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Slaughter', $smallcase, $uppercase)"
                >Slaughter</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Training', $smallcase, $uppercase)"
                >Training</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('Transit', $smallcase, $uppercase)"
                >Other</xsl:when>
            <xsl:otherwise>Other</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="FullAddress">
        <xsl:param name="address"/>
        <xsl:param name="city"/>
        <xsl:param name="county"/>
        <xsl:param name="state"/>
        <xsl:param name="zip"/>
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Address">
            <xsl:if test="$address != ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Line1">
                    <xsl:value-of select="$address"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="$city != ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Town">
                    <xsl:value-of select="$city"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="$county">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="County">
                    <xsl:value-of select="$county"/>
                </xsl:element>
            </xsl:if>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="State">
                <xsl:choose>
                    <xsl:when test="$state">
                        <xsl:value-of select="$state"/>
                    </xsl:when>
                    <xsl:otherwise>AA</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:if test="$zip">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="ZIP">
                    <xsl:value-of select="$zip"/>
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template name="BlockedAddress">
        <xsl:param name="address"/>
        <xsl:param name="city"/>
        <xsl:param name="county"/>
        <xsl:param name="state"/>
        <xsl:param name="zip"/>

        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="AddressBlock">
            <xsl:if test="$address">
                <xsl:value-of select="$address"/>
            </xsl:if>
            <xsl:text>, </xsl:text>
            <xsl:text>, </xsl:text>
            <xsl:choose>
                <xsl:when test="$city">
                    <xsl:value-of select="$city"/>
                </xsl:when>
                <xsl:otherwise>No Town</xsl:otherwise>
            </xsl:choose>
            <xsl:text>, </xsl:text>
            <xsl:if test="$county">
                <xsl:value-of select="$county"/>
            </xsl:if>
            <xsl:text>, </xsl:text>
            <xsl:choose>
                <xsl:when test="$state">
                    <xsl:value-of select="$state"/>
                </xsl:when>
                <xsl:otherwise>No State</xsl:otherwise>
            </xsl:choose>
            <xsl:text>, </xsl:text>
            <xsl:choose>
                <xsl:when test="$zip">
                    <xsl:value-of select="$zip"/>
                </xsl:when>
                <xsl:otherwise>00000</xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
    <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

    <!-- This is a placeholder for a lookup table to standard species codes -->
    <xsl:template name="Species">
        <xsl:param name="species"/>
        <xsl:choose>
            <xsl:when test="$species = 'BBOV'">BEF</xsl:when>
            <xsl:when test="$species = 'DBOV'">DAI</xsl:when>
            <xsl:when test="$species = 'AQU'">AQU</xsl:when>
            <xsl:when test="$species = 'AVI'">AVI</xsl:when>
            <xsl:when test="$species = 'BEF'">BEF</xsl:when>
            <xsl:when test="$species = 'BIS'">BIS</xsl:when>
            <xsl:when test="$species = 'BOV'">BOV</xsl:when>
            <xsl:when test="$species = 'CAM'">CAM</xsl:when>
            <xsl:when test="$species = 'CAN'">CAN</xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'CANINE'">CAN</xsl:when>
            <xsl:when test="$species = 'CAP'">CAP</xsl:when>
            <xsl:when test="$species = 'CER'">CER</xsl:when>
            <xsl:when test="$species = 'CHI'">CHI</xsl:when>
            <xsl:when test="$species = 'CLM'">CLM</xsl:when>
            <xsl:when test="$species = 'CRA'">CRA</xsl:when>
            <xsl:when test="$species = 'CTF'">CTF</xsl:when>
            <xsl:when test="$species = 'DAI'">DAI</xsl:when>
            <xsl:when test="$species = 'DEE'">DEE</xsl:when>
            <xsl:when test="$species = 'DUC'">DUC</xsl:when>
            <xsl:when test="$species = 'ELK'">ELK</xsl:when>
            <xsl:when test="$species = 'EQU'">EQU</xsl:when>
            <xsl:when test="$species = 'FEL'">FEL</xsl:when>
            <xsl:when test="$species = 'GEE'">GEE</xsl:when>
            <xsl:when test="$species = 'GUI'">GUI</xsl:when>
            <xsl:when test="$species = 'MSL'">MSL</xsl:when>
            <xsl:when test="$species = 'OTH'">OTH</xsl:when>
            <xsl:when test="$species = 'OVI'">OVI</xsl:when>
            <xsl:when test="$species = 'OYS'">OYS</xsl:when>
            <xsl:when test="$species = 'PGN'">PGN</xsl:when>
            <xsl:when test="$species = 'POR'">POR</xsl:when>
            <xsl:when test="$species = 'POU'">POU</xsl:when>
            <xsl:when test="$species = 'QUA'">QUA</xsl:when>
            <xsl:when test="$species = 'RTT'">RTT</xsl:when>
            <xsl:when test="$species = 'SAL'">SAL</xsl:when>
            <xsl:when test="$species = 'SBA'">SBA</xsl:when>
            <xsl:when test="$species = 'SHR'">SHR</xsl:when>
            <xsl:when test="$species = 'SLP'">SLP</xsl:when>
            <xsl:when test="$species = 'TIL'">TIL</xsl:when>
            <xsl:when test="$species = 'TRO'">TRO</xsl:when>
            <xsl:when test="$species = 'TUR'">TUR</xsl:when>
            <xsl:otherwise>OTH</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="SpeciesText">
        <xsl:param name="species"/>
        <xsl:choose>
            <xsl:when test="$species = 'BBOV'">Beef Cattle</xsl:when>
            <xsl:when test="$species = 'DBOV'">Dairy Cattle</xsl:when>
            <xsl:when test="$species = 'AQU'">AQU</xsl:when>
            <xsl:when test="$species = 'AVI'">AVI</xsl:when>
            <xsl:when test="$species = 'BEF'">BEF</xsl:when>
            <xsl:when test="$species = 'BIS'">BIS</xsl:when>
            <xsl:when test="$species = 'BOV'">BOV</xsl:when>
            <xsl:when test="$species = 'CAM'">CAM</xsl:when>
            <xsl:when test="$species = 'CAN'">CAN</xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'CANINE'">CAN</xsl:when>
            <xsl:when test="$species = 'CAP'">CAP</xsl:when>
            <xsl:when test="$species = 'CER'">CER</xsl:when>
            <xsl:when test="$species = 'CHI'">CHI</xsl:when>
            <xsl:when test="$species = 'CLM'">CLM</xsl:when>
            <xsl:when test="$species = 'CRA'">CRA</xsl:when>
            <xsl:when test="$species = 'CTF'">CTF</xsl:when>
            <xsl:when test="$species = 'DAI'">DAI</xsl:when>
            <xsl:when test="$species = 'DEE'">DEE</xsl:when>
            <xsl:when test="$species = 'DUC'">DUC</xsl:when>
            <xsl:when test="$species = 'ELK'">ELK</xsl:when>
            <xsl:when test="$species = 'EQU'">EQU</xsl:when>
            <xsl:when test="$species = 'FEL'">FEL</xsl:when>
            <xsl:when test="$species = 'GEE'">GEE</xsl:when>
            <xsl:when test="$species = 'GUI'">GUI</xsl:when>
            <xsl:when test="$species = 'MSL'">MSL</xsl:when>
            <xsl:when test="$species = 'OTH'">OTH</xsl:when>
            <xsl:when test="$species = 'OVI'">OVI</xsl:when>
            <xsl:when test="$species = 'OYS'">OYS</xsl:when>
            <xsl:when test="$species = 'PGN'">Pigeon</xsl:when>
            <xsl:when test="$species = 'POR'">POR</xsl:when>
            <xsl:when test="$species = 'POU'">POU</xsl:when>
            <xsl:when test="$species = 'QUA'">Quail</xsl:when>
            <xsl:when test="$species = 'RTT'">Ratite</xsl:when>
            <xsl:when test="$species = 'SAL'">SAL</xsl:when>
            <xsl:when test="$species = 'SBA'">SBA</xsl:when>
            <xsl:when test="$species = 'SHR'">SHR</xsl:when>
            <xsl:when test="$species = 'SLP'">SLP</xsl:when>
            <xsl:when test="$species = 'TIL'">TIL</xsl:when>
            <xsl:when test="$species = 'TRO'">TRO</xsl:when>
            <xsl:when test="$species = 'TUR'">TUR</xsl:when>
            <xsl:otherwise>OTH</xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- This is a placeholder for a lookup table to standard codes -->
    <xsl:template name="Breed">
        <xsl:param name="species"/>
        <xsl:param name="breed"/>
        <xsl:choose>
            <xsl:when test="$species = 'BBOV'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Crossbreed', $smallcase, $uppercase)"
                        >XB</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Beefmaster', $smallcase, $uppercase)"
                        >BM</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Black Angus', $smallcase, $uppercase)"
                        >AN</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Brahma', $smallcase, $uppercase)"
                        >BR</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Braunvieh', $smallcase, $uppercase)"
                        >BU</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Charolais', $smallcase, $uppercase)"
                        >CH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Corriente', $smallcase, $uppercase)"
                        >MC</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Galloway', $smallcase, $uppercase)"
                        >GA</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Gelbvieh', $smallcase, $uppercase)"
                        >GV</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Hereford', $smallcase, $uppercase)"
                        >HB</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Limousin', $smallcase, $uppercase)"
                        >LM</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Longhorn', $smallcase, $uppercase)"
                        >LH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Maine Anjou', $smallcase, $uppercase)"
                        >MA</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Red Angus', $smallcase, $uppercase)"
                        >AR</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Salers', $smallcase, $uppercase)"
                        >SA</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Beefmaster', $smallcase, $uppercase)"
                        >BM</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Scottish Highland', $smallcase, $uppercase)"
                        >SH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Shorthorn', $smallcase, $uppercase)"
                        >SS</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Simmental', $smallcase, $uppercase)"
                        >SM</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Unknown', $smallcase, $uppercase)"
                        >UK</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'DBOV'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Brown Swiss', $smallcase, $uppercase)"
                        >SB</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Guernesey', $smallcase, $uppercase)"
                        >GU</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Holstein', $smallcase, $uppercase)"
                        >HO</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Jersey', $smallcase, $uppercase)"
                        >JE</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Unknown', $smallcase, $uppercase)"
                        >UK</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Other PB', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'EQU'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Grade', $smallcase, $uppercase)"
                        >GX</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Appaloosa', $smallcase, $uppercase)"
                        >AP</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Arabian', $smallcase, $uppercase)"
                        >AD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Belgian', $smallcase, $uppercase)"
                        >GI</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Clydesdale', $smallcase, $uppercase)"
                        >CY</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Donkey', $smallcase, $uppercase)"
                        >DK</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Miniature', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Morgan', $smallcase, $uppercase)"
                        >MN</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Mule', $smallcase, $uppercase)"
                        >ML</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Mustang', $smallcase, $uppercase)"
                        >MT</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Paint', $smallcase, $uppercase)"
                        >PT</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Percheron', $smallcase, $uppercase)"
                        >PH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Pinto', $smallcase, $uppercase)"
                        >PN</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Pony', $smallcase, $uppercase)"
                        >PY</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Quarter Horse', $smallcase, $uppercase)"
                        >QH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Shire', $smallcase, $uppercase)"
                        >SY</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Tennessee Walker', $smallcase, $uppercase)"
                        >TW</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Thoroughbred', $smallcase, $uppercase)"
                        >TH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Warmblood', $smallcase, $uppercase)"
                        >WM</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Zebra', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Unknown', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Other PB', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'OVI'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Crossbreed', $smallcase, $uppercase)"
                        >XB</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Black-faced', $smallcase, $uppercase)"
                        >BF</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Cheviot', $smallcase, $uppercase)"
                        >BC</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Columbia', $smallcase, $uppercase)"
                        >CL</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Corriedale', $smallcase, $uppercase)"
                        >CR</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Cotswold', $smallcase, $uppercase)"
                        >CW</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Debouillet', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Dorper', $smallcase, $uppercase)"
                        >DO</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Dorset', $smallcase, $uppercase)"
                        >DP</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Hampshire', $smallcase, $uppercase)"
                        >HS</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Lincoln', $smallcase, $uppercase)"
                        >LI</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Merino', $smallcase, $uppercase)"
                        >MM</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Mottle-faced', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Natural Colored', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Navajo-Churro', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Rambouillet', $smallcase, $uppercase)"
                        >RG</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Romney', $smallcase, $uppercase)"
                        >RY</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Solid Face, not Black', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Southdown', $smallcase, $uppercase)"
                        >ST</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Suffolk', $smallcase, $uppercase)"
                        >SU</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Targhee', $smallcase, $uppercase)"
                        >TA</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('White Dorper', $smallcase, $uppercase)"
                        >DO</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('White-faced', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Unknown', $smallcase, $uppercase)"
                        >UK</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Other PB', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'CAP'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Crossbreed', $smallcase, $uppercase)"
                        >XB</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Alpine', $smallcase, $uppercase)"
                        >AL</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Angora', $smallcase, $uppercase)"
                        >AG</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Boer', $smallcase, $uppercase)"
                        >BO</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Cashmere', $smallcase, $uppercase)"
                        >CS</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Cotswold', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Dairy-type crossbred', $smallcase, $uppercase)"
                        >DR</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Fiber-type crossbred', $smallcase, $uppercase)"
                        >FI</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('La Mancha', $smallcase, $uppercase)"
                        >LN</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Meat-type crossbred', $smallcase, $uppercase)"
                        >MT</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Nubian', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Pygmy', $smallcase, $uppercase)"
                        >PY</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Saanen', $smallcase, $uppercase)"
                        >SA</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Sable', $smallcase, $uppercase)"
                        >SB</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Toggenburg', $smallcase, $uppercase)"
                        >TO</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Unknown', $smallcase, $uppercase)"
                        >UNK</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Other', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'POR'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Crossbreed', $smallcase, $uppercase)"
                        >CB</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Berkshire', $smallcase, $uppercase)"
                        >BK</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Chester White', $smallcase, $uppercase)"
                        >CW</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Duroc', $smallcase, $uppercase)"
                        >DU</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Hampshire', $smallcase, $uppercase)"
                        >HA</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Lancombe', $smallcase, $uppercase)"
                        >LC</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Landrace', $smallcase, $uppercase)"
                        >LA</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Poland', $smallcase, $uppercase)"
                        >PC</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Potbelly', $smallcase, $uppercase)"
                        >VP</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Spot', $smallcase, $uppercase)"
                        >SO</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Tamworth', $smallcase, $uppercase)"
                        >TM</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Yorkshire', $smallcase, $uppercase)"
                        >YO</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Unknown', $smallcase, $uppercase)"
                        >UNK</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Other PB', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'OTH'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Other', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'POU'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Chicken', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Game Fowl', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Geese', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Pigeons', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Rattites', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Turkey', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Water Fowl', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'CER'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Black-Tailed Deer', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Caribou', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Deer', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Elk', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Elk/Red Deer X', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Fallow Deer', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Mule Deer', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Mule Deer X', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Muntjad', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Red Deer', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Sika Deer', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('White-Tailed Deer', $smallcase, $uppercase)"
                        >BRD</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="translate($species, $smallcase, $uppercase) = 'CAM'">
                <xsl:choose>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Alpaca', $smallcase, $uppercase)"
                        >AL</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Camel', $smallcase, $uppercase)"
                        >DC</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Llama', $smallcase, $uppercase)"
                        >LL</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Unknown', $smallcase, $uppercase)"
                        >UNK</xsl:when>
                    <xsl:when
                        test="translate($breed, $smallcase, $uppercase) = translate('Other', $smallcase, $uppercase)"
                        >OTH</xsl:when>
                    <xsl:otherwise>OTH</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>OTH</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
