module Civet {
	exports main.java.edu.clemson.lph.civet.vsps;
	exports main.java.edu.clemson.lph.civet.webservice;
	exports main.java.edu.clemson.lph.civet.threads;
	exports main.java.edu.clemson.lph.utils;
	exports main.java.edu.clemson.lph.dialogs;
	exports main.java.edu.clemson.lph.civet.poultry;
	exports main.java.edu.clemson.lph.civet.emailonly;
	exports main.java.edu.clemson.lph.civet.xml;
	exports main.java.edu.clemson.lph.controls;
	exports main.java.edu.clemson.lph.civet.prefs;
	exports main.java.edu.clemson.lph.ems;
	exports main.java.edu.clemson.lph.civet.extractor;
	exports main.java.edu.clemson.lph.civet;
	exports main.java.edu.clemson.lph.civet.lookup;
	exports main.java.edu.clemson.lph.civet.inbox;
	exports main.java.edu.clemson.lph.civet.files;
	exports main.java.edu.clemson.lph.civet.xml.elements;
	exports main.java.edu.clemson.lph.mailman;
	exports main.java.edu.clemson.lph.pdfgen;
	exports main.java.edu.clemson.lph.db;

	requires commons.codec;
	requires commons.csv;
	requires httpclient;
	requires httpcore;
	requires itextpdf;
	requires java.datatransfer;
	requires java.desktop;
	requires java.rmi;
	requires java.sql;
	requires java.xml;
	requires javax.json;
	requires log4j;
	requires mail;
}