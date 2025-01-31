package com.ragnarokbot.bot;

import java.awt.AWTException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.ragnarokbot.main.BotRagnarok;
import com.ragnarokbot.main.GameController;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.GrafoMapa;
import com.ragnarokbot.model.MemoryScanner;
import com.ragnarokbot.model.MonstrosImagem;
import com.ragnarokbot.model.MyUser32;
import com.ragnarokbot.model.OcrResult;
import com.ragnarokbot.model.enums.Estado;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

import config.ConfigManager;
import config.ConfigManager.Config;
import config.ContasConfig;
import config.Pin;
import config.ScriptLoader;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.POINT;

public class Bot {
	
	private Robot robot;
	private ITesseract tesseract;
	private ITesseract tesseractLetras;
	
	public static HWND hwnd;
	
    private int coordenadasJogadorTelaX;
    private int coordenadasJogadorTelaY;
    private int width;
    private int height;
    
    private int xJanela;
    private int yJanela;
    
    public List<Skill> skills = new ArrayList<>();

    private MemoryScanner memoria = new MemoryScanner();
    
    //notebook
  	long tempoExecucao = System.currentTimeMillis();
    
    //Variaveis para coordenadas mini mapa
    public Config configOCR;

	public Bot(ITesseract tesseract, Robot robot, ITesseract tesseractLetras) {
		
		this.tesseract = tesseract;
		this.tesseractLetras = tesseractLetras;
        this.robot = robot;
        this.configOCR = ConfigManager.loadConfig();
        
        getWidthHeight();
        this.coordenadasJogadorTelaX = width / 2;
        this.coordenadasJogadorTelaY = height / 2;
		
	}
	
	/*
	public String ocr(int x, int y, int width, int height) throws IOException, TesseractException {
		Rectangle area = new Rectangle(x, y, width, height);
        BufferedImage areaCapturada = robot.createScreenCapture(area);
        
        // Salva a imagem capturada para verificar se está correta (opcional)
        //ImageIO.write(areaCapturada, "png", new File("hp_capture.png"));
        //System.out.println("Imagem do HP salva como 'hp_capture.png'.");
        
        // Executa o OCR na imagem capturada
        String ocr = tesseract.doOCR(areaCapturada);
        
        System.out.println("ocr: " + ocr);
		
		return ocr;
	}*/
	
	public String ocr(int x, int y, int width, int height) {
		Rectangle area = new Rectangle(x, y, width, height);
        BufferedImage areaCapturada = robot.createScreenCapture(area);
        
        // Converter a captura de tela para um formato Mat (imagem OpenCV)
        Mat matImage = bufferedImageToMat2(areaCapturada);
        
        // Aplicar algum processamento de imagem (exemplo: conversão para escala de cinza)
        Mat grayImage = new Mat();
        Imgproc.cvtColor(matImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        
        
        // Converter Mat de volta para BufferedImage para o OCR
        BufferedImage bufferedGrayImage = matToBufferedImage(grayImage);
        
        // Executa o OCR na imagem capturada
        String ocr = "";
		try {
			ocr = tesseract.doOCR(bufferedGrayImage);
		} catch (TesseractException e) {
			e.printStackTrace();
		}
        
        System.out.println("ocr: " + ocr);
		
		return ocr;
	}
	
	// Método para converter BufferedImage para Mat (OpenCV)
    public Mat bufferedImageToMat2(BufferedImage image) {
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                byte r = (byte) ((rgb >> 16) & 0xFF);
                byte g = (byte) ((rgb >> 8) & 0xFF);
                byte b = (byte) (rgb & 0xFF);
                mat.put(y, x, new byte[]{b, g, r});
            }
        }
        return mat;
    }
    
    public BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();

        // Se a imagem for em escala de cinza (1 canal), use BufferedImage.TYPE_BYTE_GRAY
        // Se for colorida (3 canais), use BufferedImage.TYPE_3BYTE_BGR
        int type = (channels == 1) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;

        byte[] data = new byte[width * height * channels];
        mat.get(0, 0, data);

        BufferedImage image = new BufferedImage(width, height, type);

        // Definir os dados de pixels da imagem
        image.getRaster().setDataElements(0, 0, width, height, data);

        return image;
    }
	
	
	public String ocrCoordenadas() {
		return this.ocr( xJanela + configOCR.rectangle.x, yJanela + configOCR.rectangle.y,configOCR.rectangle.width,configOCR.rectangle.height);
	}
	
	public Coordenadas obterCoordenadasMemoria() {
        Coordenadas atual = memoria.obterCoordenadas(memoria.processId, memoria.addressX, memoria.addressY);
        //System.out.println("Coordenadas atuais: " + atual);
        return atual;
	}
	
	public String ocrLetras(int x, int y, int width, int height) throws IOException, TesseractException {
		Rectangle area = new Rectangle(x, y, width, height);
        BufferedImage areaCapturada = robot.createScreenCapture(area);
        String ocr = tesseractLetras.doOCR(areaCapturada);
        
        //System.out.println("ocr letras: " + ocr);
		
		return ocr;
	}
	
	public void selecionarOpcao(int opcaoEscolhida) {
		if (opcaoEscolhida == 1) {
   		 apertarTecla(KeyEvent.VK_ENTER);
	   	} else {
	   		for (int i = 0; i < opcaoEscolhida - 1; i++) {
	       		apertarTecla(KeyEvent.VK_DOWN);
	       		sleep(200);
	       	}
	   		apertarTecla(KeyEvent.VK_ENTER);
	   	 }
	}
	
	public BufferedImage printarTela() {
		
		// Captura a tela da área da janela
        Rectangle captureArea = new Rectangle(xJanela, yJanela, width, height);
        BufferedImage print = robot.createScreenCapture(captureArea);
		
        File outputFile = new File("tela.png");
        try {
			ImageIO.write(print, "png", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        System.out.println("Printado");
        return print;
	}
	
	private Mat bufferedImageToMat(BufferedImage image) {
	    // Converter a imagem para o tipo compatível com OpenCV (TYPE_3BYTE_BGR)
	    BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
	    Graphics2D g = convertedImg.createGraphics();
	    g.drawImage(image, 0, 0, null);
	    g.dispose();

	    // Obter os bytes da imagem convertida
	    byte[] pixels = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();

	    // Criar o Mat e preencher com os bytes
	    Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
	    mat.put(0, 0, pixels);

	    return mat;
	}
	
	/*
	public MonstrosImagem analisarTela(Scalar lowerColor, Scalar upperColor) throws IOException {
		
		// Capturar a tela da área definida
	    Rectangle captureArea = new Rectangle(xJanela, yJanela, width, height);
	    BufferedImage screenFullImage = robot.createScreenCapture(captureArea);

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(screenFullImage);
	    if (screen.empty()) {
	        System.out.println("Erro ao carregar a imagem.");
	        return null;
	    }

	    // Converter a imagem para o espaço de cores HSV
	    Mat hsvImage = new Mat();
	    Imgproc.cvtColor(screen, hsvImage, Imgproc.COLOR_BGR2HSV);

	    // Definir os limites de cor
	    //Scalar lowerColor = new Scalar(148, 200, 200);  // Limite inferior (rosa)
	    //Scalar upperColor = new Scalar(154, 255, 255);  // Limite superior (rosa)

	    // Criar máscara para identificar a cor dentro do intervalo
	    Mat mask = new Mat();
	    Core.inRange(hsvImage, lowerColor, upperColor, mask);

	    // Encontrar contornos na máscara
	    List<MatOfPoint> entidades = new ArrayList<>();
	    Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	    
	    
		
	    return new MonstrosImagem(entidades, screen);
	}*/
	
	public void inserirPin(String pin) {
	    // Capturar a tela da área definida
	    Rectangle captureArea = new Rectangle(xJanela, yJanela, width, height);
	    BufferedImage screenFullImage = robot.createScreenCapture(captureArea);

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(screenFullImage);
	    if (screen.empty()) {
	        System.out.println("Erro ao carregar a imagem.");
	        return;
	    }

	    // Converter a imagem para o espaço de cores HSV
	    Mat hsvImage = new Mat();
	    Imgproc.cvtColor(screen, hsvImage, Imgproc.COLOR_BGR2HSV);

	    // Definir os limites de cor
	    Scalar lowerColor = new Scalar(0, 91, 0); // Limite inferior (ajuste conforme necessário)
	    Scalar upperColor = new Scalar(10, 171, 73); // Limite superior (ajuste conforme necessário)

	    // Criar máscara para identificar a cor dentro do intervalo
	    Mat mask = new Mat();
	    Core.inRange(hsvImage, lowerColor, upperColor, mask);

	    // Encontrar contornos na máscara
	    List<MatOfPoint> entidades = new ArrayList<>();
	    Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

	    if (entidades.isEmpty()) {
	        System.out.println("Nenhum quadrado encontrado.");
	        return;
	    }

	    // Filtrar quadrados com altura maior que 20 pixels
	    List<MatOfPoint> filtrados = entidades.stream()
	        .filter(entidade -> (Imgproc.boundingRect(entidade).height >= 50 && Imgproc.boundingRect(entidade).height <= 65)
	        		&& (Imgproc.boundingRect(entidade).width >= 50 && Imgproc.boundingRect(entidade).width <= 65))
	        .toList();

	    // Desenhar contornos na imagem original (BGR)
	    for (MatOfPoint ponto : filtrados) {
	        Imgproc.drawContours(screen, List.of(ponto), -1, new Scalar(0, 255, 0), 2);
	    }

	    // Salvar a imagem com os contornos
	    String outputPath = "pin.png";
	    MatOfByte matOfByte = new MatOfByte();
	    Imgcodecs.imencode(".png", screen, matOfByte); // Certifique-se de usar `screen` com contornos desenhados
	    byte[] byteArray = matOfByte.toArray();
	    try (FileOutputStream fos = new FileOutputStream(outputPath)) {
	        fos.write(byteArray);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    List<Rect> pins = new ArrayList<>();
	    for (int i = 0; i < 10; i++) {
	    	pins.add(new Rect());
	    }
	    boolean jsonExiste = arquivoExiste("config/pins.json");
	    ScriptLoader scriptLoader = new ScriptLoader();
	    Map<String, Integer> hashReferencia;
	    if (jsonExiste) {
	    	hashReferencia = scriptLoader.carregarPin("config/pins.json");
	    } else {
	    	hashReferencia = new HashMap<>();
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000000111100000000000111111000000000111001110000000011000111000000001100011100000000111001110000000011111110000000000111110000000000000000000000000000000000000000000000000000000000000000000000", 0);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000000111100000000000111111000000000111111110000000011111111000000001111111100000000111111110000000011111110000000000111110000000000000000000000000000000000000000000000000000000000000000000000", 1);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000000111000000000000011110000000000011111100000000011111110000000001111111000000000011111100000000000111110000000000011111000000000000000000000000000000000000000000000000000000000000000000000", 2);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000000111000000000000011110000000000011111100000000011111110000000001111111000000000011111100000000000111100000000000011110000000000000000000000000000000000000000000000000000000000000000000000", 3);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000001100000000000000111110000000000011111100000000001111110000000000111111000000000011111100000000000011110000000000000011000000000000000000000000000000000000000000000000000000000000000000000", 4);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000000111100000000000011110000000000011111100000000011111111000000001111111100000000011111100000000000111100000000000011100000000000000000000000000000000000000000000000000000000000000000000000", 5);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000001110000000000000111000000000000011100000000000001111110000000000111111100000000011100110000000000111111000000000001111000000000000000000000000000000000000000000000000000000000000000000000", 6);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000001111110000000000111111000000000111111100000000011111111000000001111111100000000111111110000000001111110000000000011110000000000000000000000000000000000000000000000000000000000000000000000", 7);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000000011000000000000111111000000000011111100000000001111110000000000111111000000000011001100000000001111110000000000011110000000000000000000000000000000000000000000000000000000000000000000000", 8);
		    hashReferencia.put("0000000000000000000000000000000000000000000000000000000000000000000000111000000000001111110000000000111011100000000011111110000000001111111000000000000111100000000000000110000000000000011000000000000000000000000000000000000000000000000000000000000000000000", 9);
		    
	    }
	    
	    //Achar as coordenadas do OK
	    int maiorX = 0;
	    int menorY = 99999;
	    int rectWidth = 0;
	    int rectHeight = 0;
	    
	    // Lista para armazenar os hashes
        List<Map<String, String>> pinsList = new ArrayList<>();
        
	    for(int i = 0; i < filtrados.size(); i++) {
            Rect rect = Imgproc.boundingRect(filtrados.get(i));

            Rectangle captureArea2 = new Rectangle(xJanela + rect.x, yJanela + rect.y, rect.width, rect.height);
            BufferedImage print = robot.createScreenCapture(captureArea2);
            
            // Calcular o hash da imagem capturada
            String hash = calcularHash(print);
            System.out.println("Hash da imagem " + i + ": " + hash);
            
            Integer numero = hashReferencia.get(hash);
            if (numero != null) {
                System.out.println("Número identificado: " + numero);
                pins.set(numero, rect);
            } else {
                System.out.println("Número não identificado.");
            }
            
            if (rect.x >= maiorX) {
            	maiorX = rect.x;
            }
            if (rect.y <= menorY) {
            	menorY = rect.y;
            }
            rectWidth = rect.width;
            rectHeight = rect.height;

            if (!jsonExiste) {
            	//Salvar imagem
                File outputFile = new File("imagem" + i + ".png");
                try {
                	ImageIO.write(print, "png", outputFile);
                } catch (IOException e) {
                	e.printStackTrace();
                }
                
                // Adiciona o hash ao JSON
                Map<String, String> pinData = new HashMap<>();
                pinData.put("imagem" + i, hash);
                pinsList.add(pinData);
            }
        }
	    if (!jsonExiste) {
	    	// Criar e salvar o JSON
	    	salvarJson(pinsList, "config/pins.json");
	    }
        
	    try {
	        for(int j = 0; j < pin.length(); j++) {
	        	char c = pin.charAt(j);
	        	int numeroConvertido = Character.getNumericValue(c);
	        	
	        	Rect pinRect = pins.get(numeroConvertido);
	        	int centerX = xJanela + pinRect.x + pinRect.width / 2;
	            int centerY = yJanela + pinRect.y + pinRect.height / 2;
	            moverMouse(centerX, centerY);
	            Thread.sleep(500);
	            clicarMouse();
	            Thread.sleep(500);
	        }
	        System.out.println("Colocar o mouse no Ok");
		    moverMouse(xJanela + maiorX + rectWidth + 67, yJanela + menorY + (2 * rectHeight) + 7);
		    Thread.sleep(500);
		    System.out.println("Clicou no Ok");
	        clicarMouse();
	    } catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String calcularHash(BufferedImage image) {
	    try {
	        // Redimensionar para 16x16 (resolução intermediária)
	        BufferedImage resized = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_GRAY);
	        Graphics2D g = resized.createGraphics();
	        g.drawImage(image, 0, 0, 16, 16, null); // Redimensionando para 16x16
	        g.dispose();

	        // Converter para escala de cinza
	        Raster raster = resized.getRaster();
	        int[] pixels = new int[256]; // 16x16 = 256 pixels
	        raster.getPixels(0, 0, 16, 16, pixels);

	        // Calcular a média dos valores dos pixels
	        double media = Arrays.stream(pixels).average().orElse(0);

	        // Gerar o hash binário
	        StringBuilder hash = new StringBuilder();
	        for (int pixel : pixels) {
	            hash.append(pixel > media ? "1" : "0");
	        }

	        return hash.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private boolean arquivoExiste(String caminhoArquivo) {
        File arquivo = new File(caminhoArquivo);
        return arquivo.exists() && arquivo.isFile();
    }

	private void salvarJson(List<Map<String, String>> pinsList, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode pinsArray = objectMapper.createArrayNode();

        for (Map<String, String> pin : pinsList) {
            ObjectNode pinNode = objectMapper.createObjectNode();
            pin.forEach(pinNode::put);
            
            pinNode.put("numero", 0);
            
            pinsArray.add(pinNode);
        }

        rootNode.set("pins", pinsArray);

        File file = new File(filePath);
        file.getParentFile().mkdirs(); // Garante que a pasta config/ exista

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
            System.out.println("JSON salvo em: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	// notebook
	public MonstrosImagem analisarTela(Map<String, Scalar[]> colorRanges) {

		// Definir a área onde os monstros aparecem na tela capturada
		int areaX = 235; // Coordenada x da área de interesse 233
		int areaY = 106; // Coordenada y da área de interesse 192
		int areaWidth = 555; // Largura da área de interesse 560
		int areaHeight = 555; // Altura da área de interesse 403
	
		// Capturar a tela da área definida
		Rectangle captureArea = null;
		if (GameController.stateMachine.getEstadoAtual().equals(Estado.ANDANDO)
				|| GameController.stateMachine.getEstadoAtual().equals(Estado.ATACANDO)) {
			captureArea = new Rectangle(xJanela + areaX, yJanela + areaY, areaWidth, areaHeight);
		} else {
			captureArea = new Rectangle(xJanela, yJanela, width, height);
		}
		BufferedImage screenFullImage = robot.createScreenCapture(captureArea);
	
		// Converter BufferedImage para Mat diretamente
		Mat screen = bufferedImageToMat(screenFullImage);
		if (screen.empty()) {
			System.out.println("Erro ao carregar a imagem.");
			return null;
		}
	
		// Converter a imagem para o espaço de cores HSV
		Mat hsvImage = new Mat();
		Imgproc.cvtColor(screen, hsvImage, Imgproc.COLOR_BGR2HSV);
	
		// Mapear os resultados
		Map<String, List<MatOfPoint>> detectedEntities = new HashMap<>();
	
		for (Map.Entry<String, Scalar[]> entry : colorRanges.entrySet()) {
			String colorName = entry.getKey();
			Scalar lowerColor = entry.getValue()[0];
			Scalar upperColor = entry.getValue()[1];
	
			// Criar máscara para identificar a cor dentro do intervalo
			Mat mask = new Mat();
			Core.inRange(hsvImage, lowerColor, upperColor, mask);
	
			// Encontrar contornos na máscara
			List<MatOfPoint> entidades = new ArrayList<>();
			Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	
			// Armazenar os contornos filtrados associados à cor
			detectedEntities.put(colorName, entidades);
		}
	
		return new MonstrosImagem(detectedEntities, screen);
	}

/*
	public List<MatOfPoint> listaMonstros() throws IOException {
		// Definir os limites de cor
	    Scalar lowerColor = new Scalar(148, 200, 200);  // Limite inferior (rosa)
	    Scalar upperColor = new Scalar(154, 255, 255);  // Limite superior (rosa)
		
		MonstrosImagem analise = analisarTela(lowerColor, upperColor);
		
		List<MatOfPoint> monstros = analise.listaEntidades;
		
		if (monstros.isEmpty()) {
        	return monstros;
        }
        
	    // Filtrar monstros com altura maior que 20 pixels
	    List<MatOfPoint> monstrosFiltrados = monstros.stream()
	        .filter(monstro -> Imgproc.boundingRect(monstro).height >= 20)
	        .toList();

	    // Retornar monstros visíveis após raycast    
	    return filtrarMonstrosVisiveisRaycast(monstrosFiltrados, analise.screen);
	}*/

	// notebook
		public Map<String, List<MatOfPoint>> listaMonstros() {

			// Definir a área onde os monstros aparecem na tela capturada
			int areaX = 235; // Coordenada x da área de interesse
			int areaY = 106; // Coordenada y da área de interesse

			// Definir os intervalos de cores
			Map<String, Scalar[]> colorRanges = new HashMap<>();
			//colorRanges.put("rosa", new Scalar[] { new Scalar(148, 200, 200), new Scalar(154, 255, 255) });
			colorRanges.put("rosa", new Scalar[] { new Scalar(148, 100, 100), new Scalar(154, 255, 255) });
			colorRanges.put("azul", new Scalar[] { new Scalar(108, 215, 204), new Scalar(128, 255, 255) }); // Azul

			MonstrosImagem analise = analisarTela(colorRanges);

			// Analisar a tela para as cores definidas
			Map<String, List<MatOfPoint>> detectedEntities = analise.listaEntidades;

			// Map para armazenar as listas filtradas e visíveis
			Map<String, List<MatOfPoint>> visibleEntities = new HashMap<>();

			// Processar cada cor
			for (Map.Entry<String, List<MatOfPoint>> entry : detectedEntities.entrySet()) {
				String color = entry.getKey();
				List<MatOfPoint> entities = entry.getValue();

				// Filtrar monstros com altura >= 16
				List<MatOfPoint> filtered = entities.stream().filter(monstro -> Imgproc.boundingRect(monstro).height >= 16)
						.toList();

				// Filtrar por visibilidade usando raycast
				List<MatOfPoint> visible = filtrarMonstrosVisiveisRaycast(filtered, analise.screen);

				// Ajustar as coordenadas para serem globais
				List<MatOfPoint> adjustedEntities = new ArrayList<>();
				for (MatOfPoint entidade : visible) {
					List<org.opencv.core.Point> adjustedPoints = new ArrayList<>();
					for (org.opencv.core.Point p : entidade.toList()) {
						adjustedPoints.add(new org.opencv.core.Point(p.x + areaX, p.y + areaY)); // Ajusta as coordenadas
					}
					MatOfPoint adjustedEntidade = new MatOfPoint();
					adjustedEntidade.fromList(adjustedPoints); // Constrói o MatOfPoint ajustado
					adjustedEntities.add(adjustedEntidade);
				}

				// System.out.println("Tamanho da lista da imagem pequena " +
				// adjustedEntities.size());

				// System.out.println("Monstros visíveis para a cor " + color + ": " +
				// adjustedEntities.size());
				// visibleEntities.put(color, visible); // Adicionar ao mapa de monstros
				// visíveis
				visibleEntities.put(color, adjustedEntities); // Adicionar ao mapa de monstros visíveis
			}

			return visibleEntities;
		}
		
		// notebook
				public Map<String, List<MatOfPoint>> listaMonstrosInstancias() {

					// Definir a área onde os monstros aparecem na tela capturada
					int areaX = 235; // Coordenada x da área de interesse
					int areaY = 106; // Coordenada y da área de interesse

					// Definir os intervalos de cores
					Map<String, Scalar[]> colorRanges = new HashMap<>();
					colorRanges.put("rosa", new Scalar[] { new Scalar(148, 200, 200), new Scalar(154, 255, 255) });
					//colorRanges.put("azul", new Scalar[] { new Scalar(108, 215, 204), new Scalar(128, 255, 255) }); // Azul

					MonstrosImagem analise = analisarTela(colorRanges);

					// Analisar a tela para as cores definidas
					Map<String, List<MatOfPoint>> detectedEntities = analise.listaEntidades;

					// Map para armazenar as listas filtradas e visíveis
					Map<String, List<MatOfPoint>> visibleEntities = new HashMap<>();

					// Processar cada cor
					for (Map.Entry<String, List<MatOfPoint>> entry : detectedEntities.entrySet()) {
						String color = entry.getKey();
						List<MatOfPoint> entities = entry.getValue();

						// Filtrar monstros com altura >= 16
						List<MatOfPoint> filtered = entities.stream().filter(monstro -> Imgproc.boundingRect(monstro).height >= 16)
								.toList();

						// Ajustar as coordenadas para serem globais
						List<MatOfPoint> adjustedEntities = new ArrayList<>();
						for (MatOfPoint entidade : filtered) {
							List<org.opencv.core.Point> adjustedPoints = new ArrayList<>();
							for (org.opencv.core.Point p : entidade.toList()) {
								adjustedPoints.add(new org.opencv.core.Point(p.x + areaX, p.y + areaY)); // Ajusta as coordenadas
							}
							MatOfPoint adjustedEntidade = new MatOfPoint();
							adjustedEntidade.fromList(adjustedPoints); // Constrói o MatOfPoint ajustado
							adjustedEntities.add(adjustedEntidade);
						}

						visibleEntities.put(color, adjustedEntities); // Adicionar ao mapa de monstros visíveis
					}

					return visibleEntities;
				}
	/*
	public List<MatOfPoint> listaNpcs() throws IOException {
		// Definir os limites de cor
	    Scalar lowerColor = new Scalar(0, 151, 215);  // Limite inferior (laranja)
	    Scalar upperColor = new Scalar(20, 231, 255);  // Limite superior (laranja)
	    
	    MonstrosImagem analise = analisarTela(lowerColor, upperColor);
	    
	    List<MatOfPoint> npcs = analise.listaEntidades;
	
			if (npcs.isEmpty()) {
	        	return npcs;
	        }
	        
		    // Filtrar monstros com altura maior que 20 pixels
		    List<MatOfPoint> npcsFiltrados = npcs.stream()
		        .filter(monstro -> Imgproc.boundingRect(monstro).height >= 40)
		        .toList();
	
		    return npcsFiltrados;   
	}*/
	
				// notebook
				public List<MatOfPoint> listaNpcs() {
					// Definir os limites de cor
					Scalar lowerColor = new Scalar(0, 215, 215); // Limite inferior (laranja)
					Scalar upperColor = new Scalar(17, 255, 255); // Limite superior (laranja)

					MonstrosImagem analise = analisarTela(Map.of("npcs", new Scalar[] { lowerColor, upperColor }));

					// Obter a lista de NPCs usando a chave "npcs"
					List<MatOfPoint> npcs = analise.listaEntidades.getOrDefault("npcs", new ArrayList<>());

					if (npcs.isEmpty()) {
						return npcs;
					}

					// Filtrar monstros com altura maior que 16 pixels
					List<MatOfPoint> npcsFiltrados = npcs.stream().filter(
							monstro -> Imgproc.boundingRect(monstro).height >= 16 && Imgproc.boundingRect(monstro).width >= 16)
							.toList();

					return npcsFiltrados;
				}
				
	// notebook
	public Map<String, List<MatOfPoint>> procurarBoss() {
		// Definir a área onde os monstros aparecem na tela capturada
		int areaX = 235; // Coordenada x da área de interesse
		int areaY = 106; // Coordenada y da área de interesse

		// Definir os intervalos de cores
		Map<String, Scalar[]> colorRanges = new HashMap<>();
		colorRanges.put("amarelo", new Scalar[] { new Scalar(22, 215, 215), new Scalar(42, 255, 255) });

		MonstrosImagem analise = analisarTela(colorRanges);

		// Analisar a tela para as cores definidas
		Map<String, List<MatOfPoint>> detectedEntities = analise.listaEntidades;

		// Map para armazenar as listas filtradas e visíveis
		Map<String, List<MatOfPoint>> visibleEntities = new HashMap<>();

		// Processar cada cor
		for (Map.Entry<String, List<MatOfPoint>> entry : detectedEntities.entrySet()) {
			String color = entry.getKey();
			List<MatOfPoint> entities = entry.getValue();

			// Filtrar monstros com altura >= 16
			List<MatOfPoint> filtered = entities.stream().filter(monstro -> Imgproc.boundingRect(monstro).height >= 16)
					.toList();

			// Ajustar as coordenadas para serem globais
			List<MatOfPoint> adjustedEntities = new ArrayList<>();
			for (MatOfPoint entidade : filtered) {
				List<org.opencv.core.Point> adjustedPoints = new ArrayList<>();
				for (org.opencv.core.Point p : entidade.toList()) {
					adjustedPoints.add(new org.opencv.core.Point(p.x + areaX, p.y + areaY)); // Ajusta as coordenadas
				}
				MatOfPoint adjustedEntidade = new MatOfPoint();
				adjustedEntidade.fromList(adjustedPoints); // Constrói o MatOfPoint ajustado
				adjustedEntities.add(adjustedEntidade);
			}

			visibleEntities.put(color, adjustedEntities); // Adicionar ao mapa de monstros visíveis
		}

		return visibleEntities;
	}
	
	
	public List<MatOfPoint> verificarBalaoNpc() {
		// Definir os limites de cor
	    Scalar lowerColor = new Scalar(0, 0, 207);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(10, 40, 255);  // Limite superior (bracno)
	    
	    //MonstrosImagem analise = analisarTela(lowerColor, upperColor);
	    MonstrosImagem analise = analisarTela(Map.of("baloesNpc", new Scalar[]{lowerColor, upperColor}));
	   
	    
	    
	    //List<MatOfPoint> npcs = analise.listaEntidades;
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> npcs = analise.listaEntidades.getOrDefault("baloesNpc", new ArrayList<>());
	
			if (npcs.isEmpty()) {
	        	return npcs;
	        }
			
			// Altura máxima da imagem
		    int imageHeight = analise.screen.rows();
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> npcsFiltrados = npcs.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            int npcBaseY = boundingBox.y + boundingBox.height; // Base do retângulo (parte inferior)
		            return boundingBox.width >= 250 &&
		                   boundingBox.height >= 50 &&
		                   npcBaseY < (imageHeight * 0.8); // Abaixo de 80% da altura da imagem
		        })
		        .toList();
		    
		    /*
		    // Desenhar contornos na imagem original
		    Mat contouredImage = analise.screen.clone();
		    Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(contouredImage, npcsFiltrados, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, contouredImage);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
	
		    return npcsFiltrados;   
	}
	
	public MonstrosImagem analisarTelaBalao(Map<String, Scalar[]> colorRanges) {
	    
		// Capturar a tela da área definida
	    Rectangle captureArea = null;
	    captureArea = new Rectangle(xJanela, yJanela, width, height);
	    BufferedImage screenFullImage = robot.createScreenCapture(captureArea);

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(screenFullImage);
	    if (screen.empty()) {
	        System.out.println("Erro ao carregar a imagem.");
	        return null;
	    }

	    // Converter a imagem para o espaço de cores HSV
	    Mat hsvImage = new Mat();
	    Imgproc.cvtColor(screen, hsvImage, Imgproc.COLOR_BGR2HSV);

	    // Mapear os resultados
	    Map<String, List<MatOfPoint>> detectedEntities = new HashMap<>();
	    
	    for (Map.Entry<String, Scalar[]> entry : colorRanges.entrySet()) {
	        String colorName = entry.getKey();
	        Scalar lowerColor = entry.getValue()[0];
	        Scalar upperColor = entry.getValue()[1];

	        // Criar máscara para identificar a cor dentro do intervalo
	        Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerColor, upperColor, mask);

	        // Encontrar contornos na máscara
	        List<MatOfPoint> entidades = new ArrayList<>();
	        Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        // Armazenar os contornos filtrados associados à cor
	        detectedEntities.put(colorName, entidades);
	    }


	    return new MonstrosImagem(detectedEntities, screen);
	}
	
	public List<MatOfPoint> verificarJanelaInstancia() {
		// Definir os limites de cor
	    //Scalar lowerColor = new Scalar(12, 177, 182);  // Limite inferior (branco)
	    //Scalar upperColor = new Scalar(32, 255, 255);  // Limite superior (bracno)
		Scalar lowerColor = new Scalar(0, 0, 215);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(10, 40, 255);  // Limite superior (bracno)

	    MonstrosImagem analise = analisarTelaBalao(Map.of("baloesNpc", new Scalar[]{lowerColor, upperColor}));
	   
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> npcs = analise.listaEntidades.getOrDefault("baloesNpc", new ArrayList<>());
	
			if (npcs.isEmpty()) {
	        	return npcs;
	        }
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> npcsFiltrados = npcs.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            return boundingBox.width >= 250 && boundingBox.height >= 250;
		            //351 335
		        })
		        .toList();
		    
		    /*
		    // Desenhar contornos na imagem original
		    Mat contouredImage = bufferedImageToMat(printarTela());;
		    Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(contouredImage, npcsFiltrados, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, contouredImage);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
	
		    return npcsFiltrados;   
	}
	
	public int calcularDistancia(Coordenadas atual, Coordenadas destino) {
	    return (int) Math.sqrt(Math.pow(destino.x - atual.x, 2) + Math.pow(destino.y - atual.y, 2));
	}
	
	//notebook
		public double calcularDistanciaCentro(MatOfPoint monstro) {
			Rect boundingRect = Imgproc.boundingRect(monstro);
			
			System.out.println("Monstro em coordenadas: " + boundingRect.x + " " + boundingRect.y);

			// Calcula o centro do monstro
			int monstroCentroX = boundingRect.x + boundingRect.width / 2;
			int monstroCentroY = boundingRect.y + boundingRect.height / 2;

			return Math.sqrt(Math.pow(monstroCentroX - coordenadasJogadorTelaX, 2) + Math.pow(monstroCentroY - coordenadasJogadorTelaY, 2));
		}
	
		// notebook
		public void moverPersonagem(Coordenadas atual, Coordenadas destino, Map<Coordenadas, Boolean> mapaCarregado) {

			if (compararCoordenadas(atual, destino)) {
				return;
			}
			
			setarMouseEmCoordenadaTela(atual, destino);
			sleep(10);
			clicarSegurarMouse();

		}
	
	public void segurarW() {
		try {
	        robot.keyPress(KeyEvent.VK_W); // Pressiona e segura a tecla W
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public void soltarW() {
		try {
	        robot.keyRelease(KeyEvent.VK_W); // Solta a tecla W
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void atacarMonstro(MatOfPoint monstro, int tecla) {
        Rect rect = Imgproc.boundingRect(monstro);
        int centerX = xJanela + rect.x + rect.width / 2;
        int centerY = yJanela + rect.y + rect.height / 2;
        
        moverMouse(centerX, centerY + 10);
        sleep(50);
        apertarTecla(tecla);
        sleep(50);
        clicarMouse();
    }
	
	public void clicarMouse() {
		robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK); // Pressionar o botão esquerdo
        sleep(50);
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK); // Liberar o botão esquerdo*/
	}
	
	public void clicarSegurarMouse() {
	    robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
	}
	public void soltarMouse() {
		robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
	}
	
	public void moverMouse(int x, int y) {
		if (x >= getxJanela() + getWidth() || y >= getyJanela() + getHeight() ) {
			robot.mouseMove(coordenadasJogadorTelaX, coordenadasJogadorTelaY);
			System.out.println("Mouse ia sair do limite da tela do ragnarok!!!");
			return;
		}
		 robot.mouseMove(x, y);
	}
	
	public void apertarTecla(int tecla) {
		 robot.keyPress(tecla); // Pressionar a tecla
		 sleep(50);
         robot.keyRelease(tecla); // Liberar a tecla
	}
	
	private void scrollMouse(int scrollAmount) {
	    // scrollAmount positivo = scroll up, scrollAmount negativo = scroll down
	    robot.mouseWheel(scrollAmount);
	    try {
			Thread.sleep(70);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void zoom(int num) {
		boolean isPositivo = true;
		if (num < 0) {
			isPositivo = false;
		}
		num = Math.abs(num);
		for(int i = 0; i < num; i++) {
			if (isPositivo) {
				scrollMouse(1);
			} else {
				scrollMouse(-1);	
			}
        	sleep(10);
        }
	}

	
	public void moverPersonagemComClick(Coordenadas atual, Coordenadas destino) {
		
		double dx = destino.x - atual.x;
		double dy = destino.y - atual.y;
			
		// Calcular o ângulo em radianos
	    double angulo = Math.atan2(dy, dx);
			
	    // Calcular a distância original entre os pontos
	    double distanciaOriginal = Math.sqrt(dx * dx + dy * dy);
 
	    //double distanciaDesejada = 200.0;
	    double distanciaDesejada = width * 17 / 100;
	    
	    // Calcular a normalização do vetor (vetor unitário)   
	    double normalizaX = dx / distanciaOriginal;    
	    double normalizaY = dy / distanciaOriginal;
	
	    int xMouse = (int) (this.xJanela + this.coordenadasJogadorTelaX + normalizaX * distanciaDesejada);
	    int yMouse = (int) (this.yJanela + this.coordenadasJogadorTelaY - normalizaY * distanciaDesejada);
		
		Random random = new Random();
		int randX = random.nextInt(4); //0 a 5
		int randY = random.nextInt(4);
        moverMouse(xMouse + randX, yMouse + randY);
        sleep(50);
        clicarMouse();
    }
	
	private void getWidthHeight() {
		try {
			User32 user32 = User32.INSTANCE;
	        //HWND hwnd = user32.FindWindow(null, "History Reborn | Gepard Shield 3.0 (^-_-^)"); // Nome da janela do Ragnarok
			hwnd = user32.FindWindow(null, "History Reborn | Gepard Shield 3.0 (^-_-^)"); // Nome da janela do Ragnarok
			MyUser32 myUser32 = MyUser32.INSTANCE; // Usar a interface personalizada
	        
	        if (hwnd == null) {
	            System.out.println("Janela do Ragnarok não encontrada.");
	            return;
	        }
	        // Garantir que a janela tenha o foco
	        User32.INSTANCE.SetForegroundWindow(hwnd);
	
	        // Obter as dimensões do cliente
	        RECT clientRect = new RECT();
	        user32.GetClientRect(hwnd, clientRect);
	        
	        // Converter coordenadas do cliente para coordenadas da tela
	        POINT topLeft = new POINT(0, 0);
	        //user32.ClientToScreen(hwnd, topLeft);
	        boolean success = myUser32.ClientToScreen(hwnd, topLeft);
	        
	        if (!success) {
	            System.out.println("Falha ao converter coordenadas do cliente para coordenadas da tela.");
	            return;
	        }
	
	        this.xJanela = topLeft.x;
	        this.yJanela = topLeft.y;
	        this.width = clientRect.right - clientRect.left;
	        this.height = clientRect.bottom - clientRect.top;
	
	        System.out.println("Resolução da janela do Ragnarok: " + width + "x" + height);
	        System.out.println("Posição da janela do Ragnarok: " + xJanela + " " + yJanela);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private List<Point> raycast(Point start, Point end) {
	    List<Point> points = new ArrayList<>();

	    int dx = Math.abs(end.x - start.x);
	    int dy = Math.abs(end.y - start.y);

	    int sx = start.x < end.x ? 1 : -1;
	    int sy = start.y < end.y ? 1 : -1;

	    int err = dx - dy;

	    int x = start.x;
	    int y = start.y;

	    while (true) {
	        points.add(new Point(x, y));
	        if (x == end.x && y == end.y) break;

	        int e2 = 2 * err;
	        if (e2 > -dy) {
	            err -= dy;
	            x += sx;
	        }
	        if (e2 < dx) {
	            err += dx;
	            y += sy;
	        }
	    }

	    return points;
	}
	
	public List<MatOfPoint> filtrarMonstrosVisiveisRaycast(List<MatOfPoint> monstros, Mat screen) {
	    List<MatOfPoint> monstrosVisiveis = new ArrayList<>();

	    //Obter posicaoJogador
        //Point posicaoJogador = new Point(width / 2, height / 2);
	    Point posicaoJogador = new Point(screen.width() / 2, screen.height() / 2);
        
	    for (MatOfPoint monstro : monstros) {
	        Rect boundingRect = Imgproc.boundingRect(monstro);

	        // Obter o centro do monstro
	        Point centroMonstro = new Point(
	            boundingRect.x + boundingRect.width / 2,
	            boundingRect.y + boundingRect.height / 2
	        );
	        
	        // Obter os pontos do raycast (linha)
	        List<Point> raycastPoints = raycast(posicaoJogador, centroMonstro);

	        boolean bloqueado = false;

	        for (Point ponto : raycastPoints) {
	            // Certificar-se de que o ponto está dentro dos limites da imagem
	            if (ponto.x < 0 || ponto.x >= screen.cols() || ponto.y < 0 || ponto.y >= screen.rows()) {
	                continue;
	            }

	            double[] pixel = screen.get((int) ponto.y, (int) ponto.x); // Obter o pixel (BGR)

	            // Verificar se o pixel é preto ou cinza
	            if (pixel != null) {
	                int b = (int) pixel[0];
	                int g = (int) pixel[1];
	                int r = (int) pixel[2];

	                // Verificar se o pixel é preto puro (parede)
	                if (b == 0 && g == 0 && r == 0) { // Preto
	                    bloqueado = true;
	                    break; // Interrompe o loop se encontrar um pixel preto
	                }
	            }
	        }

	        if (!bloqueado) {
	            monstrosVisiveis.add(monstro); // Adiciona o monstro se não houver bloqueio
	        }
	    }

	    return monstrosVisiveis;
	}
	
	public boolean compararCoordenadas(Coordenadas atual, Coordenadas destino) {
		if ((atual.x == destino.x) && (atual.y == destino.y)) {
			return true;
		}
		
		return false;
	}

	public void digitarTexto(String texto) {
	    for (char c : texto.toCharArray()) {
	        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
	        if (KeyEvent.CHAR_UNDEFINED == keyCode) {
	            throw new RuntimeException("Tecla não mapeada: " + c);
	        }
	        
	        boolean isUpperCase = Character.isUpperCase(c);
	        boolean requiresShift = false;
	        
	     // Verificar se o caractere especial requer SHIFT
	        switch (c) {
	            case '!': keyCode = KeyEvent.VK_1; requiresShift = true; break;
	            case '@': keyCode = KeyEvent.VK_2; requiresShift = true; break;
	            case '#': keyCode = KeyEvent.VK_3; requiresShift = true; break;
	            case '$': keyCode = KeyEvent.VK_4; requiresShift = true; break;
	            case '%': keyCode = KeyEvent.VK_5; requiresShift = true; break;
	            case '^': keyCode = KeyEvent.VK_6; requiresShift = true; break;
	            case '&': keyCode = KeyEvent.VK_7; requiresShift = true; break;
	            case '*': keyCode = KeyEvent.VK_8; requiresShift = true; break;
	            case '(': keyCode = KeyEvent.VK_9; requiresShift = true; break;
	            case ')': keyCode = KeyEvent.VK_0; requiresShift = true; break;
	            case '?': keyCode = KeyEvent.VK_SLASH; requiresShift = true; break;
	            case '_': keyCode = KeyEvent.VK_MINUS; requiresShift = true; break;
	            case '+': keyCode = KeyEvent.VK_EQUALS; requiresShift = true; break;
	        }
	        
	        if (isUpperCase || requiresShift) {
	            // Pressiona Shift para letras maiúsculas
	            robot.keyPress(KeyEvent.VK_SHIFT);
	        }
	        
	        robot.keyPress(keyCode);
	        robot.keyRelease(keyCode);
	        
	        if (isUpperCase || requiresShift) {
	            // Solta Shift após a letra maiúscula
	            robot.keyRelease(KeyEvent.VK_SHIFT);
	        }
	        try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Pausa entre as teclas para simular digitação humana
	    }
	}
	
	public void aceitarContrato() {
		 try {
	        System.out.println("Movendo mouse para concordo");
	        moverMouse(xJanela + 642, yJanela + 519);
	        Thread.sleep(200);
	        System.out.println("Clicando");
	        clicarMouse();
	        Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void realizarLogin(String usuario, String senha) {
		try {
			System.out.println("Realizando login");
	        System.out.println("Mouse indo para o campo de login");
	        moverMouse(xJanela + 228, yJanela + 492);
	        Thread.sleep(200);
	        apertarTecla(KeyEvent.VK_TAB);
	        Thread.sleep(500);
	        System.out.println("Digitando o login");
	        digitarTexto(usuario);
	        Thread.sleep(500);
	        System.out.println("Mouse indo para o campo de senha");
	        apertarTecla(KeyEvent.VK_TAB);
	        Thread.sleep(500);
	        System.out.println("Digitando a senha");
	        digitarTexto(senha);
	        Thread.sleep(500);
	        apertarTecla(KeyEvent.VK_ENTER);
	        Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void escolherPersonagem(int num, int pagina) {
		try {
			if (pagina == 1) {
				Thread.sleep(50);
				moverMouse(xJanela + 850, yJanela + 642);
		       	Thread.sleep(500);
		       	clicarMouse();
		       	Thread.sleep(500);
			}
			if (pagina == 2) {
				Thread.sleep(50);
				moverMouse(xJanela + 966, yJanela + 642);
		       	Thread.sleep(500);
		       	clicarMouse();
		       	Thread.sleep(500);
			}
			System.out.println("Escolher o personagem");
	      	int linha = 1;
	       	int coluna = num;
	       	
	       	if (num >= 6 && num <= 10) {
	       		linha = 2;
	       		coluna = num - 5;
	       	}
	       	if (num >= 11 && num <= 15) {
	       		linha = 3;
	       		coluna = num - 10;
	       	}
	       	int posX = ((coluna - 1) * 146) + 73 + (coluna - 1) * 10;
	       	int posY = (linha - 1) * 183 + 92 + (linha - 1) * 12;
	       	
	       	moverMouse(xJanela + 30 + posX, yJanela + 98 + posY);
	       	Thread.sleep(500);
	       	clicarMouse();
	       	Thread.sleep(500);
	       	apertarTecla(KeyEvent.VK_ENTER);
	       	Thread.sleep(2000);
	       	moverMouse(xJanela + width / 2, yJanela + height / 2);
	       	Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public double calcularDistanciaDaReta(Coordenadas inicioReta, Coordenadas fimReta, Coordenadas atual) {
	    // Fórmula para distância de um ponto a uma linha: |Ax + By + C| / sqrt(A^2 + B^2)
	    double A = fimReta.y - inicioReta.y;
	    double B = inicioReta.x - fimReta.x;
	    double C = fimReta.x * inicioReta.y - inicioReta.x * fimReta.y;

	    return Math.abs(A * atual.x + B * atual.y + C) / Math.sqrt(A * A + B * B);
	}
	
	// notebook
		public Coordenadas getCoordenadasTelaPeloMouse(Coordenadas atual, int mouseX, int mouseY) {
			// Obter a posição atual do mouse
			// java.awt.Point mousePos = java.awt.MouseInfo.getPointerInfo().getLocation();
			// int mx = (int) mousePos.getX() - xJanela;
			// int my = (int) mousePos.getY() - yJanela;
			int mx = mouseX - xJanela;
			int my = mouseY - yJanela;

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
			// System.out.println("Coordenadas pela tela: x: " + x + " y: " + y);
			return new Coordenadas(x, y);
		}
		
		// notebook
		public void setarMouseEmCoordenadaTela(Coordenadas atual, Coordenadas destino) {
			// Calcular o deslocamento entre a coordenada atual e a de destino
			int dx = destino.x - atual.x;
			int dy = destino.y - atual.y;

			// Calcular a posição do mouse na tela com base no deslocamento
			int mouseX = xJanela + 505 + dx * 18 + 9;
			int mouseY = yJanela + 376 - dy * 18 + 9;

			// Mover o mouse para a posição calculada
			moverMouse(mouseX, mouseY);

		}
		
		// notebook
		public Coordenadas escolherProximaCoordenada(List<Coordenadas> caminhoCalculado, Coordenadas atual) {
			if (caminhoCalculado.isEmpty() || caminhoCalculado.size() <= 1) {
				System.out.println("Caiu aqui #####################################");
				return atual;
			}
			// Ângulo máximo permitido (em radianos: 45 graus)
			double anguloMaximo = Math.toRadians(80);// 60

			// Direção inicial
			Coordenadas inicial = caminhoCalculado.get(1);
			int direcaoInicialX = inicial.x - atual.x;
			int direcaoInicialY = inicial.y - atual.y;

			Coordenadas candidata = atual;

			for (int i = 1; i < caminhoCalculado.size() && i <= 14; i++) {
				Coordenadas proxima = caminhoCalculado.get(i);

				// Direção para a próxima coordenada
				int direcaoProximaX = proxima.x - atual.x;
				int direcaoProximaY = proxima.y - atual.y;

				// Calcular o produto escalar
				int produtoEscalar = direcaoInicialX * direcaoProximaX + direcaoInicialY * direcaoProximaY;

				// Calcular as magnitudes dos vetores
				double magnitudeInicial = Math.sqrt(direcaoInicialX * direcaoInicialX + direcaoInicialY * direcaoInicialY);
				double magnitudeProxima = Math.sqrt(direcaoProximaX * direcaoProximaX + direcaoProximaY * direcaoProximaY);

				// Calcular o cosseno do ângulo
				double cosTheta = produtoEscalar / (magnitudeInicial * magnitudeProxima);

				// Calcular o ângulo em radianos
				double angulo = Math.acos(cosTheta);

				// Verificar se o ângulo excede o limite
				if (angulo > anguloMaximo) {
					return candidata; // Retornar última coordenada válida antes da curva acentuada
				}

				// Atualizar a candidata se o ângulo estiver dentro do limite
				candidata = proxima;
			}

			// Retornar a última coordenada válida dentro do limite de 10
			return candidata;
		}
	
	public void voltarMoroc(int numero) {
	    if (numero < 0 || numero > 9) {
	        throw new IllegalArgumentException("O número deve estar entre 0 e 9.");
	    }

	    // Pressionar Alt
	    robot.keyPress(KeyEvent.VK_ALT);

	    // Obter a tecla correspondente ao número
	    int keyCode = KeyEvent.VK_0 + numero;

	    // Pressionar o número
	    robot.keyPress(keyCode);
	    robot.keyRelease(keyCode);
	    sleep(100);
	    // Soltar Alt
	    robot.keyRelease(KeyEvent.VK_ALT);

	}
	
	//notebook
	public void visaoDeCima() {
		// Pressionar Ctrl e Shift
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_SHIFT);

				// Pressionar o botão direito do mouse
				robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);

				moverMouse(this.xJanela + coordenadasJogadorTelaX, this.yJanela + coordenadasJogadorTelaY - 100);
				sleep(20);

				// Obter a posição atual do mouse
				java.awt.Point mousePos = java.awt.MouseInfo.getPointerInfo().getLocation();
				int currentX = (int) mousePos.getX();
				int currentY = (int) mousePos.getY();

				sleep(20);
				// Mover o mouse 300 pixels para baixo
				robot.mouseMove(currentX, currentY + 250);
				sleep(100);
				// Soltar o botão direito do mouse
				robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);

				// Soltar Ctrl e Shift
				robot.keyRelease(KeyEvent.VK_SHIFT);
				robot.keyRelease(KeyEvent.VK_CONTROL);
	}
	
	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//notebook
		public boolean tempoPassou(long time) {
			long tempoAtual = System.currentTimeMillis();
			if (tempoAtual - tempoExecucao >= time) {
				tempoExecucao = tempoAtual;
				return true;
			}
			return false;
		}
	
		//notebook
		public int getHpAtual() {
			return memoria.obterHP(memoria.processId, memoria.addressHp);
		}
		
		public String getMemoriaString() {
			return memoria.obterStringMemoria(memoria.processId, memoria.addressString);
		}
		
		//notebook
		/**
	     * Simula o pressionamento de Alt + um número de 0 a 9 no teclado.
	     *
	     * @param numero O número (0-9) que será pressionado junto com Alt.
	     */
	    public void atalhoAltM(int numero) {
	        if (numero < 0 || numero > 9) {
	            throw new IllegalArgumentException("Número deve estar entre 0 e 9.");
	        }

	        // Pressiona a tecla Alt
	        robot.keyPress(KeyEvent.VK_ALT);

	        // Determina a tecla correspondente ao número
	        int keyCode = KeyEvent.VK_0 + numero;

	        // Pressiona e solta a tecla do número
	        robot.keyPress(keyCode);
	        sleep(100);
	        robot.keyRelease(keyCode);

	        // Solta a tecla Alt
	        robot.keyRelease(KeyEvent.VK_ALT);


	    }
	    
	    
	public void executarInstancia(String instancia) {
		moverMouse(getxJanela() + 65, getyJanela() + 150);
		sleep(300);
		clicarMouse();
		// Selecionar de fato a instancia
		sleep(300);
		
		int x = 0;
		int y = 0;
		List<MatOfPoint> janelaInstancia = verificarJanelaInstancia();
		if (!janelaInstancia.isEmpty()) {
			Rect janela = Imgproc.boundingRect(janelaInstancia.get(0));
			x = janela.x;
			y = janela.y;
		}

		sleep(1000);
		moverMouse(getxJanela() + x + 70, getyJanela() + y + 140);
		// int dg = 43; //tomb
		int dg = getNumeroInstancia(instancia);
		//int dg = 33; // old gh
		int counter = dg / 16;
		if (dg == 16) {
			counter = 0;
		}
		if (dg == 32) {
			counter = 1;
		}
		if (dg == 48) {
			counter = 2;
		}
		if (dg == 64) {
			counter = 3;
		}

		for (int i = 0; i < counter; i++) {
			zoom(5);
			moverMouse(getxJanela() + x + 133, getyJanela() + y + 283);
			sleep(200);
			clicarMouse();
			sleep(200);
		}

		int pos = dg % 16;
		if (pos == 0) {
			pos = 16;
		}
		int selecionar = pos * 16 - 8;

		moverMouse(getxJanela() + x + 70, getyJanela() + y + 23 + selecionar);
		sleep(300);
		clicarMouse();
		sleep(300);
		// Clicar em Criar
		moverMouse(getxJanela() + x + 141, getyJanela() + y + 304 + 17);
		sleep(300);
		clicarMouse();
		sleep(300);
		// Clicar em Ok
		moverMouse(getxJanela() + 586, getyJanela() + 429);
		sleep(300);
		clicarMouse();
		sleep(300);
		// Apertar enter que apareceu um balao de npc
		apertarTecla(KeyEvent.VK_ENTER);
		sleep(300);
		// Repetir pra criar a instancia de old gh
		// Clicar em Criar
		moverMouse(getxJanela() + x + 141, getyJanela() + y + 304 + 17);
		sleep(300);
		clicarMouse();
		sleep(300);
		// Clicar em Ok
		moverMouse(getxJanela() + 586, getyJanela() + 429);
		sleep(300);
		clicarMouse();
		sleep(300);
		// Apertar enter que apareceu um balao de npc
		apertarTecla(KeyEvent.VK_ENTER);
		sleep(300);
		//Clicar em Entrar
		moverMouse(getxJanela() + x + 48, getyJanela() + y + 304 + 17);
		sleep(300);
		clicarMouse();
		sleep(300);
		// Clicar em Ok
		moverMouse(getxJanela() + 586, getyJanela() + 429);
		sleep(300);
		clicarMouse();
		sleep(300);
		
		sleep(3000);
		visaoDeCima();
		sleep(100);
		zoom(-28);
	}
	
	private int getNumeroInstancia(String instancia) {
		int dg = 0;
		switch (instancia) {
			case "Old Glast Heim":
				dg = 33;
				break;
			case "Tomb of Remorse":
				dg = 43;
				break;
	
			default:
				break;
		}
		return dg;
	}
	
	public void encerrarInstancia() {
		moverMouse(getxJanela() + 104, getyJanela() + 436);
		sleep(300);
		clicarMouse();
		sleep(300);
	}
	
	public void deslogarPersonagem() {
		apertarTecla(KeyEvent.VK_ESCAPE);
		sleep(500);
		moverMouse(getxJanela() + 511, getyJanela() + 513);
		sleep(300);
		clicarMouse();
		sleep(300);
		clicarMouse();
		sleep(300);
	}
	
	public void voltarTelaLogin() {
		moverMouse(getxJanela() + 966, getyJanela() + 85);
		sleep(500);
		clicarMouse();
		sleep(500);
		//confirmar
		moverMouse(getxJanela() + 587, getyJanela() + 430);
		sleep(500);
		clicarMouse();
		sleep(500);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getxJanela() {
		return xJanela;
	}

	public int getyJanela() {
		return yJanela;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setxJanela(int xJanela) {
		this.xJanela = xJanela;
	}

	public void setyJanela(int yJanela) {
		this.yJanela = yJanela;
	}

	

	

	

}
