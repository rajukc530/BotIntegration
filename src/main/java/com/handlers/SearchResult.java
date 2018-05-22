package com.handlers;

public class SearchResult {

	   String title;
	     String link;
	     String subtitle;
	     String summary;
	     
	     
  public SearchResult(String title, String link, String subtitle, String summary) {
			super();
			this.title = title;
			this.link = link;
			this.subtitle = subtitle;
			this.summary = summary;
		}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
}
