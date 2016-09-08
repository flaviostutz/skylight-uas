package br.skylight.commons.plugins.workbench;

import bibliothek.gui.DockTheme;
import bibliothek.gui.dock.themes.BasicTheme;
import bibliothek.gui.dock.themes.NoStackTheme;

public class NoStackBasicTheme extends NoStackTheme {

	public NoStackBasicTheme() {
		super(getBasicTheme());
	}

	private static DockTheme getBasicTheme() {
		BasicTheme bt = new BasicTheme();
		return bt;
	}
	
}
