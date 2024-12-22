package com.ragnarokbot.bot;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.ragnarokbot.main.BotRagnarok;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.MonstrosImagem;
import com.ragnarokbot.model.MyUser32;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

import config.ConfigManager;
import config.ConfigManager.Config;

import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.POINT;

public class Bot {
	
	private Robot robot;
	private ITesseract tesseract;
	
	public static HWND hwnd;
	
    private int coordenadasJogadorTelaX;
    private int coordenadasJogadorTelaY;
    private int width;
    private int height;
    
    private int xJanela;
    private int yJanela;
    
    //Variaveis para coordenadas mini mapa
    public Config configOCR;

	public Bot(ITesseract tesseract, Robot robot) {
		
		this.tesseract = tesseract;
        this.robot = robot;
        this.configOCR = ConfigManager.loadConfig();
        
        getWidthHeight();
        this.coordenadasJogadorTelaX = width / 2;
        this.coordenadasJogadorTelaY = height / 2;
		
	}
	
	public String ocr(int x, int y, int width, int height) throws IOException, TesseractException {
		Rectangle area = new Rectangle(x, y, width, height);
        BufferedImage areaCapturada = robot.createScreenCapture(area);
        
        // Salva a imagem capturada para verificar se está correta (opcional)
        //ImageIO.write(areaCapturada, "png", new File("hp_capture.png"));
        //System.out.println("Imagem do HP salva como 'hp_capture.png'.");
        
        // Executa o OCR na imagem capturada
        String ocr = tesseract.doOCR(areaCapturada);
        
        System.out.println("x: " + (x) + ", y: " + (y) + " width: " + width + " height " + height );
		
		return ocr;
	}
	
	public String ocrCoordenadas() throws IOException, TesseractException {
		System.out.println("X da janela: " + xJanela + ", Y da janela: " +yJanela);
		//return this.ocr( xJanela + xOcrCoordenadas + configOCR.rectangle.x, yJanela + yOcrCoordenadas,widthOcrCoordenadas,heightOcrCoordenadas);
		return this.ocr( xJanela + configOCR.rectangle.x, yJanela + configOCR.rectangle.y,configOCR.rectangle.width,configOCR.rectangle.height);
	}
	
	public BufferedImage printarTela() {
		
		// Captura a tela da área da janela
        Rectangle captureArea = new Rectangle(xJanela, yJanela, width, height);
        return robot.createScreenCapture(captureArea);
		//Pegar tamanho da tela
        //Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        // Tirar o print da tela
        //return robot.createScreenCapture(screenRect);
	}
	
	//public List<MatOfPoint> listaMonstros() throws IOException {
	public MonstrosImagem monstrosImagem() throws IOException {
		
		BufferedImage screenFullImage = printarTela();
        
		// Converter BufferedImage para array de bytes
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(screenFullImage, "png", byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Converter array de bytes para Mat
        Mat buffer = new Mat(imageBytes.length, 1, CvType.CV_8UC1); // Mat com os bytes da imagem
        buffer.put(0, 0, imageBytes);

        // Decodificar o Mat para obter a imagem
        Mat screen = Imgcodecs.imdecode(buffer, Imgcodecs.IMREAD_COLOR);
        
        if (screen.empty()) {
            System.out.println("Erro ao carregar a imagem.");
            return null;
        }
        
        // Converter a imagem para o espaço de cores HSV
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(screen, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Definir os limites inferior e superior da cor (exemplo: rosa)
        //Cor origial HSV: 302 80 100 - Hue vai de 0 a 180 (não de 0 a 360) - Saturation vai de 0 a 255 (0% a 100%). - Value vai de 0 a 255 (0% a 100%).
        //Cor convertida para o OpenCV 151 204 255
        Scalar lowerColor = new Scalar(148, 200, 200);  // Limite inferior para a cor desejada (era 148,150,150)
        Scalar upperColor = new Scalar(154, 255, 255); // Limite superior para a cor desejada

        // Criar uma máscara onde a cor estiver dentro do intervalo definido
        Mat mask = new Mat();
        Core.inRange(hsvImage, lowerColor, upperColor, mask);

        // Encontrar contornos na máscara
        java.util.List<MatOfPoint> todosMonstros = new java.util.ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, todosMonstros, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        // Filtrar monstros com altura maior que 20 pixels
        List<MatOfPoint> monstrosFiltrados = new ArrayList<>();
        for (MatOfPoint monstro : todosMonstros) {
            Rect boundingRect = Imgproc.boundingRect(monstro);
            if (boundingRect.height >= 20) {
                monstrosFiltrados.add(monstro);
            }
        }

        //salvar no disco
        //BotRagnarok.apagar = screen;
        //Imgcodecs.imwrite("C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/sprites/testeRam.png", screen);
        
        return new MonstrosImagem(monstrosFiltrados, screen);
    }
	
	
	public int calcularDistancia(Coordenadas atual, Coordenadas destino) {
	    return (int) Math.sqrt(Math.pow(destino.x - atual.x, 2) + Math.pow(destino.y - atual.y, 2));
	}
	
	public double calcularDistanciaCentro(MatOfPoint monstro) {
        Rect boundingRect = Imgproc.boundingRect(monstro);
        
        // Calcula o centro do monstro
        int monstroCentroX = boundingRect.x + boundingRect.width / 2;
        int monstroCentroY = boundingRect.y + boundingRect.height / 2;
        
        return Math.sqrt(Math.pow(xJanela + monstroCentroX - coordenadasJogadorTelaX + xJanela, 2) 
        		+ Math.pow(yJanela + monstroCentroY - coordenadasJogadorTelaY + yJanela, 2));
    }
	
	public void moverPersonagem(Coordenadas atual, Coordenadas destino) throws Exception {
	
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
		
		
        moverMouse(xMouse, yMouse);
        Thread.sleep(500);
        clicarMouse();
    }
	
	public void atacarMonstro(MatOfPoint monstro) throws Exception {
        Rect rect = Imgproc.boundingRect(monstro);
        int centerX = xJanela + rect.x + rect.width / 2;
        int centerY = yJanela + rect.y + rect.height / 2;

        moverMouse(centerX, centerY);
        Thread.sleep(50);
        apertarTecla(KeyEvent.VK_Q);
        Thread.sleep(50);
        clicarMouse();
    }
	
	public void clicarMouse() throws InterruptedException {
		robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK); // Pressionar o botão esquerdo
        Thread.sleep(50);
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK); // Liberar o botão esquerdo
	}
	
	
	public void moverMouse(int x, int y) {
		 robot.mouseMove(x, y);
	}
	
	public void apertarTecla(int tecla) throws InterruptedException {
		 robot.keyPress(tecla); // Pressionar a tecla
		 Thread.sleep(50);
         robot.keyRelease(tecla); // Liberar a tecla
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
        Point posicaoJogador = new Point(width / 2, height / 2);
        
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
