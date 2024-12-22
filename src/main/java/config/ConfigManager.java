package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class ConfigManager {

	private static final String CONFIG_FILE = "config.json";
	
	// Classe interna para representar o retângulo
    public static class Rectangle {
        public int x;
        public int y;
        public int width;
        public int height;

        // Construtor vazio necessário para deserialização
        public Rectangle() {}

        public Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    // Classe interna para a configuração completa
    public static class Config {
        public Rectangle rectangle;

        public Config() {
            this.rectangle = new Rectangle(0, 0, 0, 0); // Valores padrão
        }
    }
    
    // Carrega a configuração do arquivo
    public static Config loadConfig() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(CONFIG_FILE);

        if (!file.exists()) {
        	System.out.println("Arquivo de config não encontrado");
            return new Config(); // Retorna valores padrão se o arquivo não existir
        }

        try {
            return mapper.readValue(file, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new Config(); // Retorna valores padrão em caso de erro
        }
    }
    
    // Salva a configuração no arquivo
    public static void saveConfig(Config config) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CONFIG_FILE), config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
