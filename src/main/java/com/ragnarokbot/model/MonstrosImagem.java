package com.ragnarokbot.model;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public class MonstrosImagem {
	
	public List<MatOfPoint> listaMonstros;
	public Mat screen;
	
	public MonstrosImagem(List<MatOfPoint> listaMonstros, Mat screen) {
		this.listaMonstros = listaMonstros;
		this.screen = screen;
	}
}
