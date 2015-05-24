package common.nw.modpack;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalModpack {

	public LocalModpack(String modPackName, String modPackRepo,
			String modPackVersion) {
		name = modPackName;
		url = modPackRepo;
		version = modPackVersion;
		updateFrequency = 0;
	}

	public static final int ALWAYS = 0;
	public static final int EVERY_DAY = 1;
	public static final int EVERY_WEEK = 2;

	/** modapck name */
	public String name;
	/** modpack version */
	public String version;

	/** modpack url */
	public String url;

	/** tracks all the updated mods */
	public List<String> files = new ArrayList<String>();
	
	/** used for files which updates have to be triggered */
	public Map<String, String> trackedFileVersions = new HashMap<String, String>();
	
	public int updateFrequency = 0;
	public Date lastUpdate = new Date(0);

}
