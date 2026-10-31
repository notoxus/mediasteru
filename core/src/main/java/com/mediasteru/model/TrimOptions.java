package com.mediasteru.model;

public class TrimOptions {
	private final String trimSection;
	private final boolean preciseCut;

	public TrimOptions(String trimSection, boolean preciseCut) {
		this.trimSection = trimSection;
		this.preciseCut = preciseCut;
	}

	public String getTrimSection() {
		return trimSection;
	}

	public boolean isPreciseCut() {
		return preciseCut;
	}
}
