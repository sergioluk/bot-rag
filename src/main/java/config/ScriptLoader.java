package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScriptLoader {

	public Script carregarScriptdoJson(String caminhoArquivo) {
		try {
	        ObjectMapper mapper = new ObjectMapper();
	        return mapper.readValue(new File(caminhoArquivo), Script.class);
		} catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public ContasConfig carregarContas(String caminhoArquivo) {
		try {
	        ObjectMapper mapper = new ObjectMapper();
	        return mapper.readValue(new File(caminhoArquivo), ContasConfig.class);
		} catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public Map<String, Integer> carregarPin(String caminhoArquivo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Pin pinData = mapper.readValue(new File(caminhoArquivo), Pin.class);

            Map<String, Integer> hashReferencia = new HashMap<>();
            for (Map<String, String> pinMap : pinData.getPins()) {
                for (Map.Entry<String, String> entry : pinMap.entrySet()) {
                    if (!entry.getKey().equals("numero")) { 
                        hashReferencia.put(entry.getValue(), pinMap.get("numero") != null ? Integer.parseInt(pinMap.get("numero")) : 0);
                    }
                }
            }

            return hashReferencia;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
	
	//notebook
		public SkillsConfig carregarSkills(String caminhoArquivo) {
			try {
		        ObjectMapper mapper = new ObjectMapper();
		        return mapper.readValue(new File(caminhoArquivo), SkillsConfig.class);
			} catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
}
