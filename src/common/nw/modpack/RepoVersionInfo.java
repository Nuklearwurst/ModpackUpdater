package common.nw.modpack;

import java.util.List;

/**
 * contains info about the minecraft launcher version
 * 
 * @author Nuklearwurst
 * 
 */
public class RepoVersionInfo {

	/**
	 * information shown on install (web-url or html-formatted text [detected by
	 * startsWith("http://" or "https://" or "www.")] )
	 */
	public String installInfoUrl;

	/** has to change with whenever forgeversion got changed */
	public String version;

	/**
	 * where to get the jar from (in most cases equivalent to minecraft version)
	 */
	public String versionName;

	/**
	 * where to get the jar from (eg. download, local), will use versionName as
	 * source
	 */
	public String jarUpdateType;

	/** where to get the verion-json file from */
	public String jsonName;

	/** where to get the version-json file from (eg. download, local) */
	public String jsonUpdateType;

	/**
	 * which arguments should be added to the argument list<br>
	 * TODO morph argument and library list into a Strings (a list is not
	 * necessary)
	 */
	public List<String> arguments;

	/** libraries that should be added to the version file */
	public List<String> libraries;
}
