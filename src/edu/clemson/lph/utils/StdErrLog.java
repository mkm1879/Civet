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
import java.io.PrintStream;

import org.apache.log4j.Logger;

public class StdErrLog {
	    private static final Logger logger = Logger.getLogger(edu.clemson.lph.civet.Civet.class.getName());

	    public static void tieSystemErrToLog() {
	        System.setErr(createLoggingProxy(System.err));
	    }

	    public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
	        return new PrintStream(realPrintStream) {
	            public void print(final String string) {
	                realPrintStream.print(string);
	                logger.error(string);
	            }
	        };
	    }
	}
