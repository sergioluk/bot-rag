package com.ragnarokbot.main;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
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
import com.sun.jna.platform.win32.Wincon.COORD;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;



public class GameController implements Runnable {

	private final Bot bot;
	//private final Tela tela;
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
	
	private long ultimoUpdateTela = 0;
	
	public Map<String, List<MatOfPoint>> monstros = new HashMap<>();
	
	//notebook
    public List<String> coordenadasModoSalvar = new ArrayList<>();
    public SkillsConfig skillsConfig;
    
    long startTimeBoss = System.currentTimeMillis(); // Marca o tempo inicial
    long maxSearchTime = 5000; // 5 segundos de busca mínima
    boolean foundBoss = false;
    private boolean andarForcado = false;
    long tempoAndarForcado = 0;
    
    public List<Coordenadas> coordsMonstrosAtrasParede = new ArrayList<>();
    
    private boolean isBoss = false;
    
    private Coordenadas coordsUltimoMonstro = new Coordenadas(0,0);
    private int contagemRefresh = 0;
    
    public int indexConta = 0;
    public int indexPersonagem = 0;
    public int indexInstancia = 0;
    
    private Thread botThread;

	public GameController(Bot bot) {
		this.bot = bot;
		//this.tela = tela;
		/*
		try {
			// Registrar o hook do teclado
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}*/

		/*if (modoMemoria) {
			atual = bot.obterCoordenadasMemoria();
		} else {
			String coordenadaXY = bot.ocrCoordenadas();
			atual = new Coordenadas(coordenadaXY);
		}*/
		ultimaCoordenada = atual;

	}

	@Override
	public void run() {

		ScriptLoader scriptLoader = new ScriptLoader();
		
		//notebook
    	skillsConfig = scriptLoader.carregarSkills("config/config_skills.json");
    	/*for (Classes c : skillsConfig.getClasses()) {
    		System.out.println("classe: " + c.getClasse());
    		for (Skills sk : c.getSkills()) {
    			System.out.println("atalho: " + sk.getAtalho());
    			System.out.println("cd: " + sk.getCd());
    			System.out.println("cor: " + sk.getCor());
    			System.out.println("range: " + sk.getRange());
    			int teclaAtalho = KeyMapper.getTeclaAtalho(sk.getAtalho());
    			bot.skills.add(new Skill(teclaAtalho, sk.getCor(), sk.getCd(), sk.getRange()));
    		}
    	}*/
    	

		//bot.printarTela();
		//atalho padrao para bio chef
		carregarAtalhosSkills("mago");
		bot.sleep(3000);

		// Modo instancia
		if (scriptContas != null) {
			System.out.println("Scriptcontas não é null");

			for (Conta conta : scriptContas.getContas()) {
				System.out.println("Usuário: " + conta.getUsuario());
				System.out.println("Senha: " + conta.getSenha());
				System.out.println("PIN: " + conta.getPin());
				for (Personagem personagem : conta.getPersonagens()) {
					System.out.println(" - Página: " + personagem.getPagina());
					System.out.println(" - Index: " + personagem.getIndexPersonagem());
					System.out.println(" - Passar Itens: " + personagem.isPassarItens());
					System.out.println(" - Classe: " + personagem.getClasse());
					System.out.println(" - Instâncias: " + personagem.getInstancias());
				}
			}
			String instancia = scriptContas.getContas().get(indexConta)
					.getPersonagens().get(indexPersonagem)
					.getInstancias().get(indexInstancia) + ".json";
			Script scriptTemp = scriptLoader.carregarScriptdoJson("instancias/" + instancia);
			System.out.println("instancia do script escolhida: " + instancia);
			setScript(scriptTemp);

			logarNaPrimeiraVez();

			System.out.println("###################################################################");
		}

    	carregarMapa();

		/* ligarBot = false; */
    	
    	bot.visaoDeCima();
		bot.sleep(100);
		bot.zoom(-28);

		while (ligarBot) {
			long startTime = System.currentTimeMillis();
			//bot.sleep(100);
			bot.sleep(skillsConfig.getTempoDoLoop());
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
			/*
			if (pausarBot) {
				System.out.println("Bot pausado");
				continue;
			}

			if (bot.configOCR.rectangle.x == 0) {
				System.out.println("Aguardando setar coordenadas");
				continue;
			}*/
			
			 //notebook
            int timeRand = ThreadLocalRandom.current().nextInt(5000, 10001);
            if (bot.tempoPassou(timeRand)) {
            	if (bot.getHpAtual() <= 1) {
            		String mapa = "";
            		if (script.getMapa().equals("bio.png")) {
            			mapa = "bio";
            		}
            		if (script.getMapa().equals("chef.png")) {
            			mapa = "chef";
            		}
            		if (!mapa.isBlank()) {
            			voltarParaFarmar(mapa);
            		}
            	}
            }

			// Verificar monstros antes de andar
            //notebook
            isBoss = script.getRotas().get(rota).getVerificacao().isBoss();
            if (isBoss == true) { // Boss encontrado
               processarLogicaMatarBoss();
            } else {
            	 monstros = bot.listaMonstros();
            	 
            	 if (verificarModoInstanciaProcura()) {
            		 List<MatOfPoint> monstrosAtrasParede = monstros.get("monstrosAtrasParede");
            		 
            		// Set temporário para evitar duplicações
            		Set<Coordenadas> coordenadasUnicas = new HashSet<>(coordsMonstrosAtrasParede);
            		 
            		 for(MatOfPoint monstro : monstrosAtrasParede) {
                		 Rect m = Imgproc.boundingRect(monstro);
                		 int centerX = m.x + m.width/2;
                		 int centerY = m.y + m.height/2 + 10;
                		 Coordenadas destinoBixo = bot.getCoordenadasTelaDoBixo(atual, centerX, centerY);
                		 
                		 Boolean isAndavel = mapaCarregado.get(destinoBixo);
                		 System.out.println("Coordenadas analisada x: " + destinoBixo.x + " y: " + destinoBixo.y + " | " + isAndavel);
                		 
                		 if (isAndavel) {
                			 boolean podeAdicionar = true;
                			 
                			 for(Coordenadas coord : coordenadasUnicas) {
                				 int difX = Math.abs(destinoBixo.x - coord.x);
                				 int difY = Math.abs(destinoBixo.y - coord.y);
                				 if (difX <= 3 && difY <= 3) {
                					 podeAdicionar = false;
                					 break;
                				 }
                			 }
                			 if (podeAdicionar) {
                				 coordenadasUnicas.add(destinoBixo);  // Adiciona no conjunto para garantir que não haja repetição
                				 coordsMonstrosAtrasParede.add(destinoBixo); // Adiciona na lista principal sem sobrescrever
                	         }
                		 }
            		 }
            		 
            		 /*coordsMonstrosAtrasParede.sort(Comparator.comparingInt(coord -> {
         			    List<Coordenadas> caminhoAteOBixo = aStar.encontrarCaminho(grafo, atual, coord);
         			    return caminhoAteOBixo.size();
         			}));*/
            		// Exibir resultado formatado
            		StringBuilder lista = new StringBuilder();
            		for (Coordenadas c : coordsMonstrosAtrasParede) {
            			lista.append(" (x: ").append(c.x).append(", y: ").append(c.y).append("), ");
            		}
            		System.out.println("Size: " + coordsMonstrosAtrasParede.size());
            		System.out.println(lista);
            		/*if (screen != null) {
            			String filePath = "fotoTesteCoordenadas.png"; // Nome do arquivo na pasta atual
            	        boolean success = Imgcodecs.imwrite(filePath, screen);
            	        if (success) {
            	            System.out.println("Imagem salva com sucesso em: " + filePath);
            	        } else {
            	            System.out.println("Falha ao salvar a imagem.");
            	        }
            			System.exit(0);
            		}*/
            	 }
            	 /*
            	 if (coordsMonstrosAtrasParede.isEmpty()) {
            		 List<MatOfPoint> monstrosAtrasParede = monstros.get("monstrosAtrasParede");
                	 for(MatOfPoint monstro : monstrosAtrasParede) {
                		 Rect m = Imgproc.boundingRect(monstro);
                		 int centerX = m.x + m.width/2;
                		 int centerY = m.y + m.height/2;
                		 Coordenadas destinoBixo = bot.getCoordenadasTelaPeloMouse(atual, centerX, centerY);
                		 
                		 List<Coordenadas> copiaMonstrosAtrasParede = new ArrayList<>(coordsMonstrosAtrasParede);
                		 for(Coordenadas coord : copiaMonstrosAtrasParede) {
                			 if (destinoBixo.x - coord.x > 3 && destinoBixo.y - coord.y > 3) {
                				 coordsMonstrosAtrasParede.add(destinoBixo);
                			 }
                		 }
                		 
                		 coordsMonstrosAtrasParede.sort(Comparator.comparingInt(coord -> {
                			    List<Coordenadas> caminhoAteOBixo = aStar.encontrarCaminho(grafo, atual, coord);
                			    return caminhoAteOBixo.size();
                			}));
                		 
                	 }
            	 }*/
            }
            
			// Verificar se existem monstros visíveis
            if (!monstros.getOrDefault("rosa", List.of()).isEmpty() ||
            	    !monstros.getOrDefault("azul", List.of()).isEmpty() ||
            	    !monstros.getOrDefault("amarelo", List.of()).isEmpty()) {
            	    stateMachine.mudarEstado(Estado.ATACANDO);
            }
            
            if (andarForcado) {
            	System.out.println("Andando Forçado!");
            	stateMachine.mudarEstado(Estado.ANDANDO);
            	if (System.currentTimeMillis() - tempoAndarForcado >= skillsConfig.getTempoAndarForcado()) {
            		andarForcado = false;
            	}
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
					if (System.currentTimeMillis() - ultimoUpdateTela >= 5000) {
						int refresh = skillsConfig.getRefresh();
				    	bot.atalhoAltM(refresh);
			            ultimoUpdateTela = System.currentTimeMillis(); // Atualiza o tempo da última chamada
			            andar(script);
			        } else {
			            // Se a tela já foi atualizada recentemente, força o movimento
			            moverParaDirecaoOposta(atual);
			            personagemParado = false;
			        }
					
					//moverParaDirecaoOposta(atual); // Força o movimento do personagem
					//personagemParado = false; // Reseta o estado após o movimento forçado
				} else {
					andar(script);
				}
				break;

			case ATACANDO:
				bot.soltarMouse();
				if (!monstros.getOrDefault("rosa", List.of()).isEmpty() ||
	            	    !monstros.getOrDefault("azul", List.of()).isEmpty() ||
	            	    !monstros.getOrDefault("amarelo", List.of()).isEmpty()) {
					atacar(monstros);
					stateMachine.mudarEstado(Estado.ANDANDO);
	            }
				
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
		/*try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}*/
	}

	private void carregarMapa() {
		String mapa = script.getMapa();
    	mapaCarregado = carregarMapa("mapas/" + mapa);
    	grafo = gerarGrafoDeMapa(mapaCarregado);
	}

	private void processarLogicaMatarBoss() {
		 System.out.println("BOSS BOSS BOSS BOSS BOSS BOSS BOSS BOSS BOSS BOSS BOSS");

         do {
             monstros = bot.procurarBoss();

             if (!monstros.getOrDefault("amarelo", List.of()).isEmpty()) {
                 foundBoss = true;
                 startTimeBoss = System.currentTimeMillis(); // Reseta o timer sempre que encontrar um boss
                 atacar(monstros);
					stateMachine.mudarEstado(Estado.ANDANDO);
             } else {
                 foundBoss = false;
             }

             bot.sleep(100);

         } while ((System.currentTimeMillis() - startTimeBoss) < maxSearchTime || foundBoss);

         // Se saiu do loop, significa que não achou boss por 5 segundos
         if (script.getRotas().get(rota).getVerificacao().isTerminaNoBoss()) {
             finalizarRota(script, 5);
             stateMachine.mudarEstado(Estado.ANDANDO);
         } else {
             rota++;
             elseAcoes = 0;
             passo = 0;
             passoAlternativo = 0;

             if (rota >= script.getRotas().size()) {
                 finalizarRota(script, 5);
             }
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
    	
    	if (!coordsMonstrosAtrasParede.isEmpty() && verificarModoInstanciaProcura()) {// && verificarModoInstanciaProcura()
			Coordenadas destinoBixo = coordsMonstrosAtrasParede.get(0);
			
			if (bot.calcularDistancia(atual, destinoBixo) <= distanciaMinima) {
				System.out.println("Chegou ao destino... removendo coord da lista!!!");
				coordsMonstrosAtrasParede.remove(0);
			} else {
				List <Coordenadas> caminhoAteOBixo = aStar.encontrarCaminho(grafo, atual, destinoBixo);
				Coordenadas coordDestino = bot.escolherProximaCoordenada(caminhoAteOBixo, atual);
				bot.moverPersonagem(atual, coordDestino, mapaCarregado);
				System.out.println("Indo até o monstro atrás da parede x: " + destinoBixo.x + " y: " + destinoBixo.y);
			}
			return;
    	}
    	/*
    	if (verificarModoInstanciaProcura()) {
    		List<MatOfPoint> monstrosIntancias = monstros.getOrDefault("rosa", new ArrayList<>());
    		if (!monstrosIntancias.isEmpty()) {
    			Rect m = Imgproc.boundingRect(monstrosIntancias.get(0));
    			int centerX = m.x + m.width/2;
    			int centerY = m.y + m.height/2;
    			Coordenadas destinoBixo = bot.getCoordenadasTelaPeloMouse(atual, centerX, centerY);
    			List <Coordenadas> caminhoAteOBixo = aStar.encontrarCaminho(grafo, atual, destinoBixo);
    			Coordenadas coordDestino = bot.escolherProximaCoordenada(caminhoAteOBixo, atual);
    			bot.moverPersonagem(atual, coordDestino, mapaCarregado);
    		}
    	}*/
    	
    	//System.out.println("farm: " + farm + " | tamanho da lista: " + this.listaDeFarmBioChef.size());
    	System.out.println("descricao do script: " + script.getRotas().get(rota).getDescricao());

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
		case "sleep":
			delayProBoss();
			break;
		default:
			System.out.println("Tipo de verificação desconhecido: " + verificacao);
		}
	}
	
	
	private void delayProBoss() {
		System.out.println("Mimindo mimindoMimindo mimindoMimindo mimindoMimindo mimindoMimindo mimindoMimindo mimindo");
		bot.sleep(2000);
		System.out.println("Rota aumentada !");
		rota++;
		elseAcoes = 0;
		passo = 0;
		passoAlternativo = 0;
		if (rota >= script.getRotas().size()) {
			finalizarRota(script, 5);
		}
	}

	private void processarVerificacaoTeleport(Script script, int distanciaMinima) {
		int verificarX = script.getRotas().get(rota).getVerificacao().getCoordenadas().get(0);
		int verificarY = script.getRotas().get(rota).getVerificacao().getCoordenadas().get(1);
		Coordenadas verificarCoordenadas = new Coordenadas(verificarX, verificarY);
		//System.out.println("Verificando se está proximo de " + verificarCoordenadas + " | " + (bot.calcularDistancia(atual, verificarCoordenadas) <= distanciaMinima));

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
		
		if (JanelaPrincipal.instanciaRadioButton.isSelected()) {
			// fazer as logicas pra iniciar ou finalizar as instancias
			elseAcoes = 0;
			passo = 0;
			passoAlternativo = 0;
			
			System.out.println("Caiu em rota de instancia?");
			/*int voltarMoroc = skillsConfig.getGomoroc();
			bot.atalhoAltM(voltarMoroc);
			bot.sleep(5000);
			//Encerrar instancia
			bot.encerrarInstancia();
			*/
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			System.out.println("ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ACABOU ");
			
			int ultimoIndexConta = indexConta;
			indexInstancia++;
			if (indexInstancia > scriptContas.getContas().get(indexConta)
					.getPersonagens().get(indexPersonagem).getInstancias().size() - 1) {
				indexInstancia = 0;
				
				indexPersonagem++;
				if (indexPersonagem > scriptContas.getContas().get(indexConta)
						.getPersonagens().size() - 1) {
					indexPersonagem = 0;
					
					indexConta++;
					if (indexConta > scriptContas.getContas().size() - 1) {
						indexConta = 0;
						ligarBot = false;
						fecharBot();
						return;
					}
					
				}
				System.out.println("Deslogando personagem");
				bot.deslogarPersonagem();
				
				if (ultimoIndexConta != indexConta) { //indexConta aumentou
					System.out.println("Indo tela login");
					bot.sleep(3000);
					bot.voltarTelaLogin();
					
					System.out.println("Logando");
					String usuario = scriptContas.getContas().get(indexConta).getUsuario();
					String senha = scriptContas.getContas().get(indexConta).getSenha();
					String pin = scriptContas.getContas().get(indexConta).getPin();
					bot.realizarLogin(usuario, senha);

					System.out.println("Apertando enter na escolha do canal 1");
					bot.apertarTecla(KeyEvent.VK_ENTER);
					bot.sleep(5000);

					System.out.println("Chega na parte do pin...");
					bot.inserirPin(pin);
					bot.sleep(1000);
				}
				
				int indexPersonagem = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem).getIndexPersonagem();
				int pagina = scriptContas.getContas().get(0).getPersonagens().get(0).getPagina();
				bot.escolherPersonagem(indexPersonagem, pagina);
				String classe = scriptContas.getContas().get(indexConta).getPersonagens().get(indexPersonagem).getClasse();
				carregarAtalhosSkills(classe);
				
				// Fechar Logue e Ganhe
				bot.moverMouse(bot.getxJanela() + 510, bot.getyJanela() + 567);
				bot.sleep(300);
				bot.clicarMouse();
				bot.sleep(300);
				
				//Fechar chat de npc
				bot.apertarTecla(KeyEvent.VK_ENTER);
				bot.sleep(300);
				//Fechar chat de npc
				bot.apertarTecla(KeyEvent.VK_ENTER);
				bot.sleep(300);
				
				bot.visaoDeCima();
				bot.sleep(100);
				bot.zoom(-28);
				
				
			}
			String instanciaScript = scriptContas.getContas().get(indexConta)
					.getPersonagens().get(indexPersonagem)
					.getInstancias().get(indexInstancia) + ".json";
			ScriptLoader scriptLoader = new ScriptLoader();
			Script scriptTemp = scriptLoader.carregarScriptdoJson("instancias/" + instanciaScript);
			setScript(scriptTemp);
			carregarMapa();
			
			String instancia = scriptContas.getContas().get(0).getPersonagens().get(0).getInstancias().get(0);
			//bot.executarInstancia(instancia);
			
			return;
		}
		
		int finalizarX = script.getFinalizacao().getCoordenadas().get(0);
		int finalizarY = script.getFinalizacao().getCoordenadas().get(1);
		Coordenadas finalizacao = new Coordenadas(finalizarX, finalizarY);

		if (bot.calcularDistancia(atual, finalizacao) <= distanciaMinima) {
			System.out.println("Finalizou a rota: " + script.getFinalizacao().getDescricao());
			
			// System.out.println("Ta caindo no na logica do farme mesmo?");
			// Bloco para o player fazer as outras rotas se tiver
			farm++;
			
			elseAcoes = 0;
			passo = 0;
			passoAlternativo = 0;
			
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
		
		Skill skillDisponivel = null;
		MatOfPoint monstro = null;
		// Boss
		List<MatOfPoint> monstrosAmarelo = monstros.computeIfAbsent("amarelo", k -> new ArrayList<>());
		if (!monstrosAmarelo.isEmpty()) {
			monstrosAmarelo.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
			
			skillDisponivel = getAvailableSkill("rosa");
			if (skillDisponivel != null) {
				monstro = monstrosAmarelo.get(0);
				if (usarHabilidade(monstro, skillDisponivel.getTecla(), skillDisponivel)) {
					skillDisponivel = null;
				}
			}
		}
		//monstros azuis
		List<MatOfPoint> monstrosAzul = monstros.computeIfAbsent("azul", k -> new ArrayList<>());
		if (!monstrosAzul.isEmpty()) {
			monstrosAzul.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
			
			skillDisponivel = getAvailableSkill("azul");
			if (skillDisponivel != null) {
				monstro = monstrosAzul.get(0);
				if (usarHabilidade(monstro, skillDisponivel.getTecla(), skillDisponivel)) {
					skillDisponivel = null;
				}
			}
		}

		// Monstros rosas
		List<MatOfPoint> monstrosRosa = monstros.computeIfAbsent("rosa", k -> new ArrayList<>());
		if (!monstrosRosa.isEmpty()) {
			monstrosRosa.sort(Comparator.comparingDouble(m -> bot.calcularDistanciaCentro(m)));
			
			skillDisponivel = getAvailableSkill("rosa");
			if (skillDisponivel != null) {
				monstro = monstrosRosa.get(0);
				if (usarHabilidade(monstro, skillDisponivel.getTecla(), skillDisponivel)) {
					skillDisponivel = null;
				}
			}
		}
		
		if (monstro != null && isBoss == false) {
			Rect m = Imgproc.boundingRect(monstro);
			int centerX = m.x + m.width/2;
			int centerY = m.y + m.height/2 + 10;
			Coordenadas coordBixo = bot.getCoordenadasTelaDoBixo(atual, centerX, centerY);
			if (bot.compararCoordenadas(coordBixo, coordsUltimoMonstro)) {
				contagemRefresh++;
				if (contagemRefresh > 3) {
					contagemRefresh = 0;
					int refresh = skillsConfig.getRefresh();
					bot.atalhoAltM(refresh);
				}
			} else {
				contagemRefresh = 0;
				coordsUltimoMonstro = coordBixo;
			}
		}
		
		 
	
		if (skillDisponivel == null && !JanelaPrincipal.instanciaRadioButton.isSelected()) {
			//andar(script);
			if (andarForcado) {
				return;
			}
			System.out.println("Ativando andar forçado por 1 segundos!");
			andarForcado = true;
			tempoAndarForcado = System.currentTimeMillis();
		}

	}
	
	private boolean usarHabilidade(MatOfPoint monstro, int tecla, Skill skill) {
		Rect m = Imgproc.boundingRect(monstro);
		 int centerX = m.x + m.width/2;
		 int centerY = m.y + m.height/2 + 10;
		 Coordenadas coord = bot.getCoordenadasTelaDoBixo(atual, centerX, centerY);
		 if (bot.calcularDistancia(atual, coord) <= skill.getRange()) {
			bot.atacarMonstro(monstro, tecla);
			skill.use();
			return true;
		 } 
		return false;
	}

	// Método auxiliar para obter a próxima habilidade disponível para uma cor
	private Skill getAvailableSkill(String cor) {
		Skill skill = null;
		for (Skill sk : bot.skills) {
			System.out.println("Skill: " + sk.getTecla() + " pronta: " + sk.isReady());
			if (isBoss == true) {
				if (sk.getCor().equals(cor)) {
					skill = sk;
					break;
				}
			} else if (sk.getCor().equals(cor) && sk.isReady()) {
				skill = sk;
				break;
			}
		}
		return skill;
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

		String usuario = scriptContas.getContas().get(indexConta).getUsuario();
		String senha = scriptContas.getContas().get(indexConta).getSenha();
		String pin = scriptContas.getContas().get(indexConta).getPin();
		bot.realizarLogin(usuario, senha);

		System.out.println("Apertando enter na escolha do canal 1");
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(5000);

		System.out.println("Chega na parte do pin...");
		bot.inserirPin(pin);
		bot.sleep(1000);

		int indexPersonagem = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem).getIndexPersonagem();
		int pagina = scriptContas.getContas().get(0).getPersonagens().get(0).getPagina();
		bot.escolherPersonagem(indexPersonagem, pagina);
		String classe = scriptContas.getContas().get(indexConta).getPersonagens().get(indexPersonagem).getClasse();
		carregarAtalhosSkills(classe);
		
		// Fechar Logue e Ganhe
		bot.moverMouse(bot.getxJanela() + 510, bot.getyJanela() + 567);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);
		
		//Fechar chat de npc
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(300);
		//Fechar chat de npc
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(300);

		// Selecionar Instancias
		// Abrir Janela de instancias
		//bot.executarInstancia(scriptContas);
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
	
	public void pararBot() {
		System.out.println("Tecla 'O' pressionada. Parando o bot...");
		ligarBot = false;
		
		rota = 0;
		passo = 0;
		acoesNpc = 0;
		farm = 0;
		
		if (pausarBot) {
			pausarBot();
		}
		/*try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}*/
		// Se a thread estiver rodando, interrompê-la
	    if (botThread != null && botThread.isAlive()) {
	        botThread.interrupt();
	    }
	}

	public void fecharBot() {
		ligarBot = false; // Interrompe o loop
		//if (tela != null) {
		//	tela.dispose();
		//}
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
    private void voltarParaFarmar(String mapa) {
    	//Voltar para labirinto valk
    	int labirinto = skillsConfig.getLabirintovalk();
    	bot.atalhoAltM(labirinto);
    	bot.sleep(5000);
    	//Voltar para morroc
    	int base = skillsConfig.getGoBase();
    	bot.atalhoAltM(base);
    	bot.sleep(5000);
    	//visao de cima
    	bot.visaoDeCima();
		bot.sleep(100);
		bot.zoom(-28);
		bot.sleep(100);
    	//Clicar pra curar
    	bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(231,201));
    	bot.sleep(100);
    	bot.clicarMouse();
    	bot.sleep(30);
    	bot.clicarMouse();
    	bot.sleep(100);
    	//Falar com teleporte
    	bot.atalhoAltM(base);
    	bot.sleep(2000);
    	bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(231,205));
    	bot.sleep(90);
    	bot.clicarMouse();
    	bot.sleep(4000);
    	if (mapa.equals("bio")) {
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
    	if (mapa.equals("chef")) {
    		bot.selecionarOpcao(21);
        	bot.sleep(2000);
        	bot.selecionarOpcao(1);
        	bot.sleep(2000);
        	bot.apertarTecla(KeyEvent.VK_ENTER);
        	bot.sleep(2000);
        	bot.selecionarOpcao(1);
        	bot.sleep(2000);
        	bot.apertarTecla(KeyEvent.VK_ENTER);
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
        	bot.sleep(2000);
        	bot.selecionarOpcao(1);
        	bot.sleep(2000);
        	bot.apertarTecla(KeyEvent.VK_ENTER);
        	bot.sleep(5000);
        	bot.visaoDeCima();
        	bot.zoom(-28);
    	}
    	
    }
    
    public void carregarAtalhosSkills(String classe) {
    	bot.skills.clear();
    	for (Classes c : skillsConfig.getClasses()) {
    		if (c.getClasse().equals(classe)) {
    			for (Skills sk : c.getSkills()) {
    				int teclaAtalho = KeyMapper.getTeclaAtalho(sk.getAtalho());
    				bot.skills.add(new Skill(teclaAtalho, sk.getCor(), sk.getCd(), sk.getRange()));
    			}
    			break;
    		}
    	}
    }
/*
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
        
        // Ctrl + Shift + O
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_O) {
            pararBot();
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
	}*/

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