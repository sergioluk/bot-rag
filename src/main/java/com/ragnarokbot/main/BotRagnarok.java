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
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
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
import com.ragnarokbot.bot.Tela;
import com.ragnarokbot.model.Coordenadas;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

public class BotRagnarok {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		
		//Chat GPT
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng");
        // Configurar whitelist para reconhecer apenas números
        tesseract.setTessVariable("tessedit_char_whitelist", "0123456789 ");
        tesseract.setTessVariable("preserve_interword_spaces", "1");

        Robot robot = new Robot();
        Bot bot = new Bot(tesseract, robot);

        SwingUtilities.invokeLater( () -> new Tela(bot));
        
        GameController gameController = new GameController(bot);
        gameController.run();
        
        
        
        
		//Fim Chat GPT
		
		/*Tentando Organizar sapora*/
		// Carregar a biblioteca nativa do OpenCV
        /*System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//Configurando o tesseract
		ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata"); // Defina o caminho para a pasta 'tessdata'
        tesseract.setLanguage("eng"); // Defina o idioma, se necessário
        
        Robot robot = new Robot();
        
        Bot bot = new Bot(tesseract, robot);
        
        String hp = bot.ocr(30, 68, 33, 12);
        System.out.println("Hp: " + hp);
        
        String coordenadaXY = bot.ocr(1694, 177, 53, 12);
        System.out.println("Coordenada x y: " + coordenadaXY);
        
        int rota = 0;
        
        boolean ligarBot = true;
        while(ligarBot) {
        	System.out.println("Dormindo 1s");
        	//Thread.sleep(3000);
        	Thread.sleep(500);
        	coordenadaXY = bot.ocr(1694, 177, 53, 12);
        	List<MatOfPoint> listaMonstros = bot.listaMonstros();
        	System.out.println("Quantidade de monstros encontrados: " + listaMonstros.size());
        	if (listaMonstros.size() > 0) {
        		estado = "atacando";
        	}
        	System.out.println("Estado: " + estado);
        	
        	// Ordenar a lista de monstros pela distância até o jogador
        	listaMonstros.sort(Comparator.comparingDouble(m -> 
        		Math.sqrt(Math.pow(Imgproc.boundingRect(m).x - X, 2) + Math.pow(Imgproc.boundingRect(m).y - Y, 2)))
        			);
        	
        	
        	 //for (MatOfPoint monstro : listaMonstros) {
        	//	 Rect m = Imgproc.boundingRect(monstro);
        	//	 double distancia = Math.sqrt(Math.pow(m.x - X, 2) + Math.pow(m.y - Y, 2));
            //     System.out.println("Distancia do monstro: " + distancia);
        	 
        	if (estado.equals("andando")) {
        		//coordenadaXY
        		List<Coordenadas> caminho = new ArrayList<>();
        		caminho.add(new Coordenadas(57, 36));
        		caminho.add(new Coordenadas(62, 72));
        		caminho.add(new Coordenadas(74, 120));
        		caminho.add(new Coordenadas(94, 118));
        		caminho.add(new Coordenadas(110, 83));
        		caminho.add(new Coordenadas(104, 38));
        		
        		
        		Coordenadas c = new Coordenadas(coordenadaXY);
        		
        		int distanciaMouse = 150;
        		int xMouse = 0;
        		int yMouse = 0;
        		
        		 // Distância mínima para considerar que o personagem chegou ao ponto
        	    final int distanciaMinima = 10;
        	    Coordenadas destino = caminho.get(rota); 
        		
        	    // Verificar se chegou ao destino atual
        	    if (bot.calcularDistancia(c, destino) <= distanciaMinima) {
        	        rota++;
        	        System.out.println("Rota aumentada!");
        	        if (rota >= caminho.size()) {
        	            rota = 0; // Reiniciar o caminho
        	            return;
        	        }
        	        destino = caminho.get(rota); // Atualizar próximo destino
        	        System.out.println("Nova coordenada destino: " + destino.x + ", " + destino.y);
        	    }
        		
        	    
        		//X e Y são a posição no meio da tela, ou seja do personagem
        		//if (c.x <= caminho.get(rota).x) {
        		//	xMouse = X + distanciaMouse;
        		//} else if (c.x >= caminho.get(rota).x) {
        		//	xMouse = X - distanciaMouse;
        		//}
        		//if (c.y <= caminho.get(rota).y) {
        		//	yMouse = Y - distanciaMouse;
        		//} else if (c.y >= caminho.get(rota).y) {
        		//	yMouse = Y + distanciaMouse;
        		//}
        	    
        	    xMouse = X + (destino.x - c.x) * 4; // Escalar o movimento
        	    yMouse = Y - (destino.y - c.y) * 4;
        	    
        		
        		bot.moverMouse(xMouse, yMouse);
        		Thread.sleep(500);
        		bot.clicarMouse();
        		xMouse = 0;
        		yMouse = 0;
        		System.out.println("Rota: " + rota);
        		System.out.println(("Proxima coordenada: " + caminho.get(rota).x + " " + caminho.get(rota).y));
        	} else if (estado.equals("atacando")) {
        		for (MatOfPoint monstro : listaMonstros) {
                    Rect m = Imgproc.boundingRect(monstro);
                    int x = m.x;
                    int y = m.y;
                    int width = m.width;
                    int height = m.height;
                    // Calcular o ponto central do retângulo
                    int centerX = x + width / 2;
                    int centerY = y + height / 2;
                    // Mover o mouse para o centro do retângulo
                    bot.moverMouse(centerX, centerY);
                    Thread.sleep(10);
                    // Simular pressionamento da tecla Q
                    bot.apertarTecla(KeyEvent.VK_Q);
                    // Simular clique do botão esquerdo do mouse
                    Thread.sleep(50);
                    bot.clicarMouse();
                    
                    
                    System.out.println("Cor encontrada no centro: (" + centerX + ", " + centerY + ")");
                    
                    
                    
                    break;
                    //Imgproc.rectangle(apagar, m, new Scalar(0, 255, 0), 2); // Desenha um retângulo verde em torno do contorno detectado
                    //Imgcodecs.imwrite("ram.png", apagar);
                }
        	}
            
            ligarBot = true;
            System.out.println("Player X: " + X);
            System.out.println("Player Y: " + Y);
            estado = "andando";
        }
        
        */
        
		//Fim tentando Organizar sapora
        
        Boolean inutil = false;
        if (inutil) {
        	
        
		
		
		/*
		int id = NativeKeyEvent.NATIVE_KEY_PRESSED; // Identifica o evento de tecla pressionada
        int modifiers = NativeKeyEvent.NATIVE_KEY_PRESSED; // Modificadores
        int rawCode = 0; // Código de hardware (0 para um evento de tecla simples)
        int keyCode = NativeKeyEvent.VC_Q; // Código da tecla (no caso, "Q")
        char keyChar = 'Q'; // O caractere correspondente à tecla
        NativeKeyEvent keyEvent = new NativeKeyEvent(id, modifiers, rawCode, keyCode, keyChar);*/
        
		//NativeMouseEvent mouseEvent = new NativeMouseEvent();
		
		// Carregar a biblioteca nativa do OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		
		// Usar o classloader para obter o caminho da imagem dentro de resources
        String imagePath = "C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/sprites/nina.png";
	    //String imagePath = BotRagnarok.class.getResource("/sprites/nina.png").getPath();
	    
	    // Carregar a imagem
	    Mat image = Imgcodecs.imread(imagePath);

	    if (image.empty()) {
	        System.out.println("Erro ao carregar a imagem!");
	    } else {
	        System.out.println("Imagem carregada com sucesso: " + image.size());
	    }

	    System.out.println("Bot funcionando!");
		
		
	    System.out.println("Caminho da imagem: " + imagePath);
	    
	    /*
	    tive que instalar o tesseract e pegar a dll do opencv na pasta "opencv\build\java\x64" copiar a dll e jogar na pasta libs
	    ir no projeto botao direito run as run configurations, na parte esquerda clicar no java aplication BotRagnarok
	    arguments na direita
	    VM arguments colocar esse codigo: -Djava.library.path="C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/libs"
	    */
	    
	    
	    boolean lol = true;
	    while (lol) {
	    try {
            
            
            // Defina as coordenadas da área do HP
            int x = 21; // Coordenada X da área do HP
            int y = 56; // Coordenada Y da área do HP
            int width = 36; // Largura da área do HP
            int height = 11; // Altura da área do HP (ajuste se necessário)
            
            int mapaX = 1694;
            int mapaY = 177;
            int mapaW = 51;
            int mapaH = 12;
            
            // Captura apenas a área da coordenada
            Rectangle coordenadasArea = new Rectangle(mapaX, mapaY, mapaW, mapaH);
            BufferedImage coordenadasCapture = robot.createScreenCapture(coordenadasArea);
            
            // Captura apenas a área do HP
            Rectangle hpArea = new Rectangle(x, y, width, height);
            BufferedImage hpCapture = robot.createScreenCapture(hpArea);
            
            // Salva a imagem capturada para verificar se está correta (opcional)
            ImageIO.write(hpCapture, "png", new File("hp_capture.png"));
            System.out.println("Imagem do HP salva como 'hp_capture.png'.");
            
           

            // Executa o OCR na imagem capturada
            String hpText = tesseract.doOCR(hpCapture);
            System.out.println("HP atual: " + hpText);
            
            
            // Salva a imagem capturada para verificar se está correta (opcional)
            ImageIO.write(coordenadasCapture, "png", new File("coordenadasCapture.png"));
            System.out.println("Imagem do HP salva como 'coordenadasCapture.png'.");
            // Executa o OCR na imagem capturada
            String coordenadaText = tesseract.doOCR(coordenadasCapture);
            System.out.println("Coordenada atual: " + coordenadaText);
            
	    } catch (IOException | TesseractException e) {
            e.printStackTrace();
        }
	    
	    Thread.sleep(1000); // Atualiza a cada segundo
	    
	    }
	    
	    //boolean lo2l = true;
	    boolean lo2l = false;
	    while (lo2l) {
	    
	    String imagemDeTesteDeTexto = "C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/sprites/testtitle.png";
	   

        try {
            String result = tesseract.doOCR(new File(imagemDeTesteDeTexto));
            System.out.println(result);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        
        //Pegar tamanho da tela
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        // Tirar o print da tela
        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
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
        System.out.println("Print da tela salvo em: " + arquivoImagem.getAbsolutePath());
        
        
        Mat screen = Imgcodecs.imread("C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/sprites/printTela.png"); //tela
        //Mat screen = Imgcodecs.imread("C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/sprites/testenpc.png"); //tela com npc
        Mat npc = Imgcodecs.imread("C:/Users/Sergio/eclipse-workspace/ragnarokbot/src/main/resources/sprites/npc.png"); //sprite do monstro
        
        if (screen.empty() || npc.empty()) {
            System.out.println("Erro ao carregar as imagens.");
            return;
        }

     // Converter a imagem para o espaço de cores HSV
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(screen, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Definir os limites inferior e superior da cor (exemplo: vermelho)
        Scalar lowerColor = new Scalar(148, 150, 150);  // Limite inferior para a cor desejada
        Scalar upperColor = new Scalar(154, 255, 255); // Limite superior para a cor desejada

        // Criar uma máscara onde a cor estiver dentro do intervalo definido
        Mat mask = new Mat();
        Core.inRange(hsvImage, lowerColor, upperColor, mask);

        // Encontrar contornos na máscara
        java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Desenhar os contornos detectados na imagem original
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            Thread.sleep(1000); // 500ms de pausa para evitar execução rápidaqq
            int x = rect.x;
            int y = rect.y;
            int width = rect.width;
            int height = rect.height;
            // Calcular o ponto central do retângulo
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            // Mover o mouse para o centro do retângulo
            robot.mouseMove(centerX, centerY);
            // Simular pressionamento da tecla Q
            robot.keyPress(KeyEvent.VK_Q); // Pressionar a tecla Q
            robot.keyRelease(KeyEvent.VK_Q); // Liberar a tecla Q
            // Simular clique do botão esquerdo do mouse
            Thread.sleep(50);
            robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK); // Pressionar o botão esquerdo
            Thread.sleep(50);
            robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK); // Liberar o botão esquerdo
            // Pausar um pouco entre as ações (opcional)
            Thread.sleep(500); // 500ms de pausa para evitar execução rápidaqq

            System.out.println("Cor encontrada no centro: (" + centerX + ", " + centerY + ")");
            
            Imgproc.rectangle(screen, rect, new Scalar(0, 255, 0), 2); // Desenha um retângulo verde em torno do contorno detectado
        }

        // Salvar a imagem resultante com os contornos desenhados
        Imgcodecs.imwrite("resultado_cor_detectada.png", screen);

        System.out.println("Detecção de cor finalizada.");
        
	    }
        
        
        System.out.println("fim!");
        /*
        Thread.sleep(2000); // 500ms de pausa para evitar execução rápidaqq
        String windowTitle = "History Reborn | Gepard Shield 3.0 (^-_-^)";
        //String windowTitle = "Sem título - Bloco de Notas";
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd == null) {
            System.out.println("Janela do jogo não encontrada!");
            return;
        }
       
        // Garantir que a janela tenha o foco
        User32.INSTANCE.SetForegroundWindow(hwnd);
        System.out.println("Janela encontrada! Enviando comandos...");
        // Exemplo: enviar tecla 'Q'
        int keyCode = 0x51; // Código da tecla Q
        
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYDOWN, new WinDef.WPARAM(keyCode), new WinDef.LPARAM(0));
        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_KEYUP, new WinDef.WPARAM(keyCode), new WinDef.LPARAM(0));

        // Exemplo: clicar com o botão esquerdo do mouse
        User32.INSTANCE.PostMessage(hwnd, WM_LBUTTONDOWN, new WinDef.WPARAM(0), new WinDef.LPARAM(0));
        User32.INSTANCE.PostMessage(hwnd, WM_LBUTTONUP, new WinDef.WPARAM(0), new WinDef.LPARAM(0));
        */

        System.out.println("Comandos enviados!");
	    
	    /*
        while (true) {
            Point point = MouseInfo.getPointerInfo().getLocation();
            System.out.println("Mouse position: X=" + point.x + " Y=" + point.y);
            Thread.sleep(1000); // Atualiza a cada segundo
        }*/
		
		
		
		
	}
        
	}

}
/*
class ImageProcessing {

    public static Mat sharpen(Mat image) {
        Mat sharpened = new Mat();
        // Aplicar filtro gaussiano para reduzir o ruído antes de aplicar a nitidez
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(image, blurred, new Size(0, 0), 3);

        // Adicionar nitidez (imagem original - imagem borrada)
        Core.addWeighted(image, 1.5, blurred, -0.5, 0, sharpened);
        return sharpened;
    }
}*/
