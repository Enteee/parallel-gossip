package ch.duckpond.parallel.gossip;

import java.io.Serializable;

import ch.duckpond.parallel.gossip.utils.TimeVector;

public class BulletinMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String REFERS_TO_INDICATOR = "RE:";

	private String author;
	private String message;
	private String title;
	private TimeVector timeStamp;

	public BulletinMessage(final String author, final String title,
			final String message, final TimeVector timeStamp) {
		init(author, title, message, timeStamp);
	}

	public BulletinMessage(final String author, final BulletinMessage refersTo,
			final String message, final TimeVector timeStamp) {
		init(author, REFERS_TO_INDICATOR + refersTo.getTitle(), message,
				timeStamp);
	}

	private void init(final String author, final String title,
			final String message, final TimeVector timeStamp) {
		if (author == null || title == null || message == null
				|| timeStamp == null) {
			throw new IllegalArgumentException(
					"author || title || message || timestamp");
		}
		this.author = author;
		this.message = message;
		this.title = title;
		this.timeStamp = timeStamp;
	}

	public BulletinMessage getBulletinMessageWithNewTimestamp(
			final TimeVector timeStamp) {
		return new BulletinMessage(getAuthor(), getTitle(), getMessage(),
				timeStamp);
	}

	public String getAuthor() {
		return author;
	}

	public String getMessage() {
		return message;
	}

	public String getTitle() {
		return title;
	}

	public TimeVector getTimeStamp() {
		return timeStamp;
	}

	@Override
	public String toString() {
		return String.format(
				"Title: %s Timestamp: %s %nAuthor: %s %nMessage:%s%n",
				getTitle(), getTimeStamp(), getAuthor(), getMessage());
	}
}
