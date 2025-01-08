package com.ragnarokbot.main;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.sun.jna.platform.win32.WinDef;

import com.ragnarokbot.bot.Bot;
import com.ragnarokbot.bot.ProcessSelector;
import com.ragnarokbot.bot.Tela;
import com.ragnarokbot.model.AStar;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.GrafoMapa;
import com.ragnarokbot.model.MemoryScanner;
import com.ragnarokbot.model.MemoryScanner.Kernel32;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

import config.ConfigManager;
import config.ConfigManager.Config;

public class BotRagnarok {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		/*
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

        Tela tela = new Tela(bot);
        SwingUtilities.invokeLater(() -> tela.setVisible(true)); //Exibe a janela
        //SwingUtilities.invokeLater( () -> new Tela(bot));
        
        GameController gameController = new GameController(bot, tela);*/
        
        //Apagar
        Scalar[] limites = calcularLimites(33, 16, 16);
        System.out.println("Lower: " + limites[0]);
        System.out.println("Upper: " + limites[1]);
        //Apagar
        
        /*
        GrafoMapa grafo = new GrafoMapa();

        // Adiciona conexões no grafo
        grafo.addConexao(new Coordenadas(11, 20), new Coordenadas(12, 15));
        grafo.addConexao(new Coordenadas(12, 15), new Coordenadas(12, 10));
        grafo.addConexao(new Coordenadas(12, 15), new Coordenadas(11, 20));
        grafo.addConexao(new Coordenadas(12, 10), new Coordenadas(15, 10));
        grafo.addConexao(new Coordenadas(15, 10), new Coordenadas(17, 15));
        grafo.addConexao(new Coordenadas(17, 15), new Coordenadas(17, 20));
        
        grafo.addConexao(new Coordenadas(12, 10), new Coordenadas(13, 5));
        grafo.addConexao(new Coordenadas(15, 10), new Coordenadas(14, 8));
        grafo.addConexao(new Coordenadas(13, 5), new Coordenadas(14, 2));
        grafo.addConexao(new Coordenadas(14, 8), new Coordenadas(13, 5));

        Coordenadas inicio = new Coordenadas(17, 20); // Coordenada atual não mapeada
        Coordenadas destino = new Coordenadas(14, 2); // Coordenada final não mapeada

        AStar aStar = new AStar();
        List<Coordenadas> caminho = aStar.calcularCaminhoComExpansao(inicio, destino, grafo);

        for (Coordenadas nodo : caminho) {
            System.out.println(nodo.x + " " + nodo.y);
        }*/
        
        SwingUtilities.invokeLater(() -> {
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

                        Tela tela = new Tela(bot);
                        SwingUtilities.invokeLater(() -> tela.setVisible(true)); //Exibe a janela
                        //SwingUtilities.invokeLater( () -> new Tela(bot));
                        
                        GameController gameController = new GameController(bot, tela);
						gameController.run();
                        
                        
                        
                        /*GrafoMapa mapa = gerarGrafoDeMapa(carregarMapa("bio.png"));
                        Coordenadas atual = new Coordenadas(136,237);
                        Coordenadas destino = new Coordenadas(122, 210);
                        AStar aStar = new AStar();
                        List<Coordenadas> caminho = aStar.calcularCaminhoComExpansao(atual, destino, mapa);
                        for (Coordenadas nodo : caminho) {
                            System.out.println(nodo.x + " " + nodo.y);
                        }*/
                        
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
            });
        });
        
        //gameController.run();
        System.out.println("hasque00");
        
        
        
        /*
        int processId = 11716; // Substitua pelo PID do processo alvo.
        int valueToFind = 149;

        // Abra o processo
        Pointer processHandle = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ, false, processId);
        if (processHandle == null) {
            System.err.println("Não foi possível abrir o processo.");
            return;
        }

        // Defina o intervalo de memória para escanear
        long startAddress = 0x00000000; // Início do espaço de memória.
        long endAddress = 0x7FFFFFFF;   // Fim do espaço de memória.

        byte[] buffer = new byte[4]; // Para ler valores inteiros (4 bytes).
        IntByReference bytesRead = new IntByReference();

        for (long address = startAddress; address < endAddress; address += 4) {
            Pointer baseAddress = new Pointer(address);

            boolean success = Kernel32.INSTANCE.ReadProcessMemory(processHandle, baseAddress, buffer, buffer.length, bytesRead);
            if (success && bytesRead.getValue() == 4) {
                int foundValue = java.nio.ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                if (foundValue == valueToFind) {
                    System.out.printf("Valor encontrado: %d no endereço: 0x%X%n", foundValue, address);
                }
            }
        }

        // Feche o handle do processo
        Kernel32.INSTANCE.CloseHandle(processHandle);
        */
        
        
        
        
        
        /*
        int processId = 11716; // Substitua pelo PID do processo.

        // Endereços de memória das coordenadas.
        //long addressX = 0x19D468; // Endereço da coordenada X.
        long addressX = 0x1557FBC; //Esse atualiza toda hora... Interesting
        //long addressY = 0x19D46C; // Endereço da coordenada Y.
        long addressY = 0x1557FC0; //Esse atualiza toda hora... Interesting

        // Abra o processo com permissões de leitura.
        Pointer processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_VM_READ | Kernel32.PROCESS_QUERY_INFORMATION,
                false,
                processId
        );

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
                boolean successX = Kernel32.INSTANCE.ReadProcessMemory(
                        processHandle,
                        new Pointer(addressX),
                        bufferX,
                        bufferX.length,
                        bytesRead
                );

                // Leia a memória para a coordenada Y.
                boolean successY = Kernel32.INSTANCE.ReadProcessMemory(
                        processHandle,
                        new Pointer(addressY),
                        bufferY,
                        bufferY.length,
                        bytesRead
                );

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
        */
        
        /*
        //Procurar string
        int processId = 20732; // Substitua pelo PID do processo alvo.
        String stringToFind = "sapatolegal"; // Substitua pela string que deseja procurar.

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
        long endAddress = 0x7FFFFFFFL;   // Fim do espaço de memória.

        byte[] buffer = new byte[4096]; // Buffer para leitura em blocos.
        IntByReference bytesRead = new IntByReference();

        for (long address = startAddress; address < endAddress; address += buffer.length) {
            Pointer baseAddress = new Pointer(address);

            // Leia a memória do processo.
            boolean success = Kernel32.INSTANCE.ReadProcessMemory(processHandle, baseAddress, buffer, buffer.length, bytesRead);
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
        */
        
        


	    
	    /*
	    tive que instalar o tesseract e pegar a dll do opencv na pasta "opencv\build\java\x64" copiar a dll e jogar na pasta libs
	    ir no projeto botao direito run as run configurations, na parte esquerda clicar no java aplication BotRagnarok
	    arguments na direita
	    VM arguments colocar esse codigo: -Djava.library.path="C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/libs"
	    */
	    
        
	}
	
	public static GrafoMapa gerarGrafoDeMapa(Map<Coordenadas, Boolean> mapaCoordenadas) {
	    GrafoMapa grafo = new GrafoMapa();

	    for (Map.Entry<Coordenadas, Boolean> entrada : mapaCoordenadas.entrySet()) {
	        Coordenadas atual = entrada.getKey();
	        if (entrada.getValue()) { // Se pode andar na coordenada
	            // Verificar vizinhos (4 direções: cima, baixo, esquerda, direita)
	            Coordenadas[] vizinhos = {
	                new Coordenadas(atual.x, atual.y + 1),
	                new Coordenadas(atual.x, atual.y - 1),
	                new Coordenadas(atual.x + 1, atual.y),
	                new Coordenadas(atual.x - 1, atual.y)
	            };

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
        Scalar lower = new Scalar(
            Math.max(hue - hueTolerance, 0),
            Math.max(sat - satTolerance, 0),
            Math.max(val - valTolerance, 0)
        );

        Scalar upper = new Scalar(
            Math.min(hue + hueTolerance, 180),
            Math.min(sat + satTolerance, 255),
            Math.min(val + valTolerance, 255)
        );

        return new Scalar[]{lower, upper};
    }

}

