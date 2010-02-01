/**
 * Description: Settings for the Manga Downloader which are saved in xml format
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
package de.xxlbug.md.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * @author xxlbug
 * @date 05.01.2010
 */
public class Settings {
	private final File	configDir;
	private final File	file;
	private Properties	settings;

	public Settings() {
		this.settings = new Properties();

		if (System.getProperty("os.name").toUpperCase().contains(("Windows").toUpperCase())) {
			this.configDir = new File(System.getProperty("user.home") + File.separator + "md-config");
			this.file = new File(this.configDir + File.separator + "md-settings.xml");
			try {
				Runtime.getRuntime().exec("attrib +H " + this.configDir);
			} catch (IOException e) {
				System.err.println("Error: Can't hide settings");
			}
		} else {
			this.configDir = new File(System.getProperty("user.home") + File.separator + ".md-config");
			this.file = new File(this.configDir + File.separator + "md-settings.xml");
		}

		if (!this.loadSettings()) {
			if (!this.createDefaultSettings()) {
				System.err.println("couldn't save settings");
			}
		}
	}

	public boolean getCreateSeriesFolder() {
		return Boolean.parseBoolean(this.settings.getProperty("seriesFolders"));
	}

	public boolean getCreateZip() {
		return Boolean.parseBoolean(this.settings.getProperty("createZip"));
	}

	public String getLanguage() {
		return this.settings.getProperty("lang");
	}

	public int getNumberOfDownloads() {
		return Integer.parseInt(this.settings.getProperty("numberOfDownloads"));
	}

	public String getSaveDirectory() {
		return this.settings.getProperty("saveDir");
	}

	/**
	 * Load settings from standard dir $HOME/.md-settings.xml
	 * 
	 * @return true if loading was successfull, otherwise false
	 */
	public boolean loadSettings() {
		if (this.file.exists() && this.file.canRead()) {
			try {
				this.settings.loadFromXML(new FileInputStream(this.file));
			} catch (InvalidPropertiesFormatException e) {
				System.err.println("InvalidPropertiesFormatException in loadSettings");
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				System.err.println("FileNotFoundException in loadSettings");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("IOException in loadSettings");
				e.printStackTrace();
			}

			return true;
		}
		return false;
	}

	/**
	 * Stores the settings in an xml file, placed in the config folder
	 * 
	 * @return true only if it was possible to save the settings
	 */
	public boolean saveSettings() {
		if (!this.file.exists()) {
			if (!this.configDir.exists()) {
				if (!this.configDir.mkdir()) {
					return false;
				}
			}
			return this.store();
		} else if (!this.file.canWrite()) {
			System.err.println("Exception in saveSettings");
			System.err.println("can't write to config file");
			return false;
		} else {
			return this.store();
		}
	}

	/**
	 * Should a folder for every series be created? Default is true.
	 * 
	 * @param create true if a folder should be created, false otherwise
	 */
	public void setCreateSeriesFolder(boolean createSeriesFolder) {
		this.settings.setProperty("seriesFolders", String.valueOf(createSeriesFolder));
	}

	/**
	 * Set the boolean value if a zip file should be created for every chapter
	 * 
	 * @param createZip true if zipping is wanted
	 */
	public void setCreateZip(boolean createZip) {
		this.settings.setProperty("createZip", String.valueOf(createZip));
	}

	/**
	 * Set the language, but only if the associated file exists
	 * 
	 * @param lang the desired language
	 * @return true if associated lang file exists and is readable
	 */
	public boolean setLanguage(String lang) {
		String langPath = this.configDir + File.separator + lang + ".xml";
		File langFile = new File(langPath);
		if (langFile.exists() && langFile.canRead()) {
			this.settings.setProperty("lang", lang);
			return true;
		}
		return false;
	}

	/**
	 * Set the number of simultaniously downloads aka number of active threads
	 * 
	 * @param number how many downloads should be made simultaniously
	 */
	public void setNumberOfDownloads(int number) {
		if (number > 0) {
			this.settings.setProperty("numberOfDownloads", String.valueOf(number));
		}
	}

	/**
	 * Set the save directory for manga but only if the folder exists and can be written on
	 * 
	 * @param path where the manga should be saved
	 * @return true if path exists and can be written on
	 */
	public boolean setSaveDirectory(String path) {
		File dir = new File(path);
		if (dir.exists() && dir.canWrite()) {
			this.settings.setProperty("saveDir", path);
			return true;
		}
		return false;
	}

	/**
	 * Creates and stores default settings
	 * 
	 * @return true if storing was successfull
	 */
	private boolean createDefaultSettings() {
		this.settings = this.getDefault();
		if (!this.saveSettings()) {
			return false;
		}
		return true;
	}

	/**
	 * Get default set of settings
	 * 
	 * @return the default properties
	 */
	private Properties getDefault() {
		Properties prop = new Properties();
		prop.setProperty("saveDir", System.getProperty("user.home") + File.separator + "Mangas");
		prop.setProperty("lang", "en");
		prop.setProperty("seriesFolders", String.valueOf(true));
		prop.setProperty("createZip", String.valueOf(true));
		prop.setProperty("numberOfDownloads", String.valueOf(2));

		return prop;
	}

	/**
	 * Only a little helper method
	 * 
	 * @return true if storing was successfull
	 */
	private boolean store() {
		try {
			this.settings.storeToXML(new FileOutputStream(this.file), "MangaDownloader Settings");
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFoundException in saveSettings");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println("IOException in saveSettings");
			e.printStackTrace();
			return false;
		}
	}
}
