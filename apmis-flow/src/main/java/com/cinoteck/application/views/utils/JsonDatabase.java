package com.cinoteck.application.views.utils;

import java.security.GeneralSecurityException;

import io.jsondb.JsonDBTemplate;
import io.jsondb.crypto.Default1Cipher;
import io.jsondb.crypto.ICipher;

public class JsonDatabase {

	public JsonDatabase(String lastLogged) {
		System.out.println("----dddddd--------------");
		InsertLastLogged(lastLogged);
	}

	private JsonDBTemplate jsonDBTemplate() {

		// Actual location on disk for database files, process should have read-write
		// permissions to this folder
		String dbFilesLocation = "/c/opt";

		// Java package name where POJO's are present
		String baseScanPackage = "com.cinoteck.application.views.utils.LastInstance";

		JsonDBTemplate jsonTem = new JsonDBTemplate(dbFilesLocation, baseScanPackage);
		return jsonTem;
	}

	public boolean InsertLastLogged(String lastLogged) {
		System.out.println("--------yyy----------");
		JsonDBTemplate jsonb = jsonDBTemplate();
		try {
			jsonb.createCollection(LastInstance.class);
		} catch (Exception e) {
			System.out.println("_______________1111_________________________________--" + e.getLocalizedMessage());
			return false;
		}

		try {
			LastInstance instance = new LastInstance();
			instance.setId("1");
			instance.setHostname(lastLogged);

			jsonb.insert(instance);

		} catch (Exception e) {
			System.out.println("____________2222____________________________________--" + e.getLocalizedMessage());
			return false;
		}

		return true;

	}

	public String GetLastLogged() {
		LastInstance lstd;
		try {
			JsonDBTemplate jsonb = jsonDBTemplate();
			lstd = jsonb.findById("1", LastInstance.class);
		} catch (Exception e) {
			return "dashboard";
		}
		return lstd.getHostname();
	}
}