package ru.r2cloud.amsat;

public enum Satellite {

	FOX1A("amsat.fox-1a.ihu"), FOX1B("amsat.fox-1b.ihu"), FOX1CLIFF("amsat.fox-1c.ihu"), FOX1D("amsat.fox-1d.ihu"), HUSKYSAT1("amsat.husky_sat.ihu"); 

	private final String sourcePrefix;
	
	private Satellite(String sourcePrefix) {
		this.sourcePrefix = sourcePrefix;
	}
	
	public String getSourcePrefix() {
		return sourcePrefix;
	}
}
