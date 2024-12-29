package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragnarokbot.model.Script;

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
}
