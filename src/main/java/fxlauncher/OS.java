package fxlauncher;

enum OS {
	win, mac, linux, other;

	public static final OS current;

	static {
		String os = System.getProperty("os.name", "generic").toLowerCase();

		if ((os.contains("mac")) || (os.contains("darwin")))
			current = mac;
		else if (os.contains("win"))
			current = win;
		else if (os.contains("nux"))
			current = linux;
		else
			current = other;
	}
}