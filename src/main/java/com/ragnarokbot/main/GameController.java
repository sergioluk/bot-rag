package com.ragnarokbot.main;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.ragnarokbot.bot.Bot;
import com.ragnarokbot.bot.Tela;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.enums.Estado;

import state.StateMachine;

import java.awt.event.KeyEvent;
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
    
    Coordenadas npc = new Coordenadas(156, 97);
    boolean falarComNpc = true;
    
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
        /*
         
        List<Coordenadas> caminho = inicializarCaminho();
        Thread.sleep(1000);
        while (ligarBot) {
        	long startTime = System.currentTimeMillis(); // Captura o tempo inicial em milissegundos
            Thread.sleep(100);
            
            if (pausarBot) {
            	System.out.println("Bot pausado");
            	continue;
            }
            
            if (bot.configOCR.rectangle.x == 0) {
            	System.out.println("Aguardando setar coordenadas");
            	continue;
            }
         
            List<MatOfPoint> monstros = bot.listaMonstros();
            //List<MatOfPoint> monstros = bot.listaNpcs();
            
            

            if (!monstros.isEmpty()) {
                estado = Estado.ATACANDO;          
            }
            
            if (estado == Estado.NPC) {
            	System.out.println("NPC LOGICA");
            	System.out.println("Estado: " + estado);
                List<MatOfPoint> balao = bot.verificarBalaoNpc();
            	System.out.println("Tamanho balao " + balao.size());
            	 if (balao.size() == 1) {
                 	System.out.println("Tamanho 1, apertando enter");
                 	bot.apertarTecla(KeyEvent.VK_ENTER);
                 }
                 if (balao.size() == 2) {
                	 int opcaoEscolhida = 5;
                	 bot.selecionarOpcao(opcaoEscolhida);
                 }
                 
                 estado = Estado.ANDANDO;
            } else if (estado == Estado.ANDANDO) {
                andar(caminho);
                System.out.println("Estado: " + estado);
                System.out.println("Rota destino: " + caminho.get(rota) + "|| " + (rota + 1));
            } else if (estado == Estado.ATACANDO) {
                atacar(monstros);
                estado = Estado.ANDANDO; // Volta a andar após atacar
            }
            
            long endTime = System.currentTimeMillis(); // Captura o tempo final
    	    long duration = endTime - startTime; // Calcula a duração em milissegundos

    	    System.out.println("O bloco de código levou " + duration + " milissegundos.");
        }
        
        System.out.println("Bot parado com sucesso.");
        GlobalScreen.unregisterNativeHook(); //Remover o hook do teclado
        */
    	
    	StateMachine stateMachine = new StateMachine(Estado.ANDANDO);

        List<Coordenadas> caminho = inicializarCaminho();
        Thread.sleep(1000);

        while (ligarBot) {
            long startTime = System.currentTimeMillis();
            Thread.sleep(100);

            if (pausarBot) {
                System.out.println("Bot pausado");
                continue;
            }

            if (bot.configOCR.rectangle.x == 0) {
                System.out.println("Aguardando setar coordenadas");
                continue;
            }
            
            // Verificar monstros antes de andar
            List<MatOfPoint> monstros = bot.listaMonstros();
            if (!monstros.isEmpty()) {
                stateMachine.mudarEstado(Estado.ATACANDO);  
            }

            switch (stateMachine.getEstadoAtual()) {
                case ANDANDO:
                	String coordenadaXY = bot.ocrCoordenadas();
                    Coordenadas atual = new Coordenadas(coordenadaXY);
                    
                    if (falarComNpc && bot.calcularDistancia(atual, npc) <= 10) {
                        falarComNpc();
                        stateMachine.mudarEstado(Estado.NPC);
                    } else {
                        andar(caminho, atual);
                    }
                    break;

                case ATACANDO:
                    if (!monstros.isEmpty()) {
                        atacar(monstros);
                        stateMachine.mudarEstado(Estado.ANDANDO);
                    }
                    break;

                case NPC:
                    interagirComNpc();
                    stateMachine.mudarEstado(Estado.ANDANDO);
                    break;

                default:
                    System.out.println("Estado desconhecido");
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Bloco executado em " + (endTime - startTime) + " ms.");
        }

        System.out.println("Bot parado com sucesso.");
        GlobalScreen.unregisterNativeHook();
    }
    
    private void falarComNpc() throws Exception {
        List<MatOfPoint> npcs = bot.listaNpcs();
        if (!npcs.isEmpty()) {
            MatOfPoint npcEncontrado = npcs.get(0);
            Rect rect = Imgproc.boundingRect(npcEncontrado);
            int centerX = bot.getxJanela() + rect.x + rect.width / 2;
            int centerY = bot.getyJanela() + rect.y + rect.height / 2;
            bot.moverMouse(centerX, centerY);
            Thread.sleep(50);
            bot.clicarMouse();
        }
    }
    
    private void interagirComNpc() throws Exception {
        List<MatOfPoint> balao = bot.verificarBalaoNpc();
        System.out.println("tamanho balao " + balao.size());
        if (balao.size() == 1) {
            bot.apertarTecla(KeyEvent.VK_ENTER);
        } else if (balao.size() == 2) {
            bot.selecionarOpcao(5);
        }
    }
    
    private List<Coordenadas> inicializarCaminho() {
        List<Coordenadas> caminho = new ArrayList<>();
        
        /*
        caminho.add(new Coordenadas(135, 93));
        caminho.add(new Coordenadas(147, 92));
        caminho.add(new Coordenadas(156, 91));
        caminho.add(new Coordenadas(171, 93));
        */
        
        /*
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
        caminho.add(new Coordenadas(37, 256));*/
        
        /*
        //Susurro sombrio rotas pt1
        caminho.add(new Coordenadas(55, 86));
        caminho.add(new Coordenadas(36, 80));
        caminho.add(new Coordenadas(37, 61));
        caminho.add(new Coordenadas(37, 48));
        caminho.add(new Coordenadas(62, 50));
        caminho.add(new Coordenadas(85, 51));
        caminho.add(new Coordenadas(85, 80));
        caminho.add(new Coordenadas(80, 97));
        caminho.add(new Coordenadas(54, 96));
        caminho.add(new Coordenadas(25, 94));
        caminho.add(new Coordenadas(27, 62));
        caminho.add(new Coordenadas(30, 37));
        caminho.add(new Coordenadas(61, 39));
        caminho.add(new Coordenadas(94, 41));
        caminho.add(new Coordenadas(93, 65));
        caminho.add(new Coordenadas(93, 75));
        */
        //pt 2
        caminho.add(new Coordenadas(210, 64));
        caminho.add(new Coordenadas(209, 36));
        caminho.add(new Coordenadas(163, 36));
        caminho.add(new Coordenadas(142, 39));
        caminho.add(new Coordenadas(142, 76));
        caminho.add(new Coordenadas(145, 96));
        caminho.add(new Coordenadas(179, 94));
        caminho.add(new Coordenadas(202, 91));
        caminho.add(new Coordenadas(199, 66));
        caminho.add(new Coordenadas(199, 45));
        caminho.add(new Coordenadas(172, 46));
        caminho.add(new Coordenadas(151, 48));
        caminho.add(new Coordenadas(153, 65));
        caminho.add(new Coordenadas(190, 84));
        caminho.add(new Coordenadas(190, 64));
        
        
        
        return caminho;
    }
    
    /*
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
        } else if (bot.calcularDistancia(atual, npc) <= 10) {
        	List<MatOfPoint> npcs = bot.listaNpcs();
        	
        	MatOfPoint npc = npcs.get(0);
        	Rect rect = Imgproc.boundingRect(npc);
            int centerX = bot.getxJanela() + rect.x + rect.width / 2;
            int centerY = bot.getyJanela() + rect.y + rect.height / 2;
        	bot.moverMouse(centerX, centerY);
            Thread.sleep(50);
            bot.clicarMouse();
            
        	estado = Estado.NPC;
        	
        } else {
            bot.moverPersonagem(atual, destino);
        }
    }*/
    
    private void andar(List<Coordenadas> caminho, Coordenadas atual) throws Exception {
        // Verificar se chegou ao destino atual
        int distanciaMinima = 5; // Defina a distância mínima aceitável
        Coordenadas destino = caminho.get(rota);

        if (bot.calcularDistancia(atual, destino) <= distanciaMinima) {
            rota++;
            System.out.println("Destino alcançado, mudando para a próxima rota.");
            if (rota >= caminho.size()) {
                rota = 0; // Reiniciar a rota caso chegue ao final
            }
        } else {
            // Mover o personagem em direção ao destino
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
