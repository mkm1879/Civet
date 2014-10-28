package edu.clemson.lph.utils;
/*
Copyright 2014 Michael K Martin

This file is part of Civet.

Civet is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Civet is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with Civet.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileUtils {

	public static String readTextFile( File fIn ) throws FileNotFoundException {
	    StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(new FileInputStream(fIn), "UTF-8");
	    try {
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine() + NL);
	      }
	    }
	    finally{
	      scanner.close();
	    }
	    return text.toString();
	}
	
	public static byte[] readBinaryFile( String sFilePath ) throws Exception {
		File f = new File( sFilePath );
		return readBinaryFile( f );
	}
		
	public static byte[] readBinaryFile( File fThis ) throws Exception {
		byte[] bytes = null;
		long len = fThis.length();
		FileInputStream r;
		r = new FileInputStream( fThis );
		bytes = new byte[(int)len];
		int iRead = r.read(bytes);
		r.close();
		if( iRead != len ) 
			throw new Exception("File " + fThis.getName() + " size = " + len + " read = " + iRead);
		return bytes;
	}

}
