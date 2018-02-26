Civet is open source.  It relies upon a few open source and commercial library .jar files

Required Libraries 
sqljdbc4.jar 	http://www.microsoft.com/en-us/download/confirmation.aspx?id=11774 
                   closed source but free.  License does not allow redistribution
		   so it must be downloaded by each site.  This library is ONLY needed for direct SQL
		   access, so not needed by most state/users.

jpedal.jar 	May be either 
jpedal_LGPL.jar	http://sourceforge.net/projects/jpedal/  (No CO/KS eCVI support, or some image types, free)
		   Included in distribution
or 
jpedalXFA.jar  	http://www.idrsolutions.com/jpedal-pricing/  
		   You will need the JPedalXFA Edition or newer full version, 
                   (or buy me a OEM license and everyone flies free!)

log4j		http://logging.apache.org/log4j/1.2/download.html

JavaMail	http://www.oracle.com/technetwork/java/javamail

iText		http://sourceforge.net/projects/itext/

Apache Axis2	https://axis.apache.org/axis/

Building the executable:
An ant build.xml file is included.  It has some of my local directory structure so it will need a little editing.  
PublishCivet.bat and PublishCivetAnt.bat files show how I package up the executable jar file.  These assume
the existence of a ./lib folder with all the listed library jar files.  I will supply most of them or download
newer and update the version numbers, etc.  JPedal and SqlServer if used require separate download.  

The CivetConfig.txt file will require extensive editing to conform to local absolute file paths, IP addresses,
email settings, etc.