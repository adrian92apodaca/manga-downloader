/**
 * Description: Created: 06.01.2010
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
