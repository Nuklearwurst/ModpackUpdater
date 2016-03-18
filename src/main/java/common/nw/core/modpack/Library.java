package common.nw.core.modpack;

import argo.format.CompactJsonFormatter;
import argo.jdom.*;
import argo.saj.InvalidSyntaxException;
import common.nw.core.utils.log.NwLogger;
import common.nw.creator.gui.table.IDataTableElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nuklearwurst
 */
public class Library implements IDataTableElement {

	private static final String defaultUpdaterUrl = "https://dl.dropboxusercontent.com/u/87474141/minecraft/modupdater/updater/";
	private static final String defaultUpdaterName = "common.nuklearwurst:updater:%s";

	private static final String[] dataFields = {"id", "url"};

	private static final JsonStringNode nodeName = JsonNodeFactories.string("name");
	private static final JsonStringNode nodeUrl = JsonNodeFactories.string("url");

	public String url;
	public String id;

	/**
	 * used to mark important libraries such as the updater
	 */
	private boolean isVital;

	public Library() {
	}

	@SuppressWarnings("WeakerAccess")
	public Library(String data) {
		JdomParser parser = new JdomParser();
		try {
			JsonRootNode node = parser.parse(data);
			url = node.getStringValue("url");
			id = node.getStringValue("name");
//			Map<JsonStringNode, JsonNode> fields = node.getFields();
//			if(fields.containsKey(nodeName) && fields.containsKey(nodeUrl)) {
//				url = fields.get(nodeUrl).getStringValue()
//			} else {
//				NwLogger.INSTALLER_LOGGER.warn("Failed to read library data, Field not found!");
//			}

		} catch (InvalidSyntaxException | IllegalArgumentException e) {
			NwLogger.INSTALLER_LOGGER.warn("Failed to read library data...", e);
		}


	}

	public static List<Library> createFromString(List<String> libraries) {
		List<Library> list = new ArrayList<>(libraries.size());
		for (String s : libraries) {
			list.add(new Library(s));
		}
		return list;
	}

	public String compileToJson() {
		JsonRootNode node = JsonNodeFactories.object(
				JsonNodeFactories.field(nodeName, JsonNodeFactories.string(id)),
				JsonNodeFactories.field(nodeUrl, JsonNodeFactories.string(url)));
		return CompactJsonFormatter.fieldOrderPreservingCompactJsonFormatter().format(node);
	}

	@SuppressWarnings("unused")
	public boolean isValid() {
		return id != null && !id.isEmpty() && url != null && !url.isEmpty();
	}

	@Override
	public String[] getSupportedFields() {
		return dataFields;
	}

	@Override
	public Object getValue(String field) {
		if ("url".equals(field)) {
			return url;
		} else if ("id".equals(field)) {
			return id;
		}
		return null;
	}

	@Override
	public void setValue(String field, Object value) {
		if ("url".equals(field)) {
			url = (String) value;
		} else if ("id".equals(field)) {
			id = (String) value;
		}
	}

	@Override
	public boolean canEditField(String field) {
		return true;
	}

	public boolean isVital() {
		return isVital;
	}

	@SuppressWarnings("unused")
	public void setVital(boolean vital) {
		isVital = vital;
	}

	/**
	 * creates the default updater library for the given version
	 */
	@SuppressWarnings("SameParameterValue")
	public static Library createUpdaterLibrary(String version) {
		Library library = new Library();
		library.isVital = true;
		library.id = String.format(defaultUpdaterName, version);
		library.url = defaultUpdaterUrl;
		return library;
	}

	public static List<JsonNode> parseJsonListFromStrings(List<String> list) throws InvalidSyntaxException {
		JdomParser parser = new JdomParser();
		List<JsonNode> out = new ArrayList<>(list.size());
		for (String node : list) {
			out.add(parser.parse(node));
		}
		return out;
	}
}
