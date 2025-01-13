package com.ragnarokbot.main;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.ragnarokbot.bot.Bot;
import com.ragnarokbot.bot.Skill;
import com.ragnarokbot.bot.Tela;
import com.ragnarokbot.model.AStar;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.GrafoMapa;
import com.ragnarokbot.model.enums.Estado;
import com.ragnarokbot.telas.JanelaPrincipal;

import config.ContasConfig;
import config.ContasConfig.Conta;
import config.ContasConfig.Personagem;
import config.Script;
import config.ScriptLoader;
import config.Script.Acao;
import config.Script.Links;
import config.Script.Passo;
import config.Script.Rota;
import net.sourceforge.tess4j.TesseractException;
import state.StateMachine;

import java.awt.Event;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;



public class GameController implements NativeKeyListener, Runnable {
	
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
    private int farm = 0;
    
    private boolean tentandoFalarComNpc = false;
    private boolean interagindoComNpc = false;
    
    private long ultimoMovimento = System.currentTimeMillis(); // Timestamp do último movimento
    private Coordenadas ultimaCoordenada = new Coordenadas(0,0);;
    private Coordenadas atual = new Coordenadas(0,0);
    private boolean personagemParado = false;
    
    boolean falarComNpc = true;
    
    public static StateMachine stateMachine = new StateMachine(Estado.ANDANDO);
    
    private List<Coordenadas> caminhoCalculado = new ArrayList<>();
    private int passoAlternativo = 0;
    
    private int parImpar = 0;
    private Coordenadas ultimaCoordenadaOcr = new Coordenadas(0,0);
    
    private GrafoMapa grafo = new GrafoMapa();
    AStar aStar = new AStar();
    
    private boolean modoMemoria = true;
    
    private Script script = new Script();
    
    public List<String> listaDeFarmBioChef = new ArrayList<>();
    
    public static long tempoExecucao = 0;
    
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
        
       
        if (modoMemoria) {
        	atual =  bot.obterCoordenadasMemoria();
        } else {
        	String coordenadaXY = bot.ocrCoordenadas();
            atual = new Coordenadas(coordenadaXY);
        }
        ultimaCoordenada = atual;

    }
    
	@Override
    public void run() {
    	
    	ScriptLoader scriptLoader = new ScriptLoader();
    	
    	//script = scriptLoader.carregarScriptdoJson("teste_de_json.json");
        //script = scriptLoader.carregarScriptdoJson("sussurro_sombrio.json");
    	//script = scriptLoader.carregarScriptdoJson("teste_formigueiro.json");
    	//script = scriptLoader.carregarScriptdoJson("bio.json");
    	apresentacao(script);

        bot.sleep(1000);
        
        bot.zoomOut();
        
       // inicarBotPeloLocalAtualDoPlayer(script);
        
        
    	ContasConfig scriptContas = scriptLoader.carregarContas("teste_login_instancias" + ".json");
    	
    	for (Conta conta : scriptContas.getContas()) {
            System.out.println("Usuário: " + conta.getUsuario());
            System.out.println("Senha: " + conta.getSenha());
            System.out.println("PIN: " + conta.getPin());
            for (Personagem personagem : conta.getPersonagens()) {
                System.out.println(" - Página: " + personagem.getPagina());
                System.out.println(" - Index: " + personagem.getIndexPersonagem());
                System.out.println(" - Passar Itens: " + personagem.isPassarItens());
                System.out.println(" - Instâncias: " + personagem.getInstancias());
            }
        }
    	
    	//GrafoMapa grafo = new GrafoMapa();
    	String mapa = script.getMapa();
    	grafo = gerarGrafoDeMapa(carregarMapa("mapas/" + mapa));

        
        /*
        if (script.getCoordenadasAlt() != null) {
	        for(Links links : script.getCoordenadasAlt()) {
	        	Coordenadas c1 = new Coordenadas(links.getLinks().get(0), links.getLinks().get(1));
	        	Coordenadas c2 = new Coordenadas(links.getLinks().get(2), links.getLinks().get(3));
	    		grafo.addConexao(c1, c2);
	    	}
        }*/
       

        /*
        bot.aceitarContrato();
        
        System.out.println("Apertando enter na selecao de servidor");
        bot.apertarTecla(KeyEvent.VK_ENTER);
        Thread.sleep(500);
      
        String usuario = "zeperiquito";
        String senha = "Kaioben10";
        String pin = "2010";
        bot.realizarLogin(usuario,senha);
        
        System.out.println("Apertando enter na escolha do canal 1");
        bot.apertarTecla(KeyEvent.VK_ENTER);
        Thread.sleep(5000);
        
        System.out.println("Chega na parte do pin...");
        bot.inserirPin(pin);
        Thread.sleep(1000);
        
        bot.escolherPersonagem(1);
        
        ligarBot = false;*/
    	
        while (ligarBot) {
            long startTime = System.currentTimeMillis();
            bot.sleep(100);
            synchronized (this) {
	            while (pausarBot) {
	                try {
	                	System.out.println("ta no wait");
	                    wait(); // Aguarda até que `pausado` seja false
	                } catch (InterruptedException e) {
	                	Thread.currentThread().interrupt();
	                    e.printStackTrace();
	                }
	            }
            }
            
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
                	if (modoMemoria) {
                		atual =  bot.obterCoordenadasMemoria();
                	} else {
                		String coordenadaXY = bot.ocrCoordenadas();
                        atual = new Coordenadas(coordenadaXY);
                	}

                    if (bot.compararCoordenadas(atual, ultimaCoordenadaOcr)) {
                    	if (System.currentTimeMillis() - ultimoMovimento >= 1500) {
                    		personagemParado = true;
                    		System.out.println("Personagem está parado.");
                    	}
                    } else {
                    	personagemParado = false;
                    	ultimoMovimento = System.currentTimeMillis();
                    }
                    ultimaCoordenadaOcr = atual;
                   
                    //verificar se tem falha no ocr pra nao dar coordenadas muito longe
                    //as vezes o ocr erra a leitura e o personagem fica indo para direções estranhas
                    /* if (Math.abs(ultimaCoordenada.x - atual.x) > 20 || Math.abs(ultimaCoordenada.y - atual.y) > 20 ) {
                   		int diminuirPasso = passo - 1;
                   		if (diminuirPasso <= 0) {
                   			diminuirPasso = 0;
                   		}
                       	int x = script.getRotas().get(rota).getPassos().get(diminuirPasso).getCoordenadas().get(0);
                       	int y = script.getRotas().get(rota).getPassos().get(diminuirPasso).getCoordenadas().get(1);
                       	atual = new Coordenadas(x,y);
                       	
                       	System.out.println("****************#########################");
                       	System.out.println("****************######################### " + atual);
                       	System.out.println("****************#########################" + ultimaCoordenada);
                    	Coordenadas atualMemoria = bot.obterCoordenadasMemoria();
                    	atual = atualMemoria;
                    	
                    }*/
                    /*
                    int diminuirPasso = passo - 1;
               		if (diminuirPasso <= 0) {
               			diminuirPasso = 0;
               		}
               		int xInicio = script.getRotas().get(rota).getPassos().get(diminuirPasso).getCoordenadas().get(0);
                   	int yInicio = script.getRotas().get(rota).getPassos().get(diminuirPasso).getCoordenadas().get(1);
                   	int xFim = script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(0);
                   	int yFim = script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(1);
                   	
                   	double distanciaDaReta = bot.calcularDistanciaDaReta(new Coordenadas(xInicio,yInicio), new Coordenadas(xFim,yFim), atual);
                   
                   	if (distanciaDaReta > 10) {
                    	AStar aStar = new AStar();
                    	//caminhoAlternativo = aStar.calcularCaminhoComExpansao(atual, new Coordenadas(xFim,yFim), grafo);
                    }*/
                    
                    ultimaCoordenada = atual;
                   
                    // Lógica de andar
                    if (personagemParado) {
                    	moverParaDirecaoOposta(atual); // Força o movimento do personagem
                        personagemParado = false; // Reseta o estado após o movimento forçado
                    } else {
                        andar(script);
                    }
                    break;

                case ATACANDO:
                	bot.soltarMouse();
                	 if (!monstros.get("rosa").isEmpty() || !monstros.get("azul").isEmpty()) {
                         //stateMachine.mudarEstado(Estado.ATACANDO);
                     //}
                    //if (!monstros.isEmpty()) {
                        atacar(monstros);
                        stateMachine.mudarEstado(Estado.ANDANDO);
                    }
                    break;

                case NPC:
                	bot.soltarMouse();
                	if (tentandoFalarComNpc) {
                		System.out.println("Tentando falar com o npc...");
                		falarComNpc();
                		continue;
                	}
                	if (interagindoComNpc) {
                		interagirComNpc(script);
                		continue;
                	}
                    
                    //stateMachine.mudarEstado(Estado.ANDANDO);
                    break;

                default:
                    System.out.println("Estado desconhecido");
            }

            long endTime = System.currentTimeMillis();
            tempoExecucao = endTime - startTime;
            JanelaPrincipal.updateTempoLabel(tempoExecucao);
            //System.out.println("Bloco executado em " + tempoExecucao + " ms.");
        }

        System.out.println("Bot parado com sucesso.");
        try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
    }
    
    private void calcularCaminhoAlternativo(Script script, GrafoMapa grafo) {
        int xDestino = script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(0);
        int yDestino = script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(1);
         AStar aStar = new AStar();
         caminhoCalculado = aStar.calcularCaminhoComExpansao(atual, new Coordenadas(xDestino,yDestino), grafo);
    }
    
    private void moverParaDirecaoOposta(Coordenadas atual) {
        int novaX = atual.x + (Math.random() > 0.5 ? -10 : 10); // Movimento aleatório para frente ou trás
        int novaY = atual.y + (Math.random() > 0.5 ? -10 : 10);

        Coordenadas novaCoordenada = new Coordenadas(novaX, novaY);
        bot.moverPersonagemComClick(atual, novaCoordenada);
        System.out.println("Movimento forçado para evitar inatividade.");
    }
    
    private void falarComNpc() {
        List<MatOfPoint> npcs = bot.listaNpcs();
        if (!npcs.isEmpty()) {
            MatOfPoint npcEncontrado = npcs.get(0);
            Rect rect = Imgproc.boundingRect(npcEncontrado);
            int centerX = bot.getxJanela() + rect.x + rect.width / 2;
            int centerY = bot.getyJanela() + rect.y + rect.height / 2;
            bot.moverMouse(centerX, centerY);
            bot.sleep(50);
            bot.clicarMouse();
            
            List<MatOfPoint> balao = bot.verificarBalaoNpc();
            if (!balao.isEmpty()) {
            	tentandoFalarComNpc = false;
            	interagindoComNpc = true;
            }
        }
    }
    
    private void interagirComNpc(Script script) {
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
    	
    	if (script.getCoordenadasAlt() != null) {
	    	System.out.println("CoordenadasAlt");
	    	for(Links links : script.getCoordenadasAlt()) {
	    		System.out.println("links: [" + links.getLinks().get(0) + ", " + links.getLinks().get(1) + ", " + links.getLinks().get(2) + ", " + links.getLinks().get(3) + "]");
	    	}
    	}
    	System.out.println("Mapa: " + script.getMapa());
    	System.out.println("-------------------------------------------------------------------------------------");
    }
    
    
    
    private void andar(Script script) {
    	int distanciaMinima = 5; // Defina a distância mínima aceitável 
    	
    	//System.out.println("farm: " + farm + " | tamanho da lista: " + this.listaDeFarmBioChef.size());
    	//System.out.println("descricao do script: " + script.getFinalizacao().getDescricao());

    	// Processar verificação específica da rota
        processarVerificacao(script, distanciaMinima);
            
    	Coordenadas destino = obterDestinoAtual(script);
    	//System.out.println("Atual: " + atual + " | destino: " + destino);
    	
        //caminhoAlternativo = aStar.calcularCaminhoComExpansao10(atual, destino, grafo);
    	caminhoCalculado = aStar.encontrarCaminho(grafo, atual, destino);
    	
        if (passoAlternativo < caminhoCalculado.size()) {
               int altX = caminhoCalculado.get(passoAlternativo).x;
               int altY = caminhoCalculado.get(passoAlternativo).y;
               Coordenadas destinoAlt = new Coordenadas(altX, altY);

               bot.moverPersonagem(atual, destinoAlt);

               if (bot.calcularDistancia(atual, destinoAlt) <= distanciaMinima) {
                   passoAlternativo++;
                   System.out.println("Rota alternativa aumentada");

                   if (passoAlternativo >= caminhoCalculado.size()) {
                       passoAlternativo = 0;
                       caminhoCalculado.clear();
                        
                       passo++;
                       System.out.println("Destino alcançado, mudando para a próxima rota.");
                       if (passo >= script.getRotas().get(rota).getPassos().size()) {
                           passo = 0;
                       }
                        
                       System.out.println("Final alternativo");
                   }
               }
           } else {
               // Caso inesperado
               System.out.println("Erro: passoAlternativo fora do intervalo.");
               caminhoCalculado.clear();
               passoAlternativo = 0;
           }
        	
    	
    	/*
    	// Obter coordenadas do destino atual
        Coordenadas destino = obterDestinoAtual(script);
        System.out.println("Atual: " + atual + " | destino: " + destino);
        

    	// Verificar se há um caminho alternativo
        if (!caminhoAlternativo.isEmpty()) {
            processarCaminhoAlternativo(distanciaMinima);
            return;
        }
        
        
        
        // Processar verificação específica da rota
        processarVerificacao(script, distanciaMinima);
        
        // Verificar se o destino foi alcançado
        if (bot.calcularDistancia(atual, destino) <= distanciaMinima) {
            passo++;
            System.out.println("Destino alcançado, mudando para a próxima rota.");

            if (passo >= script.getRotas().get(rota).getPassos().size()) {
                passo = 0;
            }
        } else {
            // Mover o personagem em direção ao destino
            bot.moverPersonagem(atual, destino);
        }*/
    	
    }
    
    private void processarCaminhoAlternativo(int distanciaMinima) throws Exception {
    	System.out.println("Entrou no caminho alternativo");
        for (Coordenadas nodo : caminhoCalculado) {
            System.out.println(nodo.x + " " + nodo.y);
        }

        if (passoAlternativo < caminhoCalculado.size()) {
            int altX = caminhoCalculado.get(passoAlternativo).x;
            int altY = caminhoCalculado.get(passoAlternativo).y;
            Coordenadas destinoAlt = new Coordenadas(altX, altY);

            bot.moverPersonagem(atual, destinoAlt);

            if (bot.calcularDistancia(atual, destinoAlt) <= distanciaMinima) {
                passoAlternativo++;
                System.out.println("Rota alternativa aumentada");

                if (passoAlternativo >= caminhoCalculado.size()) {
                    passoAlternativo = 0;
                    caminhoCalculado.clear();
                    System.out.println("Final alternativo");
                }
            }
        } else {
            // Caso inesperado
            System.out.println("Erro: passoAlternativo fora do intervalo.");
            caminhoCalculado.clear();
            passoAlternativo = 0;
        }
    }
    
    private Coordenadas obterDestinoAtual(Script script) {
        int destinoX = script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(0);
        int destinoY = script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(1);
        return new Coordenadas(destinoX, destinoY);
    }
    
    private void processarVerificacao(Script script, int distanciaMinima) {
        String verificacao = script.getRotas().get(rota).getVerificacao().getTipo();
        switch (verificacao) {
            case "teleport":
                processarVerificacaoTeleport(script, distanciaMinima);
                break;
            case "loop":
                reiniciarRota();
                break;
            case "npc":
            	mudarEstadoParaNpc();
                break;
            default:
                System.out.println("Tipo de verificação desconhecido: " + verificacao);
        }
    }
    
    private void processarVerificacaoTeleport(Script script, int distanciaMinima) {
        int verificarX = script.getRotas().get(rota).getVerificacao().getCoordenadas().get(0);
        int verificarY = script.getRotas().get(rota).getVerificacao().getCoordenadas().get(1);
        Coordenadas verificarCoordenadas = new Coordenadas(verificarX, verificarY);

        if (bot.calcularDistancia(atual, verificarCoordenadas) <= distanciaMinima) {
            rota++;
            elseAcoes = 0;
            passo = 0;
            passoAlternativo = 0;

            if (rota >= script.getRotas().size()) {
            	//System.out.println("Caiu na ultima rota: " + rota + " | rota.size():" + script.getRotas().size());
                finalizarRota(script, distanciaMinima);
            }
        }
    }
    
    private void finalizarRota(Script script, int distanciaMinima) {
    	//reiniciar tudo ou ir pro finalização
        rota = 0;
        int finalizarX = script.getFinalizacao().getCoordenadas().get(0);
        int finalizarY = script.getFinalizacao().getCoordenadas().get(1);
        Coordenadas finalizacao = new Coordenadas(finalizarX, finalizarY);

        if (bot.calcularDistancia(atual, finalizacao) <= distanciaMinima) {
            System.out.println("Finalizou a rota: " + script.getFinalizacao().getDescricao());
            if (JanelaPrincipal.instanciaRadioButton.isSelected()) {
            	//fazer as logicas pra iniciar as instancias
            	System.out.println("Caiu em rota de instancia?");
            	return;
            }
            //System.out.println("Ta caindo no na logica do farme mesmo?");
            //Bloco para o player fazer as outras rotas se tiver
            farm++;
            if (farm > listaDeFarmBioChef.size() - 1) {
            	farm = 0;
            }
            ScriptLoader scriptLoader = new ScriptLoader();
            String modoFarm = listaDeFarmBioChef.get(farm);
            System.out.println("Proximo script é o :" + modoFarm);
            //script = scriptLoader.carregarScriptdoJson("biochef/" + modoFarm);
            setScript(scriptLoader.carregarScriptdoJson("biochef/" + modoFarm));
            //System.out.println("descricao do script: " + script.getFinalizacao().getDescricao());
            bot.sleep(300);
        }
    }
    
    private void reiniciarRota() {
        rota = 0;
        passo = 0;
        elseAcoes = 0;
    }
    private void mudarEstadoParaNpc() {
        bot.soltarMouse();
        bot.apertarTecla(KeyEvent.VK_A);
		
        passo = 0;
        stateMachine.mudarEstado(Estado.NPC);
        tentandoFalarComNpc = true;
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
    
    private void atacar(Map<String, List<MatOfPoint>> monstros) {
    	/*
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
        }*/
    	
    	// Prioridade: monstros azuis
        List<MatOfPoint> monstrosAzul = monstros.get("azul");
        if (!monstrosAzul.isEmpty()) {
            monstrosAzul.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
            for (MatOfPoint monstro : monstrosAzul) {
                Skill skillDisponivel = getAvailableSkill("azul");
                if (skillDisponivel != null) {
                    bot.atacarMonstro(monstro, skillDisponivel.getTecla());
                    skillDisponivel.use(); // Marca a skill como usada
                    break; // Ataca um monstro e sai
                }
            }
            return;
        }

        // Monstros rosas
        List<MatOfPoint> monstrosRosa = monstros.get("rosa");
        if (!monstrosRosa.isEmpty()) {
            monstrosRosa.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
            for (MatOfPoint monstro : monstrosRosa) {
                Skill skillDisponivel = getAvailableSkill("rosa");
                if (skillDisponivel != null) {
                    bot.atacarMonstro(monstro, skillDisponivel.getTecla());
                    skillDisponivel.use(); // Marca a skill como usada
                    break; // Ataca um monstro e sai
                }
            }
        }
    	
    }
    
 // Método auxiliar para obter a próxima habilidade disponível para uma cor
    private Skill getAvailableSkill(String cor) {
    	Skill skill = null;
    	for (Skill sk : bot.skills) {
    		System.out.println("Skill: " + sk.getTecla() + " pronta: " + sk.isReady());
    		if (sk.getCor().equals(cor) && sk.isReady()) {
    			skill = sk;
    			break;
    		}
    	}
    	return skill;
        /*return bot.skills.stream()
                .filter(skill -> skill.getCor().equals(cor) && skill.isReady())
                .findFirst()
                .orElse(null);*/
    }
    
    public void inicarBotPeloLocalAtualDoPlayer(Script script) {
    	String coordenadaXY = "";
		coordenadaXY = bot.ocrCoordenadas();
		
        Coordenadas atual = new Coordenadas(coordenadaXY);
        
        int menorDistancia = 99999;
        int rotaCalculada = 0;
        int passoCalculada = 0;
    	for(int i = 0; i < script.getRotas().size(); i++) {
    		for(int j = 0; j < script.getRotas().get(i).getPassos().size(); j++) {
    			int x = script.getRotas().get(i).getPassos().get(j).getCoordenadas().get(0);
    			int y = script.getRotas().get(i).getPassos().get(j).getCoordenadas().get(1);
    			Coordenadas destino = new Coordenadas(x,y);
    			int distanciaCalculada = bot.calcularDistancia(atual, destino);
    			
    			if (distanciaCalculada <= menorDistancia ) {
    				menorDistancia = distanciaCalculada;
    				rotaCalculada = i;
    				passoCalculada = j;
    			}
    		}
    	}
    	
    	this.rota = rotaCalculada;
    	this.passo = passoCalculada;
    	
    }
    
    public GrafoMapa gerarGrafoDeMapa(Map<Coordenadas, Boolean> mapaCoordenadas) {
	    GrafoMapa grafo = new GrafoMapa();

	    for (Map.Entry<Coordenadas, Boolean> entrada : mapaCoordenadas.entrySet()) {
	        Coordenadas atual = entrada.getKey();
	        if (entrada.getValue()) { // Se pode andar na coordenada
	            // Verificar vizinhos (4 direções: cima, baixo, esquerda, direita)
	            Coordenadas[] vizinhos = {
	                new Coordenadas(atual.x, atual.y + 1),
	                new Coordenadas(atual.x, atual.y - 1),
	                new Coordenadas(atual.x + 1, atual.y),
	                new Coordenadas(atual.x - 1, atual.y)
	            };

	            for (Coordenadas vizinho : vizinhos) {
	                if (mapaCoordenadas.getOrDefault(vizinho, false)) {
	                    grafo.addConexao(atual, vizinho);
	                }
	            }
	        }
	    }

	    return grafo;
	}
	
    public Map<Coordenadas, Boolean> carregarMapa(String caminhoImagem) {
    	Map<Coordenadas, Boolean> mapaCoordenadas = new HashMap<>();
		try {
			BufferedImage mapa;
			mapa = ImageIO.read(new File(caminhoImagem));
		
	        
	
	        int largura = mapa.getWidth();
	        int altura = mapa.getHeight();
	
	        for (int y = 0; y < altura; y++) {
	            for (int x = 0; x < largura; x++) {
	                int rgb = mapa.getRGB(x, y);
	                // Determina se é branco (caminhável) ou preto (obstáculo)
	                boolean podeAndar = (rgb & 0xFFFFFF) == 0xFFFFFF; // Branco
	                // Converter pixel para coordenada no Ragnarok
	                int coordX = x;
	                int coordY = altura - y - 1; // Inverter Y (base no canto inferior esquerdo)
	                mapaCoordenadas.put(new Coordenadas(coordX, coordY), podeAndar);
	            }
	        }

	        
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mapaCoordenadas;
    }
    
    public synchronized void pausarBot() {
    	if (script.getMapa() == null) {
    		return;
    	}
    	
    	System.out.println("pausarBot: " + pausarBot);
    	pausarBot = !pausarBot;
    	if (pausarBot) {
    		System.out.println("Pausando o bot...");
    	} else {
    		System.out.println("Resumindo o bot...");
    	}
		bot.clicarMouse();
		
    	//bot.soltarMouse();
    	notify();
    }
    
    public void fecharBot() {
    	ligarBot = false; // Interrompe o loop
    	if (tela != null) {
    		tela.dispose();
    	}
        System.out.println("Tecla 'F' pressionada. Parando o bot...");
        
		bot.clicarMouse();
		
        //bot.soltarMouse();
        try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
		}
        System.exit(0);
    }
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Verifica se as teclas CTRL e SHIFT estão pressionadas
        boolean isCtrlPressed = (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;
        boolean isShiftPressed = (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;

        // Ctrl + Shift + F
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_F) {
            fecharBot();
        }

        // Ctrl + Shift + P
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_P) {
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
    
    public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
	}

	public boolean isLigarBot() {
		return ligarBot;
	}

	public void setLigarBot(boolean ligarBot) {
		this.ligarBot = ligarBot;
	}

	public int getPasso() {
		return passo;
	}

	public void setPasso(int passo) {
		this.passo = passo;
	}

	public int getRota() {
		return rota;
	}

	public void setRota(int rota) {
		this.rota = rota;
	}

	public int getAcoesNpc() {
		return acoesNpc;
	}

	public void setAcoesNpc(int acoesNpc) {
		this.acoesNpc = acoesNpc;
	}

	public int getElseAcoes() {
		return elseAcoes;
	}

	public void setElseAcoes(int elseAcoes) {
		this.elseAcoes = elseAcoes;
	}

	public int getFarm() {
		return farm;
	}

	public void setFarm(int farm) {
		this.farm = farm;
	}
	
	
	

}
