/**
 * Description: Some copyright info about the program
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
package de.xxlbug.md.vis;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Info {
	public Info(Shell parent) {
		Shell shlMain = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		shlMain.setText("Info");
		shlMain.setSize(200, 200);
		shlMain.setBackground(shlMain.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		shlMain.setLayout(new GridLayout(1, true));

		{
			Label lblInfoAuthor = new Label(shlMain, SWT.NONE);
			lblInfoAuthor.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			lblInfoAuthor.setText("Author: Steffen Splitt");
		}
		{
			Label lblSeperator = new Label(shlMain, SWT.SEPARATOR | SWT.HORIZONTAL);
			lblSeperator.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		}
		{
			Label lblIconCopyrightLogo = new Label(shlMain, SWT.NONE);
			lblIconCopyrightLogo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			lblIconCopyrightLogo.setText("MD-Logo by Steffen Splitt");

			Label lblIconCopyrightTerratag = new Label(shlMain, SWT.NONE);
			lblIconCopyrightTerratag.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			lblIconCopyrightTerratag.setText("Laughing Man by Paul Nicholson");

			Label lblIconCopyrightDryIncon = new Label(shlMain, SWT.NONE);
			lblIconCopyrightDryIncon.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			lblIconCopyrightDryIncon.setText("All other icons by http://dryicons.com");
		}
		{
			Label lblSeperator = new Label(shlMain, SWT.SEPARATOR | SWT.HORIZONTAL);
			lblSeperator.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		}
		{
			Label lblInfoGPL = new Label(shlMain, SWT.NONE);
			lblInfoGPL.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			lblInfoGPL.setText("Published under the GPL V3.");
		}
		{
			Label lblInfoSource = new Label(shlMain, SWT.NONE);
			lblInfoSource.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			lblInfoSource.setText("manga-downloader.googlecode.com");
		}
		{
			Button btnClose = new Button(shlMain, SWT.PUSH);
			btnClose.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
			btnClose.setText("Close");
			btnClose.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(Event event) {
					((Control) event.widget).getParent().dispose();
				}
			});
		}

		shlMain.addListener(SWT.Close, new Listener() {

			@Override
			public void handleEvent(Event event) {
				event.widget.dispose();
			}
		});

		// Move the dialog to the center of the top level shell.
		Rectangle shellBounds = shlMain.getParent().getBounds();
		Point winSize = shlMain.getSize();
		shlMain.setLocation(shellBounds.x + (shellBounds.width - winSize.x) / 2, shellBounds.y + (shellBounds.height - winSize.y) / 2);

		shlMain.pack();
		shlMain.open();
	}
}
