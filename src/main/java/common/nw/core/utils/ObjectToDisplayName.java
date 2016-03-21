package common.nw.core.utils;

/**
 * @author Nuklearwurst
 */
public class ObjectToDisplayName<T> implements IDisplayNameProvider {

	private final T value;
	private final String name;

	public ObjectToDisplayName(T value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public static <T> ObjectToDisplayName<T>[] createNewObjectToDisplayNameArray(T[] values, String[] names) {
		if (values.length != names.length) {
			return null;
		}
		@SuppressWarnings("unchecked") ObjectToDisplayName<T>[] out = new ObjectToDisplayName[values.length];
		for (int i = 0; i < values.length; i++) {
			out[i] = new ObjectToDisplayName<>(values[i], names[i]);
		}
		return out;
	}

}
