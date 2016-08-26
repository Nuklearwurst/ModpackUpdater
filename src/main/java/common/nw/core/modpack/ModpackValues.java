package common.nw.core.modpack;

@SuppressWarnings("unused")
public class ModpackValues {

	public static class DownloadTypes {

		private final static String directDownload = "directDownload";
		private final static String userDownload = "userDownload";
		private final static String localFile = "localFile";
		private final static String extractArchive = "extractArchive";
		private final static String minecraft = "minecraft";
		private final static String minecraft_inherit = "minecraft_inherit";
		private final static String forge_inherit = "forge_inherit";
		private final static String auto_generate = "auto_generate";


		//////////////////
		// JAR-Download //
		//////////////////

		public final static String jarDirectDownload = directDownload;
		public final static String jarUserDownload = userDownload;
		public final static String jarLocalFile = localFile;
		/**
		 * does not create an own jar, instead the version.json has to be configured to inherit from the mc-jar</br>
		 * Can be used to inherit of custom loader mods other than mc-forge
		 * TODO: allow auto insertion to inherit from another version
		 */
		public final static String jarMinecraftInherit = minecraft_inherit;

		/**
		 * same as jarMinecraftInherit, but downloads the speified forge version
		 */
		public final static String jarForgeInherit = forge_inherit;

		///////////////////
		// JSON-Download //
		///////////////////

		public final static String jsonDirectDownload = directDownload;
		public final static String jsonGenerate = auto_generate;
		public final static String jsonUserDownload = userDownload;
		public final static String jsonLocalFile = localFile;

		//////////////////
		// Mod-Download //
		//////////////////

		public final static String modDirectDownload = directDownload;
		public final static String modUserDownload = userDownload;
		public final static String modExtractDownload = extractArchive;
	}


	/**
	 * use filename as name
	 */
	public final static String nameTypeFileName = "file";
	/**
	 * use forge/.litemod name
	 */
	public final static String nameTypeZipEntry = "zipEntry";

	/**
	 * use filename as version
	 */
	public final static String versionTypeFileName = "file";
	/**
	 * use forge/.litemod version
	 */
	public final static String versionTypeZipEntry = "zipEntry";
	/**
	 * use file-md5 as version
	 */
	public final static String versionTypeMD5 = "md5";
	/**
	 * tracks last updates and update on version String change
	 */
	public final static String versionTypeTracked = "tracked";

	public final static int fileTypeServer = 1; //0b1
	public final static int fileTypeClient = 2; //0b10
	public final static int fileTypeAdmin = 4; //0b100

	public final static String URL_FORGE_INSTALLER = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/";
	public final static String URL_FORGE_VERSION_JSON = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
}
