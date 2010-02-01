/**
 * Description: Special composite to track the download progress
 * Created: 07.01.2010
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
package de.xxlbug.md.vis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;

import de.xxlbug.md.helper.Net;
import de.xxlbug.md.helper.Settings;
import de.xxlbug.md.helper.XMLResourceBundleControl;
import de.xxlbug.md.helper.Zip;

/**
 * @author xxlbug
 * @date 07.01.2010
 */
public class DownloadMonitor extends Composite {

	private static final int					FAILURE	= 3;
	private static final int					SUCCESS	= 2;
	private static final int					WORKING	= 1;

	private Button										btnOpenDir;
	private Button										btnRemove;
	private Label											lblChapterName;
	private Label											lblChapterPage;
	private Label											lblIconStatus;
	private Label											lblName;
	private Label											lblPage;
	private final String							path;

	private final ThreadPoolExecutor	pool;
	private ProgressBar								progBar;
	private ResourceBundle						res;

	private final Settings						settings;

	public DownloadMonitor(Composite parent, ThreadPoolExecutor pool, URL[] links, String path, Settings settings) {
		super(parent, SWT.BORDER);
		this.setLayout(new GridLayout(4, false));

		this.settings = settings;
		this.loadLanguage(this.settings.getLanguage());

		this.pool = pool;
		this.path = path;

		this.createContent();
		this.addToPool(this.createRunner(links, path));
	}

	/**
	 * Start the action
	 */
	private void addToPool(Runnable run) {
		if (run != null) {
			this.pool.setCorePoolSize(this.settings.getNumberOfDownloads());
			this.pool.execute(run);
		}
	}

	/**
	 * Create all neccessary ui items
	 */
	private void createContent() {
		this.lblName = new Label(this, SWT.NONE);
		this.lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		this.lblName.setText(this.res.getString("name") + ":");

		this.lblChapterName = new Label(this, SWT.NONE);
		this.lblChapterName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// remove button with listener
		this.btnRemove = new Button(this, SWT.FLAT);
		this.btnRemove.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		{
			InputStream in = this.getClass().getResourceAsStream("/ico/delete.png");
			this.btnRemove.setImage(new Image(this.getDisplay(), in));
		}
		this.btnRemove.setVisible(false);
		{
			Listener lstRemove = new Listener() {
				public void handleEvent(Event event) {
					DownloadMonitor.this.dispose();
				}
			};
			this.btnRemove.addListener(SWT.Selection, lstRemove);
		}
		// end remove button with listener

		this.lblIconStatus = new Label(this, SWT.None);
		{
			GridData gdIconStatus = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2);
			gdIconStatus.widthHint = 32;
			gdIconStatus.heightHint = 32;
			this.lblIconStatus.setLayoutData(gdIconStatus);
		}
		this.setStatus(DownloadMonitor.WORKING);

		this.lblPage = new Label(this, SWT.NONE);
		this.lblPage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		this.lblPage.setText(this.res.getString("page") + ":");

		this.lblChapterPage = new Label(this, SWT.None);
		this.lblChapterPage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		this.btnOpenDir = new Button(this, SWT.FLAT);
		this.btnOpenDir.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		{
			InputStream in = this.getClass().getResourceAsStream("/ico/folder.png");
			this.btnOpenDir.setImage(new Image(this.getDisplay(), in));
		}
		this.btnOpenDir.setVisible(false);
		{
			Listener lstOpenDir = new Listener() {
				public void handleEvent(Event event) {
					DownloadMonitor.this.openFolder(DownloadMonitor.this.path);
				}
			};
			this.btnOpenDir.addListener(SWT.Selection, lstOpenDir);
		}

		this.progBar = new ProgressBar(this, SWT.SMOOTH);
		this.progBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 4, 1));
	}

	/**
	 * Creates a runnable, which monitors a download progress and changes the status icon for the monitor
	 * 
	 * @param links array of URL you want to download
	 * @param path where should the files be placed?
	 * @param createZip create a zip out of the downloaded files=
	 * @return the runnable for further use
	 */
	private Runnable createRunner(final URL[] links, final String path) {
		if ((links == null) || (path == null)) {
			throw new NullPointerException("couldn't create thread to download\nreason: not all needed data");
		}

		// if zip should be created extend progressbar max by one, if not nr of links = max
		this.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (!DownloadMonitor.this.settings.getCreateZip()) {
					DownloadMonitor.this.progBar.setMaximum(links.length);
				} else {
					DownloadMonitor.this.progBar.setMaximum(links.length + 1);
				}
				DownloadMonitor.this.progBar.setSelection(0);

				// use last part of path = chapter name
				Matcher mat = Pattern.compile("(.*/)(.*)").matcher(path);
				if (mat.find()) {
					DownloadMonitor.this.lblChapterName.setText(mat.group(2));
				} else {
					DownloadMonitor.this.lblChapterName.setText(DownloadMonitor.this.res.getString("error.nameNotAvaible"));
				}
			}
		});

		Runnable runner = new Runnable() {
			public void run() {
				// make work in progress visible
				DownloadMonitor.this.setStatus(DownloadMonitor.WORKING);

				// download every single url in links
				int i = 0;
				String fileName = "";
				for (URL url : links) {
					fileName = url.toExternalForm().substring(url.toExternalForm().lastIndexOf("/") + 1);
					if (!fileName.equals("")) {
						if (!Net.download(url, fileName, path)) {
							System.err.println("couldn't download");
							DownloadMonitor.this.setStatus(DownloadMonitor.FAILURE);
						}
					}
					final int inn = i + 1;
					final String fileNameCopy = fileName.replaceAll("\\..*", "");
					DownloadMonitor.this.getDisplay().syncExec(new Runnable() {
						public void run() {
							DownloadMonitor.this.lblChapterPage.setText(fileNameCopy);
							DownloadMonitor.this.progBar.setSelection(inn);
						}
					});
					i++;
				}
				if (DownloadMonitor.this.settings.getCreateZip()) {
					new Zip(path);
				}

				DownloadMonitor.this.getDisplay().syncExec(new Runnable() {
					public void run() {
						DownloadMonitor.this.progBar.setSelection(DownloadMonitor.this.progBar.getMaximum());
						DownloadMonitor.this.setStatus(DownloadMonitor.SUCCESS);

						DownloadMonitor.this.btnRemove.setVisible(true);
						DownloadMonitor.this.btnOpenDir.setVisible(true);
					}
				});
			}
		};

		return runner;
	}

	/**
	 * Load the default language bundle
	 * 
	 * @return the default language bundle
	 */
	private ResourceBundle loadDefaultLanguage() {
		return ResourceBundle.getBundle("lang/default_lang", new XMLResourceBundleControl());
	}

	/**
	 * Try to load desired language file, if not possible fall back to default language (en)
	 * 
	 * @param lang the desired language
	 */
	private void loadLanguage(String lang) {
		try {
			this.res = ResourceBundle.getBundle("config/lang", new XMLResourceBundleControl());
		} catch (MissingResourceException e) {
			this.res = this.loadDefaultLanguage();
		}
	}

	/**
	 * Checks for the OS and use the right method to open the folder
	 * 
	 * @param path the folder you want to open
	 */
	private void openFolder(String path) {
		String os = System.getProperty("os.name").toUpperCase();

		// if zipping is active, get parent folder
		if (this.settings.getCreateZip()) {
			path = new File(path).getParent();
		}

		// use the appropiated method for each os, win, mac and all other (most likely unix/linux)
		if (os.contains("windows".toUpperCase())) {
			try {
				Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL \"" + path + "\"");
			} catch (IOException e) {
				System.err.println("Runtime Error: can't open " + path);
			}
		} else if (os.contains("mac".toUpperCase())) {
			try {
				Runtime.getRuntime().exec("open " + path);
			} catch (IOException e) {
				System.err.println("Runtime Error: can't open " + path);
			}
		} else {
			try {
				Runtime.getRuntime().exec("xdg-open " + path);
			} catch (IOException e) {
				System.err.println("Runtime Error: can't open " + path);
			}
		}
	}

	/**
	 * @param status
	 */
	private void setStatus(int status) {
		switch (status) {
			case DownloadMonitor.WORKING: {
				final InputStream in = this.getClass().getResourceAsStream("/ico/working.png");
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						DownloadMonitor.this.lblIconStatus.setImage(new Image(DownloadMonitor.this.getDisplay(), in));
					}
				});
				break;
			}
			case DownloadMonitor.SUCCESS: {
				final InputStream in = this.getClass().getResourceAsStream("/ico/success.png");
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						DownloadMonitor.this.lblIconStatus.setImage(new Image(DownloadMonitor.this.getDisplay(), in));
					}
				});
				break;
			}
			case DownloadMonitor.FAILURE: {
				final InputStream in = this.getClass().getResourceAsStream("/ico/failure.png");
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						DownloadMonitor.this.lblIconStatus.setImage(new Image(DownloadMonitor.this.getDisplay(), in));
					}
				});
				break;
			}
		}
	}
}
