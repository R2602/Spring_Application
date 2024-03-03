package com.example.joblisting.model;

import java.util.Arrays;

import org.springframework.data.mongodb.core.mapping.Document;
@Document (collection = "jobpost")
public class Post {
	private String profile;
	private String desc;
	private String techs[];
	private int exp;
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	public Post() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String[] getTechs() {
		return techs;
	}
	public void setTechs(String[] techs) {
		this.techs = techs;
	}
	public int getExp() {
		return exp;
	}
	public void setExp(int exp) {
		this.exp = exp;
	}
	@Override
	public String toString() {
		return "Post [profile=" + profile + ", desc=" + desc + ", techs=" + Arrays.toString(techs) + ", exp=" + exp
				+ "]";
	}

	
}
