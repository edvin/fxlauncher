package fxlauncher;

public enum OS {
	win, mac, linux, other;

	public static final OS current;

	static {
		String OS = System.getProperty("os.name", "generic").toLowerCase();

		if ((OS.contains("mac")) || (OS.contains("darwin")))
			current = mac;
		else if (OS.contains("win"))
			current = win;
		else if (OS.contains("nux"))
			current = linux;
		else
			current = other;
	}
}
