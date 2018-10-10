package it.etoken.base.common.utils;

import java.math.BigInteger;

public class EOSUtils {
	private long string_to_symbol(int precision, String str) {
		char[] strc = str.toCharArray();
		int len = strc.length;
		long result = 0;
		for (int i = 0; i < len; ++i) {
			// All characters must be upper case alphabets
			result |= ((long) (strc[i]) << (8 * (i + 1)));
		}
		result |= Long.valueOf(precision);
		return result;
	}

	private long char_to_symbol(char c) {
		if (c >= 'a' && c <= 'z')
			return (c - 'a') + 6;
		if (c >= '1' && c <= '5')
			return (c - '1') + 1;
		return 0;
	}

	private long string_to_name(String str) {
		char[] strc = str.toCharArray();
		long name = 0;
		int i = 0;
		for (; i < 12; ++i) {
			// NOTE: char_to_symbol() returns char type, and without this explicit
			// expansion to uint64 type, the compilation fails at the point of usage
			// of string_to_name(), where the usage requires constant (compile time)
			// expression.
			name |= (char_to_symbol(strc[i]) & 0x1f) << (64 - 5 * (i + 1));
		}
		// The for-loop encoded up to 60 high bits into uint64 'name' variable,
		// if (strlen(str) > 12) then encode str[12] into the low (remaining)
		// 4 bits of 'name'
		if (strc.length > 12)
			name |= char_to_symbol(strc[12]) & 0x0F;
		return name;
	}

	public String getBoundKey(String name, int precision, String symbol) {
		long i = string_to_name(name);
		long j = string_to_symbol(precision, symbol);
		BigInteger ii = new BigInteger(String.valueOf(i));
		String iis = ii.toString(2);
		int l1 = 64 - iis.length();
		for (int x = 0; x < l1; x++) {
			iis = "0" + iis;
		}
		BigInteger jj = new BigInteger(String.valueOf(j));
		String jjs = jj.toString(2);
		int t2 = 64 - jjs.length();
		for (int x = 0; x < t2; x++) {
			jjs = "0" + jjs;
		}
		String indexx = iis + jjs;
		BigInteger index2 = new BigInteger(indexx, 2);
		String index16 = index2.toString(16);
		StringBuilder result = new StringBuilder("0x");
		for (int c = index16.length() - 1; c >= 0; c--) {
			if ((c + 1) % 2 == 0) {
				result.append(index16.charAt(c - 1));
				result.append(index16.charAt(c));
			}
		}
		return result.toString();
	}
}
