package com.ragnarokbot.telas;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.ragnarokbot.bot.Bot;
import com.ragnarokbot.main.GameController;
import com.ragnarokbot.model.MemoryScanner;
import com.sun.jna.platform.win32.User32;

import config.ContasConfig;
import config.Script;
import config.ScriptLoader;
import config.SkillsConfig;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import utils.Atalho;
import utils.KeyMapper;

public class JanelaPrincipal extends JFrame  implements NativeKeyListener {

	private static final long serialVersionUID = 1L;
    private List<String> sequenciaBioChef = new ArrayList<>();
    private JLabel sequenciaLabel;
    private static JLabel timeLabel;
    private GameController gameController;
    public static JRadioButton instanciaRadioButton;
    private JRadioButton farmRadioButton;
    private JList<String> instanceList;
    private Thread botThread;
    private int pauseCounter = 0;
    private JComboBox<String> comboBox; // seleção de processos
    private JPanel rightPanel;
    private SkillsConfig config;
    private Map<String, Atalho> atalhos = new HashMap<>();

    public JanelaPrincipal(GameController gameController) {
    	System.out.println("Criando JanelaPrincipal");
        setTitle("Stonks");
        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Evita o fechamento padrão
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false); // Janela não redimensionável
        setLayout(new BorderLayout());
        
        
        try {
			// Registrar o hook do teclado
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        
        ScriptLoader scriptLoader = new ScriptLoader();
        config = scriptLoader.carregarSkills("config/config_skills.json");
        carregarAtalhos();
        
        this.gameController = gameController;
        
        

        // Adiciona a imagem de fundo
        try {
            ImageIcon backgroundImage = new ImageIcon("config/img.png");
            JLabel background = new JLabel(backgroundImage);
            background.setLayout(new BorderLayout());
            add(background);

            // Painel para os botões no topo
            JPanel topPanel = createTopPanel();
            background.add(topPanel, BorderLayout.NORTH);

            // Painel abaixo dos botões
            JPanel belowTopPanel = createBelowTopPanel();
            background.add(belowTopPanel, BorderLayout.CENTER);
            
            // Label na parte inferior para mostrar a lista sequenciaBioChef
            sequenciaLabel = new JLabel("Sequência: ");
            sequenciaLabel.setOpaque(true);
            sequenciaLabel.setBackground(Color.WHITE);
            sequenciaLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            sequenciaLabel.setHorizontalAlignment(SwingConstants.LEFT);
            sequenciaLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            sequenciaLabel.setPreferredSize(new Dimension(0, 30));
            background.add(sequenciaLabel, BorderLayout.SOUTH);
            
            createFolderIfNotExists("mapas");
            createFolderIfNotExists("contas");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar a imagem de fundo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                    JanelaPrincipal.this,
                    "Você tem certeza que deseja fechar o bot?",
                    "Confirmação",
                    JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                	try {
            			GlobalScreen.unregisterNativeHook();
            		} catch (NativeHookException f) {
            			f.printStackTrace();
            		}
                    gameController.fecharBot();
                }
            }
        });
        
        String UUID = obterUUID();
        System.out.println("UUID: " + UUID);
        boolean permitir = false;
        List<String> uuidPermitidos = Stream.of(
        		"4C4C4544-0059-3410-8036-B6C04F593533", //Notebook meu
        		"23A12DC8-7866-11E7-6895-641C6789B626", //Notebook do lucas
        		"DAB38BF6-3E48-26F2-D1BA-7C10C942BDF7", //PC de Tulete47
        		"Mais ids..."
        		).collect(Collectors.toList());
        for (String id : uuidPermitidos) {
        	if (id.equals(UUID)) {
        		permitir = true;
        	}
        }
        if (!permitir) {
        	JOptionPane.showMessageDialog(
        		    JanelaPrincipal.this,
        		    "Computador não registrado!",
        		    "Erro",
        		    JOptionPane.ERROR_MESSAGE
        		);
	        	try {
	    			GlobalScreen.unregisterNativeHook();
	    		} catch (NativeHookException f) {
	    			f.printStackTrace();
	    		}
        		gameController.fecharBot();
        }
        

    }

    private JPanel createTopPanel() {
    	JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        // Painel com ComboBox e Botão para atualizar os arquivos
        JPanel topLeftPanel = new JPanel(new BorderLayout());
        
        // ComboBox à esquerda
        comboBox = new JComboBox<>();
        populateComboBoxWithRagnarokProcesses();

        comboBox.addActionListener(e -> {
            // Quando um processo for selecionado no ComboBox
            String selectedItem = (String) comboBox.getSelectedItem();
            if (selectedItem != null) {
                int selectedPid = Integer.parseInt(selectedItem.split(" - ")[0]);
                MemoryScanner.processId = selectedPid;
                System.out.println("Selecionou o " + MemoryScanner.processId);
            }
        });
        topLeftPanel.add(comboBox, BorderLayout.WEST);

        // Botão à direita
        JButton updateButton = new JButton();
        ImageIcon refreshIcon = new ImageIcon("config/atualizar_processos.png");
        Image image = refreshIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        refreshIcon = new ImageIcon(image);
        updateButton.setIcon(refreshIcon);
        updateButton.setText("");
        updateButton.addActionListener(e -> populateComboBoxWithRagnarokProcesses());
        
        topLeftPanel.add(updateButton, BorderLayout.EAST);

        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftButtonsPanel.setOpaque(false);
        
        JButton atualizarScripts = new JButton("<html><center>Atualizar<br>Scripts</center></html>");
        atualizarScripts.addActionListener(e -> atualizarScripts()); // Adiciona ação para o botão Play

        JButton playButton = new JButton("<html><center>Play<br>" + config.getAtalhoPlay() + "</center></html>");
        playButton.addActionListener(e -> play()); // Adiciona ação para o botão Play
        
        JButton stopButton = new JButton("<html><center>Stop<br>" + config.getAtalhoStop() + "</center></html>");
        stopButton.addActionListener(e -> stop()); // Adiciona ação para o botão Play
        
        JButton pauseResumeButton = new JButton("<html><center>Pause/Resume<br>" + config.getAtalhoPause() + "</center></html>");
        pauseResumeButton.addActionListener(e -> pauseResume());
        System.out.println("Adicionando ActionListener ao botão Pause/Resume");
        
        leftButtonsPanel.add(atualizarScripts);
        leftButtonsPanel.add(playButton);
        leftButtonsPanel.add(stopButton);
        leftButtonsPanel.add(pauseResumeButton);

        JPanel timePanel = new JPanel();
        timePanel.setPreferredSize(new Dimension(50, playButton.getPreferredSize().height));
        timePanel.setBackground(Color.WHITE);
        timeLabel = new JLabel("150ms", SwingConstants.CENTER);
        
        timePanel.add(timeLabel);

        topPanel.add(topLeftPanel, BorderLayout.WEST); // Adiciona o painel com ComboBox e Botão à esquerda
        topPanel.add(leftButtonsPanel, BorderLayout.CENTER); // Adiciona o painel com os botões no centro
        topPanel.add(timePanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createBelowTopPanel() {
        JPanel belowTopPanel = new JPanel(new BorderLayout());
        belowTopPanel.setOpaque(false);
        belowTopPanel.setBorder(BorderFactory.createEmptyBorder(55, 20, 20, 20));

        // Criando o grupo de botões para garantir seleção única
        ButtonGroup radioButtonGroup = new ButtonGroup();

        JPanel leftPanel = createLeftPanel(radioButtonGroup); // Passando o grupo
        JPanel rightPanel = createRightPanel(radioButtonGroup); // Passando o grupo

        belowTopPanel.add(leftPanel, BorderLayout.WEST);
        belowTopPanel.add(rightPanel, BorderLayout.EAST);

        return belowTopPanel;
    }

    private JPanel createLeftPanel(ButtonGroup group) {
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(205, 255));
        leftPanel.setLayout(new BorderLayout());

        instanciaRadioButton = new JRadioButton("Modo Instância");
        instanciaRadioButton.setSelected(true);

        // Adiciona o botão ao grupo
        group.add(instanciaRadioButton);

        leftPanel.add(instanciaRadioButton, BorderLayout.NORTH);

        // Lista para arquivos de instâncias
        DefaultListModel<String> instanceListModel = new DefaultListModel<>();
        instanceList = new JList<>(instanceListModel);
        JScrollPane instanceScrollPane = new JScrollPane(instanceList);
        leftPanel.add(instanceScrollPane, BorderLayout.CENTER);

        // Carrega os arquivos da pasta "instancias"
        createFolderIfNotExists("instancias");
        loadFilesToModel(instanceListModel, "contas");

        return leftPanel;
    }

    private JPanel createRightPanel(ButtonGroup group) {
        rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setPreferredSize(new Dimension(205, 255));
        rightPanel.setLayout(new BorderLayout());

        farmRadioButton = new JRadioButton("Modo Bio/Chef");

        // Adiciona o botão ao grupo
        group.add(farmRadioButton);

        rightPanel.add(farmRadioButton, BorderLayout.NORTH);

        // Painel para lista e botões
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        // Carrega arquivos de "biochef"
        createFolderIfNotExists("biochef");
        File bioChefDir = new File("biochef");
        if (bioChefDir.exists() && bioChefDir.isDirectory()) {
            for (File file : bioChefDir.listFiles((dir, name) -> name.endsWith(".json"))) {
                JPanel filePanel = new JPanel(new BorderLayout());
                filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                JLabel fileLabel = new JLabel(file.getName());
                JButton addButton = new JButton("+");
                addButton.addActionListener(e -> {
                	if (farmRadioButton.isSelected()) {
	                    sequenciaBioChef.add(file.getName());
	                    updateSequenciaLabel();
	                    JOptionPane.showMessageDialog(this, "Adicionado à sequência: " + file.getName());
                	}
                });

                filePanel.add(fileLabel, BorderLayout.CENTER);
                filePanel.add(addButton, BorderLayout.EAST);

                listPanel.add(filePanel);
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        return rightPanel;
    }

    private void createFolderIfNotExists(String folderName) {
        File folder = new File(folderName);
        if (!folder.exists()) {
            if (folder.mkdir()) {
                System.out.println("Pasta criada: " + folderName);
            } else {
                System.err.println("Erro ao criar a pasta: " + folderName);
            }
        }
    }

    private void loadFilesToModel(DefaultListModel<String> model, String folderName) {
        File dir = new File(folderName);
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles((dir1, name) -> name.endsWith(".json"))) {
                model.addElement(file.getName());
            }
        }
    }
    
    // Atualiza o texto do label com os valores da lista sequenciaBioChef
    private void updateSequenciaLabel() {
        String sequenciaText = "Sequência: " + String.join(", ", sequenciaBioChef);
        sequenciaLabel.setText(sequenciaText);
    }
    // Atualiza o texto do label do ms
    public static void updateTempoLabel(long time) {
        String tempo = time + "ms";
        timeLabel.setText(tempo);
    }
    
	// Método chamado ao clicar no botão Play
    private void play() {
        System.out.println("Clicou no botão Play");
        try {
            
            ScriptLoader scriptLoader = new ScriptLoader();
            Script script = null;
            ContasConfig conta = null;
            
            if (instanciaRadioButton.isSelected()) {//instancia
            	System.out.println("Modo Instância selecionado");
            	sequenciaBioChef.clear();
            	updateSequenciaLabel();
            	
            	String instancia = instanceList.getSelectedValue();
            	if (instancia == null) {
            		JOptionPane.showMessageDialog(this, "Nenhuma instancia foi selecionada na lista");
            		return;
            	}
            	
            	conta = scriptLoader.carregarContas("contas/" + instancia);
            	this.gameController.setScriptContas(conta);
            	
                //script = scriptLoader.carregarScriptdoJson("instancias/" + instancia);
                
            } else if (farmRadioButton.isSelected()) {
            	System.out.println("Modo Bio/Chef selecionado");
            	if (sequenciaBioChef.isEmpty()) {
            		JOptionPane.showMessageDialog(this, "Nenhuma rota de farm foi selecionada");
            		return;
            	}
            	gameController.listaDeFarmBioChef = sequenciaBioChef;
            	String rota = gameController.listaDeFarmBioChef.get(0);
            	script = scriptLoader.carregarScriptdoJson("biochef/" + rota);
            	this.gameController.setScript(script);
            }
        	
            //Focar no ragnarok
            User32.INSTANCE.SetForegroundWindow(Bot.hwnd);
            
            if (botThread == null || !botThread.isAlive()) {
            	 botThread = new Thread(gameController); // Cria a thread para o GameController
            	 gameController.setLigarBot(true);
                 botThread.start(); // Inicia o loop do bot
            }
			//gameController.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void stop() {
    	gameController.pararBot();
    	Bot bot = gameController.getBot();
    	GameController gc = new GameController(bot);
    	gameController = gc;
    }
    public void pauseResume() {
    	System.out.println("Evento Pause/Resume disparado");
    	if (gameController.getScript().getMapa() == null) {
    		return;
    	}
    	//Por algum motivo tava disparando 2x quando clicava no botao
    	if (pauseCounter % 2 == 0) {
    		//Focar no ragnarok
            User32.INSTANCE.SetForegroundWindow(Bot.hwnd);
            try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
    		gameController.pausarBot();
    	}
    	pauseCounter++;
    }
    
    public String obterUUID() {
        try {
            ProcessBuilder builder = new ProcessBuilder("wmic", "csproduct", "get", "UUID");
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String uuid = "";

            while ((line = reader.readLine()) != null) {
                if (line.matches(".*[0-9A-Fa-f-]{36}.*")) { // Filtra apenas UUIDs válidos
                    uuid = line.trim();
                    break;
                }
            }

            reader.close();
            return uuid.isEmpty() ? "UUID não encontrado" : uuid;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao obter UUID.";
        }
    }
    
    private void populateComboBoxWithRagnarokProcesses() {
    	DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    	// Adicionar um item padrão vazio
        //model.addElement("Selecione um processo");
    	
        // Obter lista de processos
        List<ProcessHandle> processes = ProcessHandle.allProcesses()
                .filter(process -> process.info().command().isPresent() 
                        && process.info().command().get().endsWith(".exe") 
                        && process.info().command().get().toLowerCase().contains("ragnarok")) // Filtrar por "Ragnarok"
                .collect(Collectors.toList());

        // Preencher o ComboBox com os processos filtrados
        for (ProcessHandle process : processes) {
            String fullPath = process.info().command().orElse("Unknown");
            String executableName = Paths.get(fullPath).getFileName().toString(); // Obter apenas o nome do arquivo
            int pid = (int) process.pid();
            model.addElement(pid + " - " + executableName);
        }

        comboBox.setModel(model); // Atualizar o modelo do ComboBox com os processos
        comboBox.setSelectedItem(null); // Garante que nada seja selecionado no início
    }
    
    private void atualizarScripts() {
        // Atualiza os arquivos no painel esquerdo
        DefaultListModel<String> instanceListModel = (DefaultListModel<String>) instanceList.getModel();
        instanceListModel.clear(); // Limpa os arquivos anteriores
        loadFilesToModel(instanceListModel, "contas"); // Carrega novamente os arquivos

        // Atualiza os arquivos no painel direito
        JPanel listPanel = (JPanel) ((JScrollPane) rightPanel.getComponent(1)).getViewport().getView();
        listPanel.removeAll(); // Limpa os arquivos anteriores

        // Carrega novamente os arquivos da pasta "biochef"
        File bioChefDir = new File("biochef");
        if (bioChefDir.exists() && bioChefDir.isDirectory()) {
            for (File file : bioChefDir.listFiles((dir, name) -> name.endsWith(".json"))) {
                JPanel filePanel = new JPanel(new BorderLayout());
                filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                JLabel fileLabel = new JLabel(file.getName());
                JButton addButton = new JButton("+");
                addButton.addActionListener(e -> {
                    if (farmRadioButton.isSelected()) {
                        sequenciaBioChef.add(file.getName());
                        updateSequenciaLabel();
                        JOptionPane.showMessageDialog(this, "Adicionado à sequência: " + file.getName());
                    }
                });

                filePanel.add(fileLabel, BorderLayout.CENTER);
                filePanel.add(addButton, BorderLayout.EAST);

                listPanel.add(filePanel);
            }
        }

        // Revalidar e repintar o painel para exibir as atualizações
        listPanel.revalidate();
        listPanel.repaint();
    }
    
    /*
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Verifica se as teclas CTRL e SHIFT estão pressionadas
        boolean isCtrlPressed = (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;
        boolean isShiftPressed = (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;

        // Ctrl + Shift + F
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_F) {
            gameController.fecharBot();
        }

        // Ctrl + Shift + P
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_P) {
        	gameController.pausarBot();
        }
        
        // Ctrl + Shift + O
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_O) {
        	gameController.pararBot();
        }
        
        // Ctrl + Shift + S
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_S) {
        	gameController.modoSalvarCoordenadas();
        }
        // Ctrl + Shift + D
        if (isCtrlPressed && isShiftPressed && e.getKeyCode() == NativeKeyEvent.VC_D) {
        	gameController.modoFecharCoordenadas();
        }
    }*/
    
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    	// Não usado
    }

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		// Não usado
	}
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
	    int keyCode = e.getKeyCode();
	    int modifiers = e.getModifiers();

	    for (Map.Entry<String, Atalho> entry : atalhos.entrySet()) {
	        if (entry.getValue() != null && entry.getValue().comparaAtalho(keyCode, modifiers)) {
	            System.out.println("Atalho acionado: " + entry.getKey());

	            switch (entry.getKey()) {
	                case "atalhoPlay": play(); break;
	                case "atalhoStop": gameController.pararBot(); break;
	                case "atalhoPause": gameController.pausarBot(); break;
	                case "atalhoClose": gameController.fecharBot(); break;
	                case "atalhoAdicionarCoords": gameController.modoSalvarCoordenadas(); break;
	                case "atalhoAbrirTxtCoords": gameController.modoFecharCoordenadas(); break;
	            }
	        }
	    }
	}
	

	private void carregarAtalhos() {
	    atalhos.put("atalhoPlay", converterAtalho(config.getAtalhoPlay()));
	    atalhos.put("atalhoStop", converterAtalho(config.getAtalhoStop()));
	    atalhos.put("atalhoPause", converterAtalho(config.getAtalhoPause()));
	    atalhos.put("atalhoClose", converterAtalho(config.getAtalhoClose()));
	    atalhos.put("atalhoAdicionarCoords", converterAtalho(config.getAtalhoAdicionarCoords()));
	    atalhos.put("atalhoAbrirTxtCoords", converterAtalho(config.getAtalhoAbrirTxtCoords()));
	}
	private Atalho converterAtalho(String atalhoStr) {
	    if (atalhoStr == null || atalhoStr.isEmpty()) return null;

	    String[] partes = atalhoStr.split("\\+");
	    Set<Integer> modifiers = new HashSet<>();
	    int keyCode = -1;

	    for (String parte : partes) {
	        parte = parte.trim().toUpperCase();
	        switch (parte) {
	            case "CTRL": modifiers.add(NativeKeyEvent.CTRL_MASK); break;
	            case "SHIFT": modifiers.add(NativeKeyEvent.SHIFT_MASK); break;
	            case "ALT": modifiers.add(NativeKeyEvent.ALT_MASK); break;
	            default: 
	                keyCode = obterKeyCode(parte);
	        }
	    }

	    return keyCode != -1 ? new Atalho(keyCode, modifiers) : null;
	}

	private int obterKeyCode(String tecla) {
	    try {
	        return (int) NativeKeyEvent.class.getField("VC_" + tecla).get(null);
	    } catch (Exception e) {
	        System.err.println("Tecla desconhecida: " + tecla);
	        return -1;
	    }
	}
	
}


