/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.model.subject;

import org.activiti.engine.identity.User;

import java.io.Serializable;

/**
 * Класс - юзеры
 * 
 * @author inna
 */
public class SubjectUser implements Serializable {

	private String sLogin;
	private String sFirstName;
	private String sLastName;
	private String sEmail;
	private String sPicture;

	public String getsLogin() {
		return sLogin;
	}

	public void setsLogin(String sLogin) {
		this.sLogin = sLogin;
	}

	public String getsFirstName() {
		return sFirstName;
	}

	public void setsFirstName(String sFirstName) {
		this.sFirstName = sFirstName;
	}

	public String getsLastName() {
		return sLastName;
	}

	public void setsLastName(String sLastName) {
		this.sLastName = sLastName;
	}

	public String getsEmail() {
		return sEmail;
	}

	public void setsEmail(String sEmail) {
		this.sEmail = sEmail;
	}

	public String getsPicture() {
		return sPicture;
	}

	public void setsPicture(String sPicture) {
		this.sPicture = sPicture;
	}

	@Override
	public String toString() {
		return "sLogin=" + sLogin + ", sFirstName=" + sFirstName + ", sLastName=" + sLastName + ", sEmail="
				+ sEmail + ", sPicture=" + sPicture;
	}

	public static class BuilderHelper {
		public static SubjectUser buildByActivitiUser(User oUser) {
			final SubjectUser model = new SubjectUser();
			model.setsLogin(oUser.getId() != null ? oUser.getId() : "");
			model.setsFirstName(oUser.getFirstName() != null ? oUser.getFirstName() : "");
			model.setsLastName(oUser.getLastName() != null ? oUser.getLastName() : "");
			model.setsEmail(oUser.getEmail() != null ? oUser.getEmail() : "");
			model.setsPicture(null);
			return model;
		}
	}

}
