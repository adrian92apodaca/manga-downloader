/**
 * Description: Created: 07.01.2010
 */
package de.xxlbug.md.manga;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.HasSiblingFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.StringFilter;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author xxlbug
 * @date 07.01.2010
 */
public class OneManga implements Website {

	/*
	 * (non-Javadoc)
	 * @see de.xxlbug.md.manga.Website#download(java.lang.String)
	 */
	public String download(String adress) {
		String path = null;
		try {
			URLConnection fileStream = new URL(adress).openConnection();

			File tmp = File.createTempFile("md_", "_tmp");
			tmp.deleteOnExit();

			DataInputStream in = null;
			FileOutputStream out = null;

			// Open the input streams for the remote file
			out = new FileOutputStream(tmp);
			in = new DataInputStream(fileStream.getInputStream());

			// Read the remote on and save the file
			int data;
			while ((data = in.read()) != -1) {
				out.write(data);
			}

			in.close();
			out.flush();
			out.close();

			path = tmp.getAbsolutePath();
		} catch (MalformedURLException e) {
			System.err.println("Can't download file. Wrong adress?");
		} catch (IOException e) {
			System.err.println("Can't create temp-file.");
		}
		return path;
	}

	/*
	 * (non-Javadoc)
	 * @see de.xxlbug.md.manga.Website#getAllManga()
	 */
	public Manga[] getAllManga() {
		return this.getManga();
	}

	/**
	 * Collect all links for the chapter pages based on the first page of a chapter
	 * 
	 * @param link should point to the first chapter page
	 * @return an array with all urls ready for download
	 */
	public URL[] getChapterPages(String link) {
		URL[] urls = null;
		String type = "";
		String server = "";

		try {
			Parser parser = new Parser(this.getChapterFirstPage(new URL(link)).openConnection());

			// filter
			NodeFilter selectTag = new NodeClassFilter(SelectTag.class);
			NodeFilter pageSelect = new HasAttributeFilter("id", "id_page_select");
			NodeFilter selectPage = new AndFilter(selectTag, pageSelect);

			NodeFilter optionTag = new NodeClassFilter(OptionTag.class);
			NodeFilter optionSelectPage = new AndFilter(optionTag, new HasParentFilter(selectPage));

			NodeFilter imgTag = new NodeClassFilter(ImageTag.class);
			NodeFilter mangaPage = new HasAttributeFilter("class", "manga-page");
			NodeFilter imgMangaPage = new AndFilter(imgTag, mangaPage);

			NodeFilter divTag = new NodeClassFilter(Div.class);
			NodeFilter chapNav = new AndFilter(divTag, new HasAttributeFilter("class", "chapter-navigation"));
			NodeFilter onePage = new AndFilter(divTag, new HasAttributeFilter("class", "one-page"));
			NodeFilter divChapOrOne = new OrFilter(chapNav, onePage);
			// end filter

			NodeList all = parser.parse(divChapOrOne);
			NodeList pages = new NodeList();
			NodeList img = new NodeList();

			NodeIterator n = all.elements();
			while (n.hasMoreNodes()) {
				Node t = n.nextNode();
				t.collectInto(pages, optionSelectPage);
				t.collectInto(img, imgMangaPage);
			}

			// get infos from the img tag
			if ((img.size() > 0) && (img.elementAt(0).getClass() == ImageTag.class)) {
				ImageTag image = (ImageTag) img.elementAt(0);

				String imgUrl = image.extractImageLocn();
				type = imgUrl.substring(imgUrl.lastIndexOf("."));

				// extract media server for this chapter
				String regex = "(.*/)([\\w\\d\\-]{2,}\\.[\\w]{3,4})";
				Pattern pat = Pattern.compile(regex);
				Matcher mat = pat.matcher(imgUrl);
				if (mat.find()) {
					// group 1 = media.onemanga.com.....
					server = mat.group(1);
				}

				// put page names and server together to a url
				if ((pages.size() > 0) && (pages.elementAt(0).getClass() == OptionTag.class)) {
					urls = new URL[pages.size()];
					NodeIterator iter = pages.elements();
					int i = 0;
					while (iter.hasMoreNodes()) {
						OptionTag opt = (OptionTag) iter.nextNode();
						urls[i++] = new URL(server + opt.getValue() + type);
					}
				}
			}

			return urls;
		} catch (ParserException e) {
			System.err.println("ParserException in getChapterPages");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			System.err.println("MalformedURLException in getChapterPages");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException in getChapterPages");
			e.printStackTrace();
		}
		return urls;
	}

	/**
	 * Extract data for avaible chapter and info about the manga
	 * 
	 * @param original where all infos can be obtained
	 * @return the Manga object
	 */
	public Manga getMangaData(Manga original) {
		Manga m = original;

		try {
			Parser parser = new Parser(m.getLink().openConnection());
			NodeList nl = parser.parse(null);

			m.setChapters(this.getChapterData(nl));

			// extract data for the manga
			NodeFilter divTag = new NodeClassFilter(Div.class);
			NodeFilter sideContent = new HasAttributeFilter("id", "content-side");
			NodeFilter divContentSide = new AndFilter(divTag, sideContent);

			// extract data for license and/or notice
			NodeFilter licenseNote = new AndFilter(divTag, new HasAttributeFilter("class", "license-note"));
			NodeFilter noticeNote = new AndFilter(divTag, new HasAttributeFilter("class", "removed-note"));

			// -----
			NodeList mangaData = new NodeList();
			NodeList licenseData = new NodeList();
			NodeList noticeData = new NodeList();

			// actual extracting
			NodeIterator iter = nl.elements();
			while (iter.hasMoreNodes()) {
				Node tmp = iter.nextNode();

				tmp.collectInto(mangaData, divContentSide);
				tmp.collectInto(licenseData, licenseNote);
				tmp.collectInto(noticeData, noticeNote);
			}

			// --- manga data part
			// license
			if (licenseData.size() == 1) {
				m.setLicense(licenseData.elementAt(0).getFirstChild().getText().trim());
			} else {
				m.setLicense(null);
			}
			// notice
			if (noticeData.size() == 1) {
				m.setNotice(noticeData.elementAt(0).getLastChild().getText().trim());
			} else {
				m.setNotice(null);
			}

			// preparing tag filter
			NodeFilter imgTag = new NodeClassFilter(ImageTag.class);
			NodeFilter linkTag = new NodeClassFilter(LinkTag.class);
			NodeFilter pTag = new NodeClassFilter(ParagraphTag.class);
			NodeFilter hTag = new NodeClassFilter(HeadingTag.class);
			NodeFilter spanTag = new NodeClassFilter(Span.class);

			//summary
			NodeFilter summaryFilter = new StringFilter("Summary");
			NodeFilter summaryHeader = new AndFilter(hTag, new HasChildFilter(summaryFilter));
			NodeFilter divAndSummary = new AndFilter(divTag, new HasSiblingFilter(summaryHeader));
			NodeFilter pDiv = new AndFilter(pTag, new HasParentFilter(divTag));
			NodeFilter summary = new AndFilter(pDiv, new HasParentFilter(divAndSummary));

			// author, artist
			NodeFilter authorFilter = new StringFilter("Author");
			NodeFilter artistFilter = new StringFilter("Artist");
			NodeFilter pAuthor = new AndFilter(pTag, new HasChildFilter(authorFilter));
			NodeFilter pArtist = new AndFilter(pTag, new HasChildFilter(artistFilter));

			// status
			NodeFilter chaptersFilter = new StringFilter("Chapters");
			NodeFilter spanParentChapters = new AndFilter(spanTag, new HasSiblingFilter(chaptersFilter));

			// categories
			NodeFilter categories = new StringFilter("Categories");
			NodeFilter seriesInfo = new AndFilter(spanTag, new HasAttributeFilter("class", "series-info"));
			NodeFilter spanCategories = new AndFilter(seriesInfo, new HasSiblingFilter(categories));
			NodeFilter linkSpanCategories = new AndFilter(linkTag, new HasParentFilter(spanCategories));

			//preparing lists
			NodeList logoList = new NodeList();
			NodeList artistList = new NodeList();
			NodeList authorList = new NodeList();
			NodeList descriptionList = new NodeList();
			NodeList statusList = new NodeList();
			NodeList categoriesList = new NodeList();

			iter = mangaData.elements();
			while (iter.hasMoreNodes()) {

				Node node = iter.nextNode();
				node.collectInto(logoList, imgTag);
				node.collectInto(authorList, pAuthor);
				node.collectInto(artistList, pArtist);
				node.collectInto(descriptionList, summary);
				node.collectInto(statusList, spanParentChapters);
				node.collectInto(categoriesList, linkSpanCategories);
			}

			if (!(logoList.size() == 0)) {
				try {
					ImageTag img = (ImageTag) logoList.elementAt(0);
					m.setPathToCover(img.extractImageLocn());
				} catch (NullPointerException e) {
					m.setPathToCover(null);
				}
			} else {
				m.setPathToCover(null);
			}

			if (!(authorList.size() == 0)) {
				try {
					LinkTag lnk = ((LinkTag) authorList.elementAt(0).getLastChild().getFirstChild());
					m.setAuthor(lnk.getLinkText());
				} catch (NullPointerException e) {
					m.setAuthor(null);
				}
			} else {
				m.setAuthor(null);
			}

			if (!(artistList.size() == 0)) {
				try {
					LinkTag lnk = ((LinkTag) artistList.elementAt(0).getLastChild().getFirstChild());
					m.setArtist(lnk.getLinkText());
				} catch (NullPointerException e) {
					m.setArtist(null);
				}
			} else {
				m.setArtist(null);
			}

			if (!(descriptionList.size() == 0)) {
				try {
					m.setDescription(this.replaceSpecialChars(descriptionList.elementAt(0).getFirstChild().getText()));
				} catch (NullPointerException e) {
					m.setDescription(null);
				}
			} else {
				m.setDescription(null);
			}

			if (!(statusList.size() == 0)) {
				try {
					String status = ((Span) statusList.elementAt(0)).getStringText();
					m.setStatus(status.replaceFirst("[\\d]{1,4} \\- ", ""));
				} catch (NullPointerException e) {
					m.setStatus(null);
				}
			} else {
				m.setStatus(null);
			}

			if (!(categoriesList.size() == 0)) {
				String[] cat = new String[categoriesList.size()];
				for (int i = 0; i < categoriesList.size(); i++) {
					LinkTag lnk = ((LinkTag) categoriesList.elementAt(i));
					cat[i] = lnk.getLinkText();
				}
				m.setCategories(cat);
			}
		} catch (ParserException e) {
			System.err.println("ParserException in getMangaData");
			return null;
		} catch (IOException e) {
			System.err.println("IOException in getMangaData");
			return null;
		}
		return m;
	}

	/*
	 * (non-Javadoc)
	 * @see de.xxlbug.md.manga.Website#getName()
	 */
	public String getName() {
		return "OneManga";
	}

	/**
	 * Extract the data for every single avaible chapter
	 * 
	 * @param chapterData a NodeList where all needed tags can be found
	 * @return a array of all avaible chapter
	 * @throws ParserException if extraction was not possible
	 */
	private Chapter[] getChapterData(NodeList chapterData) throws ParserException {
		Chapter[] chapters;

		// extract data for the chapters
		NodeFilter tableColoumnTag = new NodeClassFilter(TableColumn.class);
		NodeFilter subject = new AndFilter(tableColoumnTag, new HasAttributeFilter("class", "ch-subject"));
		NodeFilter scan = new AndFilter(tableColoumnTag, new HasAttributeFilter("class", "ch-scans-by"));
		NodeFilter date = new AndFilter(tableColoumnTag, new HasAttributeFilter("class", "ch-date"));

		NodeList chapterNameTag;
		NodeList chapterScanTag;
		NodeList chapterDateTag;

		chapterNameTag = chapterData.extractAllNodesThatMatch(subject, true);
		chapterScanTag = chapterData.extractAllNodesThatMatch(scan, true);
		chapterDateTag = chapterData.extractAllNodesThatMatch(date, true);

		chapters = new Chapter[chapterNameTag.size()];

		for (int i = 0; i < chapterNameTag.size(); i++) {
			String chapName;
			String chapLink;
			try {
				LinkTag linkTag = (LinkTag) chapterNameTag.elementAt(i).getFirstChild();
				chapName = linkTag.getLinkText();
				chapLink = linkTag.extractLink();
			} catch (ClassCastException e) {
				chapName = ((TableColumn) chapterNameTag.elementAt(i)).getStringText();
				chapLink = null;
			}
			String chapScan = chapterScanTag.elementAt(i).toPlainTextString();
			String chapDate = chapterDateTag.elementAt(i).toPlainTextString();

			SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
			Date d = null;
			try {
				d = dateFormat.parse(chapDate);
			} catch (ParseException e) {
				System.err.println("Can't parse chapter date");
				d = new Date(0);
			}
			// put all together for the chapter
			chapters[i] = new Chapter(chapName, chapLink, chapScan, d);
		}
		// end extract data for the chapters
		return chapters;
	}

	/**
	 * @param url
	 * @return
	 */
	private URL getChapterFirstPage(URL url) {
		try {
			Parser parser = new Parser(url.openConnection());

			NodeFilter linkTag = new NodeClassFilter(LinkTag.class);
			NodeFilter linkList = new AndFilter(linkTag, new HasParentFilter(new NodeClassFilter(Bullet.class)));

			NodeList links = parser.extractAllNodesThatMatch(linkList);

			if (links.size() > 0) {
				String chapterFirstPageAdress = ((LinkTag) links.elementAt(0)).extractLink();

				Pattern pat = Pattern.compile("(.*/)([\\w\\d\\-]+/)");
				Matcher mat = pat.matcher(chapterFirstPageAdress);

				if (mat.find()) {
					return new URL(url.toExternalForm() + mat.group(2));
				}
			}
		} catch (ParserException e) {
			System.err.println("ParserException in getChapterFirstPage");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException in getChapterFirstPage");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Find every avaible manga and its link
	 * 
	 * @return the list of all avaible manga (only links)
	 */
	private Manga[] getManga() {
		Manga[] manga;

		try {
			Parser parser = new Parser("http://www.onemanga.com/directory/");

			// filter summary
			NodeFilter tableColoumn = new NodeClassFilter(TableColumn.class);
			NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
			NodeFilter hasLinkChild = new HasChildFilter(linkFilter);
			NodeFilter subject = new HasAttributeFilter("class", "ch-subject");
			NodeFilter notSortColumn = new NotFilter(new HasAttributeFilter("class", "sort"));
			NodeFilter tdAndData = new AndFilter(new NodeFilter[] { tableColoumn, subject, notSortColumn });
			// end filter summary

			NodeList links = new NodeList();
			NodeList nodes = parser.extractAllNodesThatMatch(tdAndData);

			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.elementAt(i);

				if (!node.toPlainTextString().trim().equals("")) {
					node.collectInto(links, new AndFilter(subject, hasLinkChild));
				}
			}

			// initialise manga array
			manga = new Manga[links.size()];
			for (int i = 0; i < manga.length; i++) {
				manga[i] = new Manga();
			}

			for (int i = 0; i < links.size(); i++) {
				LinkTag link = (LinkTag) links.elementAt(i).getFirstChild();
				manga[i].setName(this.replaceSpecialChars(link.getLinkText()));
				try {
					manga[i].setLink(new URL(link.extractLink()));
				} catch (MalformedURLException e) {
					System.err.println("MalformedURLException in getManga");
					manga[i].setLink(null);
				}
			}
		} catch (ParserException e) {
			System.err.println("ParserException trying to get names and links of all avaible manga");
			return null;
		}
		return manga;
	}

	/**
	 * Replaces some special chars because of some slacky html code in there
	 * 
	 * @param toCheck the string which possibly contains faulty chars
	 * @return the string with the replaced chars
	 */
	private String replaceSpecialChars(String toCheck) {
		toCheck = toCheck.replace("<br />", "\n");
		toCheck = toCheck.replace("&quot;", "\"");
		toCheck = toCheck.replace("&#39;", "'");
		toCheck = toCheck.replace("&amp;", "&");

		return toCheck;
	}
}
