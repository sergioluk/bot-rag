package com.ragnarokbot.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import utils.RagnarokMemoryScanner;

import com.sun.jna.platform.win32.WinDef;

import com.ragnarokbot.bot.Bot;
import com.ragnarokbot.bot.ProcessSelector;
import com.ragnarokbot.bot.Tela;
import com.ragnarokbot.bot.Updater;
import com.ragnarokbot.model.AStar;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.GrafoMapa;
import com.ragnarokbot.model.MemoryScanner;
import com.ragnarokbot.model.MyUser32;
import com.ragnarokbot.model.enums.Effects;
import com.ragnarokbot.model.MemoryScanner.Kernel32;
import com.ragnarokbot.telas.JanelaPrincipal;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.ptr.IntByReference;

import config.ConfigManager;
import config.ConfigManager.Config;

public class BotRagnarok {

	public static JanelaPrincipal janelaPrincipal;
	
	public static String REPO_OWNER = "sergioluk";  // 🔹 Coloque seu usuário do GitHub
	public static String REPO_NAME = "bot-rag";       // 🔹 Coloque o nome do repositório
	public static String DOWNLOAD_PATH = "Stonks.jar"; // 🔥 Nome do JAR gerado no release
	public static String VERSION_FILE = "version.txt"; // 🔥 Arquivo que armazena a versão local
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		boolean dev = true;
		if (dev == false) {
			try {
				 // Lê a versão atual do bot
				String currentVersion = Updater.getCurrentVersion();
	            System.out.println("📢 Versão atual: " + currentVersion);
	            
		        // Verifica a versão mais recente
		        String latestVersion = Updater.getLatestVersion();
		        System.out.println("📢 Versão mais recente: " + latestVersion);

		        if (!currentVersion.equals(latestVersion)) {
		            System.out.println("🚀 Nova versão disponível! Atualizando...");

		            // URL do download (ajuste conforme o release do GitHub)
		            String downloadUrl = "https://github.com/" + REPO_OWNER + "/" + REPO_NAME +
		                                 "/releases/latest/download/" + DOWNLOAD_PATH;

		            // Baixa a nova versão e reinicia
		            Updater.downloadNewVersion(downloadUrl);
		            
		            //Atualiza o arquivo de versão
	                Updater.saveCurrentVersion(latestVersion);
	                
		            Updater.restartBot();
		        } else {
		            System.out.println("✅ Bot já está atualizado!");
		        }

		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		} 

		 // Caminho absoluto da DLL
        String libPath = new File("libs/opencv_java451.dll").getAbsolutePath();

        // Carrega a DLL manualmente
        System.load(libPath);
        
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ITesseract tesseract = new Tesseract();
		tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
		tesseract.setLanguage("eng");
		// Configurar whitelist para reconhecer apenas números
		tesseract.setTessVariable("tessedit_char_whitelist", "0123456789 ");
		tesseract.setTessVariable("preserve_interword_spaces", "1");
		tesseract.setTessVariable("user_defined_dpi", "300"); // 150 é rápido e suficiente para números

		ITesseract tesseractLetras = new Tesseract();
		tesseractLetras.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
		tesseractLetras.setLanguage("eng");

		Robot robot = new Robot();
		Bot bot = new Bot(tesseract, robot, tesseractLetras);
		//bot.printarTela();
		
		//Tela tela = new Tela(bot);
		//SwingUtilities.invokeLater(() -> tela.setVisible(true)); //Exibe a janela
		
		//SwingUtilities.invokeLater( () -> new Tela(bot));
		//Tela.iniciarCronometro(1, 1, tela); // Cronômetro 1 conta 30 minutos
		//Tela.iniciarCronometro(2, 30, tela);
		
		// GameController gameController = new GameController(bot, tela);
		GameController gameController = new GameController(bot);
		// gameController.run();

		janelaPrincipal = new JanelaPrincipal(gameController);
		janelaPrincipal.setVisible(true);


		// Apagar
		Scalar[] limites = calcularLimites(0, 255, 8);
		System.out.println("Lower: " + limites[0]);
		System.out.println("Upper: " + limites[1]);
		// Apagar
		
		
		//Rect m = bot.getArmazem();
		//System.out.println("Armazem Altura: " + m.height + " Largura: " + m.width);
		/*List<MatOfPoint> balao = null;
		do {
			balao = bot.verificarBalaoNpcTeleport();
			System.out.print(balao.size());
			if (!balao.isEmpty()) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				System.out.println("Npc Altura: " + m.height + " Largura: " + m.width);
				
			}
			System.out.println("Procurando balao");
			bot.sleep(200);
		} while(balao.isEmpty());*/
	
		
		/*
		Rect m = null;
		
		m = bot.getAltQ();
		
		m = bot.getArmazem();//Procurando o armazem
		
		
		m = bot.getInventario();//procurando inventario
		
		
		if (m != null) {
			bot.guardarItensArmazem(m);//guardando os itens no armazem
		}
		
		m = bot.getArmazem();//Procurando o armazem
		
		if (m != null) {
			bot.removerItensArmazem( m); //pegando os itens devolta
		}
		
		m = bot.getInventario();//procurando inventario
		
		if (m != null) {
			bot.equipandoItens(m); //equipando os itens
		}*/
		
		
		
		
		/*Mat screen = bot.bufferedImageToMat(bot.printarParteTela( m.x, m.y, m.width, m.height));
		Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
	    Imgproc.drawContours(screen, inventario, -1, greenColor, 2);
	    // Salvar a imagem com os contornos desenhados
	    String contouredImagePath = "imagem_com_contornos.png";
	    Imgcodecs.imwrite(contouredImagePath, screen);
	    System.out.println("Imagem com contornos salva em: " + contouredImagePath);
		*/
		
		//MemoryScanner memoria = new MemoryScanner();
		//Coordenadas atual = memoria.obterCoordenadas(9820, memoria.addressX, memoria.addressY);
		//bot.setarMouseEmCoordenadaTela(atual, new Coordenadas(194,78));
		//System.out.println("inventario: " + m.size());
		//bot.executarInstancia("oi");
		//bot.realizarLogin("asd","123");
		//bot.detectarPixelsAmarelos(222, 240, 658, 183);
		//bot.printarTela();
		/*boolean detectou = bot.detectarPixelsAmarelos(654, 180, 658, 183);
		System.out.println("Detectou? " + detectou);*/
		/*String path = "config/minimapas/valkiria.png";
		int labirinto = 5;
    	gameController.verificarSeMudouMapa(path, labirinto);*/

		/*
		 * GrafoMapa grafo = new GrafoMapa();
		 * 
		 * // Adiciona conexões no grafo grafo.addConexao(new Coordenadas(11, 20), new
		 * Coordenadas(12, 15)); grafo.addConexao(new Coordenadas(12, 15), new
		 * Coordenadas(12, 10)); grafo.addConexao(new Coordenadas(12, 15), new
		 * Coordenadas(11, 20)); grafo.addConexao(new Coordenadas(12, 10), new
		 * Coordenadas(15, 10)); grafo.addConexao(new Coordenadas(15, 10), new
		 * Coordenadas(17, 15)); grafo.addConexao(new Coordenadas(17, 15), new
		 * Coordenadas(17, 20));
		 * 
		 * grafo.addConexao(new Coordenadas(12, 10), new Coordenadas(13, 5));
		 * grafo.addConexao(new Coordenadas(15, 10), new Coordenadas(14, 8));
		 * grafo.addConexao(new Coordenadas(13, 5), new Coordenadas(14, 2));
		 * grafo.addConexao(new Coordenadas(14, 8), new Coordenadas(13, 5));
		 * 
		 * Coordenadas inicio = new Coordenadas(17, 20); // Coordenada atual não mapeada
		 * Coordenadas destino = new Coordenadas(14, 2); // Coordenada final não mapeada
		 * 
		 * AStar aStar = new AStar(); List<Coordenadas> caminho =
		 * aStar.calcularCaminhoComExpansao(inicio, destino, grafo);
		 * 
		 * for (Coordenadas nodo : caminho) { System.out.println(nodo.x + " " + nodo.y);
		 * }
		 */

		/*SwingUtilities.invokeLater(() -> {
			ProcessSelector selector = new ProcessSelector();
			selector.setVisible(true);

			// Esperar até a janela ser fechada
			selector.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosed(java.awt.event.WindowEvent windowEvent) {
					System.out.println("PID selecionado: " + selector.getSelectedPid());
					MemoryScanner.processId = selector.getSelectedPid();
					try {
						ITesseract tesseract = new Tesseract();
						tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
						tesseract.setLanguage("eng");
						// Configurar whitelist para reconhecer apenas números
						tesseract.setTessVariable("tessedit_char_whitelist", "0123456789 ");
						tesseract.setTessVariable("preserve_interword_spaces", "1");
						tesseract.setTessVariable("user_defined_dpi", "300"); // 150 é rápido e suficiente para números

						ITesseract tesseractLetras = new Tesseract();
						tesseractLetras.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
						tesseractLetras.setLanguage("eng");

						Robot robot = new Robot();
						Bot bot = new Bot(tesseract, robot, tesseractLetras);

						// Tela tela = new Tela(bot);
						// SwingUtilities.invokeLater(() -> tela.setVisible(true)); //Exibe a janela
						// SwingUtilities.invokeLater( () -> new Tela(bot));

						// GameController gameController = new GameController(bot, tela);
						GameController gameController = new GameController(bot);
						// gameController.run();

						janelaPrincipal = new JanelaPrincipal(gameController);
						janelaPrincipal.setVisible(true);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		});*/
		
		
		//bot.printarTela();
		/*Rect r = Imgproc.boundingRect(bot.procurarBarraSkills().get(0)); //x+4,y+18 w8 h11
		int x = r.x + 4;
		int y = r.y + 18;
		BufferedImage barra = bot.abrirImagem("config/skills/barra.png");
		BufferedImage verificarCometa = bot.printarParteTela(x, y, 8, 11);
		
		if (bot.compararImagens(barra, verificarCometa, 2)) {
			System.out.println("Barra 1");
		} else {
			System.out.println("Barra 2");
		}*/
		/*bot.sleep(100);
		bot.moverMouse(bot.getxJanela() + x + 4, bot.getyJanela() + y + 5);
		bot.sleep(100);
		bot.clicarMouse();
		bot.sleep(100);
		bot.moverMouse(bot.getxJanela() + bot.getWidth()/2, bot.getyJanela() + bot.getHeight()/2);
		bot.sleep(100);*/
		
		/*BufferedImage verificarCometa2 = bot.printarParteTela(x, y, 8, 11);
		
		if (bot.compararImagens(barra, verificarCometa2, 2)) {
			System.out.println("Barra 1");
		} else {
			System.out.println("Barra 2");
		}*/
		/*int id = 22992;
		List<Integer> status = bot.listarStatus(id);
		for (int buff : status) {
			if (buff == Effects.ARMA_REMOVIDO.getId())
				System.out.println("sem arma");
			if (buff == Effects.ESCUDO_REMOVIDO.getId())
				System.out.println("sem escudo");
			if (buff == Effects.ELMO_REMOVIDO.getId())
				System.out.println("sem elmo");
			if (buff == Effects.ARMADURA_REMOVIDO.getId())
				System.out.println("sem armadura");
		}
		System.out.println("Itens status: " + status.toString());*/

		// gameController.run();
		System.out.println("Iniciando...");

		String filePath = "C:\\Users\\Sérgio\\Desktop\\enderecos.txt";
		findDuplicateAddresses(filePath);

		int processId = 4540;
		int valueToFind = 445;
		// short valueToFind = 68;
		//procurarInt(processId,valueToFind);
		// procurarShort(processId, valueToFind);
		// String nome = "Moeda de Inst";
		// String nome = "obteve Moeda de Inst�ncia";
		 //String nome = "Servidor desligado, reinicialize o jogo.";//enrederos encontrados 0x5B3BB5D0 0x6E6087F8
		 String nome = "Desconectado do servidor.";//enrederos encontrados 0x2628608 0x70B09968 0x70B0A070
		// 0x5296F33D
		System.out.println("entao");
		//procurarString(processId, nome);
		int soma = 4;
		
		/*Rect r = Imgproc.boundingRect(bot.procurarBarraSkills().get(0));//4b 133px, 3b 100px, 2b 67px, 1b 34px
		System.out.println("Height: " + r.height);
		// x + 16, y + 4 widht 24, heigh 19
		int pos = 1;
		int barra = 1;
		int x = r.x + 16 + (pos-1)*24 + (pos-1)*5;
		int y = r.y + 4 + (barra-1)*19 + (barra-1)*14;
		//bot.moverMouse(bot.getxJanela() + x, bot.getyJanela() + y);
		
		BufferedImage cometa = bot.abrirImagem("config/skills/cometa.png");
		BufferedImage verificarCometa = bot.printarParteTela(x, y, 24, 19);
		
		if (bot.compararImagens(cometa, verificarCometa, 2)) {
			System.out.println("Não está em cooldown");
		} else {
			System.out.println("Está em cooldown");
		}*/
		
		//bot.printarTela();
		//bot.printarParteTela(x + bot.getxJanela(), y + bot.getyJanela(), 10, 10);
		//System.exit(0);
		//mostrarValorMemoria(processId,0x1883F40 + soma ,0x18332BC + soma); //0x19D46C 0x156EC84 0x156FD4C 0x156EF68
		//mostrarStringMemoria(processId, 0x0158A120 + soma, 0x22488112 + soma, 256);
		// buscarItemPorId(processId, valueToFind); nao funcionou
		// obteve Moeda de Inst�ncia 0x19A9ED

		//RagnarokMemoryScanner.scanStringsInMemory(processId, 0x2EF0000, 0x7FFFFFFF);
		/*0x5B6E6F7A 0x5B6E6F7A
		 * Coordenadas janela = getWidthHeight(); Coordenadas atual = new
		 * Coordenadas(161, 69); Map<Coordenadas, Boolean> mapaCarregado =
		 * carregarMapa("mapas/moroc.png"); boolean doido = true; Scanner s = new
		 * Scanner(System.in); while (doido) { Coordenadas atualMouse =
		 * getCoordenadasTelaPeloMouse(atual, 0, 0, janela.x, janela.y); Boolean
		 * podeAndar = mapaCarregado.get(atualMouse); System.out.println(podeAndar);
		 * System.out.println("Digite a coordenada x:"); int x = s.nextInt();
		 * System.out.println("Digite a coordenada y:"); int y = s.nextInt();
		 * Thread.sleep(1000); Coordenadas destino = new Coordenadas(x,y);
		 * setarMouseEmCoordenadaTela(atual, destino, janela.x, janela.y); }
		 */
		/*List<Coordenadas> caminhoCalculado = List.of(new Coordenadas(10, 10), new Coordenadas(11, 11),
				new Coordenadas(12, 12), new Coordenadas(13, 13), new Coordenadas(14, 14), new Coordenadas(15, 15),
				new Coordenadas(16, 16), new Coordenadas(17, 17), new Coordenadas(18, 18), new Coordenadas(19, 19),
				new Coordenadas(20, 20));

		Coordenadas atual = new Coordenadas(10, 10);
		Coordenadas proximaCoordenada = escolherProximaCoordenada(caminhoCalculado, atual);

		System.out.println("Próxima Coordenada: " + proximaCoordenada);*/

		/*
		 * tive que instalar o tesseract e pegar a dll do opencv na pasta
		 * "opencv\build\java\x64" copiar a dll e jogar na pasta libs ir no projeto
		 * botao direito run as run configurations, na parte esquerda clicar no java
		 * aplication BotRagnarok arguments na direita VM arguments colocar esse codigo:
		 * -Djava.library.path=
		 * "C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/libs"
		 */
		System.out.println("finishou");
	}

	public static GrafoMapa gerarGrafoDeMapa(Map<Coordenadas, Boolean> mapaCoordenadas) {
		GrafoMapa grafo = new GrafoMapa();

		for (Map.Entry<Coordenadas, Boolean> entrada : mapaCoordenadas.entrySet()) {
			Coordenadas atual = entrada.getKey();
			if (entrada.getValue()) { // Se pode andar na coordenada
				// Verificar vizinhos (4 direções: cima, baixo, esquerda, direita)
				Coordenadas[] vizinhos = { new Coordenadas(atual.x, atual.y + 1), new Coordenadas(atual.x, atual.y - 1),
						new Coordenadas(atual.x + 1, atual.y), new Coordenadas(atual.x - 1, atual.y) };

				for (Coordenadas vizinho : vizinhos) {
					if (mapaCoordenadas.getOrDefault(vizinho, false)) {
						grafo.addConexao(atual, vizinho);
					}
				}
			}
		}

		return grafo;
	}

	public static Map<Coordenadas, Boolean> carregarMapa(String caminhoImagem) throws IOException {
		BufferedImage mapa = ImageIO.read(new File(caminhoImagem));
		Map<Coordenadas, Boolean> mapaCoordenadas = new HashMap<>();

		int largura = mapa.getWidth();
		int altura = mapa.getHeight();

		for (int y = 0; y < altura; y++) {
			for (int x = 0; x < largura; x++) {
				int rgb = mapa.getRGB(x, y);
				// Determina se é branco (caminhável) ou preto (obstáculo)
				boolean podeAndar = (rgb & 0xFFFFFF) == 0xFFFFFF; // Branco
				// Converter pixel para coordenada no Ragnarok
				int coordX = x;
				int coordY = altura - y - 1; // Inverter Y (base no canto inferior esquerdo)
				mapaCoordenadas.put(new Coordenadas(coordX, coordY), podeAndar);
			}
		}

		return mapaCoordenadas;
	}

	public static Scalar[] calcularLimites(int r, int g, int b) {
		// Criar Mat para a cor RGB
		Mat rgbColor = new Mat(1, 1, CvType.CV_8UC3, new Scalar(b, g, r)); // Usa 3 canais
		Mat hsvColor = new Mat();

		// Converter de BGR para HSV
		Imgproc.cvtColor(rgbColor, hsvColor, Imgproc.COLOR_BGR2HSV);
		double[] hsvValues = hsvColor.get(0, 0);

		// Definir tolerância para a cor
		int hue = (int) hsvValues[0]; // Hue
		int sat = (int) hsvValues[1]; // Saturation
		int val = (int) hsvValues[2]; // Value

		// Definir tolerâncias (ajuste conforme necessário)
		int hueTolerance = 10;
		int satTolerance = 40;
		int valTolerance = 40;

		// Limites inferior e superior
		Scalar lower = new Scalar(Math.max(hue - hueTolerance, 0), Math.max(sat - satTolerance, 0),
				Math.max(val - valTolerance, 0));

		Scalar upper = new Scalar(Math.min(hue + hueTolerance, 180), Math.min(sat + satTolerance, 255),
				Math.min(val + valTolerance, 255));

		return new Scalar[] { lower, upper };
	}

	public static void buscarItemPorId(int processId, int itemId) {
		// Abrir o processo do jogo com permissões de leitura.
		Pointer processHandle = Kernel32.INSTANCE
				.OpenProcess(Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION, false, processId);

		if (processHandle == null) {
			System.err.println("Não foi possível abrir o processo.");
			return;
		}

		// Defina o intervalo de memória do inventário (ajuste esses valores com base no
		// jogo).
		long startAddress = 0x00000000; // Endereço inicial (mude para onde o inventário começa).
		long endAddress = 0x7FFFFFFF; // Endereço final.

		byte[] buffer = new byte[8]; // Para ler ID (4 bytes) e quantidade (4 bytes).
		IntByReference bytesRead = new IntByReference();

		System.out.println("Procurando pelo item com ID: " + itemId);

		try {
			for (long address = startAddress; address < endAddress; address += 8) {
				Pointer baseAddress = new Pointer(address);

				// Ler 8 bytes (ID e quantidade).
				boolean success = Kernel32.INSTANCE.ReadProcessMemory(processHandle, baseAddress, buffer, buffer.length,
						bytesRead);

				if (success && bytesRead.getValue() == 8) {
					// Interpretar os 8 bytes como ID e quantidade.
					int foundId = java.nio.ByteBuffer.wrap(buffer, 0, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN)
							.getInt();
					int foundQuantity = java.nio.ByteBuffer.wrap(buffer, 4, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN)
							.getInt();

					if (foundId == itemId) {
						System.out.printf("Item encontrado! ID: %d, Quantidade: %d no endereço: 0x%X%n", foundId,
								foundQuantity, address);
					}
				}
			}
		} finally {
			// Fechar o handle do processo.
			Kernel32.INSTANCE.CloseHandle(processHandle);
		}
	}

	public static void procurarString(int processId, String stringToFind) {
		// Procurar string
		// int processId = 20732; // Substitua pelo PID do processo alvo.
		// String stringToFind = "sapatolegal"; // Substitua pela string que deseja
		// procurar.

		// Codifique a string para bytes (ASCII ou outra codificação relevante).
		byte[] searchBytes = stringToFind.getBytes(java.nio.charset.StandardCharsets.US_ASCII);

		// Abra o processo.
		Pointer processHandle = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ, false, processId);
		if (processHandle == null) {
			System.err.println("Não foi possível abrir o processo.");
			return;
		}

		// Defina o intervalo de memória para escanear.
		long startAddress = 0x00000000L; // Início do espaço de memória.
		long endAddress = 0x7FFFFFFFL; // Fim do espaço de memória.

		byte[] buffer = new byte[4096]; // Buffer para leitura em blocos.
		IntByReference bytesRead = new IntByReference();

		for (long address = startAddress; address < endAddress; address += buffer.length) {
			Pointer baseAddress = new Pointer(address);

			// Leia a memória do processo.
			boolean success = Kernel32.INSTANCE.ReadProcessMemory(processHandle, baseAddress, buffer, buffer.length,
					bytesRead);
			if (success && bytesRead.getValue() > 0) {
				// Verifique se o buffer contém a string.
				String chunk = new String(buffer, 0, bytesRead.getValue(), java.nio.charset.StandardCharsets.US_ASCII);
				int index = chunk.indexOf(stringToFind);
				if (index != -1) {
					// Calcule o endereço exato da string encontrada.
					long foundAddress = address + index;
					System.out.printf("String encontrada: '%s' no endereço: 0x%X%n", stringToFind, foundAddress);
				}
			}
		}

		// Feche o handle do processo.
		Kernel32.INSTANCE.CloseHandle(processHandle);
	}

	public static void procurarShort(int processId, short valueToFind) {
		// int processId = 16376; // Substitua pelo PID do processo alvo.
		// short valueToFind = 91; // Valor do tipo short para encontrar.

		// Abra o processo
		Pointer processHandle = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ, false, processId);
		if (processHandle == null) {
			System.err.println("Não foi possível abrir o processo.");
			return;
		}

// Defina o intervalo de memória para escanear
		long startAddress = 0x00000000; // Início do espaço de memória.
		long endAddress = 0x7FFFFFFF; // Fim do espaço de memória.

		byte[] buffer = new byte[2]; // Para ler valores short (2 bytes).
		IntByReference bytesRead = new IntByReference();

		for (long address = startAddress; address < endAddress; address += 2) { // Incremento de 2 bytes (tamanho de um
																				// short)
			Pointer baseAddress = new Pointer(address);

			boolean success = Kernel32.INSTANCE.ReadProcessMemory(processHandle, baseAddress, buffer, buffer.length,
					bytesRead);
			if (success && bytesRead.getValue() == 2) {
				short foundValue = java.nio.ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
				if (foundValue == valueToFind) {
					System.out.printf("Valor encontrado: %d no endereço: 0x%X%n", foundValue, address);
				}
			}
		}

// Feche o handle do processo
		Kernel32.INSTANCE.CloseHandle(processHandle);

	}

	public static void procurarInt(int processId, int valueToFind) {
		// int processId = 16376; // Substitua pelo PID do processo alvo.
		// int valueToFind = 91;

		// Abra o processo
		Pointer processHandle = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ, false, processId);
		if (processHandle == null) {
			System.err.println("Não foi possível abrir o processo.");
			return;
		}

		// Defina o intervalo de memória para escanear
		long startAddress = 0x00000000; // Início do espaço de memória.
		long endAddress = 0x7FFFFFFF; // Fim do espaço de memória.

		byte[] buffer = new byte[4]; // Para ler valores inteiros (4 bytes).
		IntByReference bytesRead = new IntByReference();

		for (long address = startAddress; address < endAddress; address += 4) {
			Pointer baseAddress = new Pointer(address);

			boolean success = Kernel32.INSTANCE.ReadProcessMemory(processHandle, baseAddress, buffer, buffer.length,
					bytesRead);
			if (success && bytesRead.getValue() == 4) {
				int foundValue = java.nio.ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
				if (foundValue == valueToFind) {
					System.out.printf("Valor encontrado: %d no endereço: 0x%X%n", foundValue, address);
				}
			}
		}

		// Feche o handle do processo
		Kernel32.INSTANCE.CloseHandle(processHandle);
	}
	
	

	public static void mostrarStringMemoria(int processId, long address1, long address2, int stringLength) {
		// Abra o processo com permissões de leitura.
		Pointer processHandle = Kernel32.INSTANCE
				.OpenProcess(Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION, false, processId);

		if (processHandle == null) {
			System.err.println("Não foi possível abrir o processo.");
			return;
		}

		// Buffers para leitura das strings.
		byte[] buffer1 = new byte[stringLength]; // Buffer para armazenar a string do primeiro endereço.
		byte[] buffer2 = new byte[stringLength]; // Buffer para armazenar a string do segundo endereço.
		IntByReference bytesRead = new IntByReference();

		System.out.println("Monitorando valores de strings... Pressione Ctrl+C para sair.");

		try {
			while (true) {
				// Leia a memória para o primeiro endereço.
				boolean success1 = Kernel32.INSTANCE.ReadProcessMemory(processHandle, new Pointer(address1), buffer1,
						buffer1.length, bytesRead);

				// Leia a memória para o segundo endereço.
				boolean success2 = Kernel32.INSTANCE.ReadProcessMemory(processHandle, new Pointer(address2), buffer2,
						buffer2.length, bytesRead);

				if (success1 && success2 && bytesRead.getValue() > 0) {
					// Converta os buffers para strings (remova os caracteres após o `null` se
					// existirem).
					String string1 = new String(buffer1, 0, bytesRead.getValue(), "UTF-8").split("\0")[0];
					String string2 = new String(buffer2, 0, bytesRead.getValue(), "UTF-8").split("\0")[0];

					// Imprima os valores das strings no console.
					System.out.printf("Strings atuais: String1='%s', String2='%s'%n", string1, string2);
				} else {
					System.err.println("Erro ao ler memória.");
				}

				// Aguarde um pouco antes de ler novamente (100 ms).
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			System.err.println("Monitoramento interrompido.");
		} catch (Exception e) {
			System.err.println("Erro ao processar strings: " + e.getMessage());
		} finally {
			// Feche o handle do processo.
			Kernel32.INSTANCE.CloseHandle(processHandle);
		}
	}

	public static void mostrarValorMemoria(int processId, int addressX, int addressY) {
		// int processId = 16376; // Substitua pelo PID do processo.

		// Endereços de memória das coordenadas.
		// long addressX = 0x19D468; // Endereço da coordenada X.
		// long addressX = 0x808D710; //Esse atualiza toda hora... Interesting
		// long addressY = 0x19D46C; // Endereço da coordenada Y.
		// long addressY = 0x1557FC0; //Esse atualiza toda hora... Interesting

		// Abra o processo com permissões de leitura.
		Pointer processHandle = Kernel32.INSTANCE
				.OpenProcess(Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION, false, processId);

		if (processHandle == null) {
			System.err.println("Não foi possível abrir o processo.");
			return;
		}

		// Buffers para leitura das coordenadas.
		byte[] bufferX = new byte[4]; // Buffer para armazenar o valor de X (4 bytes para um int).
		byte[] bufferY = new byte[4]; // Buffer para armazenar o valor de Y (4 bytes para um int).
		IntByReference bytesRead = new IntByReference();

		System.out.println("Monitorando coordenadas... Pressione Ctrl+C para sair.");

		try {
			while (true) {
				// Leia a memória para a coordenada X.
				boolean successX = Kernel32.INSTANCE.ReadProcessMemory(processHandle, new Pointer(addressX), bufferX,
						bufferX.length, bytesRead);

				// Leia a memória para a coordenada Y.
				boolean successY = Kernel32.INSTANCE.ReadProcessMemory(processHandle, new Pointer(addressY), bufferY,
						bufferY.length, bytesRead);

				if (successX && successY && bytesRead.getValue() == 4) {
					// Converta os buffers para valores inteiros.
					int x = java.nio.ByteBuffer.wrap(bufferX).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
					int y = java.nio.ByteBuffer.wrap(bufferY).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

					// Imprima as coordenadas no console.
					System.out.printf("Coordenadas atuais: X=%d, Y=%d%n", x, y);
				} else {
					System.err.println("Erro ao ler memória.");
				}

				// Aguarde um pouco antes de ler novamente (100 ms).
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			System.err.println("Monitoramento interrompido.");
		} finally {
			// Feche o handle do processo.
			Kernel32.INSTANCE.CloseHandle(processHandle);
		}
	}

	public static void findDuplicateAddresses(String filePath) {
		// Regex para capturar os endereços do padrão "0xXXXXXXXX"
		String regex = "0x[0-9A-Fa-f]+";
		Pattern pattern = Pattern.compile(regex);

		// Mapa para contar a ocorrência de cada endereço
		Map<String, Integer> addressCount = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;

			// Ler o arquivo linha por linha
			while ((line = reader.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				// Procurar todos os endereços na linha
				while (matcher.find()) {
					String address = matcher.group();
					addressCount.put(address, addressCount.getOrDefault(address, 0) + 1);
				}
			}

			// Verificar e imprimir os endereços que se repetiram
			System.out.println("Endereços repetidos:");
			addressCount.forEach((address, count) -> {
				if (count > 1) {
					System.out.println(address);
				}
			});

		} catch (IOException e) {
			System.err.println("Erro ao ler o arquivo: " + e.getMessage());
		}
	}

	public static Coordenadas escolherProximaCoordenada(List<Coordenadas> caminhoCalculado, Coordenadas atual) {
		if (caminhoCalculado.isEmpty())
			return atual;

		// Variáveis para rastrear direção inicial
		int direcaoX = 0, direcaoY = 0;
		Coordenadas ultimaCoordenadaValida = atual;

		// Percorrer o caminho calculado a partir da coordenada atual
		for (int i = 1; i < caminhoCalculado.size() && i <= 10; i++) {
			Coordenadas proxima = caminhoCalculado.get(i);

			// Calcular direção entre a coordenada atual e a próxima
			int novaDirecaoX = Integer.signum(proxima.x - atual.x);
			int novaDirecaoY = Integer.signum(proxima.y - atual.y);

			// Na primeira iteração, definir a direção inicial
			if (i == 1) {
				direcaoX = novaDirecaoX;
				direcaoY = novaDirecaoY;
			}

			// Verificar se a direção mudou
			if (novaDirecaoX != direcaoX || novaDirecaoY != direcaoY) {
				// Se a direção mudou, retornar a última coordenada válida antes da curva
				return ultimaCoordenadaValida;
			}

			// Atualizar a última coordenada válida
			ultimaCoordenadaValida = proxima;
		}

		// Se não houver curva e atingirmos a distância máxima, retornar a última
		// coordenada possível
		return ultimaCoordenadaValida;
	}

	public static void setarMouseEmCoordenadaTela(Coordenadas atual, Coordenadas destino, int xJanela, int yJanela) {
		// Calcular o deslocamento entre a coordenada atual e a de destino
		int dx = destino.x - atual.x;
		int dy = destino.y - atual.y;

		// Calcular a posição do mouse na tela com base no deslocamento
		int mouseX = xJanela + 505 + dx * 18 + 9;
		int mouseY = yJanela + 376 - dy * 18 + 9;

		// Mover o mouse para a posição calculada
		Robot r;
		try {
			r = new Robot();
			r.mouseMove(mouseX, mouseY);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Coordenadas getCoordenadasTelaPeloMouse(Coordenadas atual, int mouseX, int mouseY, int xJanela,
			int yJanela) {
		// Obter a posição atual do mouse
		java.awt.Point mousePos = java.awt.MouseInfo.getPointerInfo().getLocation();
		int mx = (int) mousePos.getX() - xJanela;
		int my = (int) mousePos.getY() - yJanela;
		// int mx = mouseX - xJanela;
		// int my = mouseY - yJanela;

		int x = Math.abs((mx - 505) / 18);
		if (mx > 505) {
			x += atual.x;
		} else {
			x -= atual.x - 1;
		}
		int y = Math.abs((my - 376) / 18);
		if (my < 376) {
			y += atual.y + 1;
		} else {
			y -= atual.y;
		}
		x = Math.abs(x);
		y = Math.abs(y);
		System.out.println("Coordenadas pela tela: x: " + x + " y: " + y);
		return new Coordenadas(x, y);
	}

	public static HWND hwnd;

	private static Coordenadas getWidthHeight() {
		Coordenadas janela = new Coordenadas(0, 0);
		try {
			User32 user32 = User32.INSTANCE;
			// HWND hwnd = user32.FindWindow(null, "History Reborn | Gepard Shield 3.0
			// (^-_-^)"); // Nome da janela do Ragnarok
			hwnd = user32.FindWindow(null, "History Reborn | Gepard Shield 3.0 (^-_-^)"); // Nome da janela do Ragnarok
			MyUser32 myUser32 = MyUser32.INSTANCE; // Usar a interface personalizada

			if (hwnd == null) {
				System.out.println("Janela do Ragnarok não encontrada.");
				return null;
			}
			// Garantir que a janela tenha o foco
			User32.INSTANCE.SetForegroundWindow(hwnd);

			// Obter as dimensões do cliente
			RECT clientRect = new RECT();
			user32.GetClientRect(hwnd, clientRect);

			// Converter coordenadas do cliente para coordenadas da tela
			POINT topLeft = new POINT(0, 0);
			// user32.ClientToScreen(hwnd, topLeft);
			boolean success = myUser32.ClientToScreen(hwnd, topLeft);

			if (!success) {
				System.out.println("Falha ao converter coordenadas do cliente para coordenadas da tela.");
				return null;
			}

			janela = new Coordenadas(topLeft.x, topLeft.y);
			int xJanela = topLeft.x;
			int yJanela = topLeft.y;
			int width = clientRect.right - clientRect.left;
			int height = clientRect.bottom - clientRect.top;

			System.out.println("Resolução da janela do Ragnarok: " + width + "x" + height);
			System.out.println("Posição da janela do Ragnarok: " + xJanela + " " + yJanela);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return janela;
	}
	
	public static int obterBuffs(int processId, long addressHP) {
        Pointer processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION,
                false,
                processId
        );

        if (processHandle == null) {
            System.err.println("Não foi possível abrir o processo.");
            return -1; // Retorna -1 para indicar erro
        }

        byte[] bufferHP = new byte[4]; // Buffer para o valor de HP (4 bytes para um int)
        IntByReference bytesRead = new IntByReference();

        try {
            boolean successHP = Kernel32.INSTANCE.ReadProcessMemory(
                    processHandle,
                    new Pointer(addressHP),
                    bufferHP,
                    bufferHP.length,
                    bytesRead
            );

            if (successHP && bytesRead.getValue() == 4) {
                // Converte o buffer para um inteiro
                return java.nio.ByteBuffer.wrap(bufferHP).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
            } else {
                System.err.println("Erro ao ler memória para HP.");
                return -1;
            }

        } finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
    }

}
