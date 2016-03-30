package edu.clemson.lph.civet.addons;

import java.awt.Window;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.utils.CSVWriter;

public class StateVetLookupGenerator implements AddOn {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private DatabaseConnectionFactory factory;

	public StateVetLookupGenerator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMenuText() {
		return "Generate State Vet Lookup Table";
	}

	@Override
	public void execute(Window parent) {
		if( factory == null )
			factory = InitAddOns.getFactory();
		// NOTE: There is no generator for this now.  Make one in AddOns and distribute to other states????
		String sQuery = "SELECT isnull(pt.Description,'') AS Prefix,  \n" +
				"av.LastName AS LName, av.FirstName AS FName, av.AddressLine1 AS MailAddress,  \n" +
				"av.City AS [Mail City], av.ZipCode AS MailZip, c.StateCode AS MailState, c.StateName AS State, \n" +
				"av.Email AS Email, ap.Email AS CVIEmail, \n" +
				"CASE WHEN c.StateCode = 'TX' THEN 'tahc_disapproved_cvi@tahc.texas.gov' \n" +
				"     WHEN c.StateCode = 'AL' THEN 'Dynetta.Burton@agi.alabama.gov' \n" +
				"     ELSE '' END AS CVIErrorEmail, \n" +
				"CASE WHEN EXISTS (select * from Notes n  \n" +
				"		where n.GenericKey = p.PracticeKey \n" +
				"		and n.ApplicationAreaKey = (select top 1 ApplicationAreaKey  \n" +
				"			from ApplicationAreas	 \n" +
				"			where Description = 'VeterinarianPractice')  \n" +
				"		and n.Note like '%FileType=CVI%' ) THEN 'CVI' \n" +
				"ELSE 'PDF' END AS FileType \n" +
				"FROM AllCounties c  \n" +
				"JOIN Accounts av on av.CountyKey = c.CountyKey  \n" +
				"LEFT JOIN PrefixTypes pt on pt.PrefixTypeKey = av.PrefixTypeKey  \n" +
				"JOIN Vets v on v.AccountKey = av.AccountKey  \n" +
				"JOIN PracticeVets pv ON pv.VetKey = v.VetKey \n" +
				"JOIN Practices p ON p.PracticeKey = pv.PracticeKey \n" +
				"JOIN Accounts ap ON ap.AccountKey = p.AccountKey AND ap.BusinessName LIKE '%CVI_Office' \n" +
				"JOIN VetCertificates vc on vc.VetKey = v.VetKey   \n" +
				"AND vc.VetCertificateTypeKey = (select VetCertificateTypeKey from VetCertificateTypes   \n" +
				"								where Description = 'State Veterinarian')   \n" +
				"AND ( vc.VetCertificateStatusTypeKey = 1  \n" +
				"			OR NOT EXISTS ( select * from VetCertificates vc1  \n" +
				"							join PracticeVets pv1 on pv1.VetKey = vc1.VetKey \n" +
				"					where PracticeKey = p.PracticeKey \n" +
				"and vc1.VetCertificateTypeKey = (select VetCertificateTypeKey from VetCertificateTypes \n" + 
				"		where Description = 'State Veterinarian') \n" +
				"					and VetCertificateStatusTypeKey = 1 ) ) \n" +
				"ORDER BY c.StateCode";
		String sName = "StateVetTable";
		generateLookup( sName, sQuery );
	}

	private void generateLookup( String sName, String sQuery ) {
		CSVWriter writer = new CSVWriter();
		Connection conn = factory.makeDBConnection();
		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(sQuery);
			ResultSetMetaData md = rs.getMetaData();
			int iCols = md.getColumnCount();
			ArrayList<String> aColNames = new ArrayList<String>();
			for( int i = 1; i <= iCols; i++ ) {
				aColNames.add(md.getColumnLabel(i));
			}
			writer.setHeader(aColNames);
			while( rs.next() ) {
				ArrayList<Object> aValues = new ArrayList<Object>();
				for( int i = 1; i <= iCols; i++ ) {
					String sValue = rs.getString(i);
					aValues.add(sValue);
				}
				writer.addRow(aValues);
			}
			writer.write(sName);
		} catch( SQLException e ) {
			logger.error("Error in query: " + sQuery, e);
		} catch (FileNotFoundException e) {
			logger.error("Could not find output file " + sName + ".csv", e);
		} catch (java.util.zip.DataFormatException e) {
			logger.error("Rows returned by query not equal size", e);;
		} finally {
			try {
				if( conn != null && !conn.isClosed() )
					conn.close();
			} catch( Exception e2 ) {
			}
		}

	}

}
