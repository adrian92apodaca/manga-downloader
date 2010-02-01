/**
 * Description: Created: 07.01.2010
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
