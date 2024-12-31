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
import java.util.Map;



public class GameController implements NativeKeyListener {
	
	private final Bot bot;
	private final Tela tela;
	private volatile boolean ligarBot = true;
	private volatile boolean pausarBot = false;
	//private Estado estado = Estado.ANDANDO;
	//Contagens de acoes
    private int passo = 0;
    private int rota = 0;
    private int acoesNpc = 0;
    private int elseAcoes = 0;
    
    private boolean tentandoFalarComNpc = false;
    private boolean interagindoComNpc = false;
    
    private long ultimoMovimento = System.currentTimeMillis(); // Timestamp do último movimento
    private Coordenadas ultimaCoordenada = new Coordenadas(0,0);
    private boolean personagemParado = false;
    
    boolean falarComNpc = true;
    
    public static StateMachine stateMachine = new StateMachine(Estado.ANDANDO);
    
    
    
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
    	
    	Script script = inicializarCaminho("teste_de_json");
        //Script script = inicializarCaminho("sussurro_sombrio");
    	//Script script = inicializarCaminho("teste_formigueiro");
    	apresentacao(script);

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
            Map<String, List<MatOfPoint>> monstros = bot.listaMonstros();

            // Verificar se existem monstros visíveis
            if (!monstros.get("rosa").isEmpty() || !monstros.get("azul").isEmpty()) {
                stateMachine.mudarEstado(Estado.ATACANDO);
            }
            /*
            // Verificar monstros antes de andar
            List<MatOfPoint> monstros = bot.listaMonstros();
            if (!monstros.isEmpty()) {
                stateMachine.mudarEstado(Estado.ATACANDO);  
            }*/

            switch (stateMachine.getEstadoAtual()) {
                case ANDANDO:
                	String coordenadaXY = bot.ocrCoordenadas();
                    Coordenadas atual = new Coordenadas(coordenadaXY);
                    
                    if (bot.compararCoordenadas(atual, ultimaCoordenada)) {
                    	if (System.currentTimeMillis() - ultimoMovimento >= 3000) {
                    		personagemParado = true;
                    		System.out.println("Personagem está parado.");
                    	}
                    } else {
                    	personagemParado = false;
                    	ultimoMovimento = System.currentTimeMillis();
                    }
                    
                    ultimaCoordenada = atual;
                   
                    // Lógica de andar
                    if (personagemParado) {
                        moverParaDirecaoOposta(atual); // Força o movimento do personagem
                        personagemParado = false; // Reseta o estado após o movimento forçado
                    } else {
                        andar(atual, script, stateMachine);
                    }
                    break;

                case ATACANDO:
                    if (!monstros.isEmpty()) {
                        atacar(monstros);
                        stateMachine.mudarEstado(Estado.ANDANDO);
                    }
                    break;

                case NPC:
                	if (tentandoFalarComNpc) {
                		System.out.println("Tentando falar com o npc...");
                		falarComNpc();
                		continue;
                	}
                	if (interagindoComNpc) {
                		interagirComNpc(script, stateMachine);
                		continue;
                	}
                    
                    //stateMachine.mudarEstado(Estado.ANDANDO);
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
    
    private void moverParaDirecaoOposta(Coordenadas atual) throws Exception {
        int novaX = atual.x + (Math.random() > 0.5 ? -10 : 10); // Movimento aleatório para frente ou trás
        int novaY = atual.y + (Math.random() > 0.5 ? -10 : 10);

        Coordenadas novaCoordenada = new Coordenadas(novaX, novaY);
        bot.moverPersonagemComClick(atual, novaCoordenada);
        System.out.println("Movimento forçado para evitar inatividade.");
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
            
            List<MatOfPoint> balao = bot.verificarBalaoNpc();
            if (!balao.isEmpty()) {
            	tentandoFalarComNpc = false;
            	interagindoComNpc = true;
            }
        }
    }
    
    private void interagirComNpc(Script script, StateMachine stateMachine) throws Exception {
        List<MatOfPoint> balao = bot.verificarBalaoNpc();
        System.out.println("tamanho balao " + balao.size());
        
        boolean balaoDeOpcoesUnico = script.getRotas().get(rota).getVerificacao().getAcoes().get(acoesNpc).isBalaoUnico();
        int opcao = script.getRotas().get(rota).getVerificacao().getAcoes().get(acoesNpc).getOpcao();
        
        System.out.println("Antes do if balao == 1 e balaounico false");
        System.out.println("balao.size: " + balao.size() + " balaoDeOpcoesUnico: " + balaoDeOpcoesUnico);
        if (balao.size() == 1 && balaoDeOpcoesUnico == false) {
            bot.apertarTecla(KeyEvent.VK_ENTER);
        } else if (balao.size() >= 2 || balaoDeOpcoesUnico == true) {
        	interagindoComNpc = true;
            bot.selecionarOpcao(opcao);
            acoesNpc++;
            if (acoesNpc >= script.getRotas().get(rota).getVerificacao().getAcoes().size()) {
            	acoesNpc = 0;
            	rota++;
            	interagindoComNpc = false;
            	stateMachine.mudarEstado(Estado.ANDANDO);
            }
        }
    }
    
    private Script inicializarCaminho(String arquivo) {
    	ScriptLoader scriptLoader = new ScriptLoader();
    	Script script = scriptLoader.carregarScriptdoJson(arquivo + ".json");
    	return script;
    }
    
    private void apresentacao(Script script) {
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
    }
    
    private void andar(Coordenadas atual, Script script, StateMachine stateMachine) throws Exception {
        // Verificar se chegou ao destino atual
        int distanciaMinima = 5; // Defina a distância mínima aceitável
        //Coordenadas destino = caminho.get(passo);
        
        int destinoX = script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(0);
        int destinoY = script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(1);
        Coordenadas destino = new Coordenadas(destinoX, destinoY);
        
        System.out.println(script.getRotas().get(rota).getDescricao());
        
        String verificacao = script.getRotas().get(rota).getVerificacao().getTipo();
        switch (verificacao) {
    	case "teleport":
    		int verificarX = script.getRotas().get(rota).getVerificacao().getCoordenadas().get(0);
        	int verificarY = script.getRotas().get(rota).getVerificacao().getCoordenadas().get(1);
        	if (bot.calcularDistancia(atual, new Coordenadas(verificarX, verificarY)) <= distanciaMinima) {
        		rota++;
        		elseAcoes = 0;
        		passo = 0;
        		if (rota >= script.getRotas().size()) {
        			rota = 0; //reiniciar tudo ou ir pro finalização
        			int finalizarX = script.getFinalizacao().getCoordenadas().get(0);
        			int finalizarY = script.getFinalizacao().getCoordenadas().get(1);
        			if (bot.calcularDistancia(atual, new Coordenadas(finalizarX, finalizarY)) <= distanciaMinima) {
        				System.out.println("Finalizou a rota: " + script.getFinalizacao().getDescricao());
        			}
        		}
        	} 
        	/* Implementar o else de alguma forma
        	if (passo == script.getRotas().get(rota).getPassos().size() - 1){
        		//Parte dos elseAcoes
        		int elseX = script.getRotas().get(rota).getVerificacao().getElseAcoes().get(elseAcoes).getCoordenadas().get(0);
        		int elseY = script.getRotas().get(rota).getVerificacao().getElseAcoes().get(elseAcoes).getCoordenadas().get(1);
        		bot.moverPersonagem(atual, new Coordenadas(elseX, elseY));
        		elseAcoes++;
        		if (elseAcoes >= script.getRotas().get(rota).getVerificacao().getElseAcoes().size()) {
        			elseAcoes = 0;
        		}
        	}*/
        	break;
        
    	case "loop":
    		rota = 0;
        	passo = 0;
    		elseAcoes = 0;
    		break;
    		
    	case "npc":
    		passo = 0;
        	stateMachine.mudarEstado(Estado.NPC);
    		tentandoFalarComNpc = true;
    		break;
    }

        if (bot.calcularDistancia(atual, destino) <= distanciaMinima) {
            passo++;
            System.out.println("Destino alcançado, mudando para a próxima rota.");

            if (passo >= script.getRotas().get(rota).getPassos().size()) { //Se terminou todos os passos
            	passo = 0;
            }
            
        } else {
            // Mover o personagem em direção ao destino
            bot.moverPersonagem(atual, destino);
        }
    }
    
    /*
    private void atacar(List<MatOfPoint> monstros) throws Exception {
        monstros.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
        monstros.reversed();

        for (MatOfPoint monstro : monstros) {
            bot.atacarMonstro(monstro);
            break; // Ataca um monstro e sai
        }
    }*/
    
    private void atacar(Map<String, List<MatOfPoint>> monstros) throws Exception {
    	
    	List<MatOfPoint> monstrosRosa = monstros.get("rosa");
    	List<MatOfPoint> monstrosAzul = monstros.get("azul");
    	
    	//Dar prioridade para os azuis
    	if (!monstrosAzul.isEmpty()) {
    		monstrosAzul.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
    		//monstrosAzul.reversed();
    		for (MatOfPoint monstro : monstrosAzul) {
                bot.atacarMonstro(monstro, KeyEvent.VK_E);
                break; // Ataca um monstro e sai
            }
    		return;
    	}
        
    	monstrosRosa.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
		//monstrosRosa.reversed();
        for (MatOfPoint monstro : monstrosRosa) {
            bot.atacarMonstro(monstro, KeyEvent.VK_Q);
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
    	bot.soltarMouse();
    }
    
    public void fecharBot() {
    	ligarBot = false; // Interrompe o loop
    	if (tela != null) {
    		tela.dispose();
    	}
        System.out.println("Tecla 'F' pressionada. Parando o bot...");
        bot.soltarMouse();
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
