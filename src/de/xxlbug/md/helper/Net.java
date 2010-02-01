/**
 * Description: Created: 07.01.2010
 */
package de.xxlbug.md.helper;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author xxlbug
 * @date 07.01.2010
 */
public class Net {
	/**
	 * Load the file from the given URL to the tmp-directory and returns the path of the tmp-file
	 * 
	 * @param adress what you want to download
	 * @return the path, where the file can be found in the tmp-dir
	 */
	public static String download(URL adress) {
		DataInputStream in = null;
		FileOutputStream out = null;

		File tmp = null;
		try {
			tmp = File.createTempFile("md_", ".tmp");
			tmp.deleteOnExit();
		} catch (IOException e) {
			System.err.println("Error downloading picture:");
			System.err.println("  couldn't write to file");
			return null;
		}

		try {
			URLConnection fileStream = adress.openConnection();

			// Open the input streams for the remote file
			out = new FileOutputStream(tmp);
			in = new DataInputStream(fileStream.getInputStream());

			// Read the remote on and save the file
			int data;
			while ((data = in.read()) != -1) {
				out.write(data);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error downloading picture:");
			System.err.println("  couldn't write to tmp file");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println("Error downloading picture:");
			System.err.println("  couldn't write to file");
			return null;
		} finally {
			try {
				in.close();
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return tmp.getAbsolutePath();
	}

	/**
	 * Load the file from the given URL and put it in the given path with the given name
	 * 
	 * @param adress what to download
	 * @param name rename to
	 * @param path move to
	 * @return if download and moving was sucessfull
	 */
	public static boolean download(URL adress, String name, String path) {
		if ((adress == null) || (name == null) || name.equals("") || (path == null) || path.equals("")) {
			return false;
		}

		String pathOfFile = "";

		pathOfFile = Net.download(adress);
		if (pathOfFile == null) {
			pathOfFile = "";
		}

		File tmpPath = new File(path);
		if (!tmpPath.exists()) {
			if (!tmpPath.mkdirs()) {
				throw new SecurityException("can't create folder");
			}
		}
		if (!tmpPath.canWrite()) {
			throw new SecurityException("can't write to folder");
		} else {
			if (!new File(pathOfFile).renameTo(new File(path + File.separator + name))) {
				throw new SecurityException("can't move tmp file");
			} else {
				return true;
			}
		}
	}
}
