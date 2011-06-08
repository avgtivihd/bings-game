package com.badlogic.cubocy;

import com.badlogic.cubocy.screens.MainMenu;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class Cubocy extends Game {	
	public int level;
	
	public static void main(String[] argv) {
		new LwjglApplication(new Cubocy(), "Cubocy", 480, 320, false);
	}
	
	@Override public void create () {	
		Settings.load();
		setScreen(new MainMenu(this));
	}
	
	@Override public void dispose () {
		Settings.save();
		Assests.dispose();
		super.dispose();
	}
}