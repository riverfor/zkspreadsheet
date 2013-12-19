/* SheetImpl.java

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

import java.util.ArrayList;
import java.util.List;

import org.zkoss.poi.hssf.usermodel.HSSFClientAnchor;
import org.zkoss.poi.ss.usermodel.ClientAnchor;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.xssf.usermodel.XSSFClientAnchor;
import org.zkoss.zss.api.SheetAnchor;
import org.zkoss.zss.api.UnitUtil;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Picture;
import org.zkoss.zss.api.model.Sheet;
//import org.zkoss.zss.model.sys.XBook;
//import org.zkoss.zss.model.sys.XSheet;
import org.zkoss.zss.model.sys.impl.BookHelper;
import org.zkoss.zss.model.sys.impl.DrawingManager;
import org.zkoss.zss.model.sys.impl.HSSFSheetImpl;
import org.zkoss.zss.model.sys.impl.SheetCtrl;
import org.zkoss.zss.ngmodel.NBook;
import org.zkoss.zss.ngmodel.NPicture;
import org.zkoss.zss.ngmodel.NSheet;
import org.zkoss.zss.ngmodel.NViewAnchor;
import org.zkoss.zss.ui.impl.XUtils;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class SheetImpl implements Sheet{
	private ModelRef<NSheet> _sheetRef;
	private ModelRef<NBook> _bookRef;
	private Book _book;
	public SheetImpl(ModelRef<NBook> book,ModelRef<NSheet> sheet){
		this._bookRef = book;
		this._sheetRef = sheet;
	}
	
	public NSheet getNative(){
		return _sheetRef.get();
	}
	public ModelRef<NSheet> getRef(){
		return _sheetRef;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_sheetRef == null) ? 0 : _sheetRef.hashCode());
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
		SheetImpl other = (SheetImpl) obj;
		if (_sheetRef == null) {
			if (other._sheetRef != null)
				return false;
		} else if (!_sheetRef.equals(other._sheetRef))
			return false;
		return true;
	}

	public Book getBook() {
		if(_book!=null){
			return _book;
		}
		_book = new BookImpl(_bookRef);
		return _book;
	}
	

	public boolean isProtected() {
		return getNative().isProtected();
	}

	public boolean isAutoFilterEnabled() {
		return getNative().isAutoFilterMode();
	}

	public boolean isDisplayGridlines() {
		return getNative().getViewInfo().isDisplayGridline();
	}

	public String getSheetName() {
		return getNative().getSheetName();
	}

	public boolean isRowHidden(int row) {
		return getNative().getRow(row).isHidden();
	}

	public boolean isColumnHidden(int column) {
		return getNative().getColumn(column).isHidden();
	}

	
	public List<Chart> getCharts(){
		Book book = getBook();
		List<Chart> charts = new ArrayList<Chart>();
		/*TODO zss 3.5
		DrawingManager dm = ((SheetCtrl)getNative()).getDrawingManager();
		for(org.zkoss.poi.ss.usermodel.Chart chart:dm.getCharts()){
			charts.add(new ChartImpl(_sheetRef, new SimpleRef<org.zkoss.poi.ss.usermodel.Chart>(chart)));
		}
		*/
		return charts;
	}

	
	public List<Picture> getPictures(){
		List<Picture> pictures = new ArrayList<Picture>();

		for(NPicture pic:getNative().getPictures()){
			pictures.add(new PictureImpl(_sheetRef, new SimpleRef<NPicture>(pic)));
		}
		return pictures;
	}

	public int getRowFreeze() {
		return getNative().getViewInfo().getNumOfRowFreeze();
	}

	public int getColumnFreeze() {
		return getNative().getViewInfo().getNumOfColumnFreeze();
	}

	@Override
	public boolean isPrintGridlines() {
		return getNative().getPrintInfo().isPrintGridline();
	}

	/*TODO zss 3.5
	@Override
	@Deprecated
	public org.zkoss.poi.ss.usermodel.Sheet getPoiSheet() {
		return null;
	}
	*/

	@Override
	public int getRowHeight(int row) {
		return getNative().getRow(row).getHeight();
	}

	@Override
	public int getColumnWidth(int column) {
		return getNative().getColumn(column).getWidth();
	}

	@Override
	@Deprecated
	public Object getSync() {
		return getNative();
	}
	
	
	/**
	 * Utility method, internal use only
	 */
	public static NViewAnchor toViewAnchor(NSheet sheet,SheetAnchor anchor){
		int row = anchor.getRow();
		int column = anchor.getColumn();
		int x1 = anchor.getXOffset();
		int y1 = anchor.getYOffset();
		int lrow = anchor.getLastRow();
		int lcolumn = anchor.getLastColumn();
		int x2 = anchor.getLastXOffset();
		int y2 = anchor.getLastYOffset();
		
		int w = 0;
		int h = 0;
		
		if(row == lrow){
			h = y2 - y1;
		}else{
			for(int i = row ; i<=lrow; i++){
				if(i == row){
					h += sheet.getRow(i).getHeight()-y1;
				}else if(i == lrow){
					h += y2;
				}else{
					h += sheet.getRow(i).getHeight();
				}
			}
		}
		
		if(column == lcolumn){
			w = x2 - x1;
		}else{
			for(int i = column; i<=lcolumn; i++){
				if(i == column){
					w += sheet.getColumn(i).getWidth()-x1;
				}else if(i == lcolumn){
					w += x2;
				}else{
					w += sheet.getColumn(i).getWidth();
				}
			}
		}

		NViewAnchor av = new NViewAnchor(row, column, x1, y1 ,w, h);
		return av;
	}
	
	/**
	 * Utility method, internal use only
	 */
	public static SheetAnchor toSheetAnchor(NSheet sheet,NViewAnchor anchor){
		int row = anchor.getRowIndex();
		int column = anchor.getColumnIndex();
		int x1 = anchor.getXOffset();
		int y1 = anchor.getYOffset();
		int w = anchor.getWidth();
		int h = anchor.getHeight();
		
		int row2 = row;
		int column2 = column;
		int x2 =0;
		int y2 = 0;
		
		while(w>0){
			if(column2==column){
				w = w - ( sheet.getColumn(column).getWidth() - x1);
				column2++;
			}else{
				w = w - sheet.getColumn(column).getWidth();
				column2++;
			}
		}
		column2--;
		x2 = sheet.getColumn(column2).getWidth()+w;
		
		
		while(h>0){
			if(row2==row){
				h = h - ( sheet.getRow(row2).getHeight() - y1);
				row2++;
			}else{
				h = h - sheet.getRow(row2).getHeight();
				row2++;
			}
		}
		row2--;
		y2 = sheet.getRow(row2).getHeight()+h;

		SheetAnchor san = new SheetAnchor(row,column,x1,y1,
				row2,column2,x2,y2);
		return san;
	}

	@Override
	public int getFirstRow() {
		return getNative().getStartRowIndex();
	}

	@Override
	public int getLastRow() {
		return getNative().getEndRowIndex();
	}

	@Override
	public int getFirstColumn(int row) {
		return getNative().getStartCellIndex(row);
	}

	@Override
	public int getLastColumn(int row) {
		 return getNative().getEndCellIndex(row);
	}
}
