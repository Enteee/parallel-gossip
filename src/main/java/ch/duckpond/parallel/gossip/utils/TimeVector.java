package ch.duckpond.parallel.gossip.utils;

import java.io.Serializable;

/**
 * A vector of timestamps.
 * 
 * @author ente
 * @param <T>
 *            the type of data held in this vector.
 */
public class TimeVector implements Serializable, Comparable<TimeVector> {

	private static final long serialVersionUID = 1L;
	private final int[] timeVector;

	public TimeVector(final int size) {
		timeVector = new int[size];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < size(); i++) {
			sb.append(get(i));
			if (i + 1 < size()) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public int size() {
		return timeVector.length;
	}

	public int get(int i) {
		return timeVector[i];
	}

	public int[] toArray() {
		return timeVector;
	}

	public void increment(int i) {
		if (i > size()) {
			throw new IndexOutOfBoundsException("i");
		}
		++timeVector[i];
	}

	/**
	 * Are two @{link TimeVector}s comparable to each other?
	 * 
	 * @param other
	 */
	private void checkComparable(final TimeVector other) {
		if (other == null) {
			throw new IllegalArgumentException("other");
		}
		if (size() != other.size()) {
			throw new IllegalArgumentException("other not of the same size");
		}
	}

	/**
	 * Goes through all the element of this and other and sets always the
	 * maximum of this.
	 * 
	 * @param other
	 *            the other vector used for maxing this vector.
	 */
	public void max(final TimeVector other) {
		checkComparable(other);
		for (int i = 0; i < size(); ++i) {
			int thisElement = get(i);
			int otherElement = other.get(i);
			timeVector[i] = (thisElement < otherElement) ? otherElement
					: thisElement;
		}
	}

	/**
	 * this == other iff this[k] == other[k] for all k = 1,...,size()
	 * 
	 * @param other
	 * @return
	 */
	public boolean isSame(final TimeVector other) {
		checkComparable(other);
		boolean isSame = true;
		for (int i = 0; i < size(); ++i) {
			int thisElement = get(i);
			int otherElement = other.get(i);
			if (otherElement != thisElement) {
				isSame = false;
				break;
			}
		}
		return isSame;
	}

	/**
	 * this <= other iff this[k] <= other[k] for all k = 1,...,size()
	 * 
	 * @param other
	 *            check against
	 * @return
	 */
	public boolean isLessOrEqual(final TimeVector other) {
		checkComparable(other);
		boolean isLessOrEqual = true;
		for (int i = 0; i < size(); ++i) {
			int thisElement = get(i);
			int otherElement = other.get(i);
			if (otherElement < thisElement) {
				isLessOrEqual = false;
				break;
			}
		}
		return isLessOrEqual;
	}

	/**
	 * this < other iff this.isLessOrEqual(other) && !this.isSame(other)
	 * 
	 * @param other
	 *            check against
	 * @return
	 */
	public boolean isLess(final TimeVector other) {
		checkComparable(other);
		return isLessOrEqual(other) && !isSame(other);
	}

	/**
	 * 
	 * for all k = 1,...,size(): return -1 iff this[k] < other[k] return 1 iff
	 * this[k] > other[k] iff return 0 all equal
	 */
	@Override
	public int compareTo(final TimeVector other) {
		checkComparable(other);
		int compare = 0;
		for (int i = 0; i < size(); ++i) {
			int thisElement = get(i);
			int otherElement = other.get(i);
			if (thisElement < otherElement) {
				compare = -1;
				break;
			} else if (thisElement > otherElement) {
				compare = 1;
				break;
			}
		}
		return compare;
	}
}
