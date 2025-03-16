package com.ragnarokbot.telas;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class PerfilCrud {

	private static final long serialVersionUID = 1L;
	private DefaultListModel<String> perfilModel;
    private JList<String> listaPerfis;
    
    public PerfilCrud() {
    	JFrame frame = new JFrame("Perfil");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel labelTitulo = new JLabel("Criar novo perfil");
        painelTopo.add(labelTitulo);
        frame.add(painelTopo, BorderLayout.NORTH);
        
        JPanel painelEntrada = new JPanel(new FlowLayout());
        JTextField campoPerfil = new JTextField(20);
        JButton botaoCriar = new JButton("Criar");
        painelEntrada.add(campoPerfil);
        painelEntrada.add(botaoCriar);
        frame.add(painelEntrada, BorderLayout.CENTER);
        
        JPanel painelLista = new JPanel(new BorderLayout());
        JLabel labelLista = new JLabel("Lista de perfis");
        painelLista.add(labelLista, BorderLayout.NORTH);
        
        perfilModel = new DefaultListModel<>();
        listaPerfis = new JList<>(perfilModel);
        painelLista.add(new JScrollPane(listaPerfis), BorderLayout.CENTER);
        
        JPanel painelBotoes = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton botaoRemover = new JButton("Remover");
        JButton botaoEditar = new JButton("Editar");
        JButton botaoCopiar = new JButton("Copiar");
        
        painelBotoes.add(botaoRemover);
        painelBotoes.add(botaoEditar);
        painelBotoes.add(botaoCopiar);
        
        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.add(painelLista, BorderLayout.CENTER);
        painelInferior.add(painelBotoes, BorderLayout.EAST);
        
        frame.add(painelInferior, BorderLayout.SOUTH);
        
        
        botaoCriar.addActionListener(e -> {
            String nomePerfil = campoPerfil.getText().trim();
            if (!nomePerfil.isEmpty()) {
                perfilModel.addElement(nomePerfil);
                campoPerfil.setText("");
            }
        });
        
        botaoRemover.addActionListener(e -> {
            int index = listaPerfis.getSelectedIndex();
            if (index != -1) {
                perfilModel.remove(index);
            }
        });
        
        botaoEditar.addActionListener(e -> {
            int index = listaPerfis.getSelectedIndex();
            if (index != -1) {
                String novoNome = JOptionPane.showInputDialog("Editar perfil:", perfilModel.getElementAt(index));
                if (novoNome != null && !novoNome.trim().isEmpty()) {
                    perfilModel.setElementAt(novoNome, index);
                }
            }
        });
        
        botaoCopiar.addActionListener(e -> {
            int index = listaPerfis.getSelectedIndex();
            if (index != -1) {
                perfilModel.addElement(perfilModel.getElementAt(index) + " - Cópia");
            }
        });
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    
}
