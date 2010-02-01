/**
 * Description: A manga contains data about the author, artist, chapters etc. and possible a license and/or notice note
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

import java.net.URL;

/**
 * @author xxlbug
 * @date 06.01.2010
 */
public class Manga {
	private String		artist;
	private String		author;
	private String[]	categories;
	private Chapter[]	chapters;
	private String		description;
	private String		license;
	private URL				link;
	private String		name;
	private String		notice;
	private String		pathToCover;
	private String		status;

	public Manga() {
		this.artist = "";
		this.author = "";
		this.categories = null;
		this.chapters = null;
		this.description = "";
		this.license = "";
		this.name = "";
		this.notice = "";
		this.pathToCover = "";
		this.status = "";
		this.link = null;
	}

	public Manga(
			String name,
			URL link,
			String description,
			String pathToCover,
			Chapter[] chapters,
			String artist,
			String author,
			String[] categories,
			String license,
			String notice,
			String status) {
		this.name = name;
		this.description = description;
		this.pathToCover = pathToCover;
		this.chapters = chapters.clone();
		this.artist = artist;
		this.author = author;
		this.categories = categories.clone();
		this.license = license;
		this.notice = notice;
		this.status = status;
		this.link = link;
	}

	/**
	 * @return the artist
	 */
	public String getArtist() {
		return this.artist;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return this.author;
	}

	/**
	 * @return the categories
	 */
	public String[] getCategories() {
		return this.categories.clone();
	}

	/**
	 * @return the chapters
	 */
	public Chapter[] getChapters() {
		return this.chapters;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return the license
	 */
	public String getLicense() {
		return this.license;
	}

	/**
	 * @return the link
	 */
	public URL getLink() {
		return this.link;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the notice
	 */
	public String getNotice() {
		return this.notice;
	}

	/**
	 * @return the pathToCover
	 */
	public String getPathToCover() {
		return this.pathToCover;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * @param artist the artist to set
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(String[] categories) {
		this.categories = categories.clone();
	}

	/**
	 * @param chapters the chapters to set
	 */
	public void setChapters(Chapter[] chapters) {
		this.chapters = chapters.clone();
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
	}

	/**
	 * @param link the link to set
	 */
	public void setLink(URL link) {
		this.link = link;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param notice the notice to set
	 */
	public void setNotice(String notice) {
		this.notice = notice;
	}

	/**
	 * @param pathToCover the pathToCover to set
	 */
	public void setPathToCover(String pathToCover) {
		this.pathToCover = pathToCover;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

}
