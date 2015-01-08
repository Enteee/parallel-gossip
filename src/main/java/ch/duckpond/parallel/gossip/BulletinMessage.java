package ch.duckpond.parallel.gossip;

import java.io.Serializable;

import ch.duckpond.parallel.gossip.utils.TimeVector;

public class BulletinMessage implements Serializable,
		Comparable<BulletinMessage> {

	private static final long serialVersionUID = 1L;
	private static final String REFERS_TO_INDICATOR = "RE:";

	private String author;
	private String message;
	private String title;
	private TimeVector timeStamp;
	private String createdTimeStamp;

	public BulletinMessage(final String author, final String title,
			final String message, final TimeVector timeStamp,
			final String createdTimeStamp) {
		init(author, title, message, timeStamp, createdTimeStamp);
	}

	public BulletinMessage(final String author, final BulletinMessage refersTo,
			final String message, final TimeVector timeStamp,
			final String createdTimeStamp) {
		init(author, REFERS_TO_INDICATOR + refersTo.getTitle(), message,
				timeStamp, createdTimeStamp);
	}

	private void init(final String author, final String title,
			final String message, final TimeVector timeStamp,
			final String createdTimeStamp) {
		if (author == null || title == null || message == null
				|| timeStamp == null || createdTimeStamp == null) {
			throw new IllegalArgumentException(
					"author || title || message || timestamp || createdTimeStamp");
		}
		this.author = author;
		this.message = message;
		this.title = title;
		this.timeStamp = timeStamp;
		this.createdTimeStamp = createdTimeStamp;
	}

	public BulletinMessage getBulletinMessageWithNewTimestamp(
			final TimeVector timeStamp) {
		return new BulletinMessage(getAuthor(), getTitle(), getMessage(),
				timeStamp, getCreatedTimeStamp());
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

	public String getCreatedTimeStamp() {
		return createdTimeStamp;
	}

	@Override
	public String toString() {
		return String
				.format("Title: %s%nTimestamp: %s%nCreatedTimestamp: %s%nAuthor: %s%nMessage:%s%n",
						getTitle(), getTimeStamp(), getCreatedTimeStamp(),
						getAuthor(), getMessage());
	}

	@Override
	public int compareTo(BulletinMessage other) {
		return getTimeStamp().compareTo(other.getTimeStamp());
	}
}
