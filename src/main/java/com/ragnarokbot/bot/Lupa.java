package com.ragnarokbot.bot;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Lupa extends JFrame {
    private final int zoomSize = 100; // Tamanho da área capturada
    private final int zoomFactor = 2; // Fator de zoom
    private final Robot robot; // Robot para capturar a tela
    private final Point targetPoint; // Ponto alvo (coordenadas do personagem)

    public Lupa(Point targetPoint) throws AWTException {
        this.targetPoint = targetPoint;
        this.robot = new Robot();

        // Configurar a janela
        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparente
        setSize(zoomSize * zoomFactor, zoomSize * zoomFactor);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setFocusableWindowState(false); // Permite cliques no fundo

        // Adicionar o painel de desenho
        add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawZoomedArea(g);
            }
        });
    }

    private void drawZoomedArea(Graphics g) {
        // Calcular a área ao redor do ponto alvo
        Rectangle captureArea = new Rectangle(
            targetPoint.x - zoomSize / 2,
            targetPoint.y - zoomSize / 2,
            zoomSize,
            zoomSize
        );

        // Capturar a imagem da tela
        BufferedImage screenCapture = robot.createScreenCapture(captureArea);

        // Desenhar a imagem ampliada
        g.drawImage(
            screenCapture,
            0, 0, zoomSize * zoomFactor, zoomSize * zoomFactor,
            null
        );
    }

    public void updatePosition(Point newTargetPoint) {
        this.targetPoint.setLocation(newTargetPoint);
        // Reposicionar a janela
        setLocation(targetPoint.x + 20, targetPoint.y + 20); // Ajuste de posição
        repaint(); // Atualizar a imagem
    }
}