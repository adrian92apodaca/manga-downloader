/**
 * Description: Created: 06.01.2010
 */
package de.xxlbug.md.manga;

import java.util.Date;

/**
 * @author xxlbug
 * @date 06.01.2010
 */
public class Chapter {
	private final Date		date;
	private final String	link;
	private String[]			pages;
	private final String	scanGroup;
	private final String	title;

	public Chapter(String title, String link, String scanGroup, Date date) {
		this.title = title;
		this.link = link;
		this.scanGroup = scanGroup;
		this.date = (Date) date.clone();
	}

	public Chapter(String title, String link, String[] pages, String scanGroup, Date date) {
		this.title = title;
		this.link = link;
		this.pages = pages.clone();
		this.scanGroup = scanGroup;
		this.date = (Date) date.clone();
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return (Date) this.date.clone();
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return this.link;
	}

	/**
	 * @return the pages
	 */
	public String[] getPages() {
		return this.pages.clone();
	}

	/**
	 * @return the scanGroup
	 */
	public String getScanGroup() {
		return this.scanGroup;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	public void setPages(String[] adresses) {
		this.pages = adresses.clone();
	}
}
