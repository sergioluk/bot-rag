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
import config.SkillsConfig;
import config.SkillsConfig.Classes;
import config.SkillsConfig.Skills;
import config.Script.Acao;
import config.Script.Links;
import config.Script.Passo;
import config.Script.Rota;
import net.sourceforge.tess4j.TesseractException;
import state.StateMachine;
import utils.KeyMapper;

import java.awt.Desktop;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;



public class GameController implements NativeKeyListener, Runnable {

	private final Bot bot;
	private final Tela tela;
	private volatile boolean ligarBot = true;
	private volatile boolean pausarBot = false;
	// private Estado estado = Estado.ANDANDO;
	// Contagens de acoes
	private int passo = 0;
	private int rota = 0;
	private int acoesNpc = 0;
	private int elseAcoes = 0;
	private int farm = 0;

	private boolean tentandoFalarComNpc = false;
	private boolean interagindoComNpc = false;

	private long ultimoMovimento = System.currentTimeMillis(); // Timestamp do último movimento
	private Coordenadas ultimaCoordenada = new Coordenadas(0, 0);;
	private Coordenadas atual = new Coordenadas(0, 0);
	private boolean personagemParado = false;

	boolean falarComNpc = true;

	public static StateMachine stateMachine = new StateMachine(Estado.ANDANDO);

	private List<Coordenadas> caminhoCalculado = new ArrayList<>();
	private int passoAlternativo = 0;

	private int parImpar = 0;
	private Coordenadas ultimaCoordenadaOcr = new Coordenadas(0, 0);

	private GrafoMapa grafo = new GrafoMapa();
	AStar aStar = new AStar();

	private boolean modoMemoria = true;

	private Script script = new Script();
	private ContasConfig scriptContas;

	public List<String> listaDeFarmBioChef = new ArrayList<>();

	public static long tempoExecucao = 0;
	
	public Map<Coordenadas, Boolean> mapaCarregado = null;
	
	//notebook
    public List<String> coordenadasModoSalvar = new ArrayList<>();
    public SkillsConfig skillsConfig;

	public GameController(Bot bot, Tela tela) {
		this.bot = bot;
		this.tela = tela;

		try {
			// Registrar o hook do teclado
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (modoMemoria) {
			atual = bot.obterCoordenadasMemoria();
		} else {
			String coordenadaXY = bot.ocrCoordenadas();
			atual = new Coordenadas(coordenadaXY);
		}
		ultimaCoordenada = atual;

	}

	@Override
	public void run() {

		ScriptLoader scriptLoader = new ScriptLoader();
		
		//notebook
    	skillsConfig = scriptLoader.carregarSkills("config/config_skills.json");
    	for (Classes c : skillsConfig.getClasses()) {
    		System.out.println("classe: " + c.getClasse());
    		for (Skills sk : c.getSkills()) {
    			System.out.println("atalho: " + sk.getAtalho());
    			System.out.println("cd: " + sk.getCd());
    			System.out.println("cor: " + sk.getCor());
    			System.out.println("range: " + sk.getRange());
    			int teclaAtalho = KeyMapper.getTeclaAtalho(sk.getAtalho());
    			bot.skills.add(new Skill(teclaAtalho, sk.getCor(), sk.getCd(), sk.getRange()));
    		}
    	}

		// script = scriptLoader.carregarScriptdoJson("teste_de_json.json");
		// script = scriptLoader.carregarScriptdoJson("sussurro_sombrio.json");
		// script = scriptLoader.carregarScriptdoJson("teste_formigueiro.json");
		// script = scriptLoader.carregarScriptdoJson("bio.json");
		// apresentacao(script);

		bot.printarTela();
		/*
		int x = 0;
		int y = 0;
		List<MatOfPoint> janelaInstancia = bot.verificarJanelaInstancia();
		if (!janelaInstancia.isEmpty()) {
			Rect janela = Imgproc.boundingRect(janelaInstancia.get(0));
			x = janela.x;
			y = janela.y;
		}

		bot.sleep(3000);
		bot.moverMouse(bot.getxJanela() + x + 70, bot.getyJanela() + y + 140);
		// int dg = 43; //tomb
		int dg = 33; // old gh
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
			bot.zoom(5);
			bot.moverMouse(bot.getxJanela() + x + 133, bot.getyJanela() + y + 283);
			bot.sleep(200);
			bot.clicarMouse();
			bot.sleep(200);
		}

		int pos = dg % 16;
		if (pos == 0) {
			pos = 16;
		}
		int selecionar = pos * 16 - 8;

		bot.moverMouse(bot.getxJanela() + x + 70, bot.getyJanela() + y + 23 + selecionar);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Clicar em Criar
		bot.moverMouse(bot.getxJanela() + x + 141, bot.getyJanela() + y + 304 + 17);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Clicar em Ok
		bot.moverMouse(bot.getxJanela() + 586, bot.getyJanela() + 429);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Apertar enter que apareceu um balao de npc
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(300);
		// Repetir pra criar a instancia de old gh
		// Clicar em Criar
		bot.moverMouse(bot.getxJanela() + x + 141, bot.getyJanela() + y + 304 + 17);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Clicar em Ok
		bot.moverMouse(bot.getxJanela() + 586, bot.getyJanela() + 429);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Apertar enter que apareceu um balao de npc
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(300);
		//Clicar em Entrar
		bot.moverMouse(bot.getxJanela() + x + 48, bot.getyJanela() + y + 304 + 17);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Clicar em Ok
		bot.moverMouse(bot.getxJanela() + 586, bot.getyJanela() + 429);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);

		bot.sleep(50000);*/
		bot.visaoDeCima();
		bot.sleep(100);
		bot.zoom(-28);

		// inicarBotPeloLocalAtualDoPlayer(script);

		// Modo instancia
		if (scriptContas != null) {
			// scriptContas = scriptLoader.carregarContas("teste_login_instancias" +
			// ".json");

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
			String instancia = scriptContas.getContas().get(0).getPersonagens().get(0).getInstancias().get(0) + ".json";
			Script scriptTemp = scriptLoader.carregarScriptdoJson("instancias/" + instancia);
			setScript(scriptTemp);

			logarNaPrimeiraVez();

			System.out.println("###################################################################");
		}

		// GrafoMapa grafo = new GrafoMapa();
		//notebook
    	String mapa = script.getMapa();
    	mapaCarregado = carregarMapa("mapas/" + mapa);
    	grafo = gerarGrafoDeMapa(mapaCarregado);

		/*
		 * if (script.getCoordenadasAlt() != null) { for(Links links :
		 * script.getCoordenadasAlt()) { Coordenadas c1 = new
		 * Coordenadas(links.getLinks().get(0), links.getLinks().get(1)); Coordenadas c2
		 * = new Coordenadas(links.getLinks().get(2), links.getLinks().get(3));
		 * grafo.addConexao(c1, c2); } }
		 */

		/* ligarBot = false; */

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
			
			 //notebook
            int timeRand = ThreadLocalRandom.current().nextInt(5000, 10001);
            if (bot.tempoPassou(timeRand)) {
            	if (bot.getHpAtual() <= 1) {
            		if (script.getMapa().equals("bio.png")) {
            			voltarParaBio();
            		}
            	}
            }

			// Verificar monstros antes de andar
            //notebook
            Map<String, List<MatOfPoint>> monstros;
            if (verificarModoInstanciaProcura()) {
            	monstros = bot.listaMonstrosInstancias();
            } else {
            	 monstros = bot.listaMonstros();
            }

			// Verificar se existem monstros visíveis
			if (!monstros.get("rosa").isEmpty() || !monstros.get("azul").isEmpty()) {
				stateMachine.mudarEstado(Estado.ATACANDO);
			}
			

			switch (stateMachine.getEstadoAtual()) {
			case ANDANDO:
				if (modoMemoria) {
					atual = bot.obterCoordenadasMemoria();
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

				// verificar se tem falha no ocr pra nao dar coordenadas muito longe
				// as vezes o ocr erra a leitura e o personagem fica indo para direções
				// estranhas
				/*
				 * if (Math.abs(ultimaCoordenada.x - atual.x) > 20 ||
				 * Math.abs(ultimaCoordenada.y - atual.y) > 20 ) { int diminuirPasso = passo -
				 * 1; if (diminuirPasso <= 0) { diminuirPasso = 0; } int x =
				 * script.getRotas().get(rota).getPassos().get(diminuirPasso).getCoordenadas().
				 * get(0); int y =
				 * script.getRotas().get(rota).getPassos().get(diminuirPasso).getCoordenadas().
				 * get(1); atual = new Coordenadas(x,y);
				 * 
				 * System.out.println("****************#########################");
				 * System.out.println("****************######################### " + atual);
				 * System.out.println("****************#########################" +
				 * ultimaCoordenada); Coordenadas atualMemoria = bot.obterCoordenadasMemoria();
				 * atual = atualMemoria;
				 * 
				 * }
				 */
				/*
				 * int diminuirPasso = passo - 1; if (diminuirPasso <= 0) { diminuirPasso = 0; }
				 * int xInicio =
				 * script.getRotas().get(rota).getPassos().get(diminuirPasso).getCoordenadas().
				 * get(0); int yInicio =
				 * script.getRotas().get(rota).getPassos().get(diminuirPasso).getCoordenadas().
				 * get(1); int xFim =
				 * script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(0);
				 * int yFim =
				 * script.getRotas().get(rota).getPassos().get(passo).getCoordenadas().get(1);
				 * 
				 * double distanciaDaReta = bot.calcularDistanciaDaReta(new
				 * Coordenadas(xInicio,yInicio), new Coordenadas(xFim,yFim), atual);
				 * 
				 * if (distanciaDaReta > 10) { AStar aStar = new AStar(); //caminhoAlternativo =
				 * aStar.calcularCaminhoComExpansao(atual, new Coordenadas(xFim,yFim), grafo); }
				 */

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
					// stateMachine.mudarEstado(Estado.ATACANDO);
					// }
					// if (!monstros.isEmpty()) {
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

				// stateMachine.mudarEstado(Estado.ANDANDO);
				break;

			default:
				System.out.println("Estado desconhecido");
			}

			long endTime = System.currentTimeMillis();
			tempoExecucao = endTime - startTime;
			JanelaPrincipal.updateTempoLabel(tempoExecucao);
			// System.out.println("Bloco executado em " + tempoExecucao + " ms.");
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
		caminhoCalculado = aStar.calcularCaminhoComExpansao(atual, new Coordenadas(xDestino, yDestino), grafo);
	}

	//notebook
    private void moverParaDirecaoOposta(Coordenadas atual) {
    	int distancia = 5; 

        // Gerar movimento em todas as direções
        int[] dx = {0, distancia, -distancia, 0, distancia, -distancia, distancia, -distancia}; // Direções X
        int[] dy = {distancia, 0, 0, -distancia, distancia, -distancia, -distancia, distancia}; // Direções Y

        // Seleciona aleatoriamente uma direção
        int direcao = (int) (Math.random() * dx.length);

        // Calcula as novas coordenadas
        int novaX = atual.x + dx[direcao];
        int novaY = atual.y + dy[direcao];

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

	//notebook
	private void interagirComNpc(Script script) {
		List<MatOfPoint> balao = bot.verificarBalaoNpc();
		System.out.println("tamanho balao " + balao.size());

		boolean balaoDeOpcoesUnico = script.getRotas().get(rota).getVerificacao().getAcoes().get(acoesNpc)
				.isBalaoUnico();
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
				//rota++;
				//interagindoComNpc = false;
				//stateMachine.mudarEstado(Estado.ANDANDO);
			}
		} else if (balao.isEmpty()) {
			acoesNpc = 0;
			rota++;
			interagindoComNpc = false;
			stateMachine.mudarEstado(Estado.ANDANDO);
		}
	}

	private void apresentacao(Script script) {
		for (Rota rota : script.getRotas()) {
			System.out.println("descricao: " + rota.getDescricao());
			for (Passo passo : rota.getPassos()) {
				System.out.println("[" + passo.getCoordenadas().get(0) + ", " + passo.getCoordenadas().get(1) + "]");
			}
			System.out.println("Verificacao:");
			System.out.println("tipo: " + rota.getVerificacao().getTipo());
			System.out.println("coordenadas: [" + rota.getVerificacao().getCoordenadas().get(0) + ", "
					+ rota.getVerificacao().getCoordenadas().get(1) + "]");
			if (rota.getVerificacao().getElseAcoes() != null) {
				for (Passo elseAcoes : rota.getVerificacao().getElseAcoes()) {
					System.out.println("elseAcoes: [" + elseAcoes.getCoordenadas().get(0) + ", "
							+ elseAcoes.getCoordenadas().get(1) + "]");
				}
			}
			if (rota.getVerificacao().getAcoes() != null) {
				for (Acao acao : rota.getVerificacao().getAcoes()) {
					System.out.println("balaoUnico: " + acao.isBalaoUnico() + ", opcao: " + acao.getOpcao());
				}
			}
		}
		System.out.println("finalizacao:");
		System.out.println("descricao: " + script.getFinalizacao().getDescricao());
		System.out.println("coordenadas: " + script.getFinalizacao().getCoordenadas());

		if (script.getCoordenadasAlt() != null) {
			System.out.println("CoordenadasAlt");
			for (Links links : script.getCoordenadasAlt()) {
				System.out.println("links: [" + links.getLinks().get(0) + ", " + links.getLinks().get(1) + ", "
						+ links.getLinks().get(2) + ", " + links.getLinks().get(3) + "]");
			}
		}
		System.out.println("Mapa: " + script.getMapa());
		System.out.println("-------------------------------------------------------------------------------------");
	}

	//notebook
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
    	
    	Coordenadas destinoAlt = bot.escolherProximaCoordenada(caminhoCalculado, atual);
        	
         // Verificar se o destino foi alcançado
        if (bot.calcularDistancia(atual, destino) <= distanciaMinima) {
        	passo++;
        	System.out.println("Destino alcançado, mudando para a próxima rota.");

        	if (passo >= script.getRotas().get(rota).getPassos().size()) {
        		passo = 0;
        	}
        } else {
        	// Mover o personagem em direção ao destino
        	bot.moverPersonagem(atual, destinoAlt, mapaCarregado);
        }
    	
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

            bot.moverPersonagem(atual, destinoAlt, mapaCarregado);

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
		System.out.println("Verificando se está proximo de " + verificarCoordenadas + " | " + (bot.calcularDistancia(atual, verificarCoordenadas) <= distanciaMinima));

		if (bot.calcularDistancia(atual, verificarCoordenadas) <= distanciaMinima) {
			System.out.println("Rota aumentada de verdade pela verificacao do teleport : " + verificarX + " " + verificarY);
			rota++;
			elseAcoes = 0;
			passo = 0;
			passoAlternativo = 0;

			if (rota >= script.getRotas().size()) {
				// System.out.println("Caiu na ultima rota: " + rota + " | rota.size():" +
				// script.getRotas().size());
				finalizarRota(script, distanciaMinima);
			}
		}
	}

	private void finalizarRota(Script script, int distanciaMinima) {
		// reiniciar tudo ou ir pro finalização
		rota = 0;
		int finalizarX = script.getFinalizacao().getCoordenadas().get(0);
		int finalizarY = script.getFinalizacao().getCoordenadas().get(1);
		Coordenadas finalizacao = new Coordenadas(finalizarX, finalizarY);

		if (bot.calcularDistancia(atual, finalizacao) <= distanciaMinima) {
			System.out.println("Finalizou a rota: " + script.getFinalizacao().getDescricao());
			if (JanelaPrincipal.instanciaRadioButton.isSelected()) {
				// fazer as logicas pra iniciar as instancias
				System.out.println("Caiu em rota de instancia?");
				int numeroAltMoroc = 2;
				bot.voltarMoroc(numeroAltMoroc);
				bot.sleep(5000);
				//Encerrar instancia
				bot.moverMouse(bot.getxJanela() + 104, bot.getyJanela() + 436);
				bot.sleep(300);
				bot.clicarMouse();
				bot.sleep(300);
				return;
			}
			// System.out.println("Ta caindo no na logica do farme mesmo?");
			// Bloco para o player fazer as outras rotas se tiver
			farm++;
			if (farm > listaDeFarmBioChef.size() - 1) {
				farm = 0;
			}
			ScriptLoader scriptLoader = new ScriptLoader();
			String modoFarm = listaDeFarmBioChef.get(farm);
			System.out.println("Proximo script é o :" + modoFarm);
			// script = scriptLoader.carregarScriptdoJson("biochef/" + modoFarm);
			setScript(scriptLoader.carregarScriptdoJson("biochef/" + modoFarm));
			// System.out.println("descricao do script: " +
			// script.getFinalizacao().getDescricao());
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
		passoAlternativo = 0;
		stateMachine.mudarEstado(Estado.NPC);
		tentandoFalarComNpc = true;
	}

	/*
	 * private void atacar(List<MatOfPoint> monstros) throws Exception {
	 * monstros.sort(Comparator.comparingDouble(m ->
	 * bot.calcularDistanciaCentro(m))); monstros.reversed();
	 * 
	 * for (MatOfPoint monstro : monstros) { bot.atacarMonstro(monstro); break; //
	 * Ataca um monstro e sai } }
	 */

	private void atacar(Map<String, List<MatOfPoint>> monstros) {
		/*
		 * List<MatOfPoint> monstrosRosa = monstros.get("rosa"); List<MatOfPoint>
		 * monstrosAzul = monstros.get("azul");
		 * 
		 * //Dar prioridade para os azuis if (!monstrosAzul.isEmpty()) {
		 * monstrosAzul.sort(Comparator.comparingDouble(m ->
		 * bot.calcularDistanciaCentro(m))); //monstrosAzul.reversed(); for (MatOfPoint
		 * monstro : monstrosAzul) {
		 * 
		 * bot.atacarMonstro(monstro, KeyEvent.VK_E); break; // Ataca um monstro e sai }
		 * return; }
		 * 
		 * monstrosRosa.sort(Comparator.comparingDouble(m ->
		 * bot.calcularDistanciaCentro(m))); //monstrosRosa.reversed(); for (MatOfPoint
		 * monstro : monstrosRosa) { bot.atacarMonstro(monstro, KeyEvent.VK_Q); break;
		 * // Ataca um monstro e sai }
		 */

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
		/*
		 * return bot.skills.stream() .filter(skill -> skill.getCor().equals(cor) &&
		 * skill.isReady()) .findFirst() .orElse(null);
		 */
	}

	public void inicarBotPeloLocalAtualDoPlayer(Script script) {
		String coordenadaXY = "";
		coordenadaXY = bot.ocrCoordenadas();

		Coordenadas atual = new Coordenadas(coordenadaXY);

		int menorDistancia = 99999;
		int rotaCalculada = 0;
		int passoCalculada = 0;
		for (int i = 0; i < script.getRotas().size(); i++) {
			for (int j = 0; j < script.getRotas().get(i).getPassos().size(); j++) {
				int x = script.getRotas().get(i).getPassos().get(j).getCoordenadas().get(0);
				int y = script.getRotas().get(i).getPassos().get(j).getCoordenadas().get(1);
				Coordenadas destino = new Coordenadas(x, y);
				int distanciaCalculada = bot.calcularDistancia(atual, destino);

				if (distanciaCalculada <= menorDistancia) {
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
				Coordenadas[] vizinhos = { new Coordenadas(atual.x, atual.y + 1), new Coordenadas(atual.x, atual.y - 1),
						new Coordenadas(atual.x + 1, atual.y), new Coordenadas(atual.x - 1, atual.y) };

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

	public void logarNaPrimeiraVez() {
		bot.aceitarContrato();

		System.out.println("Apertando enter na selecao de servidor");
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(500);

		String usuario = scriptContas.getContas().get(0).getUsuario();
		String senha = scriptContas.getContas().get(0).getSenha();
		String pin = scriptContas.getContas().get(0).getPin();
		bot.realizarLogin(usuario, senha);

		System.out.println("Apertando enter na escolha do canal 1");
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(5000);

		System.out.println("Chega na parte do pin...");
		bot.inserirPin(pin);
		bot.sleep(1000);

		int indexPersonagem = scriptContas.getContas().get(0).getPersonagens().get(0).getIndexPersonagem();
		int pagina = scriptContas.getContas().get(0).getPersonagens().get(0).getPagina();
		bot.escolherPersonagem(indexPersonagem, pagina);

		// Fechar Logue e Ganhe
		bot.moverMouse(bot.getxJanela() + 510, bot.getyJanela() + 567);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);

		// Selecionar Instancias
		// Abrir Janela de instancias
		bot.moverMouse(bot.getxJanela() + 65, bot.getyJanela() + 150);
		bot.sleep(300);
		bot.clicarMouse();
		// Selecionar de fato a instancia
		bot.sleep(300);
		
		int x = 0;
		int y = 0;
		List<MatOfPoint> janelaInstancia = bot.verificarJanelaInstancia();
		if (!janelaInstancia.isEmpty()) {
			Rect janela = Imgproc.boundingRect(janelaInstancia.get(0));
			x = janela.x;
			y = janela.y;
		}

		bot.sleep(1000);
		bot.moverMouse(bot.getxJanela() + x + 70, bot.getyJanela() + y + 140);
		// int dg = 43; //tomb
		String instancia = this.scriptContas.getContas().get(0).getPersonagens().get(0).getInstancias().get(0);
		int dg = getNumeroInstancia(instancia);
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
			bot.zoom(5);
			bot.moverMouse(bot.getxJanela() + x + 133, bot.getyJanela() + y + 283);
			bot.sleep(200);
			bot.clicarMouse();
			bot.sleep(200);
		}

		int pos = dg % 16;
		if (pos == 0) {
			pos = 16;
		}
		int selecionar = pos * 16 - 8;

		bot.moverMouse(bot.getxJanela() + x + 70, bot.getyJanela() + y + 23 + selecionar);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Clicar em Criar
		bot.moverMouse(bot.getxJanela() + x + 141, bot.getyJanela() + y + 304 + 17);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Clicar em Ok
		bot.moverMouse(bot.getxJanela() + 586, bot.getyJanela() + 429);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Apertar enter que apareceu um balao de npc
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(300);
		// Repetir pra criar a instancia de old gh
		// Clicar em Criar
		bot.moverMouse(bot.getxJanela() + x + 141, bot.getyJanela() + y + 304 + 17);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Clicar em Ok
		bot.moverMouse(bot.getxJanela() + 586, bot.getyJanela() + 429);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Apertar enter que apareceu um balao de npc
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(300);
		//Clicar em Entrar
		bot.moverMouse(bot.getxJanela() + x + 48, bot.getyJanela() + y + 304 + 17);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		// Clicar em Ok
		bot.moverMouse(bot.getxJanela() + 586, bot.getyJanela() + 429);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
	}

	public int getNumeroInstancia(String instancia) {
		int dg = 0;
		switch (instancia) {
		case "Old Glast Heim":
			dg = 33;
			break;
		case "Tomb of Remorse":
			dg = 43;
			break;

		default:
			break;
		}
		return dg;
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

		// bot.soltarMouse();
		notify();
	}

	public void fecharBot() {
		ligarBot = false; // Interrompe o loop
		if (tela != null) {
			tela.dispose();
		}
		System.out.println("Tecla 'F' pressionada. Parando o bot...");

		bot.clicarMouse();

		// bot.soltarMouse();
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
		}
		System.exit(0);
	}
	//notebook
    public void modoSalvarCoordenadas() {
    	Coordenadas c = bot.obterCoordenadasMemoria();
    	String coordenada = "{ \"coordenadas\": [" + c.x + ", " + c.y + "] },";
    	coordenadasModoSalvar.add(coordenada);
    	System.out.println("Coordenada salva: " + coordenada);
    	tocarSom("audio.wav");
    }
    //notebook
    public void modoFecharCoordenadas() {
        String fileName = "coordenadas.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String coord : coordenadasModoSalvar) {
                writer.write(coord);
                writer.newLine();
            }
            System.out.println("Arquivo salvo com sucesso em: " + fileName);
        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo: " + e.getMessage());
        }
        coordenadasModoSalvar.clear();
        System.out.println("Lista limpa!!!");
        abrirArquivo(fileName);
    }
    //notebook
    public void abrirArquivo(String fileName) {
        try {
            File arquivo = new File(fileName);
            if (arquivo.exists()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(arquivo); // Abre o arquivo com o aplicativo padrão do sistema
            } else {
                System.err.println("O arquivo não existe: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("Erro ao abrir o arquivo: " + e.getMessage());
        }
    }
    //notebook
    private void tocarSom(String caminhoDoArquivo) {
        try {
            // Carregar o arquivo de áudio
            File arquivoSom = new File(caminhoDoArquivo);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(arquivoSom);
            
            // Configurar o áudio para reprodução
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start(); // Tocar o som
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Formato de áudio não suportado: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de áudio: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("Erro ao reproduzir o som: " + e.getMessage());
        }
    }
  //notebook
    private boolean verificarModoInstanciaProcura() {
    	String nomeMapa = script.getMapa();
        if (nomeMapa.equals("old_glast_heim.png")) {
        	return true;
        }
        return false;
    }
  //notebook
    private void voltarParaBio() {
    	//Voltar para labirinto valk
    	int labirinto = skillsConfig.getLabirintovalk();
    	bot.atalhoAltM(labirinto);
    	bot.sleep(5000);
    	//Voltar para morroc
    	int moroc = skillsConfig.getGomoroc();
    	bot.atalhoAltM(moroc);
    	bot.sleep(5000);
    	//Clicar pra curar
    	bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(159,97));
    	bot.sleep(100);
    	bot.clicarMouse();
    	bot.sleep(30);
    	bot.clicarMouse();
    	bot.sleep(100);
    	//Falar com teleporte
    	bot.atalhoAltM(moroc);
    	bot.sleep(2000);
    	bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(164,98));
    	bot.sleep(90);
    	bot.clicarMouse();
    	bot.sleep(80);
    	bot.clicarMouse();
    	bot.sleep(100);
    	bot.selecionarOpcao(20);
    	bot.sleep(2000);
    	bot.selecionarOpcao(1);
    	bot.sleep(2000);
    	bot.selecionarOpcao(2);
    	bot.sleep(2000);
    	bot.apertarTecla(KeyEvent.VK_ENTER);
    	bot.sleep(2000);
    	bot.selecionarOpcao(1);
    	bot.sleep(2000);
    	bot.apertarTecla(KeyEvent.VK_ENTER);
    	bot.sleep(2000);
    	bot.selecionarOpcao(1);
    	bot.sleep(2000);
    	bot.apertarTecla(KeyEvent.VK_ENTER);
    	bot.sleep(5000);
    	bot.visaoDeCima();
    	bot.zoom(-28);
    	//ir pro portal
    	bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(135,257));
    	bot.sleep(3000);
    	bot.clicarMouse();
    }

    //notebook
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
        
        // Ctrl + Shift + S
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_S) {
            modoSalvarCoordenadas();
        }
        // Ctrl + Shift + D
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_D) {
            modoFecharCoordenadas();
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

	public ContasConfig getScriptContas() {
		return scriptContas;
	}

	public void setScriptContas(ContasConfig scriptContas) {
		this.scriptContas = scriptContas;
	}

	public Bot getBot() {
		return bot;
	}

}