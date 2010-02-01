/**
 * Description: A chapter contains various infos but most important the urls to the pages
 * Created: 06.01.2010
 * 
 * -----
 * 
 * Copyright by Steffen Splitt 2010
 * 
 * This file is part of Manga Downloader.
 * 
 * Manga Downloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Manga Downloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Manga Downloader. If not, see <http://www.gnu.org/licenses/>.
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
