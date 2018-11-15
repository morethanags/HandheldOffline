package com.huntloc.handheldoffline;

public class Portrait {
	private String internalCode;
	private String printedCode;
	private String portrait;
	private String name;

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	private String access;
	private String camoExpiration;
	private String expiration;

	public Portrait(String internalCode, String printedCode, String portrait, String name, String access, String camoExpiration, String expiration) {
		this.internalCode = internalCode;
		this.printedCode = printedCode;
		this.portrait = portrait;
		this.name = name;
		this.access = access;
		this.camoExpiration = camoExpiration;
		this.expiration = expiration;

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

	public String getCamoExpiration() {
		return camoExpiration;
	}

	public void setCamoExpiration(String camoExpiration) {
		this.camoExpiration = camoExpiration;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	@Override
	public String toString() {
		return "Portrait{" +
				"internalCode='" + internalCode + '\'' +
				", printedCode='" + printedCode + '\'' +
				", name='" + name + '\'' +
				", access='" + access + '\'' +
				", camoExpiration='" + camoExpiration + '\'' +
				", expiration='" + expiration + '\'' +
				'}';
	}
}
