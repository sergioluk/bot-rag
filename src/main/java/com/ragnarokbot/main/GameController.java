package com.ragnarokbot.main;

import org.opencv.core.MatOfPoint;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.ragnarokbot.bot.Bot;
import com.ragnarokbot.bot.Tela;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.enums.Estado;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



public class GameController implements NativeKeyListener {
	
	private final Bot bot;
	private final Tela tela;
	private volatile boolean ligarBot = true;
	private volatile boolean pausarBot = false;
	private Estado estado = Estado.ANDANDO;
    private int rota = 0;
    
    public GameController(Bot bot, Tela tela) {
        this.bot = bot;
        this.tela = tela;
        
        try {
        	//Registrar o hook do teclado
        	GlobalScreen.registerNativeHook();
        	GlobalScreen.addNativeKeyListener(this);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    public void run() throws Exception {
        
         
        List<Coordenadas> caminho = inicializarCaminho();
        Thread.sleep(5000);
        while (ligarBot) {
            Thread.sleep(200);
            
            if (pausarBot) {
            	System.out.println("Bot pausado");
            	continue;
            }
            
            if (bot.configOCR.rectangle.x == 0) {
            	System.out.println("Aguardando setar coordenadas");
            	continue;
            }
            //System.out.println("ocr: " + bot.ocr(1694, 177, 53, 12));
            System.out.println("ocr: " + bot.ocrCoordenadas());
            List<MatOfPoint> monstros = bot.monstrosImagem().listaMonstros;
            
            

            if (!monstros.isEmpty()) {
                estado = Estado.ATACANDO;
                System.out.println("Estado: " + estado);
            }

            if (estado == Estado.ANDANDO) {
                andar(caminho);
                System.out.println("Estado: " + estado);
                System.out.println("Rota destino: " + caminho.get(rota) + "|| " + (rota + 1));
            } else if (estado == Estado.ATACANDO) {
            	List<MatOfPoint> monstrosRaycast = bot.filtrarMonstrosVisiveisRaycast(monstros, bot.monstrosImagem().screen);
                atacar(monstrosRaycast);
                estado = Estado.ANDANDO; // Volta a andar após atacar
            }
           
        }
        
        System.out.println("Bot parado com sucesso.");
        GlobalScreen.unregisterNativeHook(); //Remover o hook do teclado
    }
    
    private List<Coordenadas> inicializarCaminho() {
        List<Coordenadas> caminho = new ArrayList<>();
        /*caminho.add(new Coordenadas(57, 36));
        caminho.add(new Coordenadas(62, 72));
        caminho.add(new Coordenadas(74, 120));
        caminho.add(new Coordenadas(94, 118));
        caminho.add(new Coordenadas(110, 83));
        caminho.add(new Coordenadas(104, 38));*/
        
        caminho.add(new Coordenadas(38, 257));
        caminho.add(new Coordenadas(49, 243));
        caminho.add(new Coordenadas(68, 223));
        caminho.add(new Coordenadas(79, 210));
        caminho.add(new Coordenadas(97, 210));
        caminho.add(new Coordenadas(106, 192));
        caminho.add(new Coordenadas(114, 175));
        caminho.add(new Coordenadas(108, 142));
        caminho.add(new Coordenadas(106, 114));
        caminho.add(new Coordenadas(75, 84));
        caminho.add(new Coordenadas(62, 64));
        caminho.add(new Coordenadas(75, 38));
        caminho.add(new Coordenadas(105, 54));
        caminho.add(new Coordenadas(132, 34));
        caminho.add(new Coordenadas(152, 28));
        caminho.add(new Coordenadas(174, 37));
        caminho.add(new Coordenadas(190, 53));
        caminho.add(new Coordenadas(190, 70));
        caminho.add(new Coordenadas(176, 86));
        caminho.add(new Coordenadas(186, 114));
        caminho.add(new Coordenadas(185, 140));
        caminho.add(new Coordenadas(182, 167));
        caminho.add(new Coordenadas(208, 188));
        caminho.add(new Coordenadas(206, 221));
        caminho.add(new Coordenadas(178, 218));
        caminho.add(new Coordenadas(166, 214));
        caminho.add(new Coordenadas(145, 206));
        caminho.add(new Coordenadas(123, 182));
        caminho.add(new Coordenadas(103, 195));
        caminho.add(new Coordenadas(87, 222));
        caminho.add(new Coordenadas(68, 221));
        caminho.add(new Coordenadas(49, 244));
        caminho.add(new Coordenadas(37, 256));
        
        
        return caminho;
    }
    
    private void andar(List<Coordenadas> caminho) throws Exception {
        String coordenadaXY = bot.ocrCoordenadas();
        Coordenadas atual = new Coordenadas(coordenadaXY);

        //Verificar se chegou no destino
        int distanciaMinima = 5;
        Coordenadas destino = caminho.get(rota);
        if (bot.calcularDistancia(atual, destino) <= distanciaMinima) {
            rota++; // Próximo ponto no caminho
            System.out.println("Rota aumentada!");
            if (rota >= caminho.size()) {
	            rota = 0; // Reiniciar o caminho
	            //return;
	        }
        } else {
            bot.moverPersonagem(atual, destino);
        }
    }
    
    private void atacar(List<MatOfPoint> monstros) throws Exception {
        monstros.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
        monstros.reversed();

        for (MatOfPoint monstro : monstros) {
            bot.atacarMonstro(monstro);
            break; // Ataca um monstro e sai
        }
    }
    
    public void pausarBot() {
    	pausarBot = !pausarBot;
    	if (pausarBot) {
    		System.out.println("Pausando o bot...");
    	} else {
    		System.out.println("Resumindo o bot...");
    	}
    }
    
    public void fecharBot() {
    	ligarBot = false; // Interrompe o loop
    	if (tela != null) {
    		tela.dispose();
    	}
        System.out.println("Tecla 'F' pressionada. Parando o bot...");
    }
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F) {
            fecharBot();
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_P) {
            pausarBot();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Não usado
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Não usado
    }

}
