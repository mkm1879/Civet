package edu.clemson.lph.logging;

import java.io.File;
import java.text.SimpleDateFormat;

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.FileUtils;

public class Logger {
	public static Logger singleInstance;
	public final static long MAXLOG = 5000000;
	private int logLevel = 1;
	private String sFileName = "Civet.log";
	private String sHost;
	private String sUser;
	private String sVersion;
	private boolean bXfa;
	SimpleDateFormat dfOut = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
	
	static {
		singleInstance = new Logger();
	}

	private Logger() {
		// TODO Auto-generated constructor stub
	}
	
	public static Logger getLogger() {
		return singleInstance;
	}
	
	public void setLevel( int iLevel) {
		if( iLevel > 0 && iLevel <= 3 )
			logLevel = iLevel;
		else
			this.error("Invalid Log Level " + iLevel);
	}
	
	public void setLevel(String sLevel) {
		if( "error".equalsIgnoreCase( sLevel ))
			setLevel(1);
		else if( "warn".equalsIgnoreCase(sLevel))
			setLevel(2);
		else if( "info".equalsIgnoreCase(sLevel))
			setLevel(3);
		else
			this.error("Invalid Log Level " + sLevel);
	}
	
	public void error( String sMsg ) {
		writeToLog( getContext() + '\n' + sMsg + '\n' );
	}
	
	public void error( Throwable e ) {
		writeToLog( getContext() + '\n' + e.getMessage() + '\n' );
	}
	
	public void error( String sMsg, Throwable e ) {
		writeToLog( getContext() + '\n' + sMsg + '\n' + e.getMessage() + '\n' );
	}
	
	public void warn( String sMsg ) {
		if( logLevel >= 2 )
			error( sMsg );
	}
	
	public void warn( Throwable e ) {
		if( logLevel >= 2 )
			error( e );
	}
	
	public void warn( String sMsg, Throwable e ) {
		if( logLevel >= 2)
			error( sMsg, e );
	}
	
	public void info( String sMsg ) {
		if( logLevel >= 3 )
			error( sMsg );
	}
	
	public void info( Throwable e ) {
		if( logLevel >= 3 )
			error( e );
	}
	
	public void info( String sMsg, Throwable e ) {
		if( logLevel >= 3)
			error( sMsg, e );
	}
	
	private String getContext() {
		StringBuffer sb = new StringBuffer();
		java.util.Date now = new java.util.Date();
		sb.append(dfOut.format(now));
		sb.append(':');
		sb.append(sUser);
		sb.append(" on ");
		sb.append(sHost);
		sb.append(':');
		sb.append(sVersion);
		if(bXfa)
			sb.append(" with XFA");
		else
			sb.append(" without XFA");
		return sb.toString();
	}
	
	private void writeToLog( String sOutput ) {
		setEnvironment();
		checkSize();
		FileUtils.writeTextFile( sOutput, sFileName, true );
	}
	
	private void setEnvironment() {
		this.sHost = CivetConfig.getHERDSWebServiceURL();
		this.sUser = CivetConfig.getHERDSUserName();
		this.sVersion = CivetConfig.getVersion();
		this.bXfa = CivetConfig.isJPedalXFA();
	}
	
	private void checkSize() {
		File fLog = new File(sFileName);
		long logLength = fLog.length();
		if( logLength > MAXLOG ) {
			rotateLogs();
		}
	}
	
	private void rotateLogs() {
		File fLog = new File(sFileName);
		fLog.renameTo( new File( "CivetLog1.txt") );
	}

}
