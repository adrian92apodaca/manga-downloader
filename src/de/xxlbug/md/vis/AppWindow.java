/**
 * Description: The main interface
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
package de.xxlbug.md.vis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.xxlbug.md.helper.Settings;
import de.xxlbug.md.helper.XMLResourceBundleControl;
import de.xxlbug.md.manga.Chapter;
import de.xxlbug.md.manga.Manga;
import de.xxlbug.md.manga.OneManga;

/**
 * @author xxlbug
 * @date 06.01.2010
 */
public class AppWindow {

	private Button										btnDownload;
	private Button										btnToDownloads;
	private Composite									cmpDownloadList;
	private Composite									cmpMangaChapter;
	private Composite									cmpMangaInfo;
	private Composite									cmpNotice;
	private Group											grpChapterList;
	private Group											grpDownloadList;
	private Group											grpInfos;
	private Group											grpLicense;
	private Label											lblArtist;
	private Label											lblArtistName;
	private Label											lblAuthor;
	private Label											lblAuthorName;
	private Label											lblCategorie;
	private Label											lblCategorieText;
	private Label											lblCover;
	private Label											lblDescription;
	private Label											lblDescriptionText;
	private Label											lblLicenseText;
	private Label											lblNoticeText;
	private Label											lblTitle;
	private Manga[]										manga;
	private final OneManga						om;
	final private ThreadPoolExecutor	pool;
	private final int									poolSize;
	private ResourceBundle						res;
	private Settings									settings;
	private Shell											shlWindow;
	private ScrolledComposite					srCmp_ChapterDownload;
	private StackLayout								stLayout;
	private Table											tblMangaList;
	private Table											tblToChoose;
	private Text											txtSearch;

	public AppWindow(Manga[] manga) {
		this.om = new OneManga();
		this.manga = manga.clone();

		this.settings = new Settings();
		this.loadLanguage(this.settings.getLanguage());

		this.poolSize = this.settings.getNumberOfDownloads();
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.poolSize);
		this.pool.setKeepAliveTime(10, TimeUnit.SECONDS);
	}

	/**
	 * Open the window.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		Display display = Display.getDefault();
		this.createContents();
		this.shlWindow.layout();
		this.updateMangaList(this.manga);
		this.shlWindow.open();
		while (!this.shlWindow.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		System.exit(0);
	}

	/**
	 * If needed add the license label to the info composite otherwise just set the text accordingly
	 * 
	 * @param license the license text
	 */
	private void addLicenseLabel(String license) {
		if ((this.grpLicense == null) || this.grpLicense.isDisposed()) {
			this.grpLicense = new Group(this.cmpMangaInfo, SWT.NONE);
			this.grpLicense.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			this.grpLicense.setText(this.res.getString("license"));
			FillLayout fill = new FillLayout();
			fill.marginHeight = 2;
			fill.marginWidth = 2;
			this.grpLicense.setLayout(fill);

			this.lblLicenseText = new Label(this.grpLicense, SWT.WRAP);
			Color lightYellow = new Color(this.shlWindow.getDisplay(), 230, 230, 0);
			this.lblLicenseText.setBackground(lightYellow);
			lightYellow.dispose();
		}
		this.lblLicenseText.setText(license);
		this.grpInfos.layout();
	}

	/**
	 * Move the shell to the center of the screen
	 * 
	 * @param shell the shell which should be centered
	 */
	private void center(Shell shell) {
		Rectangle shellBounds = shell.getDisplay().getPrimaryMonitor().getBounds();
		Point winSize = shell.getSize();
		shell.setLocation(shellBounds.x + (shellBounds.width - winSize.x) / 2, shellBounds.y + (shellBounds.height - winSize.y) / 2);
	}

	/**
	 * Create contents of the window.
	 */
	private void createContents() {
		this.shlWindow = new Shell();
		this.shlWindow.setSize(1024, 768);
		this.center(this.shlWindow);
		this.shlWindow.setText("MangaDownloader [MD]");
		InputStream inAppIcon = this.getClass().getResourceAsStream("/ico/md.png");
		this.shlWindow.setImage(new Image(this.shlWindow.getDisplay(), inAppIcon));
		this.shlWindow.setLayout(new FillLayout(SWT.HORIZONTAL));
		{
			SashForm sshListInfo = new SashForm(this.shlWindow, SWT.NONE);
			{
				Composite cmpMangaList = new Composite(sshListInfo, SWT.NONE);
				cmpMangaList.setLayout(new GridLayout(1, false));
				{
					Group grpWebsites = new Group(cmpMangaList, SWT.NONE);
					grpWebsites.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
					grpWebsites.setLayout(new GridLayout(4, false));
					grpWebsites.setText(this.res.getString("general"));

					Combo cboWebsites = new Combo(grpWebsites, SWT.READ_ONLY);
					cboWebsites.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					cboWebsites.add(this.om.getName());
					cboWebsites.select(0);
					{
						Button btnPreferences = new Button(grpWebsites, SWT.PUSH);
						btnPreferences.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
						btnPreferences.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								new Preferences(AppWindow.this.shlWindow, AppWindow.this.settings);
								AppWindow.this.settings = new Settings();
								AppWindow.this.loadLanguage(AppWindow.this.settings.getLanguage());
							}
						});
						InputStream inPreferences = this.getClass().getResourceAsStream("/ico/preferences.png");
						btnPreferences.setImage(new Image(this.shlWindow.getDisplay(), inPreferences));
					}
					{
						Button btnInfo = new Button(grpWebsites, SWT.PUSH);
						btnInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
						btnInfo.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								new Info(AppWindow.this.shlWindow);
							}
						});
						InputStream in = this.getClass().getResourceAsStream("/ico/info.png");
						btnInfo.setImage(new Image(this.shlWindow.getDisplay(), in));
					}
				}

				{
					// search group with all options
					Group grpSearch = new Group(cmpMangaList, SWT.NONE);
					grpSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
					grpSearch.setLayout(new GridLayout(2, false));
					grpSearch.setText(this.res.getString("search"));
					{
						this.txtSearch = new Text(grpSearch, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
						{
							GridData gd_txtSearch = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
							this.txtSearch.setLayoutData(gd_txtSearch);
						}
						{
							Listener lstSearch = new Listener() {

								public void handleEvent(Event event) {
									if ((event.keyCode == SWT.CR) && !AppWindow.this.txtSearch.getText().equals("")) {
										ArrayList<Manga> result = new ArrayList<Manga>();
										for (Manga m : AppWindow.this.manga) {
											if (m.getName().toLowerCase().contains(AppWindow.this.txtSearch.getText().toLowerCase())) {
												result.add(m);
											}
										}

										if (result.size() > 0) {
											Manga[] actualResults = new Manga[result.size()];
											Iterator<Manga> iter = result.iterator();
											int i = 0;
											while (iter.hasNext()) {
												actualResults[i++] = iter.next();
											}
											AppWindow.this.updateMangaList(actualResults);
										} else {
											AppWindow.this.txtSearch.setBackground(AppWindow.this.txtSearch.getDisplay().getSystemColor(SWT.COLOR_RED));
										}
									} else if (AppWindow.this.txtSearch.getText().equals("")) {
										AppWindow.this.updateMangaList(AppWindow.this.manga);
									}
								}
							};

							this.txtSearch.addListener(SWT.KeyDown, lstSearch);
						}
					}
					{
						Button btnRefresh = new Button(grpSearch, SWT.PUSH);
						{
							GridData gd_btnRefresh = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
							btnRefresh.setLayoutData(gd_btnRefresh);
						}
						{
							Listener lstRefresh = new Listener() {

								public void handleEvent(Event event) {
									AppWindow.this.txtSearch.setText("");
									AppWindow.this.manga = AppWindow.this.om.getAllManga();
									AppWindow.this.updateMangaList(AppWindow.this.manga);
								}
							};
							btnRefresh.addListener(SWT.Selection, lstRefresh);
						}
						InputStream inRefresh = this.getClass().getResourceAsStream("/ico/refresh.png");
						btnRefresh.setImage(new Image(this.shlWindow.getDisplay(), inRefresh));
					}
				}
				{
					this.tblMangaList = new Table(cmpMangaList, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
					this.tblMangaList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
					this.tblMangaList.setLinesVisible(true);

					Listener lstMangaListChange = new Listener() {
						public void handleEvent(Event event) {
							if (AppWindow.this.tblMangaList.getSelectionCount() > 0) {
								String name = AppWindow.this.tblMangaList.getItem(AppWindow.this.tblMangaList.getSelectionIndex()).getText(0);
								for (int i = 0; i < AppWindow.this.manga.length; i++) {
									if (AppWindow.this.manga[i].getName().equals(name)) {
										if (AppWindow.this.manga[i].getChapters() == null) {
											AppWindow.this.manga[i] = AppWindow.this.om.getMangaData(AppWindow.this.manga[i]);
											AppWindow.this.updateMangaInfo(AppWindow.this.manga[i]);
											AppWindow.this.updateChapterInfo(AppWindow.this.manga[i]);
										} else {
											AppWindow.this.updateMangaInfo(AppWindow.this.manga[i]);
											AppWindow.this.updateChapterInfo(AppWindow.this.manga[i]);
										}
									}
								}
							}
						}
					};
					this.tblMangaList.addListener(SWT.Selection, lstMangaListChange);
				}
			}
			{
				SashForm sshInfoDownload = new SashForm(sshListInfo, SWT.VERTICAL);
				{
					this.cmpMangaInfo = new Composite(sshInfoDownload, SWT.NONE);
					this.cmpMangaInfo.setLayout(new GridLayout(2, false));
					{
						this.lblTitle = new Label(this.cmpMangaInfo, SWT.NONE);
						Font titleFont = new Font(this.shlWindow.getDisplay(), "Lucida Grande", 28, SWT.BOLD);
						this.lblTitle.setFont(titleFont);
						titleFont.dispose();
						{
							GridData gd_lblTitle = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
							this.lblTitle.setLayoutData(gd_lblTitle);
						}
						this.lblTitle.setVisible(false);
					}
					{
						this.grpInfos = new Group(this.cmpMangaInfo, SWT.NONE);
						this.grpInfos.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
						this.grpInfos.setText(this.res.getString("infos"));
						this.grpInfos.setLayout(new GridLayout(4, false));
						{
							this.lblAuthor = new Label(this.grpInfos, SWT.NONE);
							this.lblAuthor.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false, 1, 1));
							this.lblAuthor.setText(this.res.getString("author") + ":");
							this.lblAuthor.setVisible(false);
						}
						{
							this.lblAuthorName = new Label(this.grpInfos, SWT.NONE);
							this.lblAuthorName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							this.lblAuthorName.setVisible(false);
						}
						{
							this.lblArtist = new Label(this.grpInfos, SWT.NONE);
							this.lblArtist.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, true, false, 1, 1));
							this.lblArtist.setText(this.res.getString("artist") + ":");
							this.lblArtist.setVisible(false);
						}
						{
							this.lblArtistName = new Label(this.grpInfos, SWT.NONE);
							this.lblArtistName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							this.lblArtistName.setVisible(false);
						}
						{
							this.lblCategorie = new Label(this.grpInfos, SWT.NONE);
							this.lblCategorie.setLayoutData(new GridData(SWT.TRAIL, SWT.BEGINNING, false, false, 1, 1));
							this.lblCategorie.setText(this.res.getString("categories") + ":");
							this.lblCategorie.setVisible(false);
						}
						{
							this.lblCategorieText = new Label(this.grpInfos, SWT.WRAP);
							{
								GridData gd_lblCategorie = new GridData(SWT.FILL, SWT.TRAIL, true, false, 3, 1);
								gd_lblCategorie.widthHint = 150;
								this.lblCategorieText.setLayoutData(gd_lblCategorie);
							}
							this.lblCategorieText.setVisible(false);
						}
						{
							this.lblDescription = new Label(this.grpInfos, SWT.NONE);
							this.lblDescription.setLayoutData(new GridData(SWT.TRAIL, SWT.BEGINNING, false, false, 1, 1));
							this.lblDescription.setText(this.res.getString("description") + ":");
							this.lblDescription.setVisible(false);
						}
						{
							this.lblDescriptionText = new Label(this.grpInfos, SWT.WRAP | SWT.V_SCROLL);
							{
								GridData gd_lblDescription = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
								gd_lblDescription.heightHint = SWT.DEFAULT;
								gd_lblDescription.widthHint = 150;
								this.lblDescriptionText.setLayoutData(gd_lblDescription);
							}
							this.lblDescriptionText.setVisible(false);
						}
					}
					{
						Group grpCover = new Group(this.cmpMangaInfo, SWT.NONE);
						FillLayout fill = new FillLayout();
						fill.marginHeight = 2;
						fill.marginWidth = 2;
						grpCover.setLayout(fill);
						grpCover.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
						grpCover.setText(this.res.getString("cover"));

						this.lblCover = new Label(grpCover, SWT.NONE);
						this.lblCover.setVisible(false);
					}
				}
				{
					this.cmpMangaChapter = new Composite(sshInfoDownload, SWT.NONE);
					this.stLayout = new StackLayout();
					this.cmpMangaChapter.setLayout(this.stLayout);
					{
						this.grpChapterList = new Group(this.cmpMangaChapter, SWT.NONE);
						this.grpChapterList.setText(this.res.getString("chapter"));
						this.grpChapterList.setLayout(new GridLayout(2, false));

						this.tblToChoose = new Table(this.grpChapterList, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
						this.tblToChoose.setLinesVisible(true);
						this.tblToChoose.setHeaderVisible(true);
						this.tblToChoose.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

						final TableColumn title = new TableColumn(this.tblToChoose, SWT.LEAD, 0);
						final TableColumn group = new TableColumn(this.tblToChoose, SWT.CENTER, 1);
						final TableColumn date = new TableColumn(this.tblToChoose, SWT.CENTER, 2);
						title.setText("Title");
						group.setText("Scan-Group");
						date.setText("Date");

						// if resized, change the coloumn size according to the percentage
						this.tblToChoose.addListener(SWT.Resize, new Listener() {

							@Override
							public void handleEvent(Event event) {
								title.setWidth((int) (AppWindow.this.tblToChoose.getSize().x * 0.65));
								group.setWidth((int) (AppWindow.this.tblToChoose.getSize().x * 0.15));
								date.setWidth((int) (AppWindow.this.tblToChoose.getSize().x * 0.15));
							}
						});

						// if at least one chapter is selected, activate download button
						this.tblToChoose.addListener(SWT.Selection, new Listener() {

							@Override
							public void handleEvent(Event event) {
								if (AppWindow.this.tblToChoose.getSelectionCount() > 0) {
									AppWindow.this.btnDownload.setEnabled(true);
								}
							}
						});

						this.btnDownload = new Button(this.grpChapterList, SWT.PUSH);
						this.btnDownload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
						this.btnDownload.setText(this.res.getString("startDownload"));
						this.btnDownload.setEnabled(false);

						// --- Download list ----
						this.grpDownloadList = new Group(this.cmpMangaChapter, SWT.NONE);
						this.grpDownloadList.setLayout(new GridLayout(1, true));
						this.grpDownloadList.setText(this.res.getString("downloads"));

						// add a button to change back to chapter view
						Button btnBackToChapters = new Button(this.grpDownloadList, SWT.PUSH);
						btnBackToChapters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						btnBackToChapters.setText(this.res.getString("backToChapters"));
						// if pressed, change back to chapter view
						btnBackToChapters.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								AppWindow.this.stLayout.topControl = AppWindow.this.grpChapterList;
								AppWindow.this.cmpMangaChapter.layout();
							}
						});

						// create the scrolled composite for the download monitors
						this.srCmp_ChapterDownload = new ScrolledComposite(this.grpDownloadList, SWT.V_SCROLL);
						this.srCmp_ChapterDownload.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
						this.srCmp_ChapterDownload.setShowFocusedControl(true);
						this.srCmp_ChapterDownload.setExpandHorizontal(true);

						this.cmpDownloadList = new Composite(this.srCmp_ChapterDownload, SWT.NONE);
						this.cmpDownloadList.setLayout(new GridLayout(2, false));

						// show back to downloads button if download is active
						this.btnDownload.addListener(SWT.Selection, new Listener() {
							@Override
							public void handleEvent(Event event) {
								AppWindow.this.setTopControl(AppWindow.this.grpDownloadList, true);
							}
						});

						// create dowload monitors
						this.btnDownload.addListener(SWT.Selection, new Listener() {

							@Override
							public void handleEvent(Event event) {
								//set the pool size to the current settings value
								AppWindow.this.pool.setCorePoolSize(AppWindow.this.settings.getNumberOfDownloads());

								final DownloadMonitor[] monitor = new DownloadMonitor[AppWindow.this.tblToChoose.getSelectionCount()];

								Chapter[] selectedChapters = new Chapter[AppWindow.this.tblToChoose.getSelectionCount()];

								//get the selected manga
								Manga actualManga = null;
								if (AppWindow.this.tblMangaList.getSelectionIndex() != -1) {
									String searchForManga = AppWindow.this.tblMangaList.getItem(AppWindow.this.tblMangaList.getSelectionIndex()).getText();
									for (Manga m : AppWindow.this.manga) {
										if (m.getName().toUpperCase().equals(searchForManga.toUpperCase())) {
											actualManga = m;
										}
									}
								}

								// find the selected chapters and put them in "actualChapters"
								Chapter[] actualChapters = actualManga.getChapters();
								int[] selIndices = AppWindow.this.tblToChoose.getSelectionIndices();
								int inn = 0;
								for (Chapter c : actualChapters) {
									for (int i : selIndices) {
										if (AppWindow.this.tblToChoose.getItem(i).getText(0).toUpperCase().equals(c.getTitle().toUpperCase())) {
											selectedChapters[inn++] = c;
										}
									}
								}

								// build the adresslist for all pictures in all chapters
								ArrayList<URL[]> pictureLinks = new ArrayList<URL[]>();
								for (Chapter c : selectedChapters) {
									URL[] tempURL = AppWindow.this.om.getChapterPages(c.getLink());
									pictureLinks.add(tempURL);
								}

								int i = 0;
								for (URL[] urls : pictureLinks) {

									// create path for save dir according to settings
									String saveDir = AppWindow.this.settings.getSaveDirectory() + File.separator;
									if (AppWindow.this.settings.getCreateSeriesFolder()) {
										saveDir += actualManga.getName() + File.separator + selectedChapters[i].getTitle();
									} else {
										saveDir += selectedChapters[i].getTitle();
									}

									{ // create monitors
										monitor[i] = new DownloadMonitor(AppWindow.this.cmpDownloadList, AppWindow.this.pool, urls, saveDir, AppWindow.this.settings);
										monitor[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

										// add dispose listener to update layout of download list
										monitor[i].addListener(SWT.Dispose, new Listener() {
											@Override
											public void handleEvent(Event event) {
												Control c = ((Control) event.widget);
												GridData gd = (GridData) c.getLayoutData();
												gd.exclude = true;
												c.getParent().pack();
											}
										});

										// if no children are left, change to chapter view
										monitor[i].addListener(SWT.Dispose, new Listener() {

											@Override
											public void handleEvent(Event event) {
												Control[] monitors = ((Control) event.widget).getParent().getChildren();

												if (monitors.length < 2) {
													AppWindow.this.setTopControl(AppWindow.this.grpChapterList, false);
												}
											}
										});
									}
									i++;
								}
								AppWindow.this.cmpDownloadList.pack();

								// change the view
								AppWindow.this.stLayout.topControl = AppWindow.this.grpDownloadList;
								AppWindow.this.cmpMangaChapter.layout();
							}
						});

						// set the content of the scrolled composite and use the size of the content as min size
						this.srCmp_ChapterDownload.setContent(this.cmpDownloadList);
						this.srCmp_ChapterDownload.setMinSize(this.cmpDownloadList.computeSize(SWT.DEFAULT, SWT.DEFAULT));

						// notice label on third stack
						this.cmpNotice = new Composite(this.cmpMangaChapter, SWT.NONE);
						this.cmpNotice.setLayout(new GridLayout(1, true));
						{
							this.lblNoticeText = new Label(this.cmpNotice, SWT.WRAP);
							this.lblNoticeText.setAlignment(SWT.CENTER);
							{
								GridData gd_lblNoticeText = new GridData(SWT.FILL, SWT.CENTER, true, true);
								gd_lblNoticeText.widthHint = 150;
								gd_lblNoticeText.heightHint = 150;
								this.lblNoticeText.setLayoutData(gd_lblNoticeText);
							}
							Color lightRed = new Color(this.shlWindow.getDisplay(), 230, 0, 0);
							this.lblNoticeText.setForeground(lightRed);
							lightRed.dispose();
							this.lblNoticeText.setVisible(false);
						}

						// begin with chapter view
						this.stLayout.topControl = this.grpChapterList;
					}

				}
				sshInfoDownload.setWeights(new int[] { 350, 225 });
			}
			sshListInfo.setWeights(new int[] { 200, 600 });
		}
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
	 * Removes the license label if one is currently displayed, otherwise do nothing
	 */
	private void removeLicenseLabel() {
		if (this.grpLicense != null) {
			if (!this.grpLicense.isDisposed()) {
				this.grpLicense.dispose();
				this.cmpMangaInfo.layout();
			}
		}
	}

	/**
	 * Load the cover from the given path and sets the size of the label according to the cover image
	 * 
	 * @param path the img which should be used
	 */
	private void setCover(String path) {
		if (path != null) {
			try {
				// create cover img
				InputStream in = new FileInputStream(path);
				Image img = new Image(this.shlWindow.getDisplay(), in);
				this.lblCover.setImage(img);

				// set cover visible
				this.lblCover.setVisible(true);
			} catch (FileNotFoundException e) {
				System.err.println("FileNotFoundException in changeInfo");
			}

		} else {
			this.lblCover.setImage(new Image(this.shlWindow.getDisplay(), this.getClass().getResourceAsStream("/ico/no-cover.png")));
			// set cover visible
			this.lblCover.setVisible(true);
		}
	}

	/**
	 * Used for the chapter/download view, sets the top control in the stacklayout and adds a back to downloads button to chapterlist if necessary
	 * 
	 * @param topControl the composite which should be on top
	 * @param enableSwitchButton should there a switch button be added to chapterview?
	 */
	private void setTopControl(Composite topControl, boolean enableSwitchButton) {
		this.stLayout.topControl = topControl;

		if (enableSwitchButton) {
			if ((AppWindow.this.btnToDownloads == null) || this.btnToDownloads.isDisposed()) {
				// change grpChapterList layout
				{
					// tell btnDownload to use only one column
					GridData gd = (GridData) AppWindow.this.btnDownload.getLayoutData();
					gd.horizontalSpan = 1;
					AppWindow.this.btnDownload.setLayoutData(gd);

					// add new button to point at downloadlist composite
					AppWindow.this.btnToDownloads = new Button(AppWindow.this.grpChapterList, SWT.FLAT);
					{
						InputStream in = this.getClass().getResourceAsStream("/ico/next.png");
						AppWindow.this.btnToDownloads.setImage(new Image(AppWindow.this.shlWindow.getDisplay(), in));
					}
					GridData gdToDownloads = new GridData(SWT.TRAIL, SWT.CENTER, false, false);
					gdToDownloads.widthHint = 24;
					gdToDownloads.heightHint = 24;
					AppWindow.this.btnToDownloads.setLayoutData(gdToDownloads);

					Listener lstToDownloads = new Listener() {

						@Override
						public void handleEvent(Event event) {
							AppWindow.this.stLayout.topControl = AppWindow.this.grpDownloadList;
							AppWindow.this.cmpMangaChapter.layout();
						}
					};
					AppWindow.this.btnToDownloads.addListener(SWT.Selection, lstToDownloads);

					AppWindow.this.grpChapterList.layout();
				}
				// end change grpChapterList layout
			}
		} else {
			if ((this.btnToDownloads != null) && !this.btnToDownloads.isDisposed()) {
				this.btnToDownloads.dispose();

				// tell btnDownload to use two column again
				GridData gd = (GridData) AppWindow.this.btnDownload.getLayoutData();
				gd.horizontalSpan = 2;
				AppWindow.this.btnDownload.setLayoutData(gd);

				this.grpChapterList.layout();
			}
		}

		this.cmpMangaChapter.layout();
	}

	/**
	 * Change the chapter view for the manga
	 * 
	 * @param manga
	 */
	private void updateChapterInfo(Manga manga) {
		if ((manga.getNotice() == null) || manga.getNotice().equals("")) {
			Chapter[] chapters = manga.getChapters();
			this.tblToChoose.removeAll();

			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
			Color lightGreen = new Color(Display.getCurrent(), 0, 230, 0);

			Date now = new Date();
			long sevenDays = 1000 * 60 * 60 * 24 * 7;
			for (Chapter chapter : chapters) {
				TableItem item = new TableItem(this.tblToChoose, SWT.NONE);
				item.setText(0, chapter.getTitle());
				item.setText(1, chapter.getScanGroup());
				item.setText(2, formatter.format(chapter.getDate()));

				if (chapter.getDate().after(new Date(now.getTime() - sevenDays))) {
					item.setBackground(2, lightGreen);
				}
			}
			this.btnDownload.setEnabled(false);
			this.stLayout.topControl = this.grpChapterList;
			this.cmpMangaChapter.layout();
		} else {
			this.lblNoticeText.setText(manga.getNotice());
			this.lblNoticeText.setVisible(true);
			this.stLayout.topControl = this.cmpNotice;
			this.cmpMangaChapter.layout();
		}
	}

	/**
	 * Update the info view
	 * 
	 * @param m the manga with all neccessary infos
	 */
	private void updateMangaInfo(Manga m) {
		if (m.getName() != null) {
			this.lblTitle.setText(m.getName());
			this.lblTitle.setVisible(true);
		}

		if (m.getAuthor() != null) {
			this.lblAuthorName.setText(m.getAuthor());
			this.lblAuthorName.setVisible(true);
			this.lblAuthor.setVisible(true);
		}

		if (m.getArtist() != null) {
			this.lblArtistName.setText(m.getArtist());
			this.lblArtistName.setVisible(true);
			this.lblArtist.setVisible(true);
		}

		if (m.getDescription() != null) {
			this.lblDescriptionText.setText(m.getDescription());
			this.lblDescriptionText.setVisible(true);
			this.lblDescription.setVisible(true);
		}

		if (m.getCategories() != null) {
			String[] cat = m.getCategories();
			String catTemp = "";
			for (String s : cat) {
				if (s.length() != 0) {
					s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
				}
				if (catTemp.length() > 0) {
					catTemp = catTemp.concat(", " + s);
				} else {
					catTemp = s;
				}

			}
			this.lblCategorieText.setText(catTemp);
			this.lblCategorieText.setVisible(true);
			this.lblCategorie.setVisible(true);
		}

		// license
		if ((m.getLicense() != null) && !m.getLicense().equals("")) {
			this.addLicenseLabel(m.getLicense());
		} else {
			this.removeLicenseLabel();
		}

		// cover
		if (m.getPathToCover() != null) {
			if (!m.getPathToCover().equals("")) {
				if (!new File(m.getPathToCover()).exists()) {
					// download cover
					String path = this.om.download(m.getPathToCover());
					if (path != null) {
						// if download was successfull
						m.setPathToCover(path);
						this.setCover(path);
					} else {
						// if download was not successfull
						this.setCover(null);
					}
				} else {
					this.setCover(m.getPathToCover());
				}
			} else {
				// if the cover url retrieved from the website is empty
				this.lblCover.setImage(null);
				this.lblCover.setVisible(false);
			}
		} else {
			// if no cover from the website is avaible
			this.lblCover.setImage(null);
			this.lblCover.setVisible(false);
		}
		this.cmpMangaInfo.layout();
	}

	/**
	 * Build the list of manga from the scratch
	 * 
	 * @param mangas all avaible manga
	 */
	private void updateMangaList(Manga[] mangas) {
		this.tblMangaList.removeAll();

		for (Manga m : mangas) {
			TableItem item = new TableItem(this.tblMangaList, SWT.NONE);
			item.setText(m.getName());
		}

		this.tblMangaList.setSelection(0);
		this.tblMangaList.notifyListeners(SWT.Selection, new Event());
	}
}
