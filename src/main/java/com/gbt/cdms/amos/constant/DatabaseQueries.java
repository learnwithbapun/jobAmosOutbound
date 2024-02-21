package com.gbt.cdms.amos.constant;

public class DatabaseQueries {

	public static final String DELETE_GISCXMSTP = "truncate cdms.GISCXMSTP";
	public static final String DELETE_GISDRMSTP = "truncate cdms.GISDRMSTP";
	public static final String SAVE_TO_GISCXMSTP = "select cdms.giscxmstp_inserts_query()";
	public static final String SAVE_TO_GISDRMSTP = "select cdms.gisdrmstp_inserts_query()";
	public static final String UPDATE_CXMODE_TO_CUSTXREF = "";
	
	public static final String FETCH_GISCXMSTP = "SELECT * FROM cdms.GISCXMSTP";
	public static final String FETCH_GISDRMSTP = "SELECT * FROM cdms.GISDRMSTP";
}