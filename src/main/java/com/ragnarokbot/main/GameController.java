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
import com.ragnarokbot.model.Script;
import com.ragnarokbot.model.Script.Acao;
import com.ragnarokbot.model.Script.Passo;
import com.ragnarokbot.model.Script.Rota;
import com.ragnarokbot.model.enums.Estado;

import config.ScriptLoader;
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
	//private Estado estado = Estado.ANDANDO;
    private int passo = 0;
    
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
    	
    	ScriptLoader scriptLoader = new ScriptLoader();
    	//Script script = scriptLoader.carregarScriptdoJson("sussurro_sombrio.json");
    	Script script = scriptLoader.carregarScriptdoJson("teste_de_json.json");
    	
    	System.out.println("Era pra ter 4 rotas: " + script.getRotas().size());
    	
    	for(Rota rota : script.getRotas()) {
    		System.out.println("descricao: " + rota.getDescricao());
    		for(Passo passo : rota.getPassos()) {
    			System.out.println("[" + passo.getCoordenadas().get(0) + ", " + passo.getCoordenadas().get(1) + "]");
    		}
    		System.out.println("Verificacao:");
    		System.out.println("tipo: " + rota.getVerificacao().getTipo());
    		System.out.println("coordenadas: [" + rota.getVerificacao().getCoordenadas().get(0) + ", " + rota.getVerificacao().getCoordenadas().get(1) + "]");
    		if (rota.getVerificacao().getElseAcoes() != null) {
    			for(Passo elseAcoes : rota.getVerificacao().getElseAcoes()) {
        			System.out.println("elseAcoes: [" + elseAcoes.getCoordenadas().get(0) + ", " + elseAcoes.getCoordenadas().get(1) + "]");
        		}
    		}
    		if (rota.getVerificacao().getAcoes() != null) {
    			for(Acao acao : rota.getVerificacao().getAcoes()) {
        			System.out.println("balaoUnico: " + acao.isBalaoUnico() + ", opcao: " + acao.getOpcao());
        		}
    		}
    	}
    	System.out.println("finalizacao:");
    	System.out.println("descricao: " + script.getFinalizacao().getDescricao());
    	System.out.println("coordenadas: " + script.getFinalizacao().getCoordenadas());
    	
    	System.out.println("-------------------------------------------------------------------------------------");
    	
    	
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
 
        caminho.add(new Coordenadas(135, 93));
        caminho.add(new Coordenadas(147, 92));
        caminho.add(new Coordenadas(156, 91));
        caminho.add(new Coordenadas(171, 93));
        
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
        Coordenadas destino = caminho.get(passo);

        if (bot.calcularDistancia(atual, destino) <= distanciaMinima) {
            passo++;
            System.out.println("Destino alcançado, mudando para a próxima rota.");
            if (passo >= caminho.size()) {
                passo = 0; // Reiniciar a rota caso chegue ao final
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
