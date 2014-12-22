package ch.duckpond.parallel.gossip.utils;

import java.math.BigInteger;

import ch.duckpond.parallel.gossip.Main;

public class Utils {
	public static String rndString() {
		return new BigInteger(130, Main.RND).toString(32);
	}
}
