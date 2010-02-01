/**
 * Description: Created: 05.01.2010
 */
package de.xxlbug.md.vis;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.xxlbug.md.helper.Settings;
import de.xxlbug.md.helper.XMLResourceBundleControl;

/**
 * @author xxlbug
 * @date 05.01.2010
 */
public class Preferences {
	private boolean					change;
	private boolean					createZip;
	private String					lang;
	private int							numberOfDownloads;
	private String					path;
	private ResourceBundle	res;
	private boolean					seriesFolder;

	private final Settings	settings;
	private Shell						shlPreferences;

	public Preferences(Shell parent, Settings settings) {
		this.change = false;

		this.settings = settings;
		this.path = settings.getSaveDirectory();

		this.loadLanguage(this.settings.getLanguage());
		this.open(parent);
	}

	/**
	 * Open the window.
	 */
	public void open(Shell parent) {
		Display display = Display.getDefault();

		// if lang file is corrupted fall back to default
		if (!this.createContents(parent)) {
			this.res = this.loadDefaultLanguage();
			this.createContents(parent);
		}

		this.positionInCenter();
		this.shlPreferences.open();
		this.shlPreferences.layout();
		while (!this.shlPreferences.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * 
	 * @return true if all lang statements could be loaded otherwise false
	 */
	private boolean createContents(Shell parent) {
		this.shlPreferences = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		this.shlPreferences.setLayout(new FillLayout(SWT.HORIZONTAL));

		try {
			this.shlPreferences.setText(this.res.getString("pref.title"));
			{
				Composite cmpMaster = new Composite(this.shlPreferences, SWT.NONE);
				cmpMaster.setLayout(new GridLayout(3, false));

				{
					Label lblWhereShouldYour = new Label(cmpMaster, SWT.NONE);
					lblWhereShouldYour.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false, 1, 1));
					lblWhereShouldYour.setText(this.res.getString("pref.saveDir"));
				}
				{
					final Text txtSaveDir = new Text(cmpMaster, SWT.BORDER);
					txtSaveDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
					txtSaveDir.setText(this.settings.getSaveDirectory());
					{
						Button btnDirSel = new Button(cmpMaster, SWT.NONE);
						btnDirSel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
						Image imgFolder = new Image(btnDirSel.getDisplay(), this.getClass().getResourceAsStream("/ico/folder.png"));
						btnDirSel.setImage(imgFolder);

						btnDirSel.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
								// not used
							}

							@Override
							public void widgetSelected(SelectionEvent e) {
								final String newPath = new DirectoryDialog(Preferences.this.shlPreferences, SWT.APPLICATION_MODAL).open();

								if (newPath != null) {
									Preferences.this.change = true;
									Preferences.this.path = newPath;

									Preferences.this.shlPreferences.getDisplay().syncExec(new Runnable() {

										@Override
										public void run() {
											txtSaveDir.setText(Preferences.this.path);
										}
									});
								}
							}
						});
					}
				}
				{
					Label lblWhichLanguage = new Label(cmpMaster, SWT.NONE);
					lblWhichLanguage.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false, 1, 1));
					lblWhichLanguage.setText(this.res.getString("pref.lang"));
				}
				{
					Combo cmbLang = new Combo(cmpMaster, SWT.NONE);
					cmbLang.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
					cmbLang.add("English");
					cmbLang.select(0);
					cmbLang.setEnabled(false);
				}
				{
					Label lblCreateFolder = new Label(cmpMaster, SWT.NONE);
					lblCreateFolder.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false, 1, 1));
					lblCreateFolder.setText(this.res.getString("pref.createFolder"));
				}
				{
					Composite cmpSeriesFolder = new Composite(cmpMaster, SWT.NONE);
					cmpSeriesFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
					cmpSeriesFolder.setLayout(new GridLayout(2, false));
					{
						Button btnYes = new Button(cmpSeriesFolder, SWT.RADIO);
						btnYes.setText(this.res.getString("yes"));

						Button btnNo = new Button(cmpSeriesFolder, SWT.RADIO);
						btnNo.setText(this.res.getString("no"));

						// select the button based on the current settings
						if (this.settings.getCreateSeriesFolder()) {
							btnYes.setSelection(true);
							this.seriesFolder = true;
						} else {
							btnNo.setSelection(true);
							this.seriesFolder = false;
						}

						Listener lstYes = new Listener() {

							@Override
							public void handleEvent(Event event) {
								Preferences.this.change = true;
								Preferences.this.seriesFolder = true;
							}
						};

						Listener lstNo = new Listener() {

							@Override
							public void handleEvent(Event event) {
								Preferences.this.change = true;
								Preferences.this.seriesFolder = false;
							}
						};

						btnYes.addListener(SWT.Selection, lstYes);
						btnNo.addListener(SWT.Selection, lstNo);
					}
				}
				{
					Label lblCreateZip = new Label(cmpMaster, SWT.None);
					lblCreateZip.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, true, false, 1, 1));
					lblCreateZip.setText(this.res.getString("pref.createZip"));
				}
				{
					Composite cmpCreateZip = new Composite(cmpMaster, SWT.NONE);
					cmpCreateZip.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
					cmpCreateZip.setLayout(new GridLayout(2, false));
					{
						Button btnYes = new Button(cmpCreateZip, SWT.RADIO);
						btnYes.setText(this.res.getString("yes"));

						Button btnNo = new Button(cmpCreateZip, SWT.RADIO);
						btnNo.setText(this.res.getString("no"));

						// select the button based on the current settings
						if (this.settings.getCreateZip()) {
							btnYes.setSelection(true);
							this.createZip = true;
						} else {
							btnNo.setSelection(true);
							this.createZip = false;
						}

						Listener lstYes = new Listener() {

							@Override
							public void handleEvent(Event event) {
								Preferences.this.change = true;
								Preferences.this.createZip = true;
							}
						};

						Listener lstNo = new Listener() {

							@Override
							public void handleEvent(Event event) {
								Preferences.this.change = true;
								Preferences.this.createZip = false;
							}
						};

						btnYes.addListener(SWT.Selection, lstYes);
						btnNo.addListener(SWT.Selection, lstNo);
					}
				}

				// number of simultaniously downloads
				{
					Label lblNumberDownloads = new Label(cmpMaster, SWT.NONE);
					lblNumberDownloads.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false));
					lblNumberDownloads.setText(this.res.getString("pref.numberDownloads"));

					final Scale scaNumberDownloads = new Scale(cmpMaster, SWT.READ_ONLY | SWT.BORDER);
					scaNumberDownloads.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
					scaNumberDownloads.setMinimum(1);
					scaNumberDownloads.setMaximum(5);
					scaNumberDownloads.setIncrement(1);
					scaNumberDownloads.setPageIncrement(1);
					scaNumberDownloads.setSelection(this.settings.getNumberOfDownloads());

					final Label lblScale = new Label(cmpMaster, SWT.NONE);
					lblScale.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));
					lblScale.setText(String.valueOf(this.settings.getNumberOfDownloads()));
					{
						// show actual scale selection in label
						Listener lstNumberOfDownloads = new Listener() {

							@Override
							public void handleEvent(Event event) {
								lblScale.setText(String.valueOf(scaNumberDownloads.getSelection()));
								Preferences.this.numberOfDownloads = scaNumberDownloads.getSelection();
								Preferences.this.change = true;
							}
						};
						scaNumberDownloads.addListener(SWT.Selection, lstNumberOfDownloads);
					}
				}

				// save and cancel buttons
				{
					Button btnCancel = new Button(cmpMaster, SWT.NONE);
					btnCancel.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false, 2, 1));
					btnCancel.setText(this.res.getString("cancel"));

					btnCancel.addSelectionListener(new SelectionListener() {

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							// not used
						}

						@Override
						public void widgetSelected(SelectionEvent e) {
							if (Preferences.this.change) {
								MessageBox box = new MessageBox(Preferences.this.shlPreferences, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
								box.setMessage("You don't want to save your changes?");

								switch (box.open()) {
									case SWT.OK:
										Preferences.this.shlPreferences.dispose();
										break;

									case SWT.CANCEL:
										break;
								}
							} else {
								Preferences.this.shlPreferences.dispose();
							}
						}
					});
				}
				{
					Button btnSave = new Button(cmpMaster, SWT.NONE);
					btnSave.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
					btnSave.setText(this.res.getString("save"));

					btnSave.addSelectionListener(new SelectionListener() {

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							// not used
						}

						@Override
						public void widgetSelected(SelectionEvent e) {
							if (Preferences.this.change) {
								Preferences.this.settings.setSaveDirectory(Preferences.this.path);
								Preferences.this.settings.setLanguage(Preferences.this.lang);
								Preferences.this.settings.setCreateSeriesFolder(Preferences.this.seriesFolder);
								Preferences.this.settings.setCreateZip(Preferences.this.createZip);
								Preferences.this.settings.setNumberOfDownloads(Preferences.this.numberOfDownloads);
								Preferences.this.settings.saveSettings();
							}
							Preferences.this.shlPreferences.dispose();
						}
					});
				}
			}
			this.shlPreferences.pack();
			return true;
		} catch (MissingResourceException e) {
			return false;
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
	 * Position in the center of the parent composite, if not avaible in the center of the main screen
	 */
	private void positionInCenter() {

		Rectangle shellBounds;
		if (this.shlPreferences.getParent() != null) {
			shellBounds = this.shlPreferences.getParent().getBounds();
		} else {
			shellBounds = this.shlPreferences.getDisplay().getPrimaryMonitor().getBounds();
		}
		Point winSize = this.shlPreferences.getSize();
		this.shlPreferences.setLocation(shellBounds.x + (shellBounds.width - winSize.x) / 2, shellBounds.y + (shellBounds.height - winSize.y) / 2);
	}
}
