/**
 * Description: This interface describes what an website has to offer as data to get used in the program
 * Created: 05.01.2010
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

/**
 * @author xxlbug
 * @date 07.01.2010
 */
public interface Website {

	/**
	 * Download the file from the given adress, may be used to download the cover and/or chapter pages
	 * 
	 * @param adress what to download
	 * @return the actual path of the loaded file
	 */
	public String download(String adress);

	/**
	 * Get all avaible manga from this website
	 * 
	 * @return array of mangas
	 */
	public Manga[] getAllManga();

	/**
	 * Get the website name
	 * 
	 * @return the actual name
	 */
	public String getName();
}
