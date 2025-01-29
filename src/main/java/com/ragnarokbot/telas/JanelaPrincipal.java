package com.ragnarokbot.telas;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.ragnarokbot.bot.Bot;
import com.ragnarokbot.main.GameController;
import com.sun.jna.platform.win32.User32;

import config.ContasConfig;
import config.Script;
import config.ScriptLoader;

public class JanelaPrincipal extends JFrame {

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

    public JanelaPrincipal(GameController gameController) {
    	System.out.println("Criando JanelaPrincipal");
        setTitle("RagnaBot");
        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Evita o fechamento padrão
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false); // Janela não redimensionável
        setLayout(new BorderLayout());
        
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
                    gameController.fecharBot();
                }
            }
        });
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftButtonsPanel.setOpaque(false);

        JButton playButton = new JButton("<html><center>Play<br>Poura</center></html>");
        playButton.addActionListener(e -> play()); // Adiciona ação para o botão Play
        
        JButton pauseResumeButton = new JButton("<html><center>Pause/Resume<br>CTRL SHIFT P</center></html>");
        pauseResumeButton.addActionListener(e -> pauseResume());
        System.out.println("Adicionando ActionListener ao botão Pause/Resume");
        
        leftButtonsPanel.add(playButton);
        leftButtonsPanel.add(pauseResumeButton);

        JPanel timePanel = new JPanel();
        timePanel.setPreferredSize(new Dimension(50, playButton.getPreferredSize().height));
        timePanel.setBackground(Color.WHITE);
        timeLabel = new JLabel("150ms", SwingConstants.CENTER);
        timePanel.add(timeLabel);

        topPanel.add(leftButtonsPanel, BorderLayout.WEST);
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
        loadFilesToModel(instanceListModel, "instancias");

        return leftPanel;
    }

    private JPanel createRightPanel(ButtonGroup group) {
        JPanel rightPanel = new JPanel();
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
    
}


