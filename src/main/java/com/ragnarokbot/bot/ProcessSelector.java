package com.ragnarokbot.bot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessSelector extends JFrame {
    private JList<String> processList;
    private JButton okButton;
    private int selectedPid;

    public ProcessSelector() {
        setTitle("Selecionar Processo");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // Obter lista de processos
        List<ProcessHandle> processes = ProcessHandle.allProcesses()
                .filter(process -> process.info().command().isPresent() 
                        && process.info().command().get().endsWith(".exe") 
                        && process.info().command().get().toLowerCase().contains("ragnarok")) // Filtrar por "Ragnarok"
                .collect(Collectors.toList());

        /*// Converter para exibir na lista
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (ProcessHandle process : processes) {
            String command = process.info().command().orElse("Unknown");
            int pid = (int) process.pid();
            listModel.addElement(pid + " - " + command);
        }*/
        
        // Converter para exibir na lista com nomes simplificados
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (ProcessHandle process : processes) {
            String fullPath = process.info().command().orElse("Unknown");
            String executableName = Paths.get(fullPath).getFileName().toString(); // Obter apenas o nome do arquivo
            int pid = (int) process.pid();
            listModel.addElement(pid + " - " + executableName);
        }

        // Criar a lista gráfica
        processList = new JList<>(listModel);
        processList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(processList);

        // Selecionar o primeiro item da lista, se houver
        if (!listModel.isEmpty()) {
            processList.setSelectedIndex(0);
        }
        
        // Botão OK
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = processList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedValue = processList.getSelectedValue();
                    selectedPid = Integer.parseInt(selectedValue.split(" - ")[0]);
                    JOptionPane.showMessageDialog(ProcessSelector.this, "Processo selecionado: " + selectedValue);
                    dispose(); // Fechar a janela
                } else {
                    JOptionPane.showMessageDialog(ProcessSelector.this, "Selecione um processo.", "Erro", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Adicionar componentes
        add(scrollPane, BorderLayout.CENTER);
        add(okButton, BorderLayout.SOUTH);
    }

    public int getSelectedPid() {
        return selectedPid;
    }
}