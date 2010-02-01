/**
 * Description: Splash screen with some infos and loads the first set of data before the main window opens and the user can interact
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
package de.xxlbug.md.vis;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import de.xxlbug.md.manga.Manga;
import de.xxlbug.md.manga.OneManga;

/**
 * @author xxlbug
 * @date 05.01.2010
 */
public class Splash {
	private Shell	shlMain;

	public Splash() {
		Display display = Display.getDefault();
		this.createWindow(display);

		while (!display.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (SWTException e) {
			}
		}
	}

	/**
	 * 
	 */
	private void createDescription() {
		{
			Label lblAuthor_1 = new Label(this.shlMain, SWT.NONE);
			lblAuthor_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblAuthor_1.setText("Author:");
		}
		Label lblAuthor = new Label(this.shlMain, SWT.None);
		lblAuthor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblAuthor.setText("Steffen Splitt");

		{
			Label lblCopyright = new Label(this.shlMain, SWT.NONE);
			lblCopyright.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblCopyright.setText("Copyright:");

			Label lblCopyrightText = new Label(this.shlMain, SWT.None);
			lblCopyrightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblCopyrightText.setText("For private use only");
		}
		{
			Label lblCopyYear = new Label(this.shlMain, SWT.NONE);
			lblCopyYear.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
			lblCopyYear.setText("\u00A9 2010");
		}
	}

	/**
	 * puts a image in a label
	 */
	private void createImage() {
		InputStream in = this.getClass().getResourceAsStream("/ico/LaughingMan.png");
		Image img = new Image(this.shlMain.getDisplay(), in);
		this.shlMain.setLayout(new GridLayout(2, true));

		Label lblImg = new Label(this.shlMain, SWT.None);
		lblImg.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 2, 1));
		lblImg.setImage(img);
	}

	/**
	 * 
	 */
	private void createWaiter() {
		final ProgressBar pbrWaiter = new ProgressBar(this.shlMain, SWT.NONE);
		pbrWaiter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		// if progressbar is visible, begin
		final Runnable counter = new Runnable() {
			public void run() {
				pbrWaiter.setMaximum(3);
				pbrWaiter.setSelection(0);

				OneManga om = new OneManga();
				Manga[] m = om.getAllManga();
				pbrWaiter.setSelection(1);

				m[0] = om.getMangaData(m[0]);
				pbrWaiter.setSelection(2);

				AppWindow app = new AppWindow(m);
				pbrWaiter.setSelection(3);

				Splash.this.shlMain.dispose();
				app.open();
			}
		};

		Listener showListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.display.syncExec(counter);
			}
		};
		this.shlMain.addListener(SWT.Activate, showListener);
	}

	/**
	 * builds the main window
	 */
	private void createWindow(Display display) {
		this.shlMain = new Shell(display);
		this.shlMain.setText("Starting...");
		{
			InputStream in = this.getClass().getResourceAsStream("/ico/md.png");
			this.shlMain.setImage(new Image(display, in));
		}
		this.shlMain.setSize(360, 460);
		this.shlMain.setMinimumSize(new Point(300, 300));

		this.createImage();
		this.createDescription();
		this.createWaiter();

		this.shlMain.setMinimumSize(300, 300);
		// Move the dialog to the center of the top level shell.
		Rectangle shellBounds = display.getPrimaryMonitor().getBounds();
		Point winSize = this.shlMain.getSize();
		this.shlMain.setLocation(shellBounds.x + (shellBounds.width - winSize.x) / 2, shellBounds.y + (shellBounds.height - winSize.y) / 2);

		this.shlMain.pack();
		this.shlMain.open();
	}
}
