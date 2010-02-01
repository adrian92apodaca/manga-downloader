/**
 * Description: This class represents the the saving of an zip file
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
package de.xxlbug.md.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author xxlbug
 * @date 07.01.2010
 */
public class Zip {
	public Zip(String folder) {
		File path = new File(folder);

		if (path.exists() && path.getParentFile().canWrite()) {
			this.zipping(path);
		}
	}

	/**
	 * Remove the given file
	 * 
	 * @param file the file/folder you want removed
	 * @throws SecurityException if remove is not possible
	 */
	private boolean removeFile(File file) {
		if (!file.delete()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					if (!this.removeFile(f)) {
						throw new SecurityException("can't delete folder");
					}
				}
				this.removeFile(file);
			} else {
				throw new SecurityException("can't delete folder");
			}
		} else {
			return true;
		}
		return false;
	}

	/**
	 * Zip's a given file/folder
	 * 
	 * @param f the file/folder you want zipped
	 * @return true if zipping was sucessfull
	 */
	private boolean zipping(File f) {
		try {
			FileOutputStream out = new FileOutputStream(f.getParent() + File.separator + f.getName() + ".zip");
			ZipOutputStream zOut = new ZipOutputStream(out);

			File[] list = f.listFiles();
			if (list != null) {
				for (File file : list) {
					zOut.putNextEntry(new ZipEntry(file.getName()));

					// write data....
					FileInputStream in = new FileInputStream(file);

					// Create a buffer for reading the files
					byte[] buf = new byte[1024];

					// Transfer bytes from the file to the ZIP file
					int len;
					while ((len = in.read(buf)) > 0) {
						zOut.write(buf, 0, len);
					}
					in.close();
					// end write data....

					zOut.closeEntry();
				}
			} else {
				zOut.putNextEntry(new ZipEntry(f.getName()));

				// write data....
				FileInputStream in = new FileInputStream(f);

				// Create a buffer for reading the files
				byte[] buf = new byte[1024];

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					zOut.write(buf, 0, len);
				}
				in.close();
				// end write data....

				zOut.closeEntry();
			}
			zOut.close();

			try {
				this.removeFile(f);
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			return true;
		} catch (IOException e) {
			System.err.println("Error creating zip-file");
			return false;
		}
	}
}
