package com.ragnarokbot.bot.controles;

import java.awt.Robot;
import java.awt.event.InputEvent;

import utils.MouseClicker;

public class ControlRobot implements Control {
	
	private Robot robot;
	
	public ControlRobot( Robot bot) {
		this.robot = bot;
	}

	@Override
	public void apertarTecla(int vk) {
		robot.keyPress(vk); // Pressionar a tecla
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        robot.keyRelease(vk); // Liberar a tecla
	}

	@Override
	public void moverMouse(int x, int y) {
		robot.mouseMove(x, y);
	}

	@Override
	public void click() {
		MouseClicker.clicarMouse();
	}

	@Override
	public void scrollMouse(int x) {
		robot.mouseWheel(x);	
	}

	@Override
	public void apertarSegurarTecla(int vk) {
		robot.keyPress(vk);
	}

	@Override
	public void soltarTecla(int vk) {
		robot.keyRelease(vk);
	}

	@Override
	public void clicarMouseDireito() {
		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK); // Pressionar o botão direito do mouse
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	}

	@Override
	public void clicarSegurarMouse() {
		robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
	}

	@Override
	public void soltarMouse() {
		robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
	}

	@Override
	public void segurarMouseDireito() {
		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
	}

	@Override
	public void soltarMouseDireito() {
		robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	}

}
