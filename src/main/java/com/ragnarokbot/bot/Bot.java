package com.ragnarokbot.bot;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

public class Bot {
	
	private Robot robot;
	private ITesseract tesseract;
	
    private int coordenadasJogadorTelaX;
    private int coordenadasJogadorTelaY;
    private int width;
    private int height;
    
    //Variaveis para coordenadas mini mapa
    private int xOcrCoordenadas;
    private int yOcrCoordenadas;
    private int widthOcrCoordenadas;
    private int heightOcrCoordenadas;
    private int xJanela;
    private int yJanela;

	public Bot(ITesseract tesseract, Robot robot) {
		
		this.tesseract = tesseract;
        this.robot = robot;
        
        getWidthHeight();
        this.coordenadasJogadorTelaX = width/2;
        this.coordenadasJogadorTelaY = (height/2) +5;
        
        xOcrCoordenadas = (int) (width * 0.9273);
        yOcrCoordenadas = (int) (height * 0.1582);
        widthOcrCoordenadas = (int) (width * 0.0290);
        heightOcrCoordenadas = (int) (height * 0.0107);
        
        System.out.println("xOcrCoordenadas " + xOcrCoordenadas);
        System.out.println("yOcrCoordenadas " + yOcrCoordenadas);
        System.out.println("widthOcrCoordenadas " + widthOcrCoordenadas);
        System.out.println("heightOcrCoordenadas " + heightOcrCoordenadas);
        
        //BufferedImage telaPrintada = this.printarTela();
        //this.coordenadasJogadorTelaX = telaPrintada.getWidth()/2 - 40;
        //this.coordenadasJogadorTelaY = telaPrintada.getHeight()/2 + 30;
		
	}
	
	public String ocr(int x, int y, int width, int height) throws IOException, TesseractException {
		Rectangle area = new Rectangle(x, y, width, height);
        BufferedImage areaCapturada = robot.createScreenCapture(area);
        
        // Salva a imagem capturada para verificar se está correta (opcional)
        ImageIO.write(areaCapturada, "png", new File("hp_capture.png"));
        System.out.println("Imagem do HP salva como 'hp_capture.png'.");
        
        // Executa o OCR na imagem capturada
        String ocr = tesseract.doOCR(areaCapturada);
        
        System.out.println("x: " + (x) + ", y: " + (y) + " width: " + width + " height " + height );
		
		return ocr;
	}
	
	public String ocrCoordenadas() throws IOException, TesseractException {
		System.out.println("X da janela: " + xJanela + ", Y da janela: " +yJanela);
		return this.ocr( xJanela + xOcrCoordenadas, yJanela + yOcrCoordenadas,widthOcrCoordenadas,heightOcrCoordenadas);
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
	
	public List<MatOfPoint> listaMonstros() throws IOException {
		
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
        java.util.List<MatOfPoint> monstros = new java.util.ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, monstros, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        
        //salvar no disco
        //BotRagnarok.apagar = screen;
        //Imgcodecs.imwrite("C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/sprites/testeRam.png", screen);
        
        return monstros;
    }
	
	
	public int calcularDistancia(Coordenadas atual, Coordenadas destino) {
	    return (int) Math.sqrt(Math.pow(destino.x - atual.x, 2) + Math.pow(destino.y - atual.y, 2));
	}
	
	public double calcularDistanciaCentro(MatOfPoint monstro) {
        Rect boundingRect = Imgproc.boundingRect(monstro);
        return Math.sqrt(Math.pow(xJanela + boundingRect.x - coordenadasJogadorTelaX + xJanela, 2) 
        		+ Math.pow(yJanela + boundingRect.y - coordenadasJogadorTelaY + yJanela, 2));
    }
	
	public void moverPersonagem(Coordenadas atual, Coordenadas destino) throws Exception {
        int xMouse = this.xJanela + this.coordenadasJogadorTelaX + (destino.x - atual.x) * 4;
        int yMouse = this.yJanela + this.coordenadasJogadorTelaY - (destino.y - atual.y) * 4;

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
	        HWND hwnd = user32.FindWindow(null, "History Reborn | Gepard Shield 3.0 (^-_-^)"); // Nome da janela do Ragnarok
	        
	        if (hwnd == null) {
	            System.out.println("Janela do Ragnarok não encontrada.");
	            return;
	        }
	        // Garantir que a janela tenha o foco
	        User32.INSTANCE.SetForegroundWindow(hwnd);
	
	        RECT rect = new RECT();
	        user32.GetWindowRect(hwnd, rect);
	
	        this.width = rect.right - rect.left;
	        this.height = rect.bottom - rect.top;
	        this.xJanela = rect.left;
	        this.yJanela = rect.top;
	
	        System.out.println("Resolução da janela do Ragnarok: " + width + "x" + height);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
