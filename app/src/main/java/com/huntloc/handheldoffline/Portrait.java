package com.huntloc.handheldoffline;

public class Portrait {
	private String internalCode;
	private String printedCode;
	private String portrait;
	private String name;
	public Portrait(String internalCode, String printedCode, String portrait, String name) {
		this.internalCode = internalCode;
		this.printedCode = printedCode;
		this.portrait = portrait;
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInternalCode() {
		return internalCode;
	}
	public void setInternalCode(String internalCode) {
		this.internalCode = internalCode;
	}
	public String getPrintedCode() {
		return printedCode;
	}
	public void setPrintedCode(String printedCode) {
		this.printedCode = printedCode;
	}
	public String getPortrait() {
		return portrait;
	}
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}
	
}
