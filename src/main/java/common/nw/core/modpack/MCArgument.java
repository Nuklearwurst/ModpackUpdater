package common.nw.core.modpack;

import common.nw.core.utils.IDisplayNameProvider;
import common.nw.core.utils.ObjectToDisplayName;
import common.nw.creator.gui.table.IDataTableElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nuklearwurst
 */
public class MCArgument implements IDataTableElement {

	public static final String specialArgPrefix = "%%=";

	public static final String specialArgPredefined = specialArgPrefix + "jsondefined";

	public static final String specialArgMinecraft = specialArgPrefix + "minecraft";
	public static final String specialArgForge = specialArgPrefix + "forge";
	public static final String specialArgForgeNew = specialArgPrefix + "forge_post_1.10";
	public static final String specialArgUpdater = specialArgPrefix + "updater";

	public static final String[] specialArgumentList = {
			specialArgMinecraft, specialArgForge, specialArgUpdater, specialArgForgeNew
	};

	public static final ObjectToDisplayName<String>[] specialArgumentDisplayList =
			ObjectToDisplayName.createNewObjectToDisplayNameArray(specialArgumentList,
					new String[]{"Default Minecraft arguments", "Default Forge arguments (pre mc 1.10)", "Default Updater arguments", "Default Forge arguments (mc 1.10+)"});


	private final Type type;
	private String value;

	public MCArgument(Type type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public String[] getSupportedFields() {
		return new String[]{"type", "value"};
	}

	@Override
	public Object getValue(String field) {
		switch (field) {
			case "type":
				return type;
			case "value":
				//special code to provide display names
				if (type == Type.PREDEFINED) {
					return "-- json-imported --";
				} else if (type == Type.DYNAMIC) {
					assert specialArgumentDisplayList != null;
					for (ObjectToDisplayName<String> v : specialArgumentDisplayList) {
						if (v.getValue().equals(value)) {
							return v.getDisplayName();
						}
					}
				}
				return value;
		}
		return null;
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public void setValue(String field, Object value) {
		switch (field) {
			case "value":
				this.value = (String) value;
		}
	}

	@Override
	public boolean canEditField(String field) {
		//only possible to edit value when custom arg
		//type is not editable
		return type == Type.CUSTOM && "value".equals(field);
	}

	public String getValue() {
		return value;
	}

	/**
	 * parses the given String into an {@link MCArgument} with type information
	 */
	public static MCArgument getArgumentFromString(String arg) {
		if (arg == null) {
			return null;
		}
		if (arg.equals(specialArgPredefined)) {
			return new MCArgument(Type.PREDEFINED, arg);
		}
		return new MCArgument(arg.startsWith(specialArgPrefix) ? Type.DYNAMIC : Type.CUSTOM, arg);
	}

	public static List<MCArgument> getArgumentsFromStringList(List<String> argumentsString) {
		List<MCArgument> out = new ArrayList<>(argumentsString.size());
		for (String s : argumentsString) {
			out.add(getArgumentFromString(s));
		}
		return out;
	}

	public Type getType() {
		return type;
	}


	public enum Type implements IDisplayNameProvider {
		/**
		 * user created argument
		 */
		CUSTOM("Custom"),
		/**
		 * arguments available through .json file
		 */
		PREDEFINED("json-imported"),
		/**
		 * dynamic generated arguments
		 */
		DYNAMIC("auto-generated");

		private final String displayName;

		Type(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}
	}
}
