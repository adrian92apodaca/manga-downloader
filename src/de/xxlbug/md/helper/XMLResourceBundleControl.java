/**
 * Description: The controller makes saving and loading to an xml file possible
 * Created: 01.02.2010
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author xxlbug
 * @date 06.01.2010
 */
public class XMLResourceBundleControl extends ResourceBundle.Control {
	private static class XMLResourceBundle extends ResourceBundle {
		private final Properties	props;

		XMLResourceBundle(InputStream stream) throws IOException {
			this.props = new Properties();
			this.props.loadFromXML(stream);
		}

		@Override
		public Enumeration<String> getKeys() {
			Set<String> handleKeys = this.props.stringPropertyNames();
			return Collections.enumeration(handleKeys);
		}

		@Override
		protected Object handleGetObject(String key) {
			return this.props.getProperty(key);
		}
	}

	private static String	XML	= "xml";

	@Override
	public List<String> getFormats(String baseName) {
		return Collections.singletonList(XML);
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException,
			InstantiationException, IOException {
		if ((baseName == null) || (locale == null) || (format == null) || (loader == null)) {
			throw new NullPointerException();
		}
		ResourceBundle bundle = null;
		if (format.equalsIgnoreCase(XML)) {
			String bundleName = this.toBundleName(baseName, locale);
			String resourceName = this.toResourceName(bundleName, format);
			URL url = loader.getResource(resourceName);
			if (url != null) {
				URLConnection connection = url.openConnection();
				if (connection != null) {
					if (reload) {
						connection.setUseCaches(false);
					}
					InputStream stream = connection.getInputStream();
					if (stream != null) {
						BufferedInputStream bis = new BufferedInputStream(stream);
						bundle = new XMLResourceBundle(bis);
						bis.close();
					}
				}
			}
		}
		return bundle;
	}

}
