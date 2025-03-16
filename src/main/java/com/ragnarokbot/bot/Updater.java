package com.ragnarokbot.bot;

import org.json.JSONObject;

import com.ragnarokbot.main.BotRagnarok;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater {

	public static String getLatestVersion() throws IOException {
        URL url = new URL("https://api.github.com/repos/" + BotRagnarok.REPO_OWNER + "/" + BotRagnarok.REPO_NAME + "/releases/latest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject json = new JSONObject(response.toString());
        return json.getString("tag_name");
    }
	
	public static void downloadNewVersion(String downloadUrl) throws IOException {
	    System.out.println("🔄 Baixando nova versão...");

	    URL url = new URL(downloadUrl);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("GET");
	    int fileSize = conn.getContentLength(); // Obtém o tamanho total do arquivo

	    try (InputStream in = conn.getInputStream();
	         FileOutputStream out = new FileOutputStream(BotRagnarok.DOWNLOAD_PATH)) {

	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        int totalBytesRead = 0;
	        int progressBarSize = 20; // Define o tamanho da barra de progresso
	        
	        while ((bytesRead = in.read(buffer)) != -1) {
	            out.write(buffer, 0, bytesRead);
	            totalBytesRead += bytesRead;
	            
	            // Calcula a porcentagem concluída
	            int percentComplete = (int) (((double) totalBytesRead / fileSize) * 100);
	            
	            // Calcula a quantidade de "I" na barra de progresso
	            int filledBars = (int) ((percentComplete / 100.0) * progressBarSize);
	            String progressBar = "[" + "|".repeat(filledBars) + " ".repeat(progressBarSize - filledBars) + "]";
	            
	            // Exibe o progresso
	            System.out.printf("\r📥 %s %d%%", progressBar, percentComplete);
	        }
	    }

	    System.out.println("✅ Download concluído: " + BotRagnarok.DOWNLOAD_PATH);
	}
	
	public static void restartBot() throws IOException {
	    System.out.println("🔄 Reiniciando o bot...");

	    // Nome do arquivo BAT que inicia o bot
	    String scriptBat = "Stonks.bat"; 

	    // Executa o BAT novamente
	    new ProcessBuilder("cmd.exe", "/c", scriptBat).start();
	    
	    try {
	        Thread.sleep(2000); // Espera 2 segundos antes de sair
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	    // Fecha o processo atual
	    System.exit(0);
	}
	
	// 🔹 Lê a versão salva no arquivo version.txt
    public static String getCurrentVersion() {
        File file = new File(BotRagnarok.VERSION_FILE);
        if (!file.exists()) {
            return "v0.0.0"; //Se não existir, assume que é uma versão inicial
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return br.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "v0.0.0";
        }
    }
    
    //Salva a versão do bot no arquivo version.txt
    public static void saveCurrentVersion(String version) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BotRagnarok.VERSION_FILE))) {
            bw.write(version);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
}
