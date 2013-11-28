/* NBook.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/11/14 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ngmodel.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.zkoss.zss.ngmodel.InvalidateModelOpException;
import org.zkoss.zss.ngmodel.ModelEvent;
import org.zkoss.zss.ngmodel.ModelEvents;
import org.zkoss.zss.ngmodel.NBookSeries;
import org.zkoss.zss.ngmodel.NCell;
import org.zkoss.zss.ngmodel.NCellStyle;
import org.zkoss.zss.ngmodel.NSheet;
import org.zkoss.zss.ngmodel.util.CellStyleMatcher;
import org.zkoss.zss.ngmodel.util.SpreadsheetVersion;
import org.zkoss.zss.ngmodel.util.Strings;
import org.zkoss.zss.ngmodel.util.Validations;

/**
 * @author dennis
 *
 */
public class BookImpl extends AbstractBook{
	private static final long serialVersionUID = 1L;

	private final String bookName;
	
	private NBookSeries bookSeries;
	
	private final List<AbstractSheet> sheets = new LinkedList<AbstractSheet>();
	
	private final List<AbstractCellStyle> cellStyles = new LinkedList<AbstractCellStyle>();
	private final AbstractCellStyle defaultCellStyle;

	
	private HashMap<String,AtomicInteger> objIdCounter = new HashMap<String,AtomicInteger>();
	private int maxRowSize = SpreadsheetVersion.EXCEL2007.getMaxRows();
	private int maxColumnSize = SpreadsheetVersion.EXCEL2007.getMaxColumns();
	
	public BookImpl(String bookName){
		Validations.argNotNull(bookName);
		this.bookName = bookName;
		bookSeries = new BookSeriesImpl(this);
		cellStyles.add(defaultCellStyle = new CellStyleImpl());
	}
	
	@Override
	public NBookSeries getBookSeries(){
		return bookSeries;
	}
	
	@Override
	public String getBookName(){
		return bookName;
	}
	
	@Override
	public NSheet getSheet(int i){
		return sheets.get(i);
	}
	
	@Override
	public int getNumOfSheet(){
		return sheets.size();
	}
	
	@Override
	public NSheet getSheetByName(String name){
		for(NSheet sheet:sheets){
//			if(sheet.getSheetName().equals(name)){
			if(sheet.getSheetName().equalsIgnoreCase(name)){
				return sheet;
			}
		}
		return null;
	}
	
	protected void checkOwnership(NSheet sheet){
		if(!sheets.contains(sheet)){
			throw new InvalidateModelOpException("doesn't has ownership "+ sheet);
		}
	}
	
//	protected String suggestSheetName(String basename){
//		int i = 1;
//		HashSet<String> names = new HashSet<String>();
//		for(NSheet sheet:sheets){
//			names.add(sheet.getSheetName());
//		}
//		String name = basename==null?"Sheet 1":basename;
//		while(names.contains(name)){
//			name = basename + " "+i++;
//		};
//		return name;
//	}
	
	@Override
	protected void sendEvent(ModelEvent event){	
		//implicitly deliver to sheet
		for(AbstractSheet sheet:sheets){
			sheet.onModelEvent(event);
		}
		//TODO implicitly deliver to book series member?
		
		//call super for listeners
		super.sendEvent(event);
	}
	
	@Override
	public NSheet createSheet(String name) {
		return createSheet(name,null);
	}
	
	@Override
	String nextObjId(String type){
		StringBuilder sb = new StringBuilder(type);
		sb.append("_");
		AtomicInteger i = objIdCounter.get(type);
		if(i==null){
			objIdCounter.put(type, i = new AtomicInteger(0));
		}
		sb.append(i.getAndIncrement());
		return sb.toString();
	}
	
	@Override
	public NSheet createSheet(String name,NSheet src) {
		checkLegalName(name);
		if(src!=null)
			checkOwnership(src);
		

		AbstractSheet sheet = new SheetImpl(this,nextObjId("sheet"));
		if(src instanceof AbstractSheet){
			((AbstractSheet)src).copyTo(sheet);
		}
		((AbstractSheet)sheet).setSheetName(name);
		sheets.add(sheet);
		
		sendEvent(ModelEvents.ON_SHEET_ADDED, 
				ModelEvents.PARAM_SHEET, sheet);
		return sheet;
	}

	@Override
	public void setSheetName(NSheet sheet, String newname) {
		checkLegalName(newname);
		checkOwnership(sheet);
		
		String oldname = sheet.getSheetName();
		((AbstractSheet)sheet).setSheetName(newname);
		
		sendEvent(ModelEvents.ON_SHEET_RENAMED, 
				ModelEvents.PARAM_SHEET, sheet,
				ModelEvents.PARAM_SHEET_OLD_NAME, oldname);
	}

	private void checkLegalName(String name) {
		if(Strings.isBlank(name)){
			throw new InvalidateModelOpException("sheet name '"+name+"' is not legal");
		}
		if(getSheetByName(name)!=null){
			throw new InvalidateModelOpException("sheet name '"+name+"' is dpulicated");
		}
		//TODO
	}

	@Override
	public void deleteSheet(NSheet sheet) {
		checkOwnership(sheet);
		
		((AbstractSheet)sheet).release();
		
		int index = sheets.indexOf(sheet);
		sheets.remove(index);
		
		sendEvent(ModelEvents.ON_SHEET_DELETED, 
				ModelEvents.PARAM_SHEET, sheet,
				ModelEvents.PARAM_SHEET_OLD_INDEX, index);
	}

	@Override
	public void moveSheetTo(NSheet sheet, int index) {
		checkOwnership(sheet);
		if(index<0|| index>=sheets.size()){
			throw new InvalidateModelOpException("new position out of bound "+sheets.size() +"<>" +index);
		}
		int oldindex = sheets.indexOf(sheet);
		if(oldindex==index){
			return;
		}
		sheets.remove(oldindex);
		sheets.add(index, (AbstractSheet)sheet);
		sendEvent(ModelEvents.ON_SHEET_MOVED, 
				ModelEvents.PARAM_SHEET, sheet,
				ModelEvents.PARAM_SHEET_OLD_INDEX, oldindex);
	}

	public void dump(StringBuilder builder) {
		for(AbstractSheet sheet:sheets){
			if(sheet instanceof SheetImpl){
				((SheetImpl)sheet).dump(builder);
			}else{
				builder.append("\n").append(sheet);
			}
		}
	}

	@Override
	public NCellStyle getDefaultCellStyle() {
		return defaultCellStyle;
	}

	@Override
	public NCellStyle createCellStyle(boolean inStyleTable) {
		return createCellStyle(null,inStyleTable);
	}

	@Override
	public NCellStyle createCellStyle(NCellStyle src,boolean inStyleTable) {
		if(src!=null){
			Validations.argInstance(src, AbstractCellStyle.class);
		}
		AbstractCellStyle style = new CellStyleImpl();
		if(src!=null){
			((AbstractCellStyle)src).copyTo(style);
		}
		
		if(inStyleTable){
			cellStyles.add(style);
		}
		
		return style;
	}
	
	@Override
	public NCellStyle searchCellStyle(CellStyleMatcher matcher) {
		for(NCellStyle style:cellStyles){
			if(matcher.match(style)){
				return style;
			}
		}
		return null;
	}
	
	@Override
	public int getMaxRowSize() {
		return maxRowSize;
	}

	@Override
	public int getMaxColumnSize() {
		return maxColumnSize;
	}

	@Override
	List<NCell> optimizeCellStyle() {
		//search all the cell's style , 
		//if it is same as style in the style table (but different instance), then reassign the one in the table
		// 
		//if no one match a cell's style, then set it to style table.
		//(Optional) it total cell style are too many, search the similar cell style the get a similar style and reassign to the cell
		
		//TODO
		throw new UnsupportedOperationException("not implementate la.");
	}

}
