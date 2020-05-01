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
                <xsl:value-of select="SC_Approval_Number"/>
            </xsl:attribute>
            <xsl:attribute name="IssueDate">
                <xsl:call-template name="fixDate">
                    <xsl:with-param name="dateString" select="Date_of_SC_Approval"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="ExpirationDate">
                <xsl:call-template name="splitDate">
                    <xsl:with-param name="dateString" select="Shipment_Dates"/>
                    <xsl:with-param name="toOrFrom" select="'after'"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="ShipmentDate">
                <xsl:call-template name="splitDate">
                    <xsl:with-param name="dateString" select="Shipment_Dates"/>
                    <xsl:with-param name="toOrFrom" select="'before'"/>
                </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="EntryPermitNumber">
                <xsl:value-of select="SC_Approval_Number"/>
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
                    <xsl:value-of select="SC_Authorizing_Veterinarian"/>
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
                    <xsl:with-param name="purpose" select="Purpose_of_Movement"/>
                </xsl:call-template>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="Origin">
        <xsl:variable name="lid" select="Origin_Official_Federal_Premises_ID"/>
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
            <xsl:if test="Origin_Business_Name != ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremName">
                    <xsl:value-of select="Origin_Business_Name"/>
                </xsl:element>
            </xsl:if>
            <xsl:call-template name="FullAddress">
                <xsl:with-param name="address" select="Origin_Physical_Address_of_Animals"/>
                <xsl:with-param name="city" select="Origin_City"/>
                <xsl:with-param name="county" select="''"/>
                <xsl:with-param name="state" select="translate(Origin_State, ' ', '')"/>
                <xsl:with-param name="zip" select="Origin_Zip_Code"/>
            </xsl:call-template>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                    <xsl:choose>
                        <xsl:when
                            test="Origin_Contact_Last_Name != '' and Origin_Contact_First_Name != ''">
                            <xsl:value-of select="Origin_Contact_Last_Name"/>
                            <xsl:text>, </xsl:text>
                            <xsl:value-of select="Origin_Contact_First_Name"/>
                        </xsl:when>
                        <xsl:when
                            test="Origin_Contact_Last_Name != '' and Origin_Contact_First_Name = ''">
                            <xsl:value-of select="OriginContactLastName"/>
                        </xsl:when>
                        <xsl:when
                            test="Origin_Contact_Last_Name = '' and Origin_Contact_First_Name != ''">
                            <xsl:value-of select="Origin_Contact_First_Name"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:element>
                <xsl:call-template name="fixPhone">
                    <xsl:with-param name="phone" select="Origin_Phone_Number"/>
                </xsl:call-template>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="Destination">
        <xsl:variable name="lid" select="Destination_Official_Federal_Premises_ID"/>
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
            <xsl:if test="Destination_Business_Name != ''">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="PremName">
                    <xsl:value-of select="Destination_Business_Name"/>
                </xsl:element>
            </xsl:if>
            <xsl:call-template name="FullAddress">
                <xsl:with-param name="address" select="Destination_Physical_Address_of_Animals"/>
                <xsl:with-param name="city" select="Destination_City"/>
                <xsl:with-param name="county" select="''"/>
                <xsl:with-param name="state" select="translate(Destination_State, ' ', '')"/>
                <xsl:with-param name="zip" select="Destination_Zip_Code"/>
            </xsl:call-template>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                    <xsl:choose>
                        <xsl:when
                            test="Destination_Contact_Last_Name != '' and Destination_Contact_First_Name != ''">
                            <xsl:value-of select="Destination_Contact_Last_Name"/>
                            <xsl:text>, </xsl:text>
                            <xsl:value-of select="Destination_Contact_First_Name"/>
                        </xsl:when>
                        <xsl:when
                            test="Destination_Contact_Last_Name != '' and Destination_Contact_First_Name = ''">
                            <xsl:value-of select="DestinationContactLastName"/>
                        </xsl:when>
                        <xsl:when
                            test="Destination_Contact_Last_Name = '' and Destination_Contact_First_Name != ''">
                            <xsl:value-of select="Destination_Contact_First_Name"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:element>
                <xsl:call-template name="fixPhone">
                    <xsl:with-param name="phone" select="Destination_Phone_Number"/>
                </xsl:call-template>
             </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="Consignor">
        <xsl:element name="Consignor">
            <xsl:call-template name="FullAddress">
                <xsl:with-param name="address" select="Owner_Certification_Address"/>
                <xsl:with-param name="city" select="Owner_Certification_City"/>
                <xsl:with-param name="county" select="''"/>
                <xsl:with-param name="state" select="translate(Owner_State, ' ', '')"/>
                <xsl:with-param name="zip" select="Owner_Zip_Code"/>
            </xsl:call-template>
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Person">
                <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Name">
                    <xsl:value-of select="Name_of_Representative"/>
                </xsl:element>
                <xsl:call-template name="fixPhone">
                    <xsl:with-param name="phone" select="Owner_Phone_Num"/>
                </xsl:call-template>
             </xsl:element>
        </xsl:element>
    </xsl:template>


    <xsl:template name="LargeAnimalGroup">
        <xsl:param name="table"/>
        <xsl:variable name="head" select="Num_BirdsEggs"/>
        <xsl:if test="number($head) > 1 or not(./offID) or ./offID = ''">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="GroupLot">
                <xsl:attribute name="Quantity">
                    <xsl:choose>
                        <xsl:when test="$head and contains($head, ' ')">
                            <xsl:variable name="firstPart"
                                select="translate(substring-before($head, ' '), ',', '')"/>
                            <xsl:value-of select="format-number(number($firstPart), '#')"/>
                        </xsl:when>
                        <xsl:when test="$head and contains($head, '/')">
                            <xsl:variable name="firstPart"
                                select="translate(substring-before($head, '/'), ',', '')"/>
                            <xsl:value-of select="format-number(number($firstPart), '#')"/>
                        </xsl:when>
                        <xsl:when test="$head">
                            <xsl:value-of select="translate($head, ',', '')"/>
                        </xsl:when>
                    </xsl:choose>
                </xsl:attribute>

                <xsl:attribute name="Description">
                    <xsl:value-of select="'Group of '"/>
                    <xsl:choose>
                        <xsl:when test="PoultryProduct_Type = 'Other'">
                            <xsl:value-of select="Other_PoultryProduct_Type"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="PoultryProduct_Type"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:call-template name="AnimalSpecies"/>
                <xsl:call-template name="Tests"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>


    <xsl:template name="AnimalSpecies">
        <xsl:variable name="species" select="PoultryProduct_Type"/>
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
                        <xsl:value-of select="Other_PoultryProduct_Type"/>
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
               ( (Test1_Collection_Date 
                    and contains(Test1_Collection_Date, '/20') or contains(Test1_Collection_Date, '-20') )
                or (Test2_Collection_Date  
                and ( contains(Test2_Collection_Date, '/20') or contains(Test2_Collection_Date, '-20') ) )
                or (Test3_Collection_Date 
                and ( contains(Test3_Collection_Date, '/20') or contains(Test3_Collection_Date, '-20') ) ) )
                and
                    (  (Accession_Num_Test1 and Accession_Num_Test1 != '')
                    or (Accession_Num_Test2 and Accession_Num_Test2 != '')
                    or (Accession_Num_Test3 and Accession_Num_Test3 != '') )">
            <xsl:element name="Accessions">
                <xsl:if
                    test="
                        (Test1_Collection_Date and Test1_Collection_Date != '' and 
                        (contains(Test1_Collection_Date, '/20') or contains(Test1_Collection_Date, '-20'))
                        and Accession_Num_Test1 and Accession_Num_Test1 != '')">
                    <xsl:element name="Accession">
                        <xsl:attribute name="InfieldTest">false</xsl:attribute>
                        <xsl:attribute name="id">ID1</xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:call-template name="fixDate">
                                    <xsl:with-param name="dateString" select="Test1_Collection_Date"
                                    />
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="Accession_Num_Test1"/>
                            </xsl:attribute>
                            <xsl:element name="LabName">Not Provided</xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
                <xsl:if
                    test="
                        (Test2_Collection_Date and Test2_Collection_Date != '' 
                           and (contains(Test2_Collection_Date, '/20') or contains(Test2_Collection_Date, '-20'))
                        and Accession_Num_Test2 and Accession_Num_Test2 != '')">
                    <xsl:element name="Accession">
                        <xsl:attribute name="InfieldTest">false</xsl:attribute>
                        <xsl:attribute name="id">ID2</xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:call-template name="fixDate">
                                    <xsl:with-param name="dateString" select="Test2_Collection_Date"
                                    />
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="Accession_Num_Test2"/>
                            </xsl:attribute>
                            <xsl:element name="LabName">Not Provided</xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
                <xsl:if
                    test="
                        (Test3_Collection_Date and Test3_Collection_Date != ''
                        and (contains(Test3_Collection_Date, '/20') or contains(Test3_Collection_Date, '-20'))
                        and Accession_Num_Test3 and Accession_Num_Test3 != '')">
                    <xsl:element name="Accession">
                        <xsl:attribute name="InfieldTest">false</xsl:attribute>
                        <xsl:attribute name="id">ID3</xsl:attribute>
                        <xsl:element name="Laboratory">
                            <xsl:attribute name="AccessionDate">
                                <xsl:call-template name="fixDate">
                                    <xsl:with-param name="dateString" select="Test3_Collection_Date"
                                    />
                                </xsl:call-template>
                            </xsl:attribute>
                            <xsl:attribute name="AccessionNumber">
                                <xsl:value-of select="Accession_Num_Test3"/>
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
        <xsl:if test="Result_Num1 and Result_Num1 != ''">
            <xsl:element name="Test">
                <xsl:attribute name="AccessionRef">ID1</xsl:attribute>
                <xsl:attribute name="TestCode">AIPCR</xsl:attribute>
                <xsl:element name="Result">
                    <xsl:attribute name="ResultName">RESULT</xsl:attribute>
                    <xsl:element name="ResultString">
                        <xsl:value-of select="Result_Num1"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="Result_Num2 and Result_Num2 != ''">
            <xsl:element name="Test">
                <xsl:attribute name="AccessionRef">ID2</xsl:attribute>
                <xsl:attribute name="TestCode">AIPCR</xsl:attribute>
                <xsl:element name="Result">
                    <xsl:attribute name="ResultName">RESULT</xsl:attribute>
                    <xsl:element name="ResultString">
                        <xsl:value-of select="Result_Num2"/>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="Result_Num3 and Result_Num3 != ''">
            <xsl:element name="Test">
                <xsl:attribute name="AccessionRef">ID3</xsl:attribute>
                <xsl:attribute name="TestCode">AIPCR</xsl:attribute>
                <xsl:element name="Result">
                    <xsl:attribute name="ResultName">RESULT</xsl:attribute>
                    <xsl:element name="ResultString">
                        <xsl:value-of select="Result_Num3"/>
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

    <xsl:template name="splitDate">
        <xsl:param name="dateString"/>
        <xsl:param name="toOrFrom"/>
        <xsl:choose>
            <xsl:when test="contains($dateString, '-')">
                <xsl:variable name="before" select="substring-before($dateString, '-')"/>
                <xsl:variable name="after" select="substring-after($dateString, '-')"/>
                <xsl:choose>
                    <xsl:when test="contains($after, '-')">
                        <!-- Give up can't split with multiple hyphens. -->
                        <xsl:call-template name="fixDate">
                            <xsl:with-param name="dateString" select="$dateString"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="$toOrFrom = 'before'">
                        <xsl:call-template name="fixDate">
                            <xsl:with-param name="dateString" select="$before"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$after and $after != ''">
                                <xsl:call-template name="fixDate">
                                    <xsl:with-param name="dateString" select="$after"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="fixDate">
                                    <xsl:with-param name="dateString" select="$before"/>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="fixDate">
                    <xsl:with-param name="dateString" select="$dateString"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="fixPhone">
        <xsl:param name="phone"/>
        <xsl:variable name="shortPhone" select="translate($phone, ' ()-', '')"/>
        <xsl:if test="$shortPhone and string-length($shortPhone) = 10">
            <xsl:element namespace="http://www.usaha.org/xmlns/ecvi2" name="Phone">
                <xsl:attribute name="Type">Unknown</xsl:attribute>
                <xsl:attribute name="Number">
                    <xsl:value-of select="$shortPhone"/>
                </xsl:attribute>
            </xsl:element>
        </xsl:if>
        
    </xsl:template>

    <xsl:template name="fixDate">
        <xsl:param name="dateString"/>
        <xsl:variable name="dateIn" select="translate($dateString, '-:', '/')"/>
        <xsl:variable name="mon" select="substring-before($dateIn, '/')"/>
        <xsl:variable name="rest" select="substring-after($dateIn, '/')"/>
        <xsl:variable name="day" select="substring-before($rest, '/')"/>
        <xsl:variable name="year" select="substring-after($rest, '/')"/>
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
