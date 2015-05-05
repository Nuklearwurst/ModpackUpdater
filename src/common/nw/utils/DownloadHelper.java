package common.nw.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import common.nw.modpack.ModInfo;
import common.nw.updater.Updater;
import common.nw.updater.gui.IProgressWatcher;
import common.nw.utils.log.NwLogHelper;

public class DownloadHelper {

	public static String getString(String strUrl, IProgressWatcher watcher) throws IOException {
		String result = "";

		URL url = new URL(strUrl);

		HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
		httpClient.setDoInput(true);
		httpClient.setDoOutput(false);
		httpClient.setUseCaches(false);

		httpClient.setRequestMethod("GET");
		httpClient.setRequestProperty("Connection", "Close");
		httpClient.addRequestProperty("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

		InputStream httpStream = httpClient.getInputStream();
		int max = httpStream.available();
		if(watcher != null) {
			watcher.setDownloadProgress("Downloading...", 0, max);
		}
		try {
			StringBuilder readString = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpStream));
			String readLine;
			while ((readLine = reader.readLine()) != null) {
				if(watcher != null) {
					watcher.setDownloadProgress(max - httpStream.available());;
				}
				readString.append(readLine).append("\n");
			}

			reader.close();
			result = readString.toString();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public static boolean downloadFile(String url, File out) {
		URLConnection http = null;
		InputStream httpInputStream = null;
		DataOutputStream fileOutputStream = null;

		try {
			byte[] buffer = new byte[4096];
			http = new URL(url).openConnection();
			http.setReadTimeout(10000);
			http.addRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			httpInputStream = http.getInputStream();
			int contentLength = http.getContentLength();

			if (out.exists()) {
				long receivedBytes = out.length();

				if (receivedBytes == contentLength) {
					return true;
				}

				out.delete();
			}
			fileOutputStream = new DataOutputStream(new FileOutputStream(out));

			int readBytes;
			while ((readBytes = httpInputStream.read(buffer)) >= 0) {
				fileOutputStream.write(buffer, 0, readBytes);
			}
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpInputStream != null) {
					httpInputStream.close();
				}
			} catch (IOException localIOException4) {
			}
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException localIOException5) {
			}
		}
		try {
			if (httpInputStream != null) {
				httpInputStream.close();
			}
		} catch (IOException localIOException6) {
		}
		try {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		} catch (IOException localIOException7) {
		}
		return false;

	}

	public static UpdateResult getMod(IProgressWatcher listener, ModInfo mod,
			int modNumber, float modValue, File baseDir, boolean ignoreDuplicates) {

		/** mod file */
		String file = baseDir + File.separator + mod.fileName;

		// create dirs
		if (file.lastIndexOf(File.separator) != -1) {
			File dirs = new File(file.substring(0, file.lastIndexOf(File.separator)));
			if(!dirs.exists() && !dirs.mkdirs()) {
				//use better error messages
				return UpdateResult.FailedCreatingDirs;
			}
		}

		//main file
		File modFile = new File(file);
		
		if(!ignoreDuplicates) {
			if(modFile.exists()) {
				Updater.logger.fine("ModFile " + mod.fileName + " does already exsist, checking md5!");
				String hash = getHash(modFile);
				if(hash != null && !hash.isEmpty() && hash.equals(mod.getRemoteInfo().md5)) {
					Updater.logger.info("Using already exsisting modFile! Aborting Download...");
					return UpdateResult.Good;
				} else {
					Updater.logger.fine("MD5 does NOT match, tring to download new mod.");
				}
			}
		}

		//tmp file
		File tempFile = new File(file + ".tmp");

		Updater.logger.info("Creating HTTP client for " + mod.name + " from " + mod.getRemoteInfo().downloadUrl + ". Using temp file " + tempFile);
		listener.setDownloadProgress("Downloding " + mod.name + " from " + mod.getRemoteInfo().downloadUrl + ".", 0);
		if(listener.isCancelled()) {
			return UpdateResult.Cancelled;
		}
		//download the mod
		UpdateResult result = downloadMod(tempFile, mod, listener);
		if (result == UpdateResult.Good) {
			Updater.logger.info("HTTP fetch request for " + mod.name + " completed with success!");
			
			if (!checkHash(mod.getRemoteInfo().md5, tempFile)) {
				NwLogHelper.severe("Downloading mod: " + mod.name + " failed!");
				NwLogHelper.severe("MD5 does not match! Remote: " + mod.getRemoteInfo().md5 + "; Local: " + getHash(tempFile));
				listener.setDownloadProgress("Downloading " + mod.name
						+ "failed! MD5 does not match");
				tempFile.delete();
				return UpdateResult.BadDownload;
			}
			

			//overall progress
			listener.setOverallProgress((int) (10.0F + modNumber * modValue));
			
			if (modFile.exists()) {
				Updater.logger.fine("Modfile " + modFile.getAbsolutePath().replace(File.separator, "/") + "already exsists. Deleting...");
				if(!modFile.delete()) {
					Updater.logger.warning("Modfile " + modFile.getAbsolutePath().replace(File.separator, "/") + "could not be deleted!");
				}
			}

			//working?
			File oldModFile = mod.file;
			if ((oldModFile != null) && (oldModFile.exists())) {
				listener.setDownloadProgress("Deleting old mod file...", 90, 100);
				if (!oldModFile.delete()) {
					Updater.logger.warning("Deleting legacy file failed.");
				}
			}

			listener.setDownloadProgress("Renaming downloaded file...", 95, 100);
			if (tempFile.renameTo(modFile)) {
				listener.setDownloadProgress("Renamed " + modFile.getName()
						+ " successfully!", 100);
				return UpdateResult.Good;
			}

			listener.setDownloadProgress("Failed renaming " + modFile.getName()
					+ "!");
			return UpdateResult.BadDownload;

		}
		return UpdateResult.BadDownload;
	}

	/**
	 * compares the given hash with the file
	 * 
	 * @param md5
	 * @param tempFile
	 * @return
	 */
	public static boolean checkHash(String md5, File tempFile) {
		String localMD5 = getHash(tempFile);
		Updater.logger.info("Remote MD5 is " + md5 + " local MD5 is "
				+ localMD5);
		return md5.equals(localMD5);
	}

	/**
	 * creates an md5 of the given file
	 * 
	 * @param file
	 * @return
	 */
	public static String getHash(File file) {
		DigestInputStream is = null;
		try {
			is = new DigestInputStream(new FileInputStream(file),
					MessageDigest.getInstance("MD5"));
			byte[] ignored = new byte[65536];
			for (int readBytes = is.read(ignored); readBytes >= 1; readBytes = is
					.read(ignored)) {
				;
			}
			return String.format("%1$032x", new Object[] { new BigInteger(1, is
					.getMessageDigest().digest()) });
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException localIOException2) {
			}
		}
		return "";
	}

	public static UpdateResult downloadMod(File tempFile, ModInfo mod,
			IProgressWatcher listener) {
		
		URLConnection http = null;
		InputStream httpInputStream = null;
		DataOutputStream fileOutputStream = null;
		

		try {
			byte[] buffer = new byte[4096];
			http = new URL(mod.getRemoteInfo().downloadUrl).openConnection();
			http.setReadTimeout(10000);
			http.addRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			float progress = 0.0F;
			float progressMax = 0;

			httpInputStream = http.getInputStream();
			progressMax = http.getContentLength();
			int contentLength = http.getContentLength();
			
			if(listener.isCancelled()) {
				return UpdateResult.Cancelled;
			}

			if (tempFile.exists()) {
				long receivedBytes = tempFile.length();

				if (receivedBytes == contentLength) {
					return UpdateResult.Good;
				}

				Updater.logger.info("Deleting " + tempFile
						+ " as it does not match what we currently have ("
						+ contentLength + " vs our " + receivedBytes + ").");
				tempFile.delete();
			}
			fileOutputStream = new DataOutputStream(new FileOutputStream(
					tempFile));

			listener.setDownloadProgress("Downloading " + mod.name, 0,
					(int) progressMax);
			int readBytes;
			while ((readBytes = httpInputStream.read(buffer)) >= 0) {
				progress += readBytes;
				fileOutputStream.write(buffer, 0, readBytes);
				listener.setDownloadProgress((int) progress);
				if(listener.isCancelled()) {
					return UpdateResult.Cancelled;
				}
			}
			return UpdateResult.Good;

		} catch (IOException e) {
			e.printStackTrace();
			listener.setDownloadProgress("Error during download of " + mod.fileName + ": " + e.getMessage());
			Updater.logger.severe("Failed downloading " + mod.fileName.replace(File.separator, "/") + "!");
		} finally {
			try {
				if (httpInputStream != null) {
					httpInputStream.close();
				}
			} catch (IOException localIOException4) {
			}
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException localIOException5) {
			}
		}
		try {
			if (httpInputStream != null) {
				httpInputStream.close();
			}
		} catch (IOException localIOException6) {
		}
		try {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		} catch (IOException localIOException7) {
		}
		return UpdateResult.Failed;
	}
}
