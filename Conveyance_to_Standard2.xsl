<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns="http://www.usaha.org/xmlns/ecvi2" xmlns:ecvi="http://www.usaha.org/xmlns/ecvi"
    xmlns:my="http://www.clemson.edu/public/lph/StdECVI"
    xmlns:xfdf="http://ns.adobe.com/xfdf-transition/">
    <xsl:output indent="yes" method="xml"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="*">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="eCVI">
            <xsl:attribute name="CviNumber">
                <xsl:value-of select="SCApprovalNumber"/>
            </xsl:attribute>
            <xsl:attribute name="IssueDate">
                <xsl:call-template name="fixDate">
                    <xsl:with-param name="dateString" select="DateofSCApproval"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="ExpirationDate">
                <xsl:call-template name="fixDate">
                    <xsl:with-param name="dateString" select="ShipmentDates"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="ShipmentDate">
                <xsl:call-template name="fixDate">
                    <xsl:with-param name="dateString" select="ShipmentDates"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="EntryPermitNumber">
                <xsl:value-of select="SCApprovalNumber"/>
            </xsl:attribute>
            <xsl:call-template name="vetCertification"/>

            <xsl:call-template name="Purpose"/>
            <xsl:call-template name="Origin"/>
            <xsl:call-template name="Destination"/>
            <xsl:call-template name="Consignor"/>
            <xsl:call-template name="Accessions"/>
            <xsl:call-template name="LargeAnimalGroup"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="vetCertification">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Veterinarian">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                    <xsl:value-of select="SCAuthorizingVeterinarian"/>
                </xsl:element>
            </xsl:element>
            <xsl:call-template name="FullAddress">
                <xsl:with-param name="address" select="'PO Box 102406'"/>
                <xsl:with-param name="city" select="'Columbia'"/>
                <xsl:with-param name="state" select="'SC'"/>
                <xsl:with-param name="zip" select="'29224-2406'"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template name="Purpose">
        <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="MovementPurposes">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="MovementPurpose">
                <xsl:call-template name="PurposeMap">
                    <xsl:with-param name="purpose" select="PurposeofMovement"/>
                </xsl:call-template>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="Origin">
        <xsl:variable name="lid" select="OriginOfficialFederalPremisesID"/>
        <xsl:variable name="alphanumeric"
            select="'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'"/>
        <xsl:element name="Origin">
            <xsl:if
                test="
                    $lid != '' and string-length($lid) &gt; 5 and string-length($lid) &lt; 9
                    and string-length(translate($lid, $alphanumeric, '')) = 0">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremId">
                    <xsl:value-of select="$lid"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="OriginBusinessName != ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremName">
                    <xsl:value-of select="OriginBusinessName"/>
                </xsl:element>
            </xsl:if>
            <xsl:call-template name="FullAddress">
                <xsl:with-param name="address" select="OriginPhysicalAddressofAnimals"/>
                <xsl:with-param name="city" select="OriginCity"/>
                <xsl:with-param name="county" select="''"/>
                <xsl:with-param name="state" select="OriginState"/>
                <xsl:with-param name="zip" select="OriginZipCode"/>
            </xsl:call-template>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                    <xsl:choose>
                        <xsl:when
                            test="OriginContactLastName != '' and OriginContactFirstName != ''">
                            <xsl:value-of select="OriginContactLastName"/>
                            <xsl:text>, </xsl:text>
                            <xsl:value-of select="OriginContactFirstName"/>
                        </xsl:when>
                        <xsl:when test="OriginContactLastName != '' and OriginContactFirstName = ''">
                            <xsl:value-of select="OriginContactLastName"/>
                        </xsl:when>
                        <xsl:when test="OriginContactLastName = '' and OriginContactFirstName != ''">
                            <xsl:value-of select="OriginContactFirstName"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:element>
                <xsl:if test="OriginPhoneNumber and OriginPhoneNumber != ''">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Phone">
                        <xsl:attribute name="Type">Unknown</xsl:attribute>
                        <xsl:attribute name="Number">
                            <xsl:value-of select="translate(OriginPhoneNumber, ' ()-', '')"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="Destination">
        <xsl:variable name="lid" select="DestinationOfficialFederalPremisesID"/>
        <xsl:variable name="alphanumeric"
            select="'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'"/>
        <xsl:element name="Destination">
            <xsl:if
                test="
                    $lid != '' and string-length($lid) &gt; 5 and string-length($lid) &lt; 9
                    and string-length(translate($lid, $alphanumeric, '')) = 0">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremId">
                    <xsl:value-of select="$lid"/>
                </xsl:element>
            </xsl:if>
            <xsl:if test="DestinationBusinessName != ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremName">
                    <xsl:value-of select="DestinationBusinessName"/>
                </xsl:element>
            </xsl:if>
            <xsl:call-template name="FullAddress">
                <xsl:with-param name="address" select="DestinationPhysicalAddressofAnimals"/>
                <xsl:with-param name="city" select="DestinationCity"/>
                <xsl:with-param name="county" select="''"/>
                <xsl:with-param name="state" select="DestinationState"/>
                <xsl:with-param name="zip" select="DestinationZipCode"/>
            </xsl:call-template>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                    <xsl:choose>
                        <xsl:when
                            test="DestinationContactLastName != '' and DestinationContactFirstName != ''">
                            <xsl:value-of select="DestinationContactLastName"/>
                            <xsl:text>, </xsl:text>
                            <xsl:value-of select="DestinationContactFirstName"/>
                        </xsl:when>
                        <xsl:when
                            test="DestinationContactLastName != '' and DestinationContactFirstName = ''">
                            <xsl:value-of select="DestinationContactLastName"/>
                        </xsl:when>
                        <xsl:when
                            test="DestinationContactLastName = '' and DestinationContactFirstName != ''">
                            <xsl:value-of select="DestinationContactFirstName"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:element>
                <xsl:if test="DestinationPhoneNumber and DestinationPhoneNumber != ''">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Phone">
                        <xsl:attribute name="Type">Unknown</xsl:attribute>
                        <xsl:attribute name="Number">
                            <xsl:value-of select="translate(DestinationPhoneNumber, ' ()-', '')"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="Consignor">
        <xsl:element name="Consignor">
            <xsl:call-template name="FullAddress">
                <xsl:with-param name="address" select="OwnerCertificationAddress"/>
                <xsl:with-param name="city" select="OwnerCertificationCity"/>
                <xsl:with-param name="county" select="''"/>
                <xsl:with-param name="state" select="OwnerState"/>
                <xsl:with-param name="zip" select="OwnerZipCode"/>
            </xsl:call-template>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                    <xsl:value-of select="NameofRepresentative"/>
                </xsl:element>
                <xsl:if
                    test="OwnerPhoneNum and OwnerPhoneNum != ''">
                    <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Phone">
                        <xsl:attribute name="Type">Unknown</xsl:attribute>
                        <xsl:attribute name="Number">
                            <xsl:value-of
                                select="translate(OwnerPhoneNum, ' ()-', '')"
                            />
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
            </xsl:element>
        </xsl:element>
    </xsl:template>


    <xsl:template name="LargeAnimalGroup">
        <xsl:param name="table"/>
        <xsl:variable name="head" select="NumBirds_Eggs"/>
        <xsl:if test="number($head) > 1 or not(./offID) or ./offID = ''">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="GroupLot">
                <xsl:attribute name="Quantity">
                    <xsl:value-of select="$head"/>
                </xsl:attribute>
                <xsl:attribute name="Description">
                    <xsl:value-of select="'Group of '"/>
                    <xsl:choose>
                        <xsl:when test="Poultry_ProductType = 'Other'">
                            <xsl:value-of
                                select="Poultry_ProductType"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="Poultry_ProductType"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:call-template name="AnimalSpecies"/>
                <xsl:call-template name="Tests"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>


    <xsl:template name="AnimalSpecies">
        <xsl:variable name="species" select="Poultry_ProductType"/>
        <xsl:choose>
            <xsl:when test="$species and $species != 'Other'">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="SpeciesCode">
                    <xsl:attribute name="Code">
                        <xsl:call-template name="SpeciesMap">
                            <xsl:with-param name="species" select="$species"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="SpeciesOther">
                    <xsl:attribute name="Code">OTH</xsl:attribute>
                    <xsl:attribute name="Text">
                        <xsl:value-of select="OtherPoultry_ProductType"/>
                    </xsl:attribute>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
        <!-- End SpeciesCode vs. Other -->
    </xsl:template>

    <!-- Known bug.  Does not combine tests into accession if they share accession number and date. -->
    <xsl:template name="Accessions">
        <xsl:if
            test="
                (Test1CollectionDate and Test1CollectionDate != '')
                or (Test2CollectionDate and Test2CollectionDate != '')
                or (Test3CollectionDate and Test3CollectionDate != '')">
            <xsl:element name="Accessions">
                <xsl:if test="(Test1CollectionDate and Test1CollectionDate != '')">
                    <xsl:element name="Accession">
                        <xsl:attribute name="InfieldTest">false</xsl:attribute>
                        <xsl:attribute name="id">ID1</xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:call-template name="fixDate">
                                    <xsl:with-param name="dateString" select="Test1CollectionDate"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="AccessionNumTest1"/>
                            </xsl:attribute>
                            <xsl:element name="LabName">Not Provided</xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="(Test2CollectionDate and Test2CollectionDate != '')">
                    <xsl:element name="Accession">
                        <xsl:attribute name="InfieldTest">false</xsl:attribute>
                        <xsl:attribute name="id">ID2</xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:call-template name="fixDate">
                                    <xsl:with-param name="dateString" select="Test2CollectionDate"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="AccessionNumTest2"/>
                            </xsl:attribute>
                            <xsl:element name="LabName">Not Provided</xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="(Test3CollectionDate and Test3CollectionDate != '')">
                    <xsl:element name="Accession">
                        <xsl:attribute name="InfieldTest">false</xsl:attribute>
                        <xsl:attribute name="id">ID3</xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:call-template name="fixDate">
                                    <xsl:with-param name="dateString" select="Test3CollectionDate"/>
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="AccessionNumTest3"/>
                            </xsl:attribute>
                            <xsl:element name="LabName">Not Provided</xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
            </xsl:element>
        </xsl:if>
        <!--    <Accessions>
        <Accession InfieldTest="false" id="ID1">
            <Laboratory AccessionDate="2019-05-13" AccessionNumber="Not Applicable">
                <LabName>ISDA Animal Health Lab</LabName>
            </Laboratory>
        </Accession>
 -->
    </xsl:template>

    <!-- Known bug.  Does not combine tests into accession if they share accession number and date. -->
    <xsl:template name="Tests">
        <xsl:if
            test="ResultNum1 and ResultNum1 != ''">
            <xsl:element name="Test">
                <xsl:attribute name="AccessionRef">ID1</xsl:attribute>
                <xsl:attribute name="TestCode">AIPCR</xsl:attribute>
                <xsl:element name="Result">
                    <xsl:attribute name="ResultName">RESULT</xsl:attribute>
                    <xsl:element name="ResultString">
                        <xsl:value-of select="ResultNum1"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if
            test="ResultNum2 and ResultNum2 != ''">
            <xsl:element name="Test">
                <xsl:attribute name="AccessionRef">ID2</xsl:attribute>
                <xsl:attribute name="TestCode">AIPCR</xsl:attribute>
                <xsl:element name="Result">
                    <xsl:attribute name="ResultName">RESULT</xsl:attribute>
                    <xsl:element name="ResultString">
                        <xsl:value-of select="ResultNum2"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if
            test="ResultNum3 and ResultNum3 != ''">
            <xsl:element name="Test">
                <xsl:attribute name="AccessionRef">ID3</xsl:attribute>
                <xsl:attribute name="TestCode">AIPCR</xsl:attribute>
                <xsl:element name="Result">
                    <xsl:attribute name="ResultName">RESULT</xsl:attribute>
                    <xsl:element name="ResultString">
                        <xsl:value-of select="ResultNum3"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
    </xsl:template>


    <xsl:template name="PurposeMap">
        <xsl:param name="purpose"/>
        <xsl:choose>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('To Breeding Farm', $smallcase, $uppercase)"
                >Breeding</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('To Commercial Farm', $smallcase, $uppercase)"
                >Other</xsl:when>
            <xsl:when
                test="
                    translate($purpose, $smallcase, $uppercase) =
                    translate('To Processing Plant', $smallcase, $uppercase)"
                >Slaughter</xsl:when>
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
                    <xsl:when test="$state and $state != ''">
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

    <xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'"/>
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>

    <!-- This is a placeholder for a lookup table to standard species codes -->
    <xsl:template name="SpeciesMap">
        <xsl:param name="species"/>
        <xsl:choose>
            <xsl:when test="$species = 'Broilers'">CHI</xsl:when>
            <xsl:when test="$species = 'Broiler Breeders'">CHI</xsl:when>
            <xsl:when test="$species = 'Table Egg Layer Pullets'">CHI</xsl:when>
            <xsl:when test="$species = 'Table Egg Layer Hens'">CHI</xsl:when>
            <xsl:when test="$species = 'Turkey - Meat'">TUR</xsl:when>
            <xsl:when test="$species = 'Turkey Breeders'">TUR</xsl:when>
            <xsl:otherwise>OTH</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="fixDate">
        <xsl:param name="dateString"/>
        <xsl:variable name="dateIn" select="translate($dateString, '/:', '-')"/>
        <xsl:variable name="mon" select="substring-before($dateIn, '-')"/>
        <xsl:variable name="rest" select="substring-after($dateIn, '-')"/>
        <xsl:variable name="day" select="substring-before($rest, '-')"/>
        <xsl:variable name="year" select="substring-after($rest, '-')"/>
        <xsl:call-template name="padYear">
            <xsl:with-param name="in" select="$year"/>
        </xsl:call-template>
        <xsl:value-of select="'-'"/>
        <xsl:call-template name="padMonDay">
            <xsl:with-param name="in" select="$mon"/>
        </xsl:call-template>
        <xsl:value-of select="'-'"/>
        <xsl:call-template name="padMonDay">
            <xsl:with-param name="in" select="$day"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="padMonDay">
        <xsl:param name="in"/>
        <xsl:choose>
            <xsl:when test="string-length($in) &lt; 2">
                <xsl:value-of select="'0'"/>
                <xsl:value-of select="$in"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$in"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="padYear">
        <xsl:param name="in"/>
        <xsl:choose>
            <xsl:when test="string-length($in) = 2">
                <xsl:value-of select="'20'"/>
                <xsl:value-of select="$in"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$in"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
