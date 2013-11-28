package org.zkoss.zss.ngmodel.impl;

import org.zkoss.zss.ngmodel.NViewAnchor;

public class PictureImpl extends AbstractPicture {

	String id;
	Format format;
	NViewAnchor anchor;
	byte[] data;
	AbstractSheet sheet;

	public PictureImpl(AbstractSheet sheet, String id, Format format,
			byte[] data, NViewAnchor anchor) {
		this.sheet = sheet;
		this.id = id;
		this.format = format;
		this.data = data;
		this.anchor = anchor;
	}
	@Override
	AbstractSheet getSheet() {
		checkOrphan();
		return sheet;
	}
	@Override
	public String getId() {
		return id;
	}
	@Override
	public Format getFormat() {
		return format;
	}
	@Override
	public NViewAnchor getAnchor() {
		return anchor;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public void release() {
		checkOrphan();
		sheet = null;
	}

	@Override
	public void checkOrphan() {
		if (sheet == null) {
			throw new IllegalStateException("doesn't connect to parent");
		}
	}
}
