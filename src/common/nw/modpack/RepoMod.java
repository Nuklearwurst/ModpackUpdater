package common.nw.modpack;

public class RepoMod {

	public String name;
	public String nameType; // currently not supported
	public String version;
	public String versionType; // currently not supported
	
	/**
	 * the download url of the mod
	 */
	public String downloadUrl;
	
	/** 
	 * how the mod should be downloaded
	 * @see {@link common.nw.modpack.Strings}  
	 */
	public String downloadType;
	
	/** md5 used for download */
	public String md5;
	
	/** contains dir and name in minecraft folder */
	public String fileName;

}
