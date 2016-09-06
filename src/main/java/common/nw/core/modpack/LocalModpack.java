package common.nw.core.modpack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represantation of locally stored modpack data
 */
public class LocalModpack {

	public LocalModpack(String modPackName, String modPackRepo,
	                    String modPackVersion) {
		name = modPackName;
		url = modPackRepo;
		version = modPackVersion;
	}

	/**
	 * modapck name
	 */
	public String name;
	/**
	 * modpack version
	 */
	public String version;

	/**
	 * modpack url
	 */
	@SuppressWarnings("CanBeFinal")
	public String url;

	/**
	 * tracks all the updated mods
	 */
	public List<String> files = new ArrayList<>();

	/**
	 * used for files which updates have to be triggered
	 */
	public Map<String, String> trackedFileVersions = new HashMap<>();
}
