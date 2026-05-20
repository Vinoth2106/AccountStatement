package com.bornfire.AccountStatement.util;

/**
 * Transliterates English / Latin customer names into Arabic script without
 * harakat (vowel marks). Tuned for Indian English names as commonly shown by
 * Google Translate (e.g. SANTHA KATTUPARAMBIL KRISHNAN → سانثا كاتوبارامبيل كريشنان).
 */
public final class EnglishToArabicNameTransliterator {

	private static final String[][] DIGRAPHS = { { "TH", "\u062B" }, { "SH", "\u0634" }, { "CH", "\u062A\u0634" },
			{ "KH", "\u062E" }, { "GH", "\u063A" }, { "PH", "\u0641" }, { "DH", "\u0630" }, { "EE", "\u064A" },
			{ "OO", "\u0648" }, { "AU", "\u0627\u0648" }, { "AI", "\u0627\u064A" } };

	private EnglishToArabicNameTransliterator() {
	}

	public static String transliterate(String input) {
		if (input == null || input.isEmpty()) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		String[] words = input.trim().split("\\s+");
		for (int w = 0; w < words.length; w++) {
			if (w > 0) {
				result.append(' ');
			}
			result.append(transliterateWord(words[w]));
		}
		return stripArabicDiacritics(result.toString());
	}

	private static String transliterateWord(String word) {
		String upper = word.toUpperCase();
		StringBuilder out = new StringBuilder();
		int i = 0;
		while (i < upper.length()) {
			boolean matched = false;
			for (String[] digraph : DIGRAPHS) {
				String latin = digraph[0];
				if (upper.regionMatches(i, latin, 0, latin.length())) {
					out.append(digraph[1]);
					i += latin.length();
					matched = true;
					break;
				}
			}
			if (matched) {
				continue;
			}
			char c = upper.charAt(i);
			String mapped = mapChar(c);
			if (mapped != null) {
				out.append(mapped);
			}
			i++;
		}
		return out.toString();
	}

	private static String mapChar(char c) {
		switch (c) {
		case 'A':
			return "\u0627";
		case 'B':
			return "\u0628";
		case 'C':
			return "\u0643";
		case 'D':
			return "\u062F";
		case 'E':
			return "\u064A";
		case 'F':
			return "\u0641";
		case 'G':
			return "\u062C";
		case 'H':
			return "\u0647";
		case 'I':
			return "\u064A";
		case 'J':
			return "\u062C";
		case 'K':
			return "\u0643";
		case 'L':
			return "\u0644";
		case 'M':
			return "\u0645";
		case 'N':
			return "\u0646";
		case 'O':
			return "\u0648";
		case 'P':
			return "\u0628";
		case 'Q':
			return "\u0642";
		case 'R':
			return "\u0631";
		case 'S':
			return "\u0633";
		case 'T':
			return "\u062A";
		case 'U':
			return "\u0648";
		case 'V':
			return "\u0641";
		case 'W':
			return "\u0648";
		case 'X':
			return "\u0643\u0633";
		case 'Y':
			return "\u064A";
		case 'Z':
			return "\u0632";
		default:
			return String.valueOf(c);
		}
	}

	private static String stripArabicDiacritics(String text) {
		return text.replaceAll("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06DC\\u06DF-\\u06E4\\u06E7\\u06E8\\u06EA-\\u06ED]", "");
	}
}
