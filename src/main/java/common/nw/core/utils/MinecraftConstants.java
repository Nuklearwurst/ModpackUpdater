package common.nw.core.utils;

/**
 * @author Nuklearwurst
 */
public class MinecraftConstants {
	public static final String MC_ARGUMENTS = "--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type}";
	public static final String NW_UPDATER_TWEAKER_ARGUMENT = "--tweakClass common.nw.updater.launch.Launch";
	public static final String MC_FORGE_ARGUMENTS = "--tweakClass cpw.mods.fml.common.launcher.FMLTweaker";

}
