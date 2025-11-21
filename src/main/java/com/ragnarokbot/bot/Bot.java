package com.ragnarokbot.bot;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
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
import java.util.concurrent.ThreadLocalRandom;
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
import com.ragnarokbot.bot.controles.Control;
import com.ragnarokbot.bot.controles.ControlInterception;
import com.ragnarokbot.bot.controles.ControlRobot;
import com.ragnarokbot.main.BotRagnarok;
import com.ragnarokbot.main.GameController;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.GrafoMapa;
import com.ragnarokbot.model.MemoryScanner;
import com.ragnarokbot.model.MonstrosImagem;
import com.ragnarokbot.model.MyUser32;
import com.ragnarokbot.model.OcrResult;
import com.ragnarokbot.model.enums.Effects;
import com.ragnarokbot.model.enums.Estado;
import com.ragnarokbot.model.enums.Mapa;
import com.ragnarokbot.telas.JanelaPrincipal;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import utils.KeyMapper;
import utils.MouseClicker;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

import config.ConfigManager;
import config.ConfigManager.Config;
import config.ContasConfig;
import config.Pin;
import config.ScriptLoader;
import config.SkillsConfig.Buffs;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.ptr.IntByReference;

public class Bot {
	
	private Robot robot;
	private ITesseract tesseract;
	private ITesseract tesseractLetras;
	
	public static HWND hwnd;
	public static User32 user32 = User32.INSTANCE;
	
    private int coordenadasJogadorTelaX;
    private int coordenadasJogadorTelaY;
    private int width;
    private int height;
    
    private int xJanela;
    private int yJanela;
    
    public List<Skill> skills = new ArrayList<>();
    public List<Buff> buffs = new ArrayList<>();
    
    private boolean interception = false;

    private MemoryScanner memoria = new MemoryScanner();
    
    private static final int SW_RESTORE = 9;
    
    public MouseClicker mouseClicker = new MouseClicker();
    
    //notebook
  	long tempoExecucao = System.currentTimeMillis();
    
    //Variaveis para coordenadas mini mapa
    public Config configOCR;
    
    public Control control;
    
    
    static String dependencia = new File("libs/interception.dll").getAbsolutePath();
    static String libPath = new File("libs/InterceptionDemo.dll").getAbsolutePath();

    static {
    	System.load(dependencia);
        System.load(libPath);
    }

    public native static void iniciarInterception();
    public native static void apertarTeclaInter(int vk);
    public native static void moverMouseInter(int x, int y);
    public native static void clickInter();
    public native static void encerrarInterception();
    public native static void scrollMouseInter(int x);
    public native static void apertarSegurarTeclaInter(int vk);
    public native static void soltarTeclaInter(int vk);
    public native static void clicarMouseDireitoInter();
    public native static void clicarSegurarMouseInter();
    public native static void soltarMouseInter();
    public native static void segurarMouseDireitoInter();
    public native static void soltarMouseDireitoInter();

	public Bot(ITesseract tesseract, Robot robot, ITesseract tesseractLetras) {
		
		this.tesseract = tesseract;
		this.tesseractLetras = tesseractLetras;
        this.robot = robot;
        this.configOCR = ConfigManager.loadConfig();
        
        //getWidthHeight();
        //System.out.println("Testando com os novos metodos");
        
        //inicializarBot();
		
		
	}
	
	public void inicializarBot() {
		//hwnd = getWindowHandleByPID(3676);
		hwnd = getWindowHandleByPID(memoria.processId);
        if (hwnd != null) {
        	getWindowSize(hwnd);
        	if (isWindowInFocus(hwnd)) {
                System.out.println("A janela do Ragnarok está em foco.");
            } else {
                System.out.println("A janela do Ragnarok NÃO está em foco.");
                focarRagnarok(hwnd);
            }
        } else {
            System.out.println("Janela do Ragnarok não encontrada.");
        }
        
        this.coordenadasJogadorTelaX = width / 2;
        this.coordenadasJogadorTelaY = height / 2;
        
        interception = JanelaPrincipal.obterInterception();
        if (interception) {
        	System.out.println("Interception ligado!");
        	iniciarInterception();
        	control = new ControlInterception();
        } else {
        	control = new ControlRobot(robot);
        }
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
	
	public BufferedImage selecionarOpcaoComRetorno(int opcaoEscolhida, int x, int y, int width, int height) {
		sleep(100);
		List<MatOfPoint> balao = null;
		Rect m = null;
		Rectangle captureArea = new Rectangle(getxJanela() + x, getyJanela() + y, width, height);
		BufferedImage imagem = robot.createScreenCapture(captureArea);
		sleep(500);
		if (opcaoEscolhida == 1) {
			balao = verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(getxJanela() + m.x, getyJanela() + m.y, m.width, m.height);
				imagem = robot.createScreenCapture(captureArea);
			}
			
			apertarTecla(KeyEvent.VK_ENTER);
	   	} else {
	   		for (int i = 0; i < opcaoEscolhida - 1; i++) {
	       		apertarTecla(KeyEvent.VK_DOWN);
	       		sleep(200);
	       	}
	   		balao = verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(getxJanela() + m.x, getyJanela() + m.y, m.width, m.height);
				imagem = robot.createScreenCapture(captureArea);
			} else {
		   		captureArea = new Rectangle(getxJanela() + x, getyJanela() + y, width, height);
				imagem = robot.createScreenCapture(captureArea);
			}
	   		apertarTecla(KeyEvent.VK_ENTER);
	   	 }
		return imagem;
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
	
	public boolean detectarPixelsAmarelos(int posX, int posY, int width, int height) {
		Rectangle captureArea = new Rectangle(posX, posY, width, height);
		BufferedImage imagem = robot.createScreenCapture(captureArea);
		
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        int contadorAmarelo = 0;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                Color cor = new Color(imagem.getRGB(x, y));

                // Define faixa de cor amarela
                if (ehAmarelo(cor)) {
                    contadorAmarelo++;
                }
            }
        }

        System.out.println("Pixels amarelos detectados: " + contadorAmarelo);

        // Define um limite de pixels amarelos para considerar que a mensagem apareceu
        int threshold = 1000;
        return contadorAmarelo > threshold;
    }

    private boolean ehAmarelo(Color cor) {
        int r = cor.getRed();
        int g = cor.getGreen();
        int b = cor.getBlue();

        // O amarelo tem valores altos de R e G, e baixo de B
        return (r > 180 && g > 180 && b < 100);
    }
    
    public int contarPixels(Color cor, int x, int y, int width, int height) {
        try {
            // Captura a área da tela com Robot
            Robot robot = new Robot();
            BufferedImage captura = robot.createScreenCapture(new Rectangle(x, y, width, height));

            int contador = 0;

            // Percorre cada pixel da imagem capturada
            for (int i = 0; i < captura.getWidth(); i++) {
                for (int j = 0; j < captura.getHeight(); j++) {
                    Color pixel = new Color(captura.getRGB(i, j));
                    // Se o pixel for da cor desejada, incrementa o contador
                    if (coresSaoIguais(pixel, cor)) {
                        contador++;
                    }
                }
            }

            return contador;
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return 0;
    }
   

    // Método auxiliar para comparar duas cores com tolerância
    private boolean coresSaoIguais(Color c1, Color c2) {
        int tolerancia = 10; // Pequena margem de variação
        return Math.abs(c1.getRed() - c2.getRed()) <= tolerancia &&
               Math.abs(c1.getGreen() - c2.getGreen()) <= tolerancia &&
               Math.abs(c1.getBlue() - c2.getBlue()) <= tolerancia;
    }
    
 
    
    public Rect getArmazem() {
    	Rect m = null;
		
		boolean isArmazem = false;
		do {
			List<MatOfPoint> armazem = procurarArmazem();
			System.out.println("Procurando armazem");
			if (!armazem.isEmpty()) {
				m = Imgproc.boundingRect(armazem.get(0));
				isArmazem = true;
			}
			sleep(100);
		} while(!isArmazem);
		
		return m;
    }
    
    public Rect getAltQ() {
    	Rect m = null;
    	
    	boolean isAltQ = false;
    	do {
			List<MatOfPoint> altQ = procurarAltQ();
			System.out.println("Procurando Alt Q");
			if (!altQ.isEmpty()) {
				System.out.println("Achou com tamanho: " + altQ.size());
				m = Imgproc.boundingRect(altQ.get(0));
				isAltQ = true;
			}
			sleep(100);
		} while(isAltQ == false);
    	return m;
    }
    
    public Rect getInventario() {
    	Rect m = null;
    	List<MatOfPoint> inventario = null;
		boolean isInventario = false;
		do {
			inventario = procurarIventario();
			System.out.println("Procurando inventario");
			if (!inventario.isEmpty()) {
				sleep(100);
				 m = Imgproc.boundingRect(inventario.get(0));
				int xEquip = m.x + 8;
				int yEquip = m.y + 77;
				moverMouse(getxJanela() + xEquip, getyJanela() + yEquip);
				sleep(100);
				clicarMouse();
				System.out.println("x: " + m.x + " y: " + m.y);
				isInventario = true;
			}
			
			sleep(100);
		} while(!isInventario);
		return m;
    }
    
    public boolean isInventarioAberto() {
    	BufferedImage inventarioImg = null;
		String path = "config/telas/inventario.png";
		Rect pos = null;
		int contador = 0;
		try {
			inventarioImg = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		do {
			System.out.println("Procurando pelo inventario");
			pos = encontrarImagem(inventarioImg, 0.85);
        	if (pos != null) {
        		System.out.println("Inventario aberto");
        		return true;
        	}
        	sleep(1000);
        	contador++;
		} while (contador < 5);
		System.out.println("Inventario fechado");
    	return false;
    }
    public void forcarArmazemFechado() {
    	BufferedImage armazemImg = null;
		String path = "config/telas/armazem.png";
		Rect pos = null;
		try {
			armazemImg = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		do {
			System.out.println("Procurando pelo armazem");
			pos = encontrarImagem(armazemImg, 0.85);
        	if (pos != null) {
        		System.out.println("Armazem aberto");
        		moverMouse(getxJanela() + pos.x + pos.width - 10, getyJanela() + pos.y + pos.height/2);
        		sleep(300);
        		clicarMouse();
        	}
        	sleep(1000);
        	pos = encontrarImagem(armazemImg, 0.85);
        	
		} while (pos != null);
		System.out.println("Armazem fechado");
    }
    
    public void guardarItensArmazem(Rect m) {
    	System.out.println("Guardando itens no armazem");
		int x = m.x + 58;
		int y = m.y + 21;
		moverMouse(getxJanela() + x, getyJanela() + y);
		sleep(100);
		
		apertarSegurarTecla(KeyEvent.VK_ALT);
		
		boolean isInventarioLimpo = false;
		do {
			clicarMouseDireito();
			sleep(100);
			int brancos = contarPixels(Color.WHITE, m.x + getxJanela(), m.y + getyJanela(), m.width, m.height);
			System.out.println("Quantidade de pixels brancos: " + brancos + "! É maior que 45k? " + isInventarioLimpo); //45k
			if (brancos > 45000) {
				isInventarioLimpo = true;
				
				soltarTecla(KeyEvent.VK_ALT);
			}
		} while(!isInventarioLimpo);
		sleep(100);
    }
    
    public void removerItensArmazem(Rect m) {
    	System.out.println("Removendo itens do armazém");
		int x = m.x + 10;
		int y = m.y + 71;
		moverMouse(getxJanela() + x, getyJanela() + y);
		sleep(100);
		clicarMouse();
		sleep(100);
		x = m.x + 54;
		y = m.y + 21;
		moverMouse(getxJanela() + x, getyJanela() + y);
		sleep(50);
		
		apertarSegurarTecla(KeyEvent.VK_ALT);
		
		System.out.println("Tirando ARMORS");
		boolean isArmazemLimpo = false;
		do {
			clicarMouseDireito();
			sleep(100);
			int brancos = contarPixels(Color.WHITE, m.x +getxJanela(), m.y + getyJanela(), m.width, m.height);
			System.out.println("Quantidade de pixels brancos: " + brancos + "! É maior que 102k? " + isArmazemLimpo); //45k
			if (brancos > 102000) {
				isArmazemLimpo = true;

				soltarTecla(KeyEvent.VK_ALT);
				
			}
		} while(!isArmazemLimpo);
		sleep(100);
		
		System.out.println("Tirando WEAPON");
		boolean isWeaponLimpo = false;
		x = m.x + 10;
		y = m.y + 101;
		moverMouse(getxJanela() + x, getyJanela() + y);
		sleep(100);
		clicarMouse();
		sleep(100);
		x = m.x + 54;
		y = m.y + 21;
		moverMouse(getxJanela() + x, getyJanela() + y);

		apertarSegurarTecla(KeyEvent.VK_ALT);
		
		sleep(50);
		do {
			clicarMouseDireito();
			sleep(100);
			int brancos = contarPixels(Color.WHITE, m.x +getxJanela(), m.y + getyJanela(), m.width, m.height);
			System.out.println("Quantidade de pixels brancos: " + brancos + "! É maior que 102k? " + isArmazemLimpo); //45k
			if (brancos > 102000) {
				isWeaponLimpo = true;

				soltarTecla(KeyEvent.VK_ALT);
				
			}
		} while(!isWeaponLimpo);
		
		System.out.println("Tirando CASH");
		boolean isCashLimpo = false;
		x = m.x + 10;
		y = m.y + 130;
		moverMouse(getxJanela() + x, getyJanela() + y);
		sleep(100);
		clicarMouse();
		sleep(100);
		x = m.x + 54;
		y = m.y + 21;
		moverMouse(getxJanela() + x, getyJanela() + y);

		apertarSegurarTecla(KeyEvent.VK_ALT);
		
		sleep(50);
		do {
			clicarMouseDireito();
			sleep(100);
			int brancos = contarPixels(Color.WHITE, m.x +getxJanela(), m.y + getyJanela(), m.width, m.height);
			System.out.println("Quantidade de pixels brancos: " + brancos + "! É maior que 102k? " + isArmazemLimpo); //45k
			if (brancos > 102000) {
				isCashLimpo = true;

				soltarTecla(KeyEvent.VK_ALT);
				
			}
		} while(!isCashLimpo);
    }
    
    public void equipandoItens(Rect m) {
    	System.out.println("Equipando os itens");
		int x = m.x + 58;
		int y = m.y + 21;
		moverMouse(getxJanela() + x, getyJanela() + y);
		sleep(100);
		String classe = JanelaPrincipal.obterClasseSelecionada();
		
		boolean isInventarioLimpo = false;
		do {
			clicarMouse();
			sleep(50);
			clicarMouse();
			int brancos = contarPixels(Color.WHITE, m.x + getxJanela(), m.y +getyJanela(), m.width, m.height);
			System.out.println("Quantidade de pixels brancos: " + brancos + "! É maior que 45k? " + isInventarioLimpo); //45k
			if (classe.equals("sorc") && brancos > 44130) {
				isInventarioLimpo = true;
			} else if (brancos > 45000) {
				isInventarioLimpo = true;

			}
		} while(!isInventarioLimpo);
		sleep(100);
    }

	public boolean compararImagens(BufferedImage imagem1, BufferedImage imagem2, double limite) {
		// Converte para Mat (OpenCV)
        Mat matRef = bufferedImageToMat(imagem1);
        Mat matAtual = bufferedImageToMat(imagem2);
        
        Mat img1Gray = new Mat();
        Mat img2Gray = new Mat();
        
        // Converter para tons de cinza
        Imgproc.cvtColor(matRef, img1Gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(matAtual, img2Gray, Imgproc.COLOR_BGR2GRAY);
        
        // Ajustar tamanho (caso tenha pequenas diferenças)
        Imgproc.resize(img2Gray, img2Gray, img1Gray.size());

        // Calcula diferença absoluta
        Mat diff = new Mat();
        Core.absdiff(img1Gray, img2Gray, diff);

        // Calcula o percentual de diferença
        Scalar sumDiff = Core.sumElems(diff);
        double totalDiff = sumDiff.val[0] / (img1Gray.rows() * img1Gray.cols());

        System.out.println("Diferença detectada: " + totalDiff);

        // Se a diferença for menor que um certo limiar, consideramos que são iguais
        return totalDiff < limite;
	}
	
	public boolean compararImagensCometa(BufferedImage imagem1, BufferedImage imagem2, BufferedImage cometaPreto, double limite) {
		// Converte para Mat (OpenCV) //1 original 
        Mat matRef = bufferedImageToMat(imagem1);
        Mat matAtual = bufferedImageToMat(imagem2);
        
        Mat img1Gray = new Mat();
        Mat img2Gray = new Mat();
        
        // Converter para tons de cinza
        Imgproc.cvtColor(matRef, img1Gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(matAtual, img2Gray, Imgproc.COLOR_BGR2GRAY);
        
        // Ajustar tamanho (caso tenha pequenas diferenças)
        Imgproc.resize(img2Gray, img2Gray, img1Gray.size());

        // Calcula diferença absoluta
        Mat diff = new Mat();
        Core.absdiff(img1Gray, img2Gray, diff);

        // Calcula o percentual de diferença
        Scalar sumDiff = Core.sumElems(diff);
        double totalDiff = sumDiff.val[0] / (img1Gray.rows() * img1Gray.cols());

        System.out.println("Diferença detectada: " + totalDiff);
        
        boolean isCd = false;
        
        if (totalDiff < limite == true) {
        	isCd = true;
        } else {
        	System.out.println("Comparando com o cometa preto agora...");
        	 Mat matRefNovo = bufferedImageToMat(cometaPreto);
             Mat matAtualNovo = bufferedImageToMat(imagem2);
             
             Mat img1GrayNovo = new Mat();
             Mat img2GrayNovo = new Mat();
             
             // Converter para tons de cinza
             Imgproc.cvtColor(matRefNovo, img1GrayNovo, Imgproc.COLOR_BGR2GRAY);
             Imgproc.cvtColor(matAtualNovo, img2GrayNovo, Imgproc.COLOR_BGR2GRAY);
             
             // Ajustar tamanho (caso tenha pequenas diferenças)
             Imgproc.resize(img2GrayNovo, img2GrayNovo, img1GrayNovo.size());

             // Calcula diferença absoluta
             Mat diffNovo = new Mat();
             Core.absdiff(img1GrayNovo, img2GrayNovo, diffNovo);
             
             // Calcula o percentual de diferença
             Scalar sumDiffNovo = Core.sumElems(diffNovo);
             totalDiff = sumDiffNovo.val[0] / (img1GrayNovo.rows() * img1GrayNovo.cols());
            
            if (totalDiff < limite == true) {
            	System.out.println("Cometa tinha dado refresh, fazer nada");
            	 System.out.println("Diferença detectada: " + totalDiff);
            	isCd = true;
            } else {
            	System.out.println("Cometa ta em cd mesmo");
            	 System.out.println("Diferença detectada: " + totalDiff);
            	isCd = false;
            }
        }

        // Se a diferença for menor que um certo limiar, consideramos que são iguais
        return isCd;
	}
	
	public boolean compararBalaoNpc(BufferedImage imagem1, BufferedImage imagem2) {
		// Converte para Mat (OpenCV)
        Mat matRef = bufferedImageToMat(imagem1);
        Mat matAtual = bufferedImageToMat(imagem2);
        
        Mat img1Gray = new Mat();
        Mat img2Gray = new Mat();
        
        // Converter para tons de cinza
        Imgproc.cvtColor(matRef, img1Gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(matAtual, img2Gray, Imgproc.COLOR_BGR2GRAY);
        
        // Ajustar tamanho (caso tenha pequenas diferenças)
        Imgproc.resize(img2Gray, img2Gray, img1Gray.size());

        // Calcula diferença absoluta
        Mat diff = new Mat();
        Core.absdiff(img1Gray, img2Gray, diff);

        // Calcula o percentual de diferença
        Scalar sumDiff = Core.sumElems(diff);
        double totalDiff = sumDiff.val[0] / (img1Gray.rows() * img1Gray.cols());

        System.out.println("Diferença detectada: " + totalDiff);

        // Se a diferença for menor que um certo limiar, consideramos que são iguais
        return totalDiff < 5.0;
	}
	
	public Mat bufferedImageToMat(BufferedImage image) {
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
		
		BufferedImage imagemTelaPin = null;
		String path = "config/telas/pin.png";
		try {
			imagemTelaPin = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace(); // 960 79 13 12
		}
		boolean imagensIguais = false;
		do {
			imagensIguais = procurarImagem(imagemTelaPin, 0.8);
			System.out.println("Verificando imagens: " + imagensIguais);
			sleep(500);
		} while( imagensIguais == false);
		
		for(int j = 0; j < pin.length(); j++) {
        	char c = pin.charAt(j);
        	int numeroConvertido = Character.getNumericValue(c);
        	
        	BufferedImage numImg = null;
    		String numPath = numeroConvertido + ".png";
    		try {
    			numImg = ImageIO.read(new File(numPath));
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        	Rect pos = encontrarImagem(numImg, 0.85);
        	
        	System.out.println("Numero: " + numeroConvertido + " Localizacao x: " + pos.x + " y: " + pos.y);
        	
        	int centerX = xJanela + pos.x + pos.width / 2;
        	int centerY = yJanela + pos.y + pos.height / 2;
        	
        	moverMouse(centerX, centerY);
        	sleep(500);
        	clicarMouse();
        	//Tirando o mouse de cima dos numeros pra pintar direito
        	sleep(500);
        	moverMouse(xJanela + 50, yJanela + 50);
        	sleep(500);
        	moverMouse(xJanela + 60, yJanela + 60);
        	sleep(500);
        }
		sleep(500);
		BufferedImage okImg = null;
		String okPath = "config/telas/okPin.png";
		try {
			okImg = ImageIO.read(new File(okPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Rect posOk = encontrarImagem(okImg, 0.85);
    	
        System.out.println("Colocar o mouse no Ok");
	    moverMouse(xJanela + posOk.x + posOk.width / 2, yJanela + posOk.y + posOk.height / 2);
	    sleep(500);
	    System.out.println("Clicou no Ok");
        clicarMouse();
		 
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
		//Mat screenApagar = bufferedImageToMat(robot.createScreenCapture(new Rectangle(xJanela, yJanela, width, height)));
		//GameController.screen = screenApagar;
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
	
	public MonstrosImagem analisarTelaInteira(Map<String, Scalar[]> colorRanges) {
	
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
		//Mat screenApagar = bufferedImageToMat(robot.createScreenCapture(new Rectangle(xJanela, yJanela, width, height)));
		//GameController.screen = screenApagar;
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
			colorRanges.put("rosa", new Scalar[] { new Scalar(148, 200, 200), new Scalar(154, 255, 255) });
			//colorRanges.put("rosa", new Scalar[] { new Scalar(148, 100, 100), new Scalar(154, 255, 255) });
			colorRanges.put("azul", new Scalar[] { new Scalar(108, 215, 204), new Scalar(128, 255, 255) }); // Azul

			MonstrosImagem analise = analisarTela(colorRanges);

			// Analisar a tela para as cores definidas
			Map<String, List<MatOfPoint>> detectedEntities = analise.listaEntidades;

			// Map para armazenar as listas filtradas e visíveis
			Map<String, List<MatOfPoint>> visibleEntities = new HashMap<>();
			
			List<MatOfPoint> monstrosAtrasParede = new ArrayList<>(); // Nova lista para monstros bloqueados
			
			// Processar cada cor
			for (Map.Entry<String, List<MatOfPoint>> entry : detectedEntities.entrySet()) {
				String color = entry.getKey();
				List<MatOfPoint> entities = entry.getValue();

				// Filtrar monstros com altura >= 16
				List<MatOfPoint> filtered = entities.stream().filter(monstro -> Imgproc.boundingRect(monstro).height >= 16)
						.toList();

				// Obter monstros visíveis e bloqueados
		        List<List<MatOfPoint>> visibilidade = filtrarMonstrosVisiveisRaycast(filtered, analise.screen);
		        List<MatOfPoint> visible = visibilidade.get(0);
		        List<MatOfPoint> bloqueados = visibilidade.get(1);

		        // Ajustar coordenadas para serem globais
		        List<MatOfPoint> adjustedEntities = ajustarCoordenadas(visible, areaX, areaY);
		        List<MatOfPoint> adjustedBloqueados = ajustarCoordenadas(bloqueados, areaX, areaY);

				// System.out.println("Tamanho da lista da imagem pequena " +
				// adjustedEntities.size());

				// System.out.println("Monstros visíveis para a cor " + color + ": " +
				// adjustedEntities.size());
				// visibleEntities.put(color, visible); // Adicionar ao mapa de monstros
				// visíveis
				visibleEntities.put(color, adjustedEntities); // Adicionar ao mapa de monstros visíveis
				monstrosAtrasParede.addAll(adjustedBloqueados); // Adicionar os bloqueados à lista geral
			}

			// Adicionar a nova lista ao mapa
		    visibleEntities.put("monstrosAtrasParede", monstrosAtrasParede);
		    
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
			List<MatOfPoint> filtered = entities.stream().filter(monstro -> Imgproc.boundingRect(monstro).height >= 10)
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
	    /*Scalar lowerColor = new Scalar(0, 0, 207);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(10, 40, 255);  // Limite superior (bracno)*/
		Scalar lowerColor = new Scalar(0, 215, 215); // Limite inferior (laranja)
		Scalar upperColor = new Scalar(17, 255, 255); // Limite superior (laranja)
	    
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
	
	public List<MatOfPoint> verificarBalaoNpcTeleport() {
		// Definir os limites de cor
		Scalar lowerColor = new Scalar(0, 215, 215); // Limite inferior (laranja)
		Scalar upperColor = new Scalar(17, 255, 255); // Limite superior (laranja)
	    
	    //MonstrosImagem analise = analisarTela(lowerColor, upperColor);
	    MonstrosImagem analise = analisarTela(Map.of("baloesNpc", new Scalar[]{lowerColor, upperColor}));
	   
	    
	    
	    //List<MatOfPoint> npcs = analise.listaEntidades;
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> npcs = analise.listaEntidades.getOrDefault("baloesNpc", new ArrayList<>());
	
			if (npcs.isEmpty()) {
	        	return npcs;
	        }
			
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> npcsFiltrados = npcs.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            
		            return (boundingBox.width >= 250) &&
		                   (boundingBox.height > 56 && boundingBox.height <= 379); // Abaixo de 80% da altura da imagem
		        })
		        .toList();
		    
		    // Desenhar contornos na imagem original
		    /*Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(analise.screen, npcsFiltrados, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, analise.screen);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
		    
		    return npcsFiltrados;   
	}
	
	public List<MatOfPoint> balaoNpc() {
		
		Scalar lowerColor = new Scalar(0, 215, 215); // Limite inferior (laranja)
		Scalar upperColor = new Scalar(17, 255, 255); // Limite superior (laranja)
		Map<String, Scalar[]> colorRanges = Map.of("baloesNpc", new Scalar[]{lowerColor, upperColor});
		
		Rectangle captureArea = new Rectangle(xJanela, yJanela, width, height);
		BufferedImage screenFullImage = robot.createScreenCapture(captureArea);
		
		Mat screen = bufferedImageToMat(screenFullImage);
		if (screen.empty()) {
			System.out.println("Erro ao carregar a imagem.");
			return null;
		}
		
		Mat hsvImage = new Mat();
		Imgproc.cvtColor(screen, hsvImage, Imgproc.COLOR_BGR2HSV);
	
		Map<String, List<MatOfPoint>> detectedEntities = new HashMap<>();
	
		for (Map.Entry<String, Scalar[]> entry : colorRanges.entrySet()) {
			String colorName = entry.getKey();
			Scalar lowerColor2 = entry.getValue()[0];
			Scalar upperColor2 = entry.getValue()[1];
	
			// Criar máscara para identificar a cor dentro do intervalo
			Mat mask = new Mat();
			Core.inRange(hsvImage, lowerColor2, upperColor2, mask);
	
			// Encontrar contornos na máscara
			List<MatOfPoint> entidades = new ArrayList<>();
			Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	
			// Armazenar os contornos filtrados associados à cor
			detectedEntities.put(colorName, entidades);
		}
		
		List<MatOfPoint> npcs = detectedEntities.getOrDefault("baloesNpc", new ArrayList<>());
		
		if (npcs.isEmpty()) {
        	return npcs;
        }
		
	    // Filtrar NPCs por tamanho e posição
	    List<MatOfPoint> npcsFiltrados = npcs.stream()
	        .filter(npc -> {
	            Rect boundingBox = Imgproc.boundingRect(npc);
	            
	            return (boundingBox.width >= 250) &&
	                   (boundingBox.height > 56 && boundingBox.height <= 379); // Abaixo de 80% da altura da imagem
	        })
	        .toList();
	    
	    // Desenhar contornos na imagem original
	    /*Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
	    Imgproc.drawContours(screen, npcsFiltrados, -1, greenColor, 2);
	    // Salvar a imagem com os contornos desenhados
	    String contouredImagePath = "imagem_com_contornos.png";
	    Imgcodecs.imwrite(contouredImagePath, screen);
	    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		
		return npcsFiltrados;
	}
	
	
	
	
	
	public List<MatOfPoint> encontrarCor(
	        Scalar lowerColor,
	        Scalar upperColor,
	        int minWidth,
	        int maxWidth,
	        int minHeight,
	        int maxHeight,
	        boolean salvarImagem) {

	    // Criar mapa com as cores a serem analisadas
	    Map<String, Scalar[]> cores = Map.of("alvo", new Scalar[]{lowerColor, upperColor});

	    // Analisar a tela
	    MonstrosImagem analise = analisarTelaInteira(cores);

	    // Obter a lista de entidades da cor analisada
	    List<MatOfPoint> encontrados = analise.listaEntidades.getOrDefault("alvo", new ArrayList<>());

	    // Filtrar por tamanho
	    List<MatOfPoint> filtrados = encontrados.stream()
	        .filter(entidade -> {
	            Rect boundingBox = Imgproc.boundingRect(entidade);
	            return boundingBox.width >= minWidth &&
	                   boundingBox.width <= maxWidth &&
	                   boundingBox.height >= minHeight &&
	                   boundingBox.height <= maxHeight;
	        })
	        .toList();

	    // Se solicitado, salvar imagem com contornos
	    if (salvarImagem) {
	        Scalar corContorno = new Scalar(0, 255, 0); // verde
	        Imgproc.drawContours(analise.screen, filtrados, -1, corContorno, 2);
	        String caminho = "imagem_contornada.png";
	        Imgcodecs.imwrite(caminho, analise.screen);
	        System.out.println("Imagem salva em: " + caminho);
	    }

	    return filtrados;
	}

	
	public String obterMapa() {
		return memoria.obterMapa();
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
	
	public List<MatOfPoint> verificarJanelaEncerrarInstancia() {
		Scalar lowerColor = new Scalar(0, 0, 215);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(10, 40, 255);  // Limite superior (bracno)
	    
	    // Capturar a tela da área definida
	    Rectangle captureArea = null;
	    captureArea = new Rectangle(xJanela, yJanela, width, height);
	    BufferedImage screenFullImage = robot.createScreenCapture(captureArea);
	    
	    Map<String, Scalar[]> colorRanges = Map.of("baloesNpc", new Scalar[]{lowerColor, upperColor});

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
	        Scalar lowerColor2 = entry.getValue()[0];
	        Scalar upperColor2 = entry.getValue()[1];

	        // Criar máscara para identificar a cor dentro do intervalo
	        Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerColor2, upperColor2, mask);

	        // Encontrar contornos na máscara
	        List<MatOfPoint> entidades = new ArrayList<>();
	        Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        // Armazenar os contornos filtrados associados à cor
	        detectedEntities.put(colorName, entidades);
	    }
	    
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> janela = detectedEntities.getOrDefault("baloesNpc", new ArrayList<>());
	
			if (janela.isEmpty()) {
	        	return janela;
	        }
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> janelaEncerrarInstancia = janela.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            return (boundingBox.width >= 190 && boundingBox.width <= 210) && (boundingBox.height >= 120 && boundingBox.height <= 140);
		        })
		        .toList();
		    
		    
		    // Desenhar contornos na imagem original
		    /*Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(screen, janelaEncerrarInstancia, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, screen);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
	
		    return janelaEncerrarInstancia;   
	    
	}
	
	public List<MatOfPoint> verificarTamanhoJanelaHp() {
		Scalar lowerColor = new Scalar(0, 0, 215);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(10, 40, 255);  // Limite superior (bracno)
	    
	    // Capturar a tela da área definida
	    BufferedImage hpImage = printarParteTela(0, 0, 219, 157);
	    
	    
	    Map<String, Scalar[]> colorRanges = Map.of("janelaHp", new Scalar[]{lowerColor, upperColor});

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(hpImage);
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
	        Scalar lowerColor2 = entry.getValue()[0];
	        Scalar upperColor2 = entry.getValue()[1];

	        // Criar máscara para identificar a cor dentro do intervalo
	        Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerColor2, upperColor2, mask);

	        // Encontrar contornos na máscara
	        List<MatOfPoint> entidades = new ArrayList<>();
	        Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        // Armazenar os contornos filtrados associados à cor
	        detectedEntities.put(colorName, entidades);
	    }
	    
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> janela = detectedEntities.getOrDefault("janelaHp", new ArrayList<>());
	
			if (janela.isEmpty()) {
	        	return janela;
	        }
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> janelaEncerrarInstancia = janela.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            return boundingBox.width > 200 && boundingBox.height > 80;
		        })
		        .toList();
		    
		    
		    // Desenhar contornos na imagem original
		    /*Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(screen, janelaEncerrarInstancia, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, screen);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
	
		    return janelaEncerrarInstancia;   
	    
	}
	
	public List<MatOfPoint> procurarIventario() {
		Scalar lowerColor = new Scalar(0, 0, 215);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(10, 40, 255);  // Limite superior (bracno)
	    
	    // Capturar a tela da área definida
	    BufferedImage inventario = printarParteTela(0, 0, width, height);
	    
	    
	    Map<String, Scalar[]> colorRanges = Map.of("inventario", new Scalar[]{lowerColor, upperColor});

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(inventario);
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
	        Scalar lowerColor2 = entry.getValue()[0];
	        Scalar upperColor2 = entry.getValue()[1];

	        // Criar máscara para identificar a cor dentro do intervalo
	        Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerColor2, upperColor2, mask);

	        // Encontrar contornos na máscara
	        List<MatOfPoint> entidades = new ArrayList<>();
	        Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        // Armazenar os contornos filtrados associados à cor
	        detectedEntities.put(colorName, entidades);
	    }
	    
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> janela = detectedEntities.getOrDefault("inventario", new ArrayList<>());
	
			if (janela.isEmpty()) {
	        	return janela;
	        }
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> janelaEncerrarInstancia = janela.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            return boundingBox.width > 230 && (boundingBox.height > 170 && boundingBox.height < 231);
		        })
		        .toList();
		    
		    
		    // Desenhar contornos na imagem original
		    /*Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(screen, janelaEncerrarInstancia, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, screen);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
	
		    return janelaEncerrarInstancia;   
	    
	}
	
	public List<MatOfPoint> procurarArmazem() {
		Scalar lowerColor = new Scalar(0, 0, 215);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(10, 40, 255);  // Limite superior (bracno)
	    
	    // Capturar a tela da área definida
	    BufferedImage inventario = printarParteTela(0, 0, width, height);
	    
	    
	    Map<String, Scalar[]> colorRanges = Map.of("armazem", new Scalar[]{lowerColor, upperColor});

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(inventario);
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
	        Scalar lowerColor2 = entry.getValue()[0];
	        Scalar upperColor2 = entry.getValue()[1];

	        // Criar máscara para identificar a cor dentro do intervalo
	        Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerColor2, upperColor2, mask);

	        // Encontrar contornos na máscara
	        List<MatOfPoint> entidades = new ArrayList<>();
	        Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        // Armazenar os contornos filtrados associados à cor
	        detectedEntities.put(colorName, entidades);
	    }
	    
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> janela = detectedEntities.getOrDefault("armazem", new ArrayList<>());
	
			if (janela.isEmpty()) {
	        	return janela;
	        }
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> janelaEncerrarInstancia = janela.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            return boundingBox.width > 250 && (boundingBox.height > 350);
		        })
		        .toList();
		    
		    
		    // Desenhar contornos na imagem original
		    /*Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(screen, janelaEncerrarInstancia, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, screen);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
	
		    return janelaEncerrarInstancia;   
	    
	}
	
	public List<MatOfPoint> procurarMensagemPrivada() {
		Scalar lowerColor = new Scalar(80, 163, 214);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(100, 243, 255);  // Limite superior (bracno)
	    
	    // Capturar a tela da área definida
	    BufferedImage inventario = printarParteTela(0, 0, width, height);
	    
	    
	    Map<String, Scalar[]> colorRanges = Map.of("armazem", new Scalar[]{lowerColor, upperColor});

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(inventario);
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
	        Scalar lowerColor2 = entry.getValue()[0];
	        Scalar upperColor2 = entry.getValue()[1];

	        // Criar máscara para identificar a cor dentro do intervalo
	        Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerColor2, upperColor2, mask);

	        // Encontrar contornos na máscara
	        List<MatOfPoint> entidades = new ArrayList<>();
	        Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        // Armazenar os contornos filtrados associados à cor
	        detectedEntities.put(colorName, entidades);
	    }
	    
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> janela = detectedEntities.getOrDefault("armazem", new ArrayList<>());
	
			if (janela.isEmpty()) {
	        	return janela;
	        }
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> janelaEncerrarInstancia = janela.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            return boundingBox.width == 13 && (boundingBox.height == 13);
		        })
		        .toList();
		    
		    
		    // Desenhar contornos na imagem original
		    /*Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(screen, janelaEncerrarInstancia, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, screen);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
	
		    return janelaEncerrarInstancia;   
	    
	}
	
	public List<MatOfPoint> procurarAltQ() {
		Scalar lowerColor = new Scalar(0, 0, 215);  // Limite inferior (branco)
	    Scalar upperColor = new Scalar(10, 40, 255);  // Limite superior (bracno)
	    
	    // Capturar a tela da área definida
	    BufferedImage inventario = printarParteTela(0, 0, width, height);
	    
	    
	    Map<String, Scalar[]> colorRanges = Map.of("armazem", new Scalar[]{lowerColor, upperColor});

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(inventario);
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
	        Scalar lowerColor2 = entry.getValue()[0];
	        Scalar upperColor2 = entry.getValue()[1];

	        // Criar máscara para identificar a cor dentro do intervalo
	        Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerColor2, upperColor2, mask);

	        // Encontrar contornos na máscara
	        List<MatOfPoint> entidades = new ArrayList<>();
	        Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        // Armazenar os contornos filtrados associados à cor
	        detectedEntities.put(colorName, entidades);
	    }
	    
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> janela = detectedEntities.getOrDefault("armazem", new ArrayList<>());
	
			if (janela.isEmpty()) {
	        	return janela;
	        }
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> janelaEncerrarInstancia = janela.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            return boundingBox.width > 270 && (boundingBox.height > 130);
		        })
		        .toList();
		    
		    
		    // Desenhar contornos na imagem original
		    /*Scalar greenColor = new Scalar(0, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(screen, janelaEncerrarInstancia, -1, greenColor, 2);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, screen);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
	
		    return janelaEncerrarInstancia;   
	    
	}
	
	public List<MatOfPoint> procurarBarraSkills() {
		Scalar lowerColor = new Scalar(51, 215, 215);  // Limite inferior (Verde claro)
	    Scalar upperColor = new Scalar(71, 255, 255);  // Limite superior (Verde claro)
	    
	    // Capturar a tela da área definida
	    BufferedImage inventario = printarParteTela(0, 0, width, height);
	    
	    //salvarImagem(inventario, "teste de print");
	    
	    
	    Map<String, Scalar[]> colorRanges = Map.of("barra", new Scalar[]{lowerColor, upperColor});

	    // Converter BufferedImage para Mat diretamente
	    Mat screen = bufferedImageToMat(inventario);
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
	        Scalar lowerColor2 = entry.getValue()[0];
	        Scalar upperColor2 = entry.getValue()[1];

	        // Criar máscara para identificar a cor dentro do intervalo
	        Mat mask = new Mat();
	        Core.inRange(hsvImage, lowerColor2, upperColor2, mask);

	        // Encontrar contornos na máscara
	        List<MatOfPoint> entidades = new ArrayList<>();
	        Imgproc.findContours(mask, entidades, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	        
	        // Armazenar os contornos filtrados associados à cor
	        detectedEntities.put(colorName, entidades);
	    }
	    
	    // Obter a lista de balões de NPC usando a chave "baloesNpc"
	    List<MatOfPoint> janela = detectedEntities.getOrDefault("barra", new ArrayList<>());
	
			if (janela.isEmpty()) {
	        	return janela;
	        }
	        
		    // Filtrar NPCs por tamanho e posição
		    List<MatOfPoint> janelaEncerrarInstancia = janela.stream()
		        .filter(npc -> {
		            Rect boundingBox = Imgproc.boundingRect(npc);
		            return boundingBox.width > 250 ;
		        })
		        .toList();
		    
		    
		    // Desenhar contornos na imagem original
		    /*Scalar greenColor = new Scalar(255, 255, 0); // Cor verde para os contornos
		    Imgproc.drawContours(screen, janelaEncerrarInstancia, -1, greenColor, 1);
		    // Salvar a imagem com os contornos desenhados
		    String contouredImagePath = "imagem_com_contornos.png";
		    Imgcodecs.imwrite(contouredImagePath, screen);
		    System.out.println("Imagem com contornos salva em: " + contouredImagePath);*/
		    
		    if (!janelaEncerrarInstancia.isEmpty()) {
		    	System.out.println("Barra de skills encontrada!!!");
		    }
		    return janelaEncerrarInstancia;   
	    
	}
	
	public BufferedImage abrirImagem(String path) {
		BufferedImage imagem = null;
		try {
			imagem = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imagem;
	}
	
	public int calcularDistancia(Coordenadas atual, Coordenadas destino) {
	    return (int) Math.sqrt(Math.pow(destino.x - atual.x, 2) + Math.pow(destino.y - atual.y, 2));
	}
	
	//notebook
		public double calcularDistanciaCentro(MatOfPoint monstro) {
			Rect boundingRect = Imgproc.boundingRect(monstro);
			
			//System.out.println("Monstro em coordenadas: " + boundingRect.x + " " + boundingRect.y);

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
		apertarSegurarTecla(KeyEvent.VK_W);
	}
	public void soltarW() {
		soltarTecla(KeyEvent.VK_W);
	}
	
	public void atacarMonstro(MatOfPoint monstro, int tecla, Boolean selfSkill) {
        Rect rect = Imgproc.boundingRect(monstro);
        int centerX = xJanela + rect.x + rect.width / 2;
        int centerY = yJanela + rect.y + rect.height / 2;
        
        if (selfSkill) {
        	if (JanelaPrincipal.obterClasseSelecionada().equals("pala")) {
        		int asdp = 100;
        		if (GameController.aspdPala > 0) {
        			asdp = GameController.aspdPala;
        		}
        		apertarTecla(tecla);
        		sleep(asdp);
        		apertarTecla(tecla);
        		sleep(asdp);
        		apertarTecla(tecla);
        	}
        	apertarTecla(tecla);
        } else {
        	moverMouse(centerX, centerY + 10);
        	if (JanelaPrincipal.obterClasseSelecionada().equals("pala")) {
        		int aspd = 100;
        		if (GameController.aspdPalaTarget > 0) {
        			aspd = GameController.aspdPalaTarget;
        		}
        		sleep(aspd);
                apertarTecla(tecla);
                sleep(aspd);
                clicarMouse();
                sleep(aspd);
                apertarTecla(tecla);
                sleep(aspd);
                clicarMouse();
        		return;
        	}
            sleep(50);
            apertarTecla(tecla);
            sleep(50);
            clicarMouse();
        }
    }
	
	public void clicarMouse() {
		control.click();
	}
	public void clicarMouseDireito() {
		control.clicarMouseDireito();
	}
	
	public void clicarSegurarMouse() {
		control.clicarSegurarMouse();
	}
	public void soltarMouse() {
		control.soltarMouse();
	}
	
	public void moverMouse(int x, int y) {
		if (x >= getxJanela() + getWidth() || y >= getyJanela() + getHeight() ) {
			control.moverMouse(coordenadasJogadorTelaX, coordenadasJogadorTelaY);
			System.out.println("Mouse ia sair do limite da tela do ragnarok!!!");
			return;
		}
		control.moverMouse(x, y);
	}
	
	public void apertarTecla(int tecla) {
		control.apertarTecla(tecla);
	}
	
	public void apertarSegurarTecla( int tecla ) {
		control.apertarSegurarTecla(tecla);
	}
	public void soltarTecla( int tecla ) {
		control.soltarTecla(tecla);
	}
	public void segurarMouseDireito() {
		control.segurarMouseDireito();
	}
	public void soltarMouseDireito() {
		control.soltarMouseDireito();
	}
	
	private void scrollMouse(int scrollAmount) {
	    // scrollAmount positivo = scroll up, scrollAmount negativo = scroll down
		control.scrollMouse(scrollAmount);
	    sleep(70);
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
			//User32 user32 = User32.INSTANCE;
			
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
	
	/**
     * Obtém o HWND da janela do Ragnarok pelo número do processo (PID).
     */
    public static HWND getWindowHandleByPID(int pid) {
        final HWND[] hwndFound = { null };

        user32.EnumWindows((hWnd, data) -> {
            IntByReference procId = new IntByReference();
            user32.GetWindowThreadProcessId(hWnd, procId);

            if (procId.getValue() == pid) {
                hwndFound[0] = hWnd;
                return false; // Parar a enumeração, pois já encontramos a janela
            }
            return true;
        }, null);

        return hwndFound[0]; // Retorna o HWND encontrado (ou null se não achou)
    }
    
    public void focarRagnarok(HWND hwnd) {
    	User32.INSTANCE.SetForegroundWindow(hwnd);
    }
    
    public void getWindowSize(HWND hwnd) {
        if (hwnd == null) {
            System.out.println("Janela do Ragnarok não encontrada.");
            return;
        }
        
        MyUser32 myUser32 = MyUser32.INSTANCE;
       
        
        // Verifica se a janela está minimizada
        int state = MyUser32.INSTANCE.IsIconic(hwnd); // Retorna 1 se estiver minimizada
        if (state == 1) {
            System.out.println("Janela minimizada. Restaurando...");
            User32.INSTANCE.ShowWindow(hwnd, SW_RESTORE); // Restaura a janela
            try {
                Thread.sleep(500); // Pequena pausa para garantir que a janela seja restaurada
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
 
        RECT rect = new RECT();
        user32.GetClientRect(hwnd, rect);
        
        POINT topLeft = new POINT(0, 0);
        myUser32.ClientToScreen(hwnd, topLeft);
        
        this.xJanela = topLeft.x;
        this.yJanela = topLeft.y;
        this.width = rect.right - rect.left;
        this.height = rect.bottom - rect.top;

        System.out.println("Resolução da janela do Ragnarok: " + width + "x" + height);
        System.out.println("Posição da janela do Ragnarok: " + xJanela + " " + yJanela);
    }
    
    /**
     * Verifica se a janela do Ragnarok está em foco.
     */
    public static boolean isWindowInFocus(HWND hwnd) {
    	if (hwnd != null) {
    		if (user32.GetForegroundWindow().equals(hwnd)) {
    			return true;
    		}
    	}
        return false;
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
	
	public List<List<MatOfPoint>> filtrarMonstrosVisiveisRaycast(List<MatOfPoint> monstros, Mat screen) {
	    List<MatOfPoint> monstrosVisiveis = new ArrayList<>();
	    List<MatOfPoint> monstrosBloqueados = new ArrayList<>(); // Nova lista para os bloqueados

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
	        } else {
	            monstrosBloqueados.add(monstro);
	        }
	    }

	    // Retorna uma lista contendo as duas listas
	    return List.of(monstrosVisiveis, monstrosBloqueados);
	}
	
	private List<MatOfPoint> ajustarCoordenadas(List<MatOfPoint> monstros, int areaX, int areaY) {
	    List<MatOfPoint> ajustados = new ArrayList<>();
	    for (MatOfPoint entidade : monstros) {
	        List<org.opencv.core.Point> adjustedPoints = new ArrayList<>();
	        for (org.opencv.core.Point p : entidade.toList()) {
	            adjustedPoints.add(new org.opencv.core.Point(p.x + areaX, p.y + areaY));
	        }
	        MatOfPoint adjustedEntidade = new MatOfPoint();
	        adjustedEntidade.fromList(adjustedPoints);
	        ajustados.add(adjustedEntidade);
	    }
	    return ajustados;
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
	        	apertarSegurarTecla(KeyEvent.VK_SHIFT);
	        }
	        
	        apertarTecla(keyCode);
	        
	        if (isUpperCase || requiresShift) {
	            // Solta Shift após a letra maiúscula
	        	soltarTecla(KeyEvent.VK_SHIFT);
	        }
	       
			sleep(100);
			
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
		BufferedImage imagemTelaLogin = null;
		String path = "config/telas/login.png";
		try {
			imagemTelaLogin = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace(); // 573, 537, 76, 35
		}
		boolean imagensIguais = false;
		do {
			//BufferedImage atual = printarParteTela(573, 537, 76, 35);
			//imagensIguais = compararImagens(atual, imagemTelaLogin, 30.0);
			imagensIguais = procurarImagem(imagemTelaLogin, 0.8);
			System.out.println("Verificando imagens realizar login: " + imagensIguais);
			sleep(500);
		} while( imagensIguais == false);
		System.out.println("Realizando login");
		System.out.println("Mouse indo para o campo de login");
		moverMouse(xJanela + 228, yJanela + 492);
		sleep(200);
		apertarTecla(KeyEvent.VK_TAB);
		sleep(500);
		System.out.println("Digitando o login");
		digitarTexto(usuario);
		sleep(500);
		System.out.println("Mouse indo para o campo de senha");
		apertarTecla(KeyEvent.VK_TAB);
		sleep(500);
		System.out.println("Digitando a senha");
		digitarTexto(senha);
		sleep(500);
		apertarTecla(KeyEvent.VK_ENTER);
		sleep(500);
	}
	
	public boolean procurarImagem(BufferedImage imagemAlvo, double threshold) {
	    // Captura a tela do jogo
	    BufferedImage tela = printarTela();

	    // Converte as imagens BufferedImage para Mat
	    Mat telaMat = bufferedImageToMat(tela);
	    Mat imagemAlvoMat = bufferedImageToMat(imagemAlvo);

	    // Converte para tons de cinza para facilitar a detecção
	    Mat telaGray = new Mat();
	    Mat alvoGray = new Mat();
	    Imgproc.cvtColor(telaMat, telaGray, Imgproc.COLOR_BGR2GRAY);
	    Imgproc.cvtColor(imagemAlvoMat, alvoGray, Imgproc.COLOR_BGR2GRAY);

	    // Resultado da comparação
	    int resultCols = telaGray.cols() - alvoGray.cols() + 1;
	    int resultRows = telaGray.rows() - alvoGray.rows() + 1;
	    Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

	    // Aplica o template matching
	    Imgproc.matchTemplate(telaGray, alvoGray, result, Imgproc.TM_CCOEFF_NORMED);

	    // Localiza o melhor resultado
	    Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
	    double maxVal = mmr.maxVal;

	    System.out.println("Resultado da similaridade (template matching): " + maxVal);

	    // Se o valor máximo for maior que o limiar (threshold), a imagem foi encontrada
	    return maxVal >= threshold;
	}
	
	public BufferedImage printarParteTela(int x, int y, int width, int heigh) {
		Rectangle captureArea = new Rectangle(xJanela + x, yJanela + y, width, heigh);
		return robot.createScreenCapture(captureArea);
	}
	public void salvarImagem(BufferedImage imagem, String nomeArquivo) {
	    try {
	        File outputFile = new File(nomeArquivo + ".png"); // Salva na raiz do projeto
	        ImageIO.write(imagem, "png", outputFile);
	        System.out.println("Imagem salva como: " + outputFile.getAbsolutePath());
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public Rect encontrarImagem(BufferedImage imagemAlvo, double threshold) {
	    // Captura a tela do jogo
	    BufferedImage tela = printarTela();

	    // Converte as imagens BufferedImage para Mat
	    Mat telaMat = bufferedImageToMat(tela);
	    Mat imagemAlvoMat = bufferedImageToMat(imagemAlvo);

	    // Converte para tons de cinza para facilitar a detecção
	    Mat telaGray = new Mat();
	    Mat alvoGray = new Mat();
	    Imgproc.cvtColor(telaMat, telaGray, Imgproc.COLOR_BGR2GRAY);
	    Imgproc.cvtColor(imagemAlvoMat, alvoGray, Imgproc.COLOR_BGR2GRAY);

	    // Resultado da comparação
	    int resultCols = telaGray.cols() - alvoGray.cols() + 1;
	    int resultRows = telaGray.rows() - alvoGray.rows() + 1;
	    Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

	    // Aplica o template matching
	    Imgproc.matchTemplate(telaGray, alvoGray, result, Imgproc.TM_CCOEFF_NORMED);

	    // Localiza o melhor resultado
	    Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
	    double maxVal = mmr.maxVal;

	    System.out.println("Resultado da similaridade (template matching): " + maxVal);

	    // Se o valor máximo for maior que o limiar (threshold), retorna o retângulo da posição
	    if (maxVal >= threshold) {
	    	org.opencv.core.Point matchLoc = mmr.maxLoc;
	        return new Rect(
	            (int) matchLoc.x,
	            (int) matchLoc.y,
	            imagemAlvoMat.cols(),
	            imagemAlvoMat.rows()
	        );
	    }

	    // Caso contrário, não encontrou nada
	    return null;
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
		
		public Coordenadas getCoordenadasTelaDoBixo(Coordenadas atual, int monstroX, int monstroY) {
			// Obter a posição atual do mouse
			// java.awt.Point mousePos = java.awt.MouseInfo.getPointerInfo().getLocation();
			// int mx = (int) mousePos.getX() - xJanela;
			// int my = (int) mousePos.getY() - yJanela;
			int mx = monstroX;
			int my = monstroY;

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
			//int mouseX = xJanela + 505 + dx * 18 + 9;
			int mouseX = xJanela + 505 + dx * 18 + 5; //(-5)
			//int mouseY = yJanela + 376 - dy * 18 + 9;
			int mouseY = yJanela + 376 - dy * 18 + 7; //(-2)

			// Mover o mouse para a posição calculada
			moverMouse(mouseX, mouseY);

		}
		
		// notebook
		/*public Coordenadas escolherProximaCoordenada(List<Coordenadas> caminhoCalculado, Coordenadas atual) {
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
		}*/
		
		public Coordenadas escolherProximaCoordenada(List<Coordenadas> caminhoCalculado, Coordenadas atual) {
			
		    if (caminhoCalculado.isEmpty() || caminhoCalculado.size() <= 1) {
		        System.out.println("Caiu aqui #####################################");
		        return atual;
		    }

		    // Ângulo máximo permitido (80 graus)
		    double anguloMaximo = Math.toRadians(80);

		    // Direção inicial
		    Coordenadas inicial = caminhoCalculado.get(1);
		    int direcaoInicialX = inicial.x - atual.x;
		    int direcaoInicialY = inicial.y - atual.y;

		    // Verifica se o movimento é em linha reta ou diagonal
		    boolean movimentoReto = (direcaoInicialX == 0 || direcaoInicialY == 0);
		    boolean movimentoDiagonal = (Math.abs(direcaoInicialX) == Math.abs(direcaoInicialY));

		    // Define o limite baseado no tipo de movimento
		    int limiteMovimento = 20; // Valor padrão era 14

		    if (movimentoReto) {
		        limiteMovimento = 12; // Caminho reto → máximo de 9 células
		    } else if (movimentoDiagonal) {
		        limiteMovimento = 12; // Caminho diagonal → máximo de 12 células
		    }

		    Coordenadas candidata = atual;

		    for (int i = 1; i < caminhoCalculado.size() && i <= limiteMovimento; i++) {
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
		            return candidata; // Retorna a última coordenada válida antes da curva acentuada
		        }

		        // Atualizar a candidata se o ângulo estiver dentro do limite
		        candidata = proxima;
		    }

		    // Retornar a última coordenada válida dentro do limite ajustado
		    return candidata;
		}
	
	public void voltarMoroc(int numero) {
	    if (numero < 0 || numero > 9) {
	        throw new IllegalArgumentException("O número deve estar entre 0 e 9.");
	    }

	    // Pressionar Alt
	    apertarSegurarTecla(KeyEvent.VK_ALT);

	    // Obter a tecla correspondente ao número
	    int keyCode = KeyEvent.VK_0 + numero;

	    // Pressionar o número
	    apertarTecla(keyCode);
	    sleep(100);
	    // Soltar Alt
	    soltarTecla(KeyEvent.VK_ALT);
	}
	
	//notebook
	public void visaoDeCima() {
		// Pressionar Ctrl e Shift
		apertarSegurarTecla(KeyEvent.VK_CONTROL);
		apertarSegurarTecla(KeyEvent.VK_SHIFT);
		
		// Pressionar o botão direito do mouse
		segurarMouseDireito();
		
		if (JanelaPrincipal.obterSalaSelecionada().equals("1")) {
			moverMouse(this.xJanela + coordenadasJogadorTelaX, this.yJanela + coordenadasJogadorTelaY - 50);
		} else {
			moverMouse(this.xJanela + coordenadasJogadorTelaX, this.yJanela + coordenadasJogadorTelaY - 100);
		}
		sleep(20);

		// Obter a posição atual do mouse
		java.awt.Point mousePos = java.awt.MouseInfo.getPointerInfo().getLocation();
		int currentX = (int) mousePos.getX();
		int currentY = (int) mousePos.getY();

		sleep(20);
		// Mover o mouse 300 pixels para baixo
		moverMouse(currentX, currentY + 250);
		
		sleep(100);
		// Soltar o botão direito do mouse
		soltarMouseDireito();

		// Soltar Ctrl e Shift

		soltarTecla(KeyEvent.VK_SHIFT);
		soltarTecla(KeyEvent.VK_CONTROL);
		
		
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
		public int getMaxHp() {
			return memoria.obterHP(memoria.processId, memoria.addressMaxHp);
		}
		public int getZeny() {
			return memoria.obterZeny(memoria.processId, memoria.addressZeny);
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
	        apertarSegurarTecla(KeyEvent.VK_ALT);

	        // Determina a tecla correspondente ao número
	        int keyCode = KeyEvent.VK_0 + numero;

	        // Pressiona e solta a tecla do número
	        apertarTecla(keyCode);

	        // Solta a tecla Alt
	        soltarTecla(KeyEvent.VK_ALT);

	    }
	    
	    
	public void executarInstancia(String instancia) {
		
		List<MatOfPoint> janelaHpGrande = verificarTamanhoJanelaHp(); 
		System.out.println("Tamanho da janela do hp é grande?: " + (janelaHpGrande.isEmpty()? false : true));
		
		int xJ = 65;
		int yJ = 0;
		
		if (janelaHpGrande.isEmpty()) {//clicar mais em cima
			yJ = 150;
		} else {//clicar mais em baixo
			yJ = 227;
		}
		
		List<MatOfPoint> janelaInstancia = new ArrayList<>();
		do {
			if (janelaInstancia.isEmpty()) {
				moverMouse(getxJanela() + xJ, getyJanela() + yJ);
				sleep(300);
				clicarMouse();
			}
			
			sleep(300);
			
			janelaInstancia = verificarJanelaInstancia();
			
		} while(janelaInstancia.isEmpty());
		
		
		// Selecionar de fato a instancia
		sleep(300);
		
		int x = 0;
		int y = 0;
		
		if (!janelaInstancia.isEmpty()) {
			Rect janela = Imgproc.boundingRect(janelaInstancia.get(0));
			x = janela.x;
			y = janela.y;
		}

		sleep(1000);
		moverMouse(getxJanela() + x + 70, getyJanela() + y + 140);
		// int dg = 43; //tomb
		int dg = getNumeroInstancia(instancia);
		System.out.println("Numero dg: " + dg + " instancia: " + instancia);
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
		
		List<MatOfPoint> janelinhaInstancia = new ArrayList<>();
		do {
			if (JanelaPrincipal.obterSlave() == true) {
				break;
			}
			Scalar[] limites = calcularLimites(255, 72, 0);// Cor laranja
			Scalar minLaranja = limites[0];
			Scalar maxLaranja = limites[1];
			//Procurando o botao de Ok
			List<MatOfPoint> btnOk = new ArrayList<MatOfPoint>();
			do {
				btnOk = encontrarCor(minLaranja, maxLaranja, 35, 45, 15, 25, false);
				System.out.println("Botao Ok - Lista está vazia? " + btnOk.isEmpty());
				if (btnOk.isEmpty()) {
					System.out.println("Clicando no botão de Criar!");
					moverMouse(getxJanela() + x + 141, getyJanela() + y + 304 + 17);
					sleep(300);
					clicarMouse();
					sleep(300);
				} else {
					System.out.println("Clicando nob botão de Ok");
					Rect ma = Imgproc.boundingRect(btnOk.get(0));
					moverMouse(getxJanela() + ma.x + ma.width / 2, getyJanela() + ma.y + ma.height / 2);
					sleep(50);
					clicarMouse();
				}
				sleep(100);
			} while (btnOk.isEmpty());
			
			
			List<MatOfPoint> balaoNpc = new ArrayList<MatOfPoint>();
			int contador = 0;
			do {
				System.out.println("Procurando pelo balão de npc");
				balaoNpc = balaoNpc();
				if (!balaoNpc.isEmpty()) {
					sleep(1000);
					System.out.println("Apertando Enter no balão de npc");
					apertarTecla(KeyEvent.VK_ENTER);
				}
				contador++;
				sleep(1000);
			} while (balaoNpc.isEmpty() && contador < 10);
			
			sleep(1000);
			System.out.println("Verificando se a janela de instancia apareceu...");
			janelinhaInstancia = verificarJanelaEncerrarInstancia();
			
		} while (janelinhaInstancia.isEmpty());
		
		sleep(1000);
		
		clicarEmEntrarInstancia(x, y);
		
		System.out.println("Caso tenha resetado as instancias" );
		List<MatOfPoint> balaoNpc = null;
		int contador = 0;
		do {
			System.out.println("Procurando pelo balão de npc");
			balaoNpc = balaoNpc();
			if (!balaoNpc.isEmpty()) {
				apertarTecla(KeyEvent.VK_ENTER);
				sleep(300);
				clicarEmEntrarInstancia(x,y);
				break;
			}
			contador++;
			sleep(1000);
			if (dg == 44) {
				if (obterMapa().equals(Mapa.TOMB.getNome())) {
					System.out.println("Chegou em tomb");
					break;
				}
			}
		} while (contador < 10);
		
		
		boolean chegou = false;
		if (dg == 34) {
			do {
				chegou = obterMapa().equals(Mapa.OLDGH.getNome());
				System.out.println("Chegou em Old Glast Heim? " + chegou);
				sleep(1000);
			} while (chegou == false);
		} else if (dg == 44) {
			do {
				chegou = obterMapa().equals(Mapa.TOMB.getNome());
				System.out.println("Chegou em Tomb of Remorse? " + chegou);
				sleep(1000);
			} while (chegou == false);
		}
		sleep(3000);
		visaoDeCima();
		sleep(100);
		zoom(-28);
	}
	
	private void clicarEmEntrarInstancia(int x, int y) {
		System.out.println("Clicando em entrar");
		Scalar[] limites = calcularLimites(255, 72, 0);// Cor laranja
		Scalar minLaranja = limites[0];
		Scalar maxLaranja = limites[1];
		//Procurando o botao de Ok
		List<MatOfPoint> btnOk = new ArrayList<MatOfPoint>();
		do {
			btnOk = encontrarCor(minLaranja, maxLaranja, 35, 45, 15, 25, false);
			System.out.println("Botao Ok - Lista está vazia? " + btnOk.isEmpty());
			if (btnOk.isEmpty()) {
				System.out.println("Clicando no botão de Entrar!");
				moverMouse(getxJanela() + x + 48, getyJanela() + y + 304 + 17);
				sleep(300);
				clicarMouse();
				sleep(500);
			} else {
				System.out.println("Clicando nob botão de Ok");
				Rect ma = Imgproc.boundingRect(btnOk.get(0));
				moverMouse(getxJanela() + ma.x + ma.width / 2, getyJanela() + ma.y + ma.height / 2);
				sleep(50);
				clicarMouse();
			}
			sleep(100);
		} while (btnOk.isEmpty());
		
		sleep(1000);
		System.out.println("Procurando balão de npc");
		List<MatOfPoint> balaoNpc = new ArrayList<MatOfPoint>();
		do {
			System.out.println("Procurando pelo balão de npc");
			balaoNpc = balaoNpc();
			if (!balaoNpc.isEmpty()) {
				System.out.println("Apertando pra baixo varias vezes para escolher a ultima opção");
				for (int i = 0; i < 10; i++) {
					apertarTecla(KeyEvent.VK_DOWN);
					sleep(300);
				}
				apertarTecla(KeyEvent.VK_ENTER);
			}
			sleep(1000);
		} while (balaoNpc.isEmpty());
	}
	
	private int getNumeroInstancia(String instancia) {
		int dg = 0;
		switch (instancia) {
			case "Old Glast Heim":
				dg = 34;
				break;
			case "Tomb of Remorse":
				dg = 44;
				break;
	
			default:
				break;
		}
		return dg;
	}
	
	public void encerrarInstancia() {
		List<MatOfPoint> janela = verificarJanelaEncerrarInstancia();
		System.out.println("tamanho da lista janela: " + janela.size());
		if (!janela.isEmpty()) {
			Rect j = Imgproc.boundingRect(janela.get(0));
			int centerX = j.x + j.width/2;
			int centerY = j.y + j.height/2;
			
			moverMouse(getxJanela() + centerX, getyJanela() + centerY + 40);
			sleep(1000);
			clicarMouse();
			sleep(500);
			System.out.println("Verificando se a instancia fechou mesmo");
			List<MatOfPoint> janelaFechada = verificarJanelaEncerrarInstancia();
			while(!janelaFechada.isEmpty()) {
				System.out.println("Não fechou ainda, tentando fechar");
				moverMouse(getxJanela() + centerX, getyJanela() + centerY + 40);
				sleep(1000);
				clicarMouse();
				janelaFechada = verificarJanelaEncerrarInstancia();
				sleep(500);
			}
			
		}
		System.out.println("Instancia Fechada com sucesso!");
	}
	
	public void deslogarPersonagem() {
		
		BufferedImage imagemTelaPin = null;
		String path = "config/telas/pin.png";
		try {
			imagemTelaPin = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace(); // 960 79 13 12
		}
		boolean imagensIguais = false;
		do {
			
			//BufferedImage atual = printarParteTela(960, 79, 13, 12);
			//imagensIguais = compararImagens(atual, imagemTelaPin, 30.0);
			imagensIguais = procurarImagem(imagemTelaPin, 0.8);
			System.out.println("Verificando imagens: " + imagensIguais);
			sleep(1000);
			
			if (!imagensIguais) {
				BufferedImage selecionarPersonagemImg = null;
				String pathImg = "config/telas/selecionarPersonagem.png";
				Rect pos = null;
				try {
	    			selecionarPersonagemImg = ImageIO.read(new File(pathImg));
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
				do {
					System.out.println("Apertando ESC e clicando Selecionar Personagem");
					pos = encontrarImagem(selecionarPersonagemImg, 0.85);
		        	if (pos != null) {
		        		moverMouse(getxJanela() + pos.x + pos.width/2, getyJanela() + pos.y + pos.height/2);
						sleep(300);
						clicarMouse();
						sleep(300);
		        		break;
		        	}
		        	sleep(1000);
		        	apertarTecla(KeyEvent.VK_ESCAPE);
		        	sleep(50);
		        	moverMouse(getxJanela() + 50,getyJanela() + 50);
					sleep(1000);
				} while (true);
				System.out.println("Selecionar Personagem Clicado");
				
			}
			
			sleep(1000);
		} while( imagensIguais == false);
		
		
		
		
		
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
	
	public List<Integer> listarStatus() {
		return memoria.listarStatus();
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

	public Robot getRobot() {
		return robot;
	}

	public void setRobot(Robot robot) {
		this.robot = robot;
	}

	public ITesseract getTesseract() {
		return tesseract;
	}

	public void setTesseract(ITesseract tesseract) {
		this.tesseract = tesseract;
	}

	public ITesseract getTesseractLetras() {
		return tesseractLetras;
	}

	public void setTesseractLetras(ITesseract tesseractLetras) {
		this.tesseractLetras = tesseractLetras;
	}

	public void teste() {
		Mat tela = Imgcodecs.imread("teste de print.png");
		Mat template = Imgcodecs.imread("testeopencv.png");
		Mat resultado = new Mat();

		Imgproc.matchTemplate(tela, template, resultado, Imgproc.TM_CCOEFF_NORMED);

		// Encontrar ponto de maior correspondência
		Core.MinMaxLocResult mmr = Core.minMaxLoc(resultado);
		if (mmr.maxVal > 0.8) { // Threshold de confiança
		    org.opencv.core.Point matchLoc = mmr.maxLoc;
		    System.out.println("Imagem encontrada em: " + matchLoc);
		    moverMouse(getxJanela() + (int)matchLoc.x, getyJanela() + (int)matchLoc.y);
		}
	}
	
	public Scalar[] calcularLimites(int r, int g, int b) {
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
	
	public int contarPixelsDeUmaImagem(String path, Color cor, int x, int y, int width, int height) {
		try {
			File arquivoOriginal = new File(path);
			BufferedImage imagemGrande = ImageIO.read(arquivoOriginal);
			
			
			/*int x = 535;
            int y = 45;
            int width = 146;
            int height = 116;*/
            
            BufferedImage imagemCortada = imagemGrande.getSubimage(x, y, width, height);
            
            File arquivoDestino = new File("imagemCortada.png");
            ImageIO.write(imagemCortada, "png", arquivoDestino);

            System.out.println("Imagem cortada e salva com sucesso em: " + arquivoDestino.getAbsolutePath());
            
            int contador = 0;
            int tolerancia = 10;
            // Percorre cada pixel da imagem capturada
            for (int i = 0; i < imagemCortada.getWidth(); i++) {
                for (int j = 0; j < imagemCortada.getHeight(); j++) {
                    Color pixel = new Color(imagemCortada.getRGB(i, j));
                    // Se o pixel for da cor desejada, incrementa o contador
                    if (Math.abs(pixel.getRed() - cor.getRed()) <= tolerancia &&
                    		Math.abs(pixel.getGreen() - cor.getGreen()) <= tolerancia &&
                    		Math.abs(pixel.getBlue() - cor.getBlue()) <= tolerancia) {
                    	contador++;
                    }
                    
                }
            }
            
            System.out.println("Quantidade de cor do texto: " + contador);
            
            return contador;
            
            //int pixelsAzulTexto = bot.contarPixels(azulTexto, x, y, width, height);
			
		} catch (IOException e) {
            System.err.println("Erro ao ler ou salvar imagem: " + e.getMessage());
        }
		return 0;
	}
	
	
}
