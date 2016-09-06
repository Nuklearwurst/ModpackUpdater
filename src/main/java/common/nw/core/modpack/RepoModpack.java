package common.nw.core.modpack;

import java.util.List;

/**
 * Represantation of the remote modpack file
 */
public class RepoModpack {

	/**
	 * The name of the modpack
	 */
	public String modpackName;

	/**
	 * updater version this modpack requires
	 */
	public int updaterRevision;

	/**
	 * Basic Info about the modpack (version data for mc-launcher etc)
	 */
	public RepoVersionInfo minecraft;

	/**
	 * base url, currently unused in updated
	 */
	public String modpackRepo;

	/**
	 * blacklisted mods and files
	 */
	public List<RepoMod> blacklist;

	/**
	 * mods and files that get udpated
	 */
	public List<RepoMod> files;
}
