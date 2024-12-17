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
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Bot {
	
	private Robot robot;
	private ITesseract tesseract;

	public Bot(ITesseract tesseract, Robot robot) {
		
		this.tesseract = tesseract;
        this.robot = robot;
		
	}
	
	public String ocr(int x, int y, int width, int height) throws IOException, TesseractException {
		Rectangle area = new Rectangle(x, y, width, height);
        BufferedImage areaCapturada = robot.createScreenCapture(area);
        
        // Salva a imagem capturada para verificar se está correta (opcional)
        //ImageIO.write(areaCapturada, "png", new File("hp_capture.png"));
        //System.out.println("Imagem do HP salva como 'hp_capture.png'.");
        
        // Executa o OCR na imagem capturada
        String ocr = tesseract.doOCR(areaCapturada);
		
		return ocr;
	}
	
	public List<MatOfPoint> listaMonstros() throws IOException {
		//Pegar tamanho da tela
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        // Tirar o print da tela
        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
        
        BotRagnarok.X = screenFullImage.getWidth()/2 - 40;
        BotRagnarok.Y = screenFullImage.getHeight()/2 + 30;
        
        /*
        // Caminho onde a imagem será salva
        String diretorio = "C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/sprites"; // Substitua pelo seu diretório
        String nomeArquivo = "printTela.png";
        // Criar o diretório, caso não exista
        File diretorioFile = new File(diretorio);
        if (!diretorioFile.exists()) {
            diretorioFile.mkdirs();
        }
        // Salvar a imagem no diretório especificado
        File arquivoImagem = new File(diretorio + "/" + nomeArquivo);
        ImageIO.write(screenFullImage, "png", arquivoImagem);
        
        Mat screen = Imgcodecs.imread(diretorio + "/" + nomeArquivo); //tela
        */
        
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
	
	public void moverMouse(int x, int y) {
		 robot.mouseMove(x, y);
	}
	
	public void apertarTecla(int tecla) throws InterruptedException {
		 robot.keyPress(tecla); // Pressionar a tecla
		 Thread.sleep(50);
         robot.keyRelease(tecla); // Liberar a tecla
	}
	
	public void clicarMouse() throws InterruptedException {
		robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK); // Pressionar o botão esquerdo
        Thread.sleep(50);
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK); // Liberar o botão esquerdo
	}
	
	

}
