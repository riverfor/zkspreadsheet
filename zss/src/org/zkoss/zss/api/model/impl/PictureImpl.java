/* PictureImpl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model.impl;

import org.zkoss.poi.ss.usermodel.ClientAnchor;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.SheetAnchor;
import org.zkoss.zss.api.impl.RangeImpl;
import org.zkoss.zss.api.model.Picture;
import org.zkoss.zss.model.sys.XBook;
import org.zkoss.zss.model.sys.XSheet;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class PictureImpl implements Picture{
	
	ModelRef<XSheet> sheetRef;
	ModelRef<org.zkoss.poi.ss.usermodel.Picture> picRef;
	
	public PictureImpl(ModelRef<XSheet> sheetRef, ModelRef<org.zkoss.poi.ss.usermodel.Picture> picRef) {
		this.sheetRef = sheetRef;
		this.picRef = picRef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((picRef == null) ? 0 : picRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PictureImpl other = (PictureImpl) obj;
		if (picRef == null) {
			if (other.picRef != null)
				return false;
		} else if (!picRef.equals(other.picRef))
			return false;
		return true;
	}
	
	public org.zkoss.poi.ss.usermodel.Picture getNative() {
		return picRef.get();
	}
	
	
	public String getId(){
		return getNative().getPictureId();
	}

	@Override
	public SheetAnchor getAnchor() {
		ClientAnchor anchor = getNative().getPreferredSize();
		return anchor==null?null:SheetImpl.toSheetAnchor(sheetRef.get(), anchor);
	}
}
