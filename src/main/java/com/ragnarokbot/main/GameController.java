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
import com.ragnarokbot.bot.Buff;
import com.ragnarokbot.bot.Skill;
import com.ragnarokbot.bot.Tela;
import com.ragnarokbot.model.AStar;
import com.ragnarokbot.model.Coordenadas;
import com.ragnarokbot.model.GrafoMapa;
import com.ragnarokbot.model.enums.Effects;
import com.ragnarokbot.model.enums.Estado;
import com.ragnarokbot.model.enums.Mapa;
import com.ragnarokbot.telas.JanelaPrincipal;
import com.sun.jna.platform.win32.Wincon.COORD;

import config.ContasConfig;
import config.ContasConfig.Conta;
import config.ContasConfig.Personagem;
import config.Script;
import config.ScriptLoader;
import config.SkillsConfig;
import config.SkillsConfig.Buffs;
import config.SkillsConfig.Classes;
import config.SkillsConfig.Skills;
import config.Script.Acao;
import config.Script.Links;
import config.Script.Passo;
import config.Script.Rota;
import net.sourceforge.tess4j.TesseractException;
import state.StateMachine;
import utils.KeyMapper;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.Robot;
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
	// private final Tela tela;
	public volatile boolean ligarBot = true;
	public volatile boolean pausarBot = false;
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

	// notebook
	public List<String> coordenadasModoSalvar = new ArrayList<>();
	public SkillsConfig skillsConfig;

	long startTimeBoss = System.currentTimeMillis(); // Marca o tempo inicial
	long maxSearchTime = 5000; // 5 segundos de busca mínima
	boolean foundBoss = false;
	private boolean andarForcado = false;
	long tempoAndarForcado = 0;
	long tempoVerificacaoOldGH = System.currentTimeMillis();

	public List<Coordenadas> coordsMonstrosAtrasParede = new ArrayList<>();

	private boolean isBoss = false;
	private boolean verificarSeLimpouOldGh = false;

	private Coordenadas coordsUltimoMonstro = new Coordenadas(0, 0);
	private int contagemRefresh = 0;

	public int indexConta = 0;
	public int indexPersonagem = 0;
	public int indexInstancia = 0;

	boolean saindoDeCimaBio = false;

	boolean isBase = false;

	// voltar Pro farme
	private boolean falouComCurandeiro = false;
	boolean modoVoltarParaFarmar = false;
	private int ultimoIndex = 0;
	private int indexVoltarFarm = 0;
	private boolean jaEscolheuPrimeiraOpcao = false;
	private boolean jaEscolheuSegundaBioOpcao = false;
	private List<Integer> listaOpcoesFarmNpc = new ArrayList<>();
	private boolean verificarFullStrip = false;
	private int contagemBalaoSizeZero = 0;

	private boolean finalizandoInstancia = false;
	private boolean guardandoEquipsArmazem = false;
	private int passosInteragirKafra = 0;
	private Rect rectAltQ = null;
	private Rect rectArmazem = null;
	private Rect rectInventario = null;
	private boolean modoDesequiparEquips = false;
	private int passosDesequiparEquips = 0;
	private boolean pegarEquipsArmazem = false;
	private boolean pegarEquipsArmazem2 = false;
	private int passosInteragirKafraRemoverItens = 0;
	
	//public static boolean isVelocidade = false;
	//public static boolean isChicleteGoma = false;
	long tempoVeloGoma = System.currentTimeMillis();
	
	long tempoCooldownRefresh = System.currentTimeMillis();
	boolean aguardarCdRefresh = false;
	
	private BufferedImage cometa = null;
	private BufferedImage cometaPreto = null;
	BufferedImage barra = null;
	private Rect barraSkills = null;

	private Thread botThread;
	public Tela tela;
	public static int aspdPala = 0;
	public static int aspdPalaTarget = 0;
	
	private boolean horaDeBuffar = false;
	
	long tempoPm = System.currentTimeMillis();

	public GameController(Bot bot) {
		this.bot = bot;
		cometa = bot.abrirImagem("config/skills/cometa.png");
		cometaPreto = bot.abrirImagem("config/skills/cometa preto.png");
		barra = bot.abrirImagem("config/skills/barra.png");
		//this.tela = tela;
		/*
		 * try { // Registrar o hook do teclado GlobalScreen.registerNativeHook();
		 * GlobalScreen.addNativeKeyListener(this); } catch (Exception e) {
		 * e.printStackTrace(); }
		 */

		/*
		 * if (modoMemoria) { atual = bot.obterCoordenadasMemoria(); } else { String
		 * coordenadaXY = bot.ocrCoordenadas(); atual = new Coordenadas(coordenadaXY); }
		 */
		ultimaCoordenada = atual;

	}

	@Override
	public void run() {

		ScriptLoader scriptLoader = new ScriptLoader();

		// notebook
		skillsConfig = scriptLoader.carregarSkills("config/config_skills.json");
		
		if (skillsConfig.getAspdPala() != null) {
			aspdPala = skillsConfig.getAspdPala();
		}
		if (skillsConfig.getAspdPalaTarget() != null) {
			aspdPalaTarget = skillsConfig.getAspdPalaTarget();
		}

		// bot.encerrarInstancia();
		//bot.printarTela();
		// atalho padrao para bio chef
		String classe = JanelaPrincipal.obterClasseSelecionada();
		carregarAtalhosSkills(classe);
		
		bot.sleep(3000);
		
		barraSkills = Imgproc.boundingRect(bot.procurarBarraSkills().get(0));

		// Modo instancia
		if (scriptContas != null) {
			System.out.println("Scriptcontas não é null");
			String instancia = scriptContas.getContas().get(indexConta).getPersonagens().get(indexPersonagem)
					.getInstancias().get(indexInstancia) + ".json";
			Script scriptTemp = scriptLoader.carregarScriptdoJson("instancias/" + instancia);
			System.out.println("instancia do script escolhida: " + instancia);
			setScript(scriptTemp);

			logarNaPrimeiraVez();

			System.out.println("###################################################################");
		}

		carregarMapa();
		carregarOpcoesFarmNpc();
		
		//Verifica se vc ja está nos mapas de farme, se nao estiver, volta base e fala com o npc
		if (!JanelaPrincipal.instanciaRadioButton.isSelected()) {
			iniciarBotQualquerLugar();
		}
		

		// Testar se funcionou a programagem e desativar logar pela primeira vez pra
		// testar...
		//modoDesequiparEquips = true;
		//voltarBase();

		/* ligarBot = false; */

		bot.visaoDeCima();
		bot.sleep(100);
		bot.zoom(-28);

		while (ligarBot) {
			long startTime = System.currentTimeMillis();
			// bot.sleep(100);
			bot.sleep(skillsConfig.getTempoDoLoop());
			synchronized (this) {
				while (pausarBot) {
					try {
						System.out.println("Bot pausado");
						wait(); // Aguarda até que `pausado` seja false
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
					}
				}
			}

			if (horaDeBuffar) {
				System.out.println("Hora de buffar Hora de buffar Hora de buffar Hora de buffar Hora de buffar Hora de buffar");
				System.out.println("Hora de buffar Hora de buffar Hora de buffar Hora de buffar Hora de buffar Hora de buffar");
				System.out.println("Hora de buffar Hora de buffar Hora de buffar Hora de buffar Hora de buffar Hora de buffar");
				
				if (!bot.buffs.isEmpty()) {
					int contador = 1;
					for (Buff b : bot.buffs) {
						System.out.println("Buffando o " + contador + " buff");
						int tecla = b.getTecla();
						bot.apertarTecla(tecla);
						if (!b.isSelf()) {
							bot.moverMouse(bot.getxJanela() + bot.getWidth()/2, bot.getyJanela() + bot.getHeight() / 2);
							bot.sleep(50);
							bot.clicarMouse();
						}
						bot.sleep(200);
						contador++;
					}
				}
				horaDeBuffar = false;
				System.out.println("Rota aumentada !");
				rota++;
				elseAcoes = 0;
				passo = 0;
				passoAlternativo = 0;
				if (rota >= script.getRotas().size()) {
					finalizarRota(script, 5);
				}
			}
			
			int timeRand = ThreadLocalRandom.current().nextInt(5000, 10001);
			if (bot.tempoPassou(timeRand)) {
				if (bot.getHpAtual() <= 1) {
					System.out.println("Personagem morreu!!!");
					System.out.println("Personagem morreu!!!");
					System.out.println("Personagem morreu!!!");
					System.out.println("Voltando valkiria e depois base");
					voltarBase();
				}
			}
			
			//Essa parte é só pra nao verificar o cd do cometa durante um refresh, pra dar um tempo de refreshar e depois olhar a imagem do cometa
			if (aguardarCdRefresh) {
				if (System.currentTimeMillis() - tempoCooldownRefresh >= 3000) {
					aguardarCdRefresh = false;
				} else {
					System.out.println("Acabou de dar refrsh, só mantendo a variavel aguardarCdRefresh true");
				}
			}
			
			if (System.currentTimeMillis() - tempoPm >= 30000) {
				tempoPm = System.currentTimeMillis();
				System.out.println("Verificando se recebesse PMS");
				List<MatOfPoint> msg = bot.procurarMensagemPrivada();
				if (msg.isEmpty()) {
					System.out.println("Nenhum pm identificado...");
				} else {
					for (MatOfPoint m : msg) {
						Rect r = Imgproc.boundingRect(m);
						Color c = bot.getRobot().getPixelColor(bot.getxJanela() + r.x - 40, bot.getyJanela() + r.y);
						if (!c.equals(Color.WHITE)) {
							System.out.println("PM ENCONTRADO, TIRA TIRA");
							tocarSom("config/despertador.wav");
						}
					}
				}
			}

			if (JanelaPrincipal.obterVelocidade() == true || JanelaPrincipal.obterGoma() == true) {
				long tempoAtual = System.currentTimeMillis();
				int tempoRandom = ThreadLocalRandom.current().nextInt(13000, 15001);
				if (tempoAtual - tempoVeloGoma >= tempoRandom) {
					tempoVeloGoma = tempoAtual;
					
					List<Integer> status = bot.listarStatus();
					boolean gomaEncontrada = false;
					boolean velocidadeEncontrada = false;
					for (int buffs : status) {
						if (buffs == Effects.CHICLETEGOMA.getId()) {
							System.out.println("Ta com Chiclete/Goma...");
							gomaEncontrada = true;
						}
						if (buffs == Effects.SPEED_POT.getId()) {
							System.out.println("Ta com Velocidade...");
							velocidadeEncontrada = true;
						}
						if (buffs == Effects.ARMA_REMOVIDO.getId() || buffs == Effects.ESCUDO_REMOVIDO.getId()
								|| buffs == Effects.ARMADURA_REMOVIDO.getId() || buffs == Effects.ELMO_REMOVIDO.getId()) {
							boolean isMapaFarme = false;
							/*BufferedImage imagemRef = null;
							String path = "";
							if (script.getMapa().equals("bio.png")) {
								path = "config/minimapas/bio.png";
							}
							if (script.getMapa().equals("chef.png")) {
								path = "config/minimapas/chef.png";
							}
							try {
								imagemRef = ImageIO.read(new File(path));
							} catch (IOException e) {
								e.printStackTrace();
							}
							// Captura do minimapa atual
							Rectangle captureArea = new Rectangle(bot.getxJanela() + 880, bot.getyJanela() + 16, 128, 128);
							BufferedImage minimapaAtual = bot.getRobot().createScreenCapture(captureArea);
							isMapaFarme = bot.compararImagens(imagemRef, minimapaAtual, 30.0);
							System.out.println("Verificando se está no mapa de farme pra usar veneno: " + isMapaFarme);
							
							if (isMapaFarme) {
								int atalho = KeyMapper.getTeclaAtalho(this.skillsConfig.getAtalhoVeneno());
								bot.apertarTecla(atalho);
							}*/
							
							String mapaScript = getNomeMapa();
							String mapaMemoria = bot.obterMapa();
							isMapaFarme = mapaScript.equals(mapaMemoria);
							System.out.println("Mapa script: " + mapaScript + " | Mapa atual: " + mapaMemoria);
							System.out.println("Verificando se está no mapa de farme pra usar veneno: " + isMapaFarme);
							
							if (isMapaFarme) {
								int atalho = KeyMapper.getTeclaAtalho(this.skillsConfig.getAtalhoVeneno());
								bot.apertarTecla(atalho);
							}
						}
					}
					if (!gomaEncontrada && JanelaPrincipal.obterGoma() == true) {
						System.out.println("Tá sem goma e o botão de goma está ativo... Potando goma");
						int atalhoChicleteGoma = KeyMapper.getTeclaAtalho(this.skillsConfig.getAtalhoChicleteGoma());
						bot.apertarTecla(atalhoChicleteGoma);
						bot.sleep(500);
					}
					if (!velocidadeEncontrada && JanelaPrincipal.obterVelocidade() == true) {
						System.out.println("Tá sem velocidade e o botão de velocidade está ativo... Potando velocidade");
						int atalhoVelocidade = KeyMapper.getTeclaAtalho(this.skillsConfig.getAtalhoVelocidade());
						bot.apertarTecla(atalhoVelocidade);
						bot.sleep(500);
					}
				}
			}

			if (modoDesequiparEquips) {
				desequiparEquips();
			}

			// Verificar monstros antes de andar
			// notebook
			isBoss = script.getRotas().get(rota).getVerificacao().isBoss();
			if (isBoss == true) { // Boss encontrado
				processarLogicaMatarBoss();
			} else {
				monstros = bot.listaMonstros();

				if (verificarModoInstanciaProcura()) {
					List<MatOfPoint> monstrosAtrasParede = monstros.get("monstrosAtrasParede");

					// Set temporário para evitar duplicações
					Set<Coordenadas> coordenadasUnicas = new HashSet<>(coordsMonstrosAtrasParede);

					for (MatOfPoint monstro : monstrosAtrasParede) {
						Rect m = Imgproc.boundingRect(monstro);
						int centerX = m.x + m.width / 2;
						int centerY = m.y + m.height / 2 + 10;
						Coordenadas destinoBixo = bot.getCoordenadasTelaDoBixo(atual, centerX, centerY);

						Boolean isAndavel = mapaCarregado.get(destinoBixo);
						System.out.println("Coordenadas analisada x: " + destinoBixo.x + " y: " + destinoBixo.y + " | "
								+ isAndavel);

						if (isAndavel) {
							boolean podeAdicionar = true;

							for (Coordenadas coord : coordenadasUnicas) {
								int difX = Math.abs(destinoBixo.x - coord.x);
								int difY = Math.abs(destinoBixo.y - coord.y);
								if (difX <= 3 && difY <= 3) {
									podeAdicionar = false;
									break;
								}
							}
							if (podeAdicionar) {
								coordenadasUnicas.add(destinoBixo); // Adiciona no conjunto para garantir que não haja
																	// repetição
								coordsMonstrosAtrasParede.add(destinoBixo); // Adiciona na lista principal sem
																			// sobrescrever
							}
						}
					}

					// Exibir resultado formatado
					StringBuilder lista = new StringBuilder();
					for (Coordenadas c : coordsMonstrosAtrasParede) {
						lista.append(" (x: ").append(c.x).append(", y: ").append(c.y).append("), ");
					}
					System.out.println("Size: " + coordsMonstrosAtrasParede.size());
					System.out.println(lista);

				}

			}

			// Verificar se existem monstros visíveis
			if (!monstros.getOrDefault("rosa", List.of()).isEmpty()
					|| !monstros.getOrDefault("azul", List.of()).isEmpty()
					|| !monstros.getOrDefault("amarelo", List.of()).isEmpty()) {
				stateMachine.mudarEstado(Estado.ATACANDO);
			}

			if (andarForcado) {
				System.out.println("Andando Forçado!");
				stateMachine.mudarEstado(Estado.ANDANDO);
				if (System.currentTimeMillis() - tempoAndarForcado >= skillsConfig.getTempoAndarForcado()) {
					andarForcado = false;
				}
			}

			if (script.getMapa().equals("bio.png")) { // parte de cima de bio
				if ((atual.x >= 126 && atual.x <= 153) && (atual.y >= 257 && atual.y <= 265)) {
					saindoDeCimaBio = true;
				} else {
					saindoDeCimaBio = false;
				}
			}

			if (verificarSeLimpouOldGh) {
				if (System.currentTimeMillis() - tempoVerificacaoOldGH <= 2000) {
					System.out.println("Analisando Old GH");
					boolean areaLimpa = bot.detectarPixelsAmarelos(bot.getxJanela() + 222, bot.getyJanela() + 40, 658,
							183);
					System.out.println("Todos monstros estão mortos? " + areaLimpa);
					if (areaLimpa) {
						if (rota == 2) { // parte da esquerda
							System.out.println("Indo para o portal...");
							passo = script.getRotas().get(2).getPassos().size() - 1;
							verificarSeLimpouOldGh = false;
							coordsMonstrosAtrasParede.clear();
						} else if (rota == 3) { // parte da direita
							System.out.println("Indo para o portal...");
							passo = script.getRotas().get(3).getPassos().size() - 1;
							verificarSeLimpouOldGh = false;
							coordsMonstrosAtrasParede.clear();
						}
					}
				} else {
					tempoVerificacaoOldGH = System.currentTimeMillis();
					verificarSeLimpouOldGh = false;
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

				ultimaCoordenada = atual;

				// Lógica de andar
				if (personagemParado) {
					if (System.currentTimeMillis() - ultimoUpdateTela >= 5000) {
						//Pala não é pra refrashar a tela
						if (!JanelaPrincipal.obterClasseSelecionada().equals("pala")) {
							System.out.println("PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO ");
							System.out.println("PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO ");
							System.out.println("PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO ");
							System.out.println("PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO ");
							System.out.println("PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO ");
							System.out.println("PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO ");
							System.out.println("PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO ");
							System.out.println("PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO PERSONAGEM PARADO ");
							int refresh = skillsConfig.getRefresh();
							bot.atalhoAltM(refresh);
						}
						ultimoUpdateTela = System.currentTimeMillis(); // Atualiza o tempo da última chamada
						tempoCooldownRefresh = System.currentTimeMillis();
						aguardarCdRefresh = true;
						andar(script);
					} else {
						// Se a tela já foi atualizada recentemente, força o movimento
						moverParaDirecaoOposta(atual);
						personagemParado = false;
					}

					// moverParaDirecaoOposta(atual); // Força o movimento do personagem
					// personagemParado = false; // Reseta o estado após o movimento forçado
				} else {
					andar(script);
				}
				break;

			case ATACANDO:
				bot.soltarMouse();
				if (!monstros.getOrDefault("rosa", List.of()).isEmpty()
						|| !monstros.getOrDefault("azul", List.of()).isEmpty()
						|| !monstros.getOrDefault("amarelo", List.of()).isEmpty()) {
					atacar(monstros);

					// Verificar se matou todos os bixos de cada lado de old gh
					if (script.getMapa().equals("old_glast_heim.png") && passo >= 10 && (rota == 2 || rota == 3)) {
						System.out.println("Ja passou do passo 10 e ta na esquerda ou direita");
						verificarSeLimpouOldGh = true;
						tempoVerificacaoOldGH = System.currentTimeMillis();
					}
					
					if (!stateMachine.getEstadoAtual().equals(Estado.NPC)) {
						stateMachine.mudarEstado(Estado.ANDANDO);
					}
				}
				break;

			case NPC:
				bot.soltarMouse();
				System.out.println("Desligando andar forçado no case NPC");
				System.out.println("Desligando andar forçado no case NPC");
				System.out.println("Desligando andar forçado no case NPC");
				andarForcado = false;
				if (tentandoFalarComNpc) {
					System.out.println("Tentando falar com o npc...");
					falarComNpc();
					continue;
				}
				if (interagindoComNpc) {
					interagirComNpc(script);
					continue;
				}
				if (modoVoltarParaFarmar) {
					interagirComNpcParaVoltarFarmar();
					continue;
				}
				if (guardandoEquipsArmazem) {
					interagirComAKafra();
					continue;
				}
				if (pegarEquipsArmazem2) {
					interagirComAKafraRetirarEquips();
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
		/*
		 * try { GlobalScreen.unregisterNativeHook(); } catch (NativeHookException e) {
		 * e.printStackTrace(); }
		 */
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

			bot.sleep(100);// velocidade de atk

		} while ((System.currentTimeMillis() - startTimeBoss) < maxSearchTime || foundBoss);

		// Se saiu do loop, significa que não achou boss por 5 segundos
		if (script.getRotas().get(rota).getVerificacao().isTerminaNoBoss()) {
			System.out.println("Termina no chefe, indo para verificação de finalização de instancia");
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

	// notebook
	private void moverParaDirecaoOposta(Coordenadas atual) {
		int distancia = 5;

		// Gerar movimento em todas as direções
		int[] dx = { 0, distancia, -distancia, 0, distancia, -distancia, distancia, -distancia }; // Direções X
		int[] dy = { distancia, 0, 0, -distancia, distancia, -distancia, -distancia, distancia }; // Direções Y

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
		stateMachine.mudarEstado(Estado.NPC);
		String mapa = script.getMapa();
		if (mapa.equals("bio.png") || mapa.equals("chef.png")) { // mapas de farme
			bot.sleep(200);
			if (!verificarFullStrip) {
				System.out.println("Verificando se levou full strip...");
				List<Integer> status = bot.listarStatus();
				boolean isFullstrip = false;
				for (int buffs : status) {
					if (buffs == Effects.ARMA_REMOVIDO.getId() || buffs == Effects.ESCUDO_REMOVIDO.getId()
							|| buffs == Effects.ARMADURA_REMOVIDO.getId() || buffs == Effects.ELMO_REMOVIDO.getId()) {
						isFullstrip = true;
					}
				}
				System.out.println("Full strip status: " + isFullstrip);
				if (isFullstrip) {
					int hp = 999;
					do {
						int atalho = KeyMapper.getTeclaAtalho(this.skillsConfig.getAtalhoVeneno());
						bot.apertarTecla(atalho);
						bot.sleep(500);
						hp = bot.getHpAtual();
						bot.sleep(500);
					} while(hp > 1);
					System.out.println("Personagem morto...");
					int base = skillsConfig.getGoBase();
					bot.atalhoAltM(base);
					bot.sleep(4000);
				}
				verificarFullStrip = true;
			}
			if (!falouComCurandeiro) {
				System.out.println("Falando com o npc de cura");
				bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(231, 202));
				bot.sleep(100);
				bot.clicarMouse();
				bot.sleep(30);
				bot.clicarMouse();
				bot.sleep(100);
				
				List<Integer> status = bot.listarStatus();
				for (int buffs : status) {
					if (buffs == Effects.BLESSING.getId()) {
						System.out.println("Falou com a curandeira");
						falouComCurandeiro = true;
					}
				}
				
				if (!falouComCurandeiro) {
					return;
				}
				
				//Equipar os itens caso esteja quebrados ou desequipados
				int x = barraSkills.x + 4;
				int y = barraSkills.y + 18;
				boolean barra1 = true;
				do {
					System.out.println("Mudando pra segunda barra");
					BufferedImage barraVerificada = bot.printarParteTela(x, y, 8, 11);
					barra1 = bot.compararImagens(barra, barraVerificada, 2); //true barra1 | false barra2
					if (barra1 == true) {
						bot.sleep(100);
						bot.moverMouse(bot.getxJanela() + x + 4, bot.getyJanela() + y + 5);
						bot.sleep(100);
						bot.clicarMouse();
						bot.sleep(100);
						bot.moverMouse(bot.getxJanela() + bot.getWidth()/2, bot.getyJanela() + bot.getHeight()/2);
						bot.sleep(500);
					}
					System.out.println("barra1: " + barra1);
				} while(barra1 == true);
				
				System.out.println("Equipando os itens... e verificando a quantidade de pixel verde pra saber se equipou tudo...");
				int contador = 0;
				Color verde = new Color(0,255,8);
				int tentativas = 0;
				do {
					for (int i = 0; i < skillsConfig.getAtalhoEquipamento().size(); i++) {
						System.out.println("Equipando o " + (i + 1));
						int atalho = KeyMapper.getTeclaAtalho(this.skillsConfig.getAtalhoEquipamento().get(i));
						bot.apertarTecla(atalho);
						bot.sleep(200);
						tentativas++;
					}
					
					// Se atingir 3 tentativas, sai do loop
			        if (tentativas >= 3) {
			            System.out.println("Número máximo de tentativas atingido!");
			            break;
			        }
					
					contador = bot.contarPixels(verde, bot.getxJanela() + barraSkills.x, bot.getyJanela() + barraSkills.y, barraSkills.width, 34);
					System.out.println("Quantidades de verde: " + contador + " é maior que 7450? " + (contador < 7450 ? false:true));
				} while(contador < 7450);
				
				do {
					System.out.println("Mudando pra primeira barra");
					BufferedImage barraVerificada = bot.printarParteTela(x, y, 8, 11);
					barra1 = bot.compararImagens(barra, barraVerificada, 2); //true barra1 | false barra2
					if (barra1 == false) {
						bot.sleep(100);
						bot.moverMouse(bot.getxJanela() + x + 4, bot.getyJanela() + y + 5);
						bot.sleep(100);
						bot.clicarMouse();
						bot.sleep(100);
						bot.moverMouse(bot.getxJanela() + bot.getWidth()/2, bot.getyJanela() + bot.getHeight()/2);
						bot.sleep(500);
					}
				} while(barra1 == false);
				System.out.println("Equipamentos equipados com o sucesso de um equipador");
			}

			System.out.println("Tentando falar com npc teleporte");
			int base = skillsConfig.getGoBase();
			bot.atalhoAltM(base);
			bot.sleep(2000);
			bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(231, 205));
			bot.sleep(90);
			bot.clicarMouse();
			bot.sleep(1000);

			List<MatOfPoint> balao = bot.verificarBalaoNpcTeleport();
			if (!balao.isEmpty()) {
				tentandoFalarComNpc = false;
				falouComCurandeiro = false;
				verificarFullStrip = false;
				modoVoltarParaFarmar = true;
			}
			return;
		}

		if (JanelaPrincipal.instanciaRadioButton.isSelected() && (finalizandoInstancia || pegarEquipsArmazem)) {
			System.out.println("Tentando falar com a kafra!!!!!!!!11!");
			int base = skillsConfig.getGoBase();
			bot.atalhoAltM(base);
			bot.sleep(2000);
			bot.visaoDeCima();
			bot.zoom(-28);
			bot.sleep(1000);
			System.out.println("Tacando mouse na kafra, coordenadas atual: " + atual);
			bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(234, 204));
			bot.sleep(90);
			bot.clicarMouse();
			bot.sleep(1000);

			List<MatOfPoint> balao = bot.verificarBalaoNpcTeleport();
			if (!balao.isEmpty()) {
				tentandoFalarComNpc = false;
				finalizandoInstancia = false;
				guardandoEquipsArmazem = true;
				if (pegarEquipsArmazem) {
					pegarEquipsArmazem2 = true;
					pegarEquipsArmazem = false;
					guardandoEquipsArmazem = false;
				}
				System.out.println("Falou com a kafra");
				System.out.println("Método falarComNpc()");
			}
			return;
		}

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

	// notebook
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
				// rota++;
				// interagindoComNpc = false;
				// stateMachine.mudarEstado(Estado.ANDANDO);
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

	// notebook
	private void andar(Script script) {
		int distanciaMinima = 5; // Defina a distância mínima aceitável

		if (saindoDeCimaBio) {
			bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(135, 257));
			bot.sleep(100);
			bot.clicarMouse();
			return;
		}

		if (!coordsMonstrosAtrasParede.isEmpty() && verificarModoInstanciaProcura()) {// &&
																						// verificarModoInstanciaProcura()
			Coordenadas destinoBixo = coordsMonstrosAtrasParede.get(0);

			if (bot.calcularDistancia(atual, destinoBixo) <= distanciaMinima) {
				System.out.println("Chegou ao destino... removendo coord da lista!!!");
				coordsMonstrosAtrasParede.remove(0);
			} else {
				List<Coordenadas> caminhoAteOBixo = aStar.encontrarCaminho(grafo, atual, destinoBixo);
				Coordenadas coordDestino = bot.escolherProximaCoordenada(caminhoAteOBixo, atual);
				bot.moverPersonagem(atual, coordDestino, mapaCarregado);
				System.out.println("Indo até o monstro atrás da parede x: " + destinoBixo.x + " y: " + destinoBixo.y);
			}
			return;
		}
		// System.out.println("farm: " + farm + " | tamanho da lista: " +
		// this.listaDeFarmBioChef.size());
		System.out.println("Rota atual: " + script.getRotas().get(rota).getDescricao());

		// Processar verificação específica da rota
		processarVerificacao(script, distanciaMinima);

		Coordenadas destino = obterDestinoAtual(script);
		// System.out.println("Atual: " + atual + " | destino: " + destino);

		// caminhoAlternativo = aStar.calcularCaminhoComExpansao10(atual, destino,
		// grafo);
		caminhoCalculado = aStar.encontrarCaminho(grafo, atual, destino);
		
		//Modo BioChef
		//Só testando mesmo... Codigo adicionado pra nao fazer o mouse focar nas coordenadsa finais fazendo o personagem parar algumas vezes
		//Se ficar ruim so apagar
		/*if (script.getMapa().equals("bio.png") || script.getMapa().equals("chef.png")) {
		    if (caminhoCalculado.size() < 14) {
		        int passoAlt = passo + 1;
		        int rotaAlt = rota;

		        // Verifica se 'rota' está dentro dos limites
		        if (!script.getRotas().isEmpty() && rota < script.getRotas().size()) {
		            if (passoAlt >= script.getRotas().get(rota).getPassos().size()) {
		                passoAlt = 0;
		                rotaAlt += 1;
		                
		                // Garante que 'rotaAlt' esteja dentro dos limites
		                if (rotaAlt >= script.getRotas().size()) {
		                    rotaAlt = 0;
		                }
		            }

		            // Verifica se 'rotaAlt' está dentro dos limites e tem passos
		            if (rotaAlt < script.getRotas().size() && 
		                !script.getRotas().get(rotaAlt).getPassos().isEmpty() &&
		                passoAlt < script.getRotas().get(rotaAlt).getPassos().size()) {
		                
		                List<Integer> coordenadas = script.getRotas().get(rotaAlt).getPassos().get(passoAlt).getCoordenadas();

		                // Verifica se há pelo menos duas coordenadas
		                if (coordenadas.size() >= 2) {
		                    int xAlt = coordenadas.get(0);
		                    int yAlt = coordenadas.get(1);
		                    Coordenadas destinoAlt = new Coordenadas(xAlt, yAlt);
		                    caminhoCalculado = aStar.encontrarCaminho(grafo, atual, destinoAlt);
		                }
		            }
		        }
		    }
		}*/

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
		case "buffs":
			ativarBuff();
			break;
		default:
			System.out.println("Tipo de verificação desconhecido: " + verificacao);
		}
	}
	
	private void ativarBuff() {
		System.out.println("Buffando Buffando Buffando Buffando Buffando Buffando Buffando Buffando Buffando ");
		System.out.println("Buffando Buffando Buffando Buffando Buffando Buffando Buffando Buffando Buffando ");
		horaDeBuffar = true;
		bot.sleep(1000);
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
		// System.out.println("Verificando se está proximo de " + verificarCoordenadas +
		// " | " + (bot.calcularDistancia(atual, verificarCoordenadas) <=
		// distanciaMinima));

		// Diminuir a verificacao pra passar pra proxima rota ao passar no portal
		if (script.getMapa().equals("old_glast_heim.png") && ((passo == script.getRotas().get(2).getPassos().size() - 1)
				|| (passo == script.getRotas().get(3).getPassos().size() - 1))) {
			distanciaMinima = 3;
			System.out.println("Diminuindo range de verificacao pra passar no portal");
		}
		if (bot.calcularDistancia(atual, verificarCoordenadas) <= distanciaMinima) {
			System.out.println(
					"Rota aumentada de verdade pela verificacao do teleport : " + verificarX + " " + verificarY);
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
			/*
			 * int voltarMoroc = skillsConfig.getGomoroc(); bot.atalhoAltM(voltarMoroc);
			 * bot.sleep(5000); //Encerrar instancia bot.encerrarInstancia();
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

			System.out.println("voltando para base");
			bot.sleep(1000);
			//String path = "config/minimapas/base.png";
			int voltarBase = skillsConfig.getGoBase();
			//verificarSeMudouMapa(path, voltarBase);
			mudarMapa(Mapa.BASE.getNome(), voltarBase);
			System.out.println("Aguardando 3 segundos");
			bot.sleep(1000);
			System.out.println("Aguardando 2 segundos");
			bot.sleep(1000);
			System.out.println("Aguardando 1 segundo");

			System.out.println("Encerrando Instancia");
			bot.encerrarInstancia();
			System.out.println("Desequipando os equipamentos...");
			bot.sleep(2000);

			modoDesequiparEquips = true;
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

	private void deslogarPersonagemContaIniciarInstancia() {
		int ultimoIndexConta = indexConta;
		indexInstancia++;
		if (indexInstancia > scriptContas.getContas().get(indexConta).getPersonagens().get(indexPersonagem)
				.getInstancias().size() - 1) {
			indexInstancia = 0;

			indexPersonagem++;
			if (indexPersonagem > scriptContas.getContas().get(indexConta).getPersonagens().size() - 1) {
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

			if (ultimoIndexConta != indexConta) { // indexConta aumentou
				System.out.println("Indo tela login");
				bot.sleep(3000);
				bot.voltarTelaLogin();

				System.out.println("Logando");
				String usuario = scriptContas.getContas().get(indexConta).getUsuario();
				String senha = scriptContas.getContas().get(indexConta).getSenha();
				String pin = scriptContas.getContas().get(indexConta).getPin();
				bot.realizarLogin(usuario, senha);

				System.out.println("Apertando enter na escolha do canal 1");
				BufferedImage imagemTelaCanal = null;
				String canal = "config/telas/canal.png";
				try {
					imagemTelaCanal = ImageIO.read(new File(canal));
				} catch (IOException e) {
					e.printStackTrace(); // 377 571 280 29
				}
				boolean imagensIguais = false;
				do {
					BufferedImage atual = bot.printarParteTela(377, 571, 280, 29);
					imagensIguais = bot.compararImagens(atual, imagemTelaCanal, 30.0);
					System.out.println("Verificando imagens: " + imagensIguais);
					bot.sleep(500);
				} while (imagensIguais == false);
				bot.apertarTecla(KeyEvent.VK_UP);
				bot.sleep(500);
				bot.apertarTecla(KeyEvent.VK_UP);
				bot.sleep(500);
				bot.apertarTecla(KeyEvent.VK_UP);
				bot.sleep(500);
				bot.apertarTecla(KeyEvent.VK_ENTER);
				bot.sleep(5000);

				System.out.println("Chega na parte do pin...");
				bot.inserirPin(pin);
				bot.sleep(1000);
			}

			int indexPersonagem = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem)
					.getIndexPersonagem();
			int pagina = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem)
					.getPagina();// tava 0 0 por algum motivo
			bot.escolherPersonagem(indexPersonagem, pagina);
			String classe = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem)
					.getClasse();
			System.out.println("Carregando atalhos da classe: " + classe);
			carregarAtalhosSkills(classe);

			// Fechar Logue e Ganhe
			System.out.println("Fechando Logue e Ganhe");
			bot.moverMouse(bot.getxJanela() + 510, bot.getyJanela() + 567);
			bot.sleep(300);
			bot.clicarMouse();
			bot.sleep(300);

			// Fechar chat de npc
			System.out.println("Apertando enter 2x para fechar chat de npc");
			bot.apertarTecla(KeyEvent.VK_ENTER);
			bot.sleep(300);
			// Fechar chat de npc
			bot.apertarTecla(KeyEvent.VK_ENTER);
			bot.sleep(300);

			System.out.println("Visão topdown");
			bot.visaoDeCima();
			bot.sleep(100);
			bot.zoom(-28);

			pegarEquipsArmazem = true;
			tentandoFalarComNpc = true;
			stateMachine.mudarEstado(Estado.NPC);

		}

		// iniciarInstancia();
	}

	private void iniciarInstancia() {
		String instanciaScript = scriptContas.getContas().get(indexConta).getPersonagens().get(indexPersonagem)
				.getInstancias().get(indexInstancia) + ".json";
		ScriptLoader scriptLoader = new ScriptLoader();
		Script scriptTemp = scriptLoader.carregarScriptdoJson("instancias/" + instanciaScript);
		setScript(scriptTemp);
		carregarMapa();

		String instancia = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem)
				.getInstancias().get(indexInstancia);
		System.out.println("Executando instancia");
		bot.executarInstancia(instancia);
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
		// monstros azuis
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
			int centerX = m.x + m.width / 2;
			int centerY = m.y + m.height / 2 + 10;
			Coordenadas coordBixo = bot.getCoordenadasTelaDoBixo(atual, centerX, centerY);
			if (bot.compararCoordenadas(coordBixo, coordsUltimoMonstro)) {
				contagemRefresh++;
				int quantidadeMax = 3;
				if (JanelaPrincipal.obterClasseSelecionada().equals("pala")) {
					quantidadeMax = 15;
				}
				if (contagemRefresh > quantidadeMax) {
					contagemRefresh = 0;
					int refresh = skillsConfig.getRefresh();
					bot.atalhoAltM(refresh);
					tempoCooldownRefresh = System.currentTimeMillis();
					aguardarCdRefresh = true;
				}
			} else {
				contagemRefresh = 0;
				coordsUltimoMonstro = coordBixo;
			}
		}

		if (skillDisponivel == null && !JanelaPrincipal.instanciaRadioButton.isSelected()) {
			// andar(script);
			if (andarForcado) {
				return;
			}
			if (!stateMachine.getEstadoAtual().equals(Estado.NPC)) {
				System.out.println("Ativando andar forçado por 1 segundos!");
				andarForcado = true;
			}
			tempoAndarForcado = System.currentTimeMillis();
		}

	}

	private boolean usarHabilidade(MatOfPoint monstro, int tecla, Skill skill) {
		Rect m = Imgproc.boundingRect(monstro);
		int centerX = m.x + m.width / 2;
		int centerY = m.y + m.height / 2 + 10;
		Coordenadas coord = bot.getCoordenadasTelaDoBixo(atual, centerX, centerY);
		if (bot.calcularDistancia(atual, coord) <= skill.getRange()) {
			bot.atacarMonstro(monstro, tecla, skill.getSelfSkill());
			skill.use();
			if (skill.getMain() == true && script.getMapa().equals("chef.png")) {
				if (aguardarCdRefresh == false && verificarCooldownCometa(skill)) {
					System.out.println("Voltando base...");
					System.out.println("Voltando base...");
					System.out.println("Voltando base...");
					voltarBase();
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean verificarCooldownCometa(Skill skill) {
		String[] partes = skill.getPosicao().split("-");
		int barra = Integer.parseInt(partes[0]);
		int pos = Integer.parseInt(partes[1]);
			
		int x = barraSkills.x + 16 + (pos-1)*24 + (pos-1)*5;
		int y = barraSkills.y + 4 + (barra-1)*19 + (barra-1)*14;
			
		BufferedImage verificarCometa = bot.printarParteTela(x, y, 24, 19);
			
		if (bot.compararImagensCometa(cometa, verificarCometa, cometaPreto, 2)) {
			System.out.println("Não está em cooldown");
			return false;
		} else {
			System.out.println("Está em cooldown");
			System.out.println("Está em cooldown");
			System.out.println("Está em cooldown");
			System.out.println("Está em cooldown");
			try {
				File arquivo = new File("cometa.png");
				ImageIO.write(verificarCometa, "png", arquivo);
				System.out.println("Img cd cometa salva em: " + arquivo.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
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
		BufferedImage imagemTelaCanal = null;
		String path = "config/telas/canal.png";
		try {
			imagemTelaCanal = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace(); // 377 571 280 29
		}
		boolean imagensIguais = false;
		do {
			BufferedImage atual = bot.printarParteTela(377, 571, 280, 29);
			imagensIguais = bot.compararImagens(atual, imagemTelaCanal, 30.0);
			System.out.println("Verificando imagens: " + imagensIguais);
			bot.sleep(500);
		} while (imagensIguais == false);
		bot.apertarTecla(KeyEvent.VK_UP);
		bot.sleep(500);
		bot.apertarTecla(KeyEvent.VK_UP);
		bot.sleep(500);
		bot.apertarTecla(KeyEvent.VK_UP);
		bot.sleep(500);
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(10000);

		System.out.println("Chega na parte do pin...");
		bot.inserirPin(pin);
		bot.sleep(1000);

		int indexPersonagem = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem)
				.getIndexPersonagem();
		int pagina = scriptContas.getContas().get(0).getPersonagens().get(0).getPagina();
		bot.escolherPersonagem(indexPersonagem, pagina);
		String classe = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem).getClasse();
		carregarAtalhosSkills(classe);

		// Fechar Logue e Ganhe
		bot.moverMouse(bot.getxJanela() + 510, bot.getyJanela() + 567);
		bot.sleep(300);
		bot.clicarMouse();
		bot.sleep(300);

		// Fechar chat de npc
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(300);
		// Fechar chat de npc
		bot.apertarTecla(KeyEvent.VK_ENTER);
		bot.sleep(300);

		// Selecionar Instancias
		// Abrir Janela de instancias
		String instancia = scriptContas.getContas().get(indexConta).getPersonagens().get(this.indexPersonagem)
				.getInstancias().get(indexInstancia);
		//bot.executarInstancia(instancia);
		stateMachine.mudarEstado(Estado.NPC);
		tentandoFalarComNpc = true;
		pegarEquipsArmazem = true;
	}

	public void resetarRotas() {
		rota = 0;
		passo = 0;
		acoesNpc = 0;
		farm = 0;
	}

	public synchronized void pausarBot() {
		if (script.getMapa() == null) {
			return;
		}
		
		if (stateMachine.getEstadoAtual().equals(Estado.NPC)) {
			System.out.println("Apertou pause durante fase de npc... PARANDO STONKS!!!");
			pararBot();
			return;
		}

		System.out.println("pausarBot: " + pausarBot);
		pausarBot = !pausarBot;
		if (pausarBot) {
			System.out.println("Pausando o bot...");
		} else {
			System.out.println("Resumindo o bot...");
		}
		if (skillsConfig.getTela() == true) {
			tela.updateState(ligarBot, pausarBot);
		}
		bot.clicarMouse();

		// bot.soltarMouse();
		notify();
	}

	public void pararBot() {
		System.out.println("Tecla 'O' pressionada. Parando o bot...");
		ligarBot = false;
		

		resetarRotas();
		resetarVariaveisVoltarFarme();
		resetandoVariaveisDeFinalInstancia();

		if (pausarBot) {
			pausarBot();
		}
		if (skillsConfig.getTela() == true) {
			tela.updateState(ligarBot, pausarBot);
		}
		/*
		 * try { GlobalScreen.unregisterNativeHook(); } catch (NativeHookException e) {
		 * e.printStackTrace(); }
		 */
		// Se a thread estiver rodando, interrompê-la
		if (botThread != null && botThread.isAlive()) {
			botThread.interrupt();
		}
	}

	public void fecharBot() {
		ligarBot = false; // Interrompe o loop
		// if (tela != null) {
		// tela.dispose();
		// }
		System.out.println("Tecla 'F' pressionada. Parando o bot...");

		bot.clicarMouse();

		// bot.soltarMouse();
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
		}
		System.exit(0);
	}

	// notebook
	public void modoSalvarCoordenadas() {
		Coordenadas c = bot.obterCoordenadasMemoria();
		String coordenada = "{ \"coordenadas\": [" + c.x + ", " + c.y + "] },";
		coordenadasModoSalvar.add(coordenada);
		System.out.println("Coordenada salva: " + coordenada);
		tocarSom("audio.wav");
	}

	// notebook
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

	// notebook
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

	// notebook
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

	// notebook
	private boolean verificarModoInstanciaProcura() {
		String nomeMapa = script.getMapa();
		if (nomeMapa.equals("old_glast_heim.png")) {
			return true;
		}
		return false;
	}
	
	public void mudarMapa(String mapa, int altOpcao) {
		boolean mapasIguais = false;
		do {
			if (altOpcao != -1) {
				bot.atalhoAltM(altOpcao);
			}
			String mapaAtual = bot.obterMapa();
			mapasIguais = mapaAtual.equals(mapa);
			System.out.println("Mapa atual: " + mapaAtual + " | Mapa destino: " + mapa + " | Status: " + mapasIguais);
			bot.sleep(1000);
		} while(mapasIguais == false);
	}

	public boolean verificarSeMudouMapa(String path, int altOpcao) {
		BufferedImage imagemRef = null;
		try {
			imagemRef = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean imagensIguais = false;
		do {
			if (altOpcao != -1) {
				bot.atalhoAltM(altOpcao);
			}

			// Captura do minimapa atual
			Rectangle captureArea = new Rectangle(bot.getxJanela() + 880, bot.getyJanela() + 16, 128, 128);
			BufferedImage imagemAtual = bot.getRobot().createScreenCapture(captureArea);

			imagensIguais = bot.compararImagens(imagemRef, imagemAtual, 30.0);

			System.out.println("As imagens são similares? " + imagensIguais);
			bot.sleep(100);
		} while (imagensIguais == false);

		return true;
	}

	private void verificarSeBalaoNpcMudou(BufferedImage imagemBalaoAnterior, int x, int y, int width, int height) {
		bot.sleep(100);
		BufferedImage imagemBalaoAtual = null;
		boolean comparacao = true;
		do {
			List<MatOfPoint> balao = bot.verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				Rectangle captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width,
						m.height);
				imagemBalaoAtual = bot.getRobot().createScreenCapture(captureArea);
			} else {
				Rectangle captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAtual = bot.getRobot().createScreenCapture(captureArea);
			}
			comparacao = bot.compararBalaoNpc(imagemBalaoAnterior, imagemBalaoAtual);
			bot.sleep(100);
		} while (comparacao == true);
	}

	private void voltarBase() {
		int labirinto = skillsConfig.getLabirintovalk();
		//String path = "config/minimapas/valkiria.png";
		//verificarSeMudouMapa(path, labirinto);
		mudarMapa(Mapa.VALKIRIA.getNome(), labirinto);

		//path = "config/minimapas/base.png";
		int base = skillsConfig.getGoBase();
		//isBase = verificarSeMudouMapa(path, base);
		mudarMapa(Mapa.BASE.getNome(), base);
		bot.sleep(2000);
		// visao de cima
		bot.visaoDeCima();
		bot.sleep(100);
		bot.zoom(-28);
		bot.sleep(100);
		tentandoFalarComNpc = true;
		System.out.println("Desligando andar forçado no voltarBase");
		System.out.println("Desligando andar forçado no voltarBase");
		System.out.println("Desligando andar forçado no voltarBase");
		andarForcado = false;//as vezes ativava o andar forçado e o personagem não falava com o npc
		stateMachine.mudarEstado(Estado.NPC);
	}

	private void carregarOpcoesFarmNpc() {
		String mapa = script.getMapa();
		String dificuldade = JanelaPrincipal.obterDificuldadeSelecionada();
		String sala = JanelaPrincipal.obterSalaSelecionada();

		if (mapa.equals("bio.png")) {
			if (dificuldade.equals("normal")) {
				if (sala.equals("1")) {
					listaOpcoesFarmNpc = List.of(20, 1, 1, 1, 1);
				} else if (sala.equals("2")) {
					listaOpcoesFarmNpc = List.of(20, 1, 2, 1, 1);
				}
			} else if (dificuldade.equals("hard")) {
				if (sala.equals("1")) {
					listaOpcoesFarmNpc = List.of(20, 2, 1, 1, 1);
				} else if (sala.equals("2")) {
					listaOpcoesFarmNpc = List.of(20, 2, 2, 1, 1);
				}
			}
		}
		if (mapa.equals("chef.png")) {
			if (dificuldade.equals("normal")) {
				if (sala.equals("1")) {
					listaOpcoesFarmNpc = List.of(21, 1, 1, 1, 1, 1, 1);
				} else if (sala.equals("2")) {
					listaOpcoesFarmNpc = List.of(21, 1, 1, 2, 1, 1, 1);
				}
			} else if (dificuldade.equals("hard")) {
				if (sala.equals("1")) {
					listaOpcoesFarmNpc = List.of(21, 1, 2, 1, 1, 1, 1);
				} else if (sala.equals("2")) {
					listaOpcoesFarmNpc = List.of(21, 1, 2, 2, 1, 1, 1);
				}
			}
		}

		System.out.println("Lista de opcao de chef bio: " + listaOpcoesFarmNpc.toString());

	}

	private void aumentarIndex() {
		indexVoltarFarm++;
		ultimoIndex = indexVoltarFarm;
		if (indexVoltarFarm >= listaOpcoesFarmNpc.size()) {
			indexVoltarFarm = 99;
		}
		System.out.println("Index aumentando de " + ultimoIndex + " para: " + indexVoltarFarm);
	}

	private void interagirComNpcParaVoltarFarmar() {
		bot.sleep(200);
		System.out.println("--------------");
		System.out.println("Interagindo com npc...");
		System.out.println("Index: " + indexVoltarFarm);
		List<MatOfPoint> balao = bot.verificarBalaoNpcTeleport();

		int opcao = 0;
		if (indexVoltarFarm < listaOpcoesFarmNpc.size() - 1) {
			opcao = listaOpcoesFarmNpc.get(indexVoltarFarm);
		}
		System.out.println("opcao escolhida: " + opcao);
		System.out.println("Balao size? " + balao.size());

		if (indexVoltarFarm == 0) {
			bot.selecionarOpcao(opcao);
			aumentarIndex();
			ultimoIndex = 0;
			jaEscolheuPrimeiraOpcao = true;
			System.out.println("Escolheu a opcao de chefenia;");
			System.out.println("Ja escolheu a primeira opcao? " + jaEscolheuPrimeiraOpcao);
			return;
		}

		if (script.getMapa().equals("bio.png") && indexVoltarFarm == 1) {
			bot.selecionarOpcao(opcao);
			aumentarIndex();
			ultimoIndex = 0;
			jaEscolheuSegundaBioOpcao = true;
			System.out.println("Escolheu a opcao de chefenia;");
			System.out.println("Ja escolheu a primeira opcao? " + jaEscolheuPrimeiraOpcao);
			return;
		}

		if (jaEscolheuPrimeiraOpcao == false) {
			System.out.println("Valor da primeira opcao: " + jaEscolheuPrimeiraOpcao);
			System.out.println("Retornando");
			return;
		}
		if (script.getMapa().equals("bio.png") && jaEscolheuSegundaBioOpcao == false) {
			System.out.println("Valor da segunda opcao: " + jaEscolheuSegundaBioOpcao);
			System.out.println("Retornando");
			return;
		}

		if (balao.size() == 1) {
			if (indexVoltarFarm == 99) {
				bot.apertarTecla(KeyEvent.VK_ENTER);
				return;
			}
			bot.apertarTecla(KeyEvent.VK_ENTER);
			ultimoIndex = 0;
			System.out.println("Dando enter");
		} else if (balao.size() == 2) {
			if (indexVoltarFarm != ultimoIndex) {
				aumentarIndex();
			}
			bot.selecionarOpcao(opcao);
			System.out.println("balao de 2 opcoes escolhido");
		} else if (indexVoltarFarm == 99 && balao.isEmpty()) {
			System.out.println("Acabou o dialogo");
			String path = "";
			String dificuldade = JanelaPrincipal.obterDificuldadeSelecionada();
			String sala = JanelaPrincipal.obterSalaSelecionada();
			String mapa = "";
			if (script.getMapa().equals("bio.png")) {
				if (dificuldade.equals("hard")) {
					if (sala.equals("1")) {
						mapa = Mapa.BIOHARD1.getNome();
					} else if (sala.equals("2")) {
						mapa = Mapa.BIOHARD2.getNome();
					}
				} else if (dificuldade.equals("normal")) {
					if (sala.equals("1")) {
						mapa = Mapa.BIONORMAL1.getNome();
					} else if (sala.equals("2")) {
						mapa = Mapa.BIONORMAL2.getNome();
					}
				}
				//path = "config/minimapas/bio.png";
				//verificarSeMudouMapa(path, -1);
				mudarMapa(mapa, -1);
				bot.sleep(1000);
				bot.visaoDeCima();
				bot.zoom(-28);
				resetarVariaveisVoltarFarme();
			}
			if (script.getMapa().equals("chef.png")) {
				if (dificuldade.equals("hard")) {
					if (sala.equals("1")) {
						mapa = Mapa.CHEFHARD1.getNome();
					} else if (sala.equals("2")) {
						mapa = Mapa.CHEFHARD2.getNome();
					}
				} else if (dificuldade.equals("normal")) {
					if (sala.equals("1")) {
						mapa = Mapa.CHEFNORMAL1.getNome();
					} else if (sala.equals("2")) {
						mapa = Mapa.CHEFNORMAL2.getNome();
					}
				}
				//path = "config/minimapas/chef.png";
				//verificarSeMudouMapa(path, -1);
				mudarMapa(mapa, -1);
				bot.sleep(1000);
				bot.visaoDeCima();
				bot.zoom(-28);
				resetarVariaveisVoltarFarme();
			}
		} else if (balao.size() == 0) {
			contagemBalaoSizeZero++;
			System.out.println("Deu balão vazio... " + contagemBalaoSizeZero + "/30");
			System.out.println("Se passar de 30 tentativas, voltar base e iniciar de novo");
			if (contagemBalaoSizeZero == 30) {
				System.out.println("Bateu as 30 tentativas...\n Voltando Base");
				boolean isBase = false;
				do {
					int base = skillsConfig.getGoBase();
					bot.atalhoAltM(base);
					bot.sleep(2000);
					isBase = bot.compararCoordenadas(new Coordenadas(242,211), bot.obterCoordenadasMemoria());
				} while (isBase == false);
				resetarVariaveisVoltarFarme();
				System.out.println("Resetando pra falar com os npcs de novo...");
				tentandoFalarComNpc = true;
				stateMachine.mudarEstado(Estado.NPC);
			}
		}

	}

	public void resetarVariaveisVoltarFarme() {
		resetarRotas();
		modoVoltarParaFarmar = false;
		falouComCurandeiro = false;
		tentandoFalarComNpc = false;
		interagindoComNpc = false;
		indexVoltarFarm = 0;
		ultimoIndex = 0;
		jaEscolheuSegundaBioOpcao = false;
		jaEscolheuPrimeiraOpcao = false;
		contagemBalaoSizeZero = 0;
		stateMachine.mudarEstado(Estado.ANDANDO);
	}

	public void voltarParaFarmar(String mapa) {
		resetarRotas();

		String dificuldade = JanelaPrincipal.obterDificuldadeSelecionada();
		String sala = JanelaPrincipal.obterSalaSelecionada();
		// Voltar para labirinto valk
		int labirinto = skillsConfig.getLabirintovalk();
		String path = "config/minimapas/valkiria.png";
		verificarSeMudouMapa(path, labirinto);

		// Voltar para morroc
		path = "config/minimapas/base.png";
		int base = skillsConfig.getGoBase();
		verificarSeMudouMapa(path, base);
		// visao de cima
		bot.visaoDeCima();
		bot.sleep(100);
		bot.zoom(-28);
		bot.sleep(100);
		// Clicar pra curar
		bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(231, 202));
		bot.sleep(100);
		bot.clicarMouse();
		bot.sleep(30);
		bot.clicarMouse();
		bot.sleep(100);
		// Falar com teleporte
		bot.atalhoAltM(base);
		bot.sleep(2000);
		List<MatOfPoint> balao = new ArrayList<MatOfPoint>();
		BufferedImage imagemBalaoAnterior = null;
		Rectangle captureArea = null;
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;
		bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(231, 205));
		bot.sleep(90);
		bot.clicarMouse();
		do {
			balao = bot.verificarBalaoNpcTeleport();
			System.out.println("Tamanho balao: " + balao.size());
			System.out.println("Balao de npc detectado?: " + (balao.size() > 0 ? true : false));

			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				x = m.x;
				y = m.y;
				width = m.width;
				height = m.height;
				captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			}
			bot.sleep(500);
		} while (balao.isEmpty());
		// bot.sleep(4000);
		if (mapa.equals("bio")) {
			imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(20, x, y, width, height);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			if (dificuldade.equals("normal")) {
				imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			} else if (dificuldade.equals("hard")) {
				imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(2, x, y, width, height);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			}
			// bot.sleep(2000);
			if (sala.equals("1")) {
				imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			} else if (sala.equals("2")) {
				imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(2, x, y, width, height);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			}

			// bot.sleep(2000);
			balao = bot.verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width, m.height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			} else {
				captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			}
			bot.apertarTecla(KeyEvent.VK_ENTER);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			balao = bot.verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width, m.height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			} else {
				captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			}
			bot.apertarTecla(KeyEvent.VK_ENTER);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			bot.sleep(1000);
			// bot.sleep(2000);
			bot.apertarTecla(KeyEvent.VK_ENTER);
			path = "config/minimapas/bio.png";
			verificarSeMudouMapa(path, -1);
			// bot.sleep(5000);
			bot.sleep(1000);
			bot.visaoDeCima();
			bot.zoom(-28);
			// ir pro portal
			bot.setarMouseEmCoordenadaTela(bot.obterCoordenadasMemoria(), new Coordenadas(135, 257));
			bot.sleep(3000);
			bot.clicarMouse();
		}
		if (mapa.equals("chef")) {
			imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(21, x, y, width, height);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			balao = bot.verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width, m.height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			} else {
				captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			}
			bot.apertarTecla(KeyEvent.VK_ENTER);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			if (dificuldade.equals("normal")) {
				imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			} else if (dificuldade.equals("hard")) {
				imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(2, x, y, width, height);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			}

			// bot.sleep(2000);
			balao = bot.verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width, m.height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			} else {
				captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			}
			bot.apertarTecla(KeyEvent.VK_ENTER);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			if (sala.equals("1")) {
				imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			} else if (sala.equals("2")) {
				imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(2, x, y, width, height);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			}

			// bot.sleep(2000);
			balao = bot.verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width, m.height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			} else {
				captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			}
			bot.apertarTecla(KeyEvent.VK_ENTER);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			balao = bot.verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width, m.height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			} else {
				captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			}
			bot.apertarTecla(KeyEvent.VK_ENTER);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			imagemBalaoAnterior = bot.selecionarOpcaoComRetorno(1, x, y, width, height);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			// bot.sleep(2000);
			balao = bot.verificarBalaoNpcTeleport();
			if (balao.size() > 0) {
				Rect m = Imgproc.boundingRect(balao.get(0));
				captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width, m.height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			} else {
				captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
				imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
			}
			bot.apertarTecla(KeyEvent.VK_ENTER);
			verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);

			if (sala.equals("1")) {
				balao = bot.verificarBalaoNpcTeleport();
				if (balao.size() > 0) {
					Rect m = Imgproc.boundingRect(balao.get(0));
					captureArea = new Rectangle(bot.getxJanela() + m.x, bot.getyJanela() + m.y, m.width, m.height);
					imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
				} else {
					captureArea = new Rectangle(bot.getxJanela() + x, bot.getyJanela() + y, width, height);
					imagemBalaoAnterior = bot.getRobot().createScreenCapture(captureArea);
				}
				bot.apertarTecla(KeyEvent.VK_ENTER);
				verificarSeBalaoNpcMudou(imagemBalaoAnterior, x, y, width, height);
			}
			bot.sleep(1000);

			// bot.sleep(2000);
			bot.apertarTecla(KeyEvent.VK_ENTER);
			bot.sleep(100);
			bot.apertarTecla(KeyEvent.VK_ENTER);
			path = "config/minimapas/chef.png";
			verificarSeMudouMapa(path, -1);
			// bot.sleep(5000);
			bot.sleep(1000);
			bot.visaoDeCima();
			bot.zoom(-28);
		}

	}

	public void interagirComAKafraRetirarEquips() {

		bot.sleep(200);
		System.out.println("passosInteragirKafraRemoverItens: " + passosInteragirKafraRemoverItens);

		if (passosInteragirKafraRemoverItens == 0 || passosInteragirKafraRemoverItens == 1) {
			System.out.println("Falando com a kafra pra remover os itens");
			List<MatOfPoint> balao = bot.verificarBalaoNpcTeleport();
			bot.sleep(200);

			if (balao.size() == 1 || passosInteragirKafraRemoverItens == 1) {
				bot.apertarTecla(KeyEvent.VK_ENTER);
			}
			if (balao.size() == 2) {
				bot.selecionarOpcao(2);
				passosInteragirKafraRemoverItens = 1;
			}
			
			if (balao.isEmpty()) {
				passosInteragirKafraRemoverItens = 2;
			}
		}

		if (passosInteragirKafraRemoverItens == 2) {
			System.out.println("Procurando o armazém pra remover os itens");
			rectArmazem = bot.getArmazem();
			if (rectArmazem != null) {
				System.out.println("Armazém encontrado pra remover os itens");
				passosInteragirKafraRemoverItens = 3;
			}
		}

		if (passosInteragirKafraRemoverItens == 3) {
			System.out.println("Abrindo inventario pra remover os itens");
			bot.getRobot().keyPress(KeyEvent.VK_ALT);
			bot.sleep(50);
			bot.getRobot().keyPress(KeyEvent.VK_E);
			rectInventario = bot.getInventario();
			bot.sleep(200);
			if (rectInventario != null) {
				System.out.println("Inventario aberto pra remover os itens!!!");
				bot.getRobot().keyRelease(KeyEvent.VK_ALT);
				bot.sleep(50);
				bot.getRobot().keyRelease(KeyEvent.VK_E);
				passosInteragirKafraRemoverItens = 4;
			}
		}

		if (passosInteragirKafraRemoverItens == 4) {
			System.out.println("Retirando itens do armazém");
			bot.removerItensArmazem(rectArmazem);

			System.out.println("Fechando o armazém");
			int x = rectArmazem.x + 241;
			int y = rectArmazem.y + 428;
			bot.moverMouse(bot.getxJanela() + x, bot.getyJanela() + y);
			bot.sleep(100);
			bot.clicarMouse();
			bot.sleep(100);

			System.out.println("Equipando os itens");
			bot.equipandoItens(rectInventario);

			bot.sleep(100);
			System.out.println("Fechando o inventário");
			x = rectInventario.x + rectInventario.width - 8;
			y = rectInventario.y - 10;
			bot.moverMouse(bot.getxJanela() + x, bot.getyJanela() + y);
			bot.sleep(100);
			bot.clicarMouse();
			bot.sleep(100);

			passosInteragirKafraRemoverItens = 0;
			pegarEquipsArmazem2 = false;
			stateMachine.mudarEstado(Estado.ANDANDO);
			resetandoVariaveisDeFinalInstancia();
			iniciarInstancia();
		}

	}

	public void resetandoVariaveisDeFinalInstancia() {
		finalizandoInstancia = false;
		guardandoEquipsArmazem = false;
		passosInteragirKafra = 0;
		stateMachine.mudarEstado(Estado.ANDANDO);
		modoDesequiparEquips = false;
		passosDesequiparEquips = 0;
		pegarEquipsArmazem = false;
		pegarEquipsArmazem2 = false;
		passosInteragirKafraRemoverItens = 0;
	}

	public void interagirComAKafra() {
		// passosInteragirKafra = 0;
		bot.sleep(200);
		System.out.println("passosInteragirKafra: " + passosInteragirKafra);
		
		if (passosInteragirKafra == 0 || passosInteragirKafra == 1) {
			System.out.println("Falando com a kafra");
			List<MatOfPoint> balao = bot.verificarBalaoNpcTeleport();
			System.out.println("O balão está vazio? " + balao.isEmpty() + " Size: " + balao.size());
			System.out.println("Estado: " + stateMachine.getEstadoAtual());
			bot.sleep(200);

			if (balao.size() == 1 || passosInteragirKafra == 1) {
				bot.apertarTecla(KeyEvent.VK_ENTER);
			}
			if (balao.size() == 2) {
				bot.selecionarOpcao(2);
				passosInteragirKafra = 1;
			} 
			
			if (balao.isEmpty()) {
				passosInteragirKafra = 2;
			}
		}

		if (passosInteragirKafra == 2) {
			System.out.println("Procurando o armazém");
			rectArmazem = bot.getArmazem();
			if (rectArmazem != null) {
				System.out.println("Armazém encontrado");
				passosInteragirKafra = 3;
			}
		}

		if (passosInteragirKafra == 3) {
			System.out.println("Abrindo inventario");
			bot.getRobot().keyPress(KeyEvent.VK_ALT);
			bot.sleep(50);
			bot.getRobot().keyPress(KeyEvent.VK_E);
			rectInventario = bot.getInventario();
			bot.sleep(200);
			if (rectInventario != null) {
				System.out.println("Inventario aberto!!!");
				bot.getRobot().keyRelease(KeyEvent.VK_ALT);
				bot.sleep(50);
				bot.getRobot().keyRelease(KeyEvent.VK_E);
				passosInteragirKafra = 4;
			}
		}

		if (passosInteragirKafra == 4) {
			System.out.println("Passando itens para o armazém");
			bot.guardarItensArmazem(rectInventario);
			bot.sleep(100);
			System.out.println("Fechando o inventário");
			int x = rectInventario.x + rectInventario.width - 8;
			int y = rectInventario.y - 10;
			bot.moverMouse(bot.getxJanela() + x, bot.getyJanela() + y);
			bot.sleep(100);
			bot.clicarMouse();
			bot.sleep(100);

			passosInteragirKafra = 0;
			guardandoEquipsArmazem = false;
			deslogarPersonagemContaIniciarInstancia();
		}

	}

	public void desequiparEquips() {
		// desequipar alt q
		// falar com a kafra
		// abrir armazem
		// guardar os itens no armazem
		// deslogarPersonagemContaInicarInstancia() mas ao logar o personagem antes de
		// iniciar a instancia, abrir o armazem, pegar os itens, equipar os itens...

		if (passosDesequiparEquips == 0) {
			System.out.println("Abrir alt Q");
			bot.getRobot().keyPress(KeyEvent.VK_ALT);
			bot.sleep(50);
			bot.getRobot().keyPress(KeyEvent.VK_Q);
			rectAltQ = bot.getAltQ();
			if (rectAltQ != null) {
				System.out.println("Alt Q aberto!!!");
				bot.getRobot().keyRelease(KeyEvent.VK_ALT);
				bot.sleep(50);
				bot.getRobot().keyRelease(KeyEvent.VK_Q);
				passosDesequiparEquips = 1;
			}
		}

		if (passosDesequiparEquips == 1) {
			System.out.println("Clicando no botao de desequipar Hehe boy");
			bot.sleep(100);
			int x = rectAltQ.x + 253;
			int y = rectAltQ.y + 159;
			bot.moverMouse(bot.getxJanela() + x, bot.getyJanela() + y);
			bot.sleep(100);
			bot.clicarMouse();
			bot.sleep(100);
			System.out.println("Clicando no botao de especial");
			x = rectAltQ.x + 95;
			y = rectAltQ.y + 10;
			bot.moverMouse(bot.getxJanela() + x, bot.getyJanela() + y);
			bot.sleep(100);
			bot.clicarMouse();
			bot.sleep(100);
			System.out.println("Clicando no botao de desequipar Hehe boy");
			x = rectAltQ.x + 253;
			y = rectAltQ.y + 159;
			bot.moverMouse(bot.getxJanela() + x, bot.getyJanela() + y);
			bot.sleep(100);
			bot.clicarMouse();
			bot.sleep(100);
			System.out.println("Clicando no botao de fechar o Alt Q");
			x = rectAltQ.x + 273;
			y = rectAltQ.y - 11;
			bot.moverMouse(bot.getxJanela() + x, bot.getyJanela() + y);
			bot.sleep(100);
			bot.clicarMouse();
			bot.sleep(100);
			passosDesequiparEquips = 0;
			modoDesequiparEquips = false;
			stateMachine.mudarEstado(Estado.NPC);
			tentandoFalarComNpc = true;
			finalizandoInstancia = true;
		}

	}
	
	public String getNomeMapa() {
		String mapa = "";
		String dificuldade = JanelaPrincipal.obterDificuldadeSelecionada();
		String sala = JanelaPrincipal.obterSalaSelecionada();
		switch (script.getMapa()) {
		case "chef.png":
			if (dificuldade.equals("hard")) {
				if (sala.equals("1")) {
					mapa = Mapa.CHEFHARD1.getNome();
				} else if (sala.equals("2")) {
					mapa = Mapa.CHEFHARD2.getNome();
				}
			} else if (dificuldade.equals("normal")) {
				if (sala.equals("1")) {
					mapa = Mapa.CHEFNORMAL1.getNome();
				} else if (sala.equals("2")) {
					mapa = Mapa.CHEFNORMAL2.getNome();
				}
			}
			break;
		case "bio.png":
			if (dificuldade.equals("hard")) {
				if (sala.equals("1")) {
					mapa = Mapa.BIOHARD1.getNome();
				} else if (sala.equals("2")) {
					mapa = Mapa.BIOHARD2.getNome();
				}
			} else if (dificuldade.equals("normal")) {
				if (sala.equals("1")) {
					mapa = Mapa.BIONORMAL1.getNome();
				} else if (sala.equals("2")) {
					mapa = Mapa.BIONORMAL2.getNome();
				}
			}
			break;
		}
		return mapa;
	}
	
	private boolean estaEmMapaDeFarm(String mapaMemoria) {
	    return mapaMemoria.equals(Mapa.CHEFHARD1.getNome()) ||
	           mapaMemoria.equals(Mapa.CHEFHARD2.getNome()) ||
	           mapaMemoria.equals(Mapa.CHEFNORMAL1.getNome()) ||
	           mapaMemoria.equals(Mapa.CHEFNORMAL2.getNome()) ||
	           mapaMemoria.equals(Mapa.BIOHARD1.getNome()) ||
	           mapaMemoria.equals(Mapa.BIOHARD2.getNome()) ||
	           mapaMemoria.equals(Mapa.BIONORMAL1.getNome()) ||
	           mapaMemoria.equals(Mapa.BIONORMAL2.getNome());
	}
	
	public void iniciarBotQualquerLugar() {
		String mapa = getNomeMapa();
		String mapaMemoria = bot.obterMapa();
		System.out.println("Verificando mapa atual: " + mapaMemoria + " e mapa de destino: " + mapa);
		
		if (!mapa.equals(mapaMemoria)) {
			 // Se estiver em um mapa de farm, vá para Valkiria primeiro
	        if (estaEmMapaDeFarm(mapaMemoria)) {
	            System.out.println("Saindo do mapa de farm e indo para Valkiria...");
	            boolean isValk = false;
	            do {
	            	int valkiria = skillsConfig.getLabirintovalk();
		            bot.atalhoAltM(valkiria);
		            if (bot.obterMapa().equals(Mapa.VALKIRIA.getNome())) {
		            	isValk = true;
		            }
		            bot.sleep(2000);
	            } while(isValk == false);
	        }
			System.out.println("Começando bot a partir da base...");
			Coordenadas cordsBase = new Coordenadas(242, 211);
			int base = skillsConfig.getGoBase();
			boolean mesmaCoords = false;
			do {
				bot.atalhoAltM(base);
				mesmaCoords = bot.compararCoordenadas(cordsBase, bot.obterCoordenadasMemoria());
				System.out.println("Ja está na base? " + mesmaCoords);
				bot.sleep(2000);
			} while(mesmaCoords == false);
			
			tentandoFalarComNpc = true;
			andarForcado = false;
			stateMachine.mudarEstado(Estado.NPC);
		}
	}

	public void carregarAtalhosSkills(String classe) {
		bot.skills.clear();
		bot.buffs.clear();
		for (Classes c : skillsConfig.getClasses()) {
			if (c.getClasse().equals(classe)) {
				for (Skills sk : c.getSkills()) {
					int teclaAtalho = KeyMapper.getTeclaAtalho(sk.getAtalho());
					Boolean main = false;
					Boolean selfSkill = false;
					if (sk.isMain() != null) {
						main = sk.isMain();
					}
					if (sk.getSelfSkill() != null) {
						selfSkill = sk.getSelfSkill();
					}
					bot.skills.add(new Skill(teclaAtalho, sk.getCor(), sk.getCd(), sk.getRange(), sk.getPosicao(), main, selfSkill));
				}
				if (c.getBuffs() != null) {
					for (Buffs b : c.getBuffs()) {
						int teclaAtalho = KeyMapper.getTeclaAtalho(b.getAtalho());
						bot.buffs.add(new Buff(teclaAtalho, b.getCd(), b.isSelf()));
					}
				}

				break;
			}
		}
	}
	
	
	
	/*
	 * //notebook
	 * 
	 * @Override public void nativeKeyPressed(NativeKeyEvent e) { // Verifica se as
	 * teclas CTRL e SHIFT estão pressionadas boolean isCtrlPressed =
	 * (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0; boolean isShiftPressed =
	 * (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;
	 * 
	 * // Ctrl + Shift + F if (isCtrlPressed && isShiftPressed && e.getKeyCode() ==
	 * NativeKeyEvent.VC_F) { fecharBot(); }
	 * 
	 * // Ctrl + Shift + P if (isCtrlPressed && isShiftPressed && e.getKeyCode() ==
	 * NativeKeyEvent.VC_P) { pausarBot(); }
	 * 
	 * // Ctrl + Shift + O if (isCtrlPressed && isShiftPressed && e.getKeyCode() ==
	 * NativeKeyEvent.VC_O) { pararBot(); }
	 * 
	 * // Ctrl + Shift + S if (isCtrlPressed && isShiftPressed && e.getKeyCode() ==
	 * NativeKeyEvent.VC_S) { modoSalvarCoordenadas(); } // Ctrl + Shift + D if
	 * (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_D) {
	 * modoFecharCoordenadas(); } }
	 * 
	 * @Override public void nativeKeyReleased(NativeKeyEvent e) { // Não usado }
	 * 
	 * @Override public void nativeKeyTyped(NativeKeyEvent e) { // Não usado }
	 */

	
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