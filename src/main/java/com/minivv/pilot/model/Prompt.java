package com.minivv.pilot.model;

public class Prompt extends DomainObject {
	private String option;
	private String snippet;

//	private Integer index;

	public Prompt() {
	}

//	public Prompt(String option, String snippet,int index) {
//		this.option = option;
//		this.snippet = snippet;
//		this.index = index;
//	}

//	public static Prompt of(String name, String value,int index) {
//		return new Prompt(name, value,index);
//	}
	public Prompt(String option, String snippet) {
		this.option = option;
		this.snippet = snippet;
	}

	public static Prompt of(String name, String value) {
		return new Prompt(name, value);
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String applyTo(String commandLine) {
		if (option != null && snippet != null) {
			return commandLine.replace(option, snippet);
		}
		return commandLine;
	}

//	public Integer getIndex() {
//		return index;
//	}
//
//	public void setIndex(Integer index) {
//		this.index = index;
//	}
}