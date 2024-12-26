package com.ragnarokbot.model;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public class MonstrosImagem {
	
	public List<MatOfPoint> listaEntidades;
	public Mat screen;
	
	public MonstrosImagem(List<MatOfPoint> listaEntidades, Mat screen) {
		this.listaEntidades = listaEntidades;
		this.screen = screen;
	}
}
