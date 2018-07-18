package edu.clemson.lph.civet.vsps;

import org.apache.log4j.Logger;
import edu.clemson.lph.civet.Civet;

public class AnimalTag {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private String sTagType;
	private String sTagNumber;
	private boolean bOfficial;

	public AnimalTag(String sTagType, String sTagNumber, boolean bOfficial) {
		this.sTagType = sTagType;
		this.sTagNumber = sTagNumber;
		this.bOfficial = bOfficial;
	}
	
	public String getType() {
		return sTagType;
	}
	public String getNumber() {
		return sTagNumber;
	}
	public boolean isOfficial() {
		return bOfficial;
	}

}
