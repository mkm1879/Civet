package edu.clemson.lph.civet.addons;

import java.util.HashMap;

public class CactusVets {
	private static HashMap<String,String> hCactusVets = null;
	public CactusVets() {
	}
	
	static {
		hCactusVets = new HashMap<String,String>();
		hCactusVets.put("00GNW3H", "Daniel Boykin");
		hCactusVets.put("00JPACP", "Randy Jones");
		hCactusVets.put("00CXHHV", "Randy Jones");
		hCactusVets.put("00CXKAX", "Randy Jones");
		hCactusVets.put("00JPACP", "Randy Jones");
		hCactusVets.put("00JPACP", "Randy Jones");
		hCactusVets.put("00GNTJY", "Daniel Boykin");
		hCactusVets.put("00HB4F4", "Daniel Boykin");
		hCactusVets.put("00GNSLW", "Daniel Boykin");
		hCactusVets.put("00HB213", "Daniel Boykin");
		hCactusVets.put("00G6JY1", "Daniel Boykin");
		hCactusVets.put("00GP7V8", "Daniel Boykin");
		hCactusVets.put("00GNSUE", "Daniel Boykin");
		hCactusVets.put("00F516B", "Daniel Boykin");
		hCactusVets.put("00GNSMU", "Daniel Boykin");
		hCactusVets.put("00GPU47", "Daniel Boykin");
		hCactusVets.put("00GNWS4", "Daniel Boykin");
		hCactusVets.put("00J8S6R", "Daniel Boykin");
		hCactusVets.put("00F5YUC", "Daniel Boykin");
		hCactusVets.put("00F6JVN", "Daniel Boykin");
		hCactusVets.put("00JNYYC", "Daniel Boykin");
		hCactusVets.put("00G6QG9", "Daniel Boykin");
		hCactusVets.put("00P7RMS", "Daniel Boykin");
		hCactusVets.put("00JHHL4", "Daniel Boykin");
		hCactusVets.put("00JHHWH", "Daniel Boykin");
		hCactusVets.put("00GNT7L", "Daniel Boykin");
		hCactusVets.put("00P8292", "Daniel Boykin");
		hCactusVets.put("00NF6AU", "Daniel Boykin");
		hCactusVets.put("00GBRW3", "Daniel Boykin");
		hCactusVets.put("00NIK7M", "Daniel Boykin");
		hCactusVets.put("00F6EC9", "Daniel Boykin");
		hCactusVets.put("00NNT13", "Daniel Boykin");
		hCactusVets.put("00FPEP8", "Daniel Boykin");
		hCactusVets.put("00K7PH9", "Daniel Boykin");
		hCactusVets.put("00JSSFW", "Steve Healy");
		hCactusVets.put("00GNWDY", "Daniel Boykin");
		hCactusVets.put("00GNURE", "Daniel Boykin");
		hCactusVets.put("00GNVQC", "Daniel Boykin");
		hCactusVets.put("00GNUB9", "Daniel Boykin");
		hCactusVets.put("00GNUAB", "Daniel Boykin");
		hCactusVets.put("00GQ990", "Daniel Boykin");
		hCactusVets.put("00GNT9H", "Daniel Boykin");
		hCactusVets.put("00GNW3H", "Daniel Boykin");
		hCactusVets.put("00GNT7L", "Daniel Boykin");
		hCactusVets.put("00GNTD9", "Daniel Boykin");
		hCactusVets.put("00GRMW4", "Daniel Boykin");
		hCactusVets.put("00MCMEX", "Daniel Boykin");
		hCactusVets.put("00FNK53", "Daniel Boykin");
		hCactusVets.put("00HK58A", "Daniel Boykin");
		hCactusVets.put("00C3GRA", "Daniel Boykin");
		hCactusVets.put("00JSBS4", "Daniel Boykin");
		hCactusVets.put("00KBQMX", "Daniel Boykin");
		hCactusVets.put("00NP9Z1", "Daniel Boykin");
		hCactusVets.put("00GNSUE", "Daniel Boykin");
		hCactusVets.put("00JSSFW", "Steve Healy");
	}
	
	public String getVetNameForPin( String sPin ) {
		String sRet = hCactusVets.get(sPin);
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = "Randy Jones";
		return sRet;
	}

}
