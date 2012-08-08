package com.gigaspaces.eviction;

public class IndexValue implements Comparable<IndexValue>{
	private long major;
	private long minor;

	public IndexValue(Long major, Long minor) {
		this.setMajor(major);
		this.setMinor(minor);
	}

	public Long getMajor() {
		return major;
	}

	public void setMajor(long major) {
		this.major = major;
	}

	public Long getMinor() {
		return minor;
	}

	public void setMinor(long minor) {
		this.minor = minor;
	}

	@Override
	public int compareTo(IndexValue o) {
		long ans = this.getMajor() - o.getMajor();
		return (int) ((ans == 0)? this.getMinor() - o.getMinor() : ans);
	}

	@Override
	public String toString() {
		return "IndexValue [major=" + major + ", minor=" + minor + "]";
	}

}
