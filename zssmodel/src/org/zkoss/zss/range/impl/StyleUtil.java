/* StyleUtil.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 16, 2008 2:50:27 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.CellStyleHolder;
import org.zkoss.zss.model.SColor;
import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.util.CellStyleMatcher;
import org.zkoss.zss.model.util.FontMatcher;
/**
 * A utility class to help spreadsheet set style of a cell
 * @author Dennis.Chen
 * @since 3.5.0
 */
public class StyleUtil {
//	private static final Log log = Log.lookup(NStyles.class);
	
	public static SCellStyle cloneCellStyle(SCell cell) {
		final SCellStyle destination = cell.getSheet().getBook().createCellStyle(cell.getCellStyle(), true);
		return destination;
	}
	public static SCellStyle cloneCellStyle(SBook book,SCellStyle style) {
		final SCellStyle destination = book.createCellStyle(style, true);
		return destination;
	}
	
	public static void setFontColor(SBook book,CellStyleHolder holder, String color/*,HashMap<Integer,NCellStyle> cache*/){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		final SColor orgColor = orgFont.getColor();
		final SColor newColor = book.createColor(color);
		if (orgColor == newColor || orgColor != null && orgColor.equals(newColor)) {
			return;
		}
		
//		NCellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
		
		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setColor(color);
		
		SFont font = book.searchFont(fontmatcher);
		
		
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setColor(newColor);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
		
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
	}
	
	
	public static void setFillColor(SBook book,CellStyleHolder holder, String htmlColor){
		final SCellStyle orgStyle = holder.getCellStyle();
		final SColor orgColor = orgStyle.getFillColor();
		final SColor newColor = book.createColor(htmlColor);
		if (orgColor == newColor || orgColor != null  && orgColor.equals(newColor)) { //no change, skip
			return;
		}
		
		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setFillColor(htmlColor);
		matcher.setFillPattern(SCellStyle.FillPattern.SOLID_FOREGROUND);
		
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFillColor(newColor);
			style.setFillPattern(SCellStyle.FillPattern.SOLID_FOREGROUND);
		}
		holder.setCellStyle(style);
		
	}
	
	public static void setTextWrap(SBook book,CellStyleHolder holder,boolean wrap){
		final SCellStyle orgStyle = holder.getCellStyle();
		final boolean textWrap = orgStyle.isWrapText();
		if (wrap == textWrap) { //no change, skip
			return;
		}
		
		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setWrapText(wrap);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style  = cloneCellStyle(book,orgStyle);
			style.setWrapText(wrap);
		}
		holder.setCellStyle(style);
	}
	
	public static void setFontHeightPoints(SBook book,CellStyleHolder holder,int fontHeightPoints){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final int orgSize = orgFont.getHeightPoints();
		if (orgSize == fontHeightPoints) { //no change, skip
			return;
		}
		
		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setHeightPoints(fontHeightPoints);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setHeightPoints(fontHeightPoints);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
	}
	
	public static void setFontStrikethrough(SBook book,CellStyleHolder holder, boolean strikeout){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final boolean orgStrikeout = orgFont.isStrikeout();
		if (orgStrikeout == strikeout) { //no change, skip
			return;
		}

		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setStrikeout(strikeout);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setStrikeout(strikeout);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
		
	}
	
	public static void setFontName(SBook book,CellStyleHolder holder,String name){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final String orgName = orgFont.getName();
		if (orgName.equals(name)) { //no change, skip
			return;
		}
		
		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setName(name);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setName(name);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
		
	}
	
	public static final short BORDER_EDGE_BOTTOM		= 0x01;
	public static final short BORDER_EDGE_RIGHT			= 0x02;
	public static final short BORDER_EDGE_TOP			= 0x04;
	public static final short BORDER_EDGE_LEFT			= 0x08;
	public static final short BORDER_EDGE_ALL			= BORDER_EDGE_BOTTOM|BORDER_EDGE_RIGHT|BORDER_EDGE_TOP|BORDER_EDGE_LEFT;
	
	public static void setBorder(SBook book,CellStyleHolder holder, String color, SCellStyle.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_ALL);
	}
	
	public static void setBorderTop(SBook book,CellStyleHolder holder,String color, SCellStyle.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_TOP);
	}
	public static void setBorderLeft(SBook book,CellStyleHolder holder,String color, SCellStyle.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_LEFT);
	}
	public static void setBorderBottom(SBook book,CellStyleHolder holder,String color, SCellStyle.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_BOTTOM);
	}
	public static void setBorderRight(SBook book,CellStyleHolder holder,String color, SCellStyle.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_RIGHT);
	}
	
	public static void setBorder(SBook book,CellStyleHolder holder, String htmlColor, SCellStyle.BorderType lineStyle, short at){
		
		final SCellStyle orgStyle = holder.getCellStyle();
		//ZSS-464 try to search existed matched style
		SCellStyle style = null;
		final SColor color = book.createColor(htmlColor);
		boolean hasBorder = lineStyle != SCellStyle.BorderType.NONE;
		if(htmlColor!=null){
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			if((at & BORDER_EDGE_LEFT)!=0) {
				if(hasBorder)
					matcher.setBorderLeftColor(htmlColor);
				else
					matcher.removeBorderLeftColor();
				
				matcher.setBorderLeft(lineStyle);
			}
			if((at & BORDER_EDGE_TOP)!=0){
				if(hasBorder) 
					matcher.setBorderTopColor(htmlColor);
				else
					matcher.removeBorderTopColor();
				
				matcher.setBorderTop(lineStyle);
			}
			if((at & BORDER_EDGE_RIGHT)!=0){
				if(hasBorder)
					matcher.setBorderRightColor(htmlColor);
				else
					matcher.removeBorderRightColor();
				
				matcher.setBorderRight(lineStyle);
			}
			if((at & BORDER_EDGE_BOTTOM)!=0){
				if(hasBorder)
					matcher.setBorderBottomColor(htmlColor);
				else
					matcher.removeBorderBottomColor();
				
				matcher.setBorderBottom(lineStyle);
			}
			style = book.searchCellStyle(matcher);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			if((at & BORDER_EDGE_LEFT)!=0) {
				if(hasBorder)
					style.setBorderLeftColor(color);
				style.setBorderLeft(lineStyle);
			}
			if((at & BORDER_EDGE_TOP)!=0){
				if(hasBorder)
					style.setBorderTopColor(color);
				style.setBorderTop(lineStyle);
			}
			if((at & BORDER_EDGE_RIGHT)!=0){
				if(hasBorder)
					style.setBorderRightColor(color);
				style.setBorderRight(lineStyle);
			}
			if((at & BORDER_EDGE_BOTTOM)!=0){
				if(hasBorder)
					style.setBorderBottomColor(color);
				style.setBorderBottom(lineStyle);
			}
		}
		
		holder.setCellStyle(style);
	}
	
//	private static void debugStyle(String msg,int row, int col, Workbook book, NCellStyle style){
//		StringBuilder sb = new StringBuilder(msg);
//		sb.append("[").append(Ranges.getCellRefString(row, col)).append("]");
//		sb.append("Top:[").append(style.getBorderTop()).append(":").append(BookHelper.colorToBorderHTML(book,style.getTopBorderColorColor())).append("]");
//		sb.append("Left:[").append(style.getBorderLeft()).append(":").append(BookHelper.colorToBorderHTML(book,style.getLeftBorderColorColor())).append("]");
//		sb.append("Bottom:[").append(style.getBorderBottom()).append(":").append(BookHelper.colorToBorderHTML(book,style.getBottomBorderColorColor())).append("]");
//		sb.append("Right:[").append(style.getBorderRight()).append(":").append(BookHelper.colorToBorderHTML(book,style.getRightBorderColorColor())).append("]");
//		System.out.println(">>"+sb.toString());
//	}
	
	public static void setFontBoldWeight(SBook book,CellStyleHolder holder,SFont.Boldweight boldWeight){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final SFont.Boldweight orgBoldWeight = orgFont.getBoldweight();
		if (orgBoldWeight.equals(boldWeight)) { //no change, skip
			return;
		}
		
		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setBoldweight(boldWeight);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setBoldweight(boldWeight);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
	}
	
	public static void setFontItalic(SBook book,CellStyleHolder holder, boolean italic) {
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final boolean orgItalic = orgFont.isItalic();
		if (orgItalic == italic) { //no change, skip
			return;
		}

		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setItalic(italic);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setItalic(italic);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
		
	}
	
	public static void setFontUnderline(SBook book,CellStyleHolder holder, SFont.Underline underline){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final SFont.Underline orgUnderline = orgFont.getUnderline();
		if (orgUnderline.equals(underline)) { //no change, skip
			return;
		}
		
		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setUnderline(underline);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setUnderline(underline);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
	}
	
	public static void setTextHAlign(SBook book,CellStyleHolder holder, SCellStyle.Alignment align){
		final SCellStyle orgStyle = holder.getCellStyle();
		final SCellStyle.Alignment orgAlign = orgStyle.getAlignment();
		if (align.equals(orgAlign)) { //no change, skip
			return;
		}
		
		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setAlignment(align);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setAlignment(align);
		}
		holder.setCellStyle(style);
	}
	
	public static void setTextVAlign(SBook book,CellStyleHolder holder, SCellStyle.VerticalAlignment valign){
		final SCellStyle orgStyle = holder.getCellStyle();
		final SCellStyle.VerticalAlignment orgValign = orgStyle.getVerticalAlignment();
		if (valign.equals(orgValign)) { //no change, skip
			return;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setVerticalAlignment(valign);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setVerticalAlignment(valign);
		}
		holder.setCellStyle(style);

	}
	
	public static void setDataFormat(SBook book,CellStyleHolder holder, String format) {
		final SCellStyle orgStyle = holder.getCellStyle();
		final String orgFormat = orgStyle.getDataFormat();
		if (format == orgFormat || (format!=null && format.equals(orgFormat))) { //no change, skip
			return;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);

		matcher.setDataFormat(format);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setDataFormat(format);
		}
		holder.setCellStyle(style);
	}
}
