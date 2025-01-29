package config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

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
