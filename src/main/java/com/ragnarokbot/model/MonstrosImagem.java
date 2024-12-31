package com.ragnarokbot.model;

import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public class MonstrosImagem {
	
	//public List<MatOfPoint> listaEntidades;
	public Mat screen;
	
	public Map<String, List<MatOfPoint>> listaEntidades;
	
	/*
	public MonstrosImagem(List<MatOfPoint> listaEntidades, Mat screen) {
		this.listaEntidades = listaEntidades;
		this.screen = screen;
	}*/
	
	public MonstrosImagem(Map<String, List<MatOfPoint>> listaEntidades, Mat screen) {
		this.listaEntidades = listaEntidades;
		this.screen = screen;
	}
	
}
