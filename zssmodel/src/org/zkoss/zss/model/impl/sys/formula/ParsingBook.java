/* ParsingBook.java

	Purpose:
		
	Description:
		
	History:
		Dec 13, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.model.impl.sys.formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.poi.ss.SpreadsheetVersion;
import org.zkoss.poi.ss.formula.EvaluationName;
import org.zkoss.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.zkoss.poi.ss.formula.FormulaParser;
import org.zkoss.poi.ss.formula.FormulaParsingWorkbook;
import org.zkoss.poi.ss.formula.FormulaRenderingWorkbook;
import org.zkoss.poi.ss.formula.FormulaType;
import org.zkoss.poi.ss.formula.ptg.NamePtg;
import org.zkoss.poi.ss.formula.ptg.NameXPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.sys.formula.FormulaEngine;

/**
 * A pseudo formula parsing workbook for parsing only.
 * @author Pao
 * @since 3.5.0
 */
public class ParsingBook implements FormulaParsingWorkbook, FormulaRenderingWorkbook {
	private static final Log logger = Log.lookup(ParsingBook.class.getName());

	private SBook book;
	// defined names
	private List<String> index2name = new ArrayList<String>();
	private Map<String, Integer> name2index = new HashMap<String, Integer>();
	// sheets
	private List<ExternalSheet> index2sheet = new ArrayList<ExternalSheet>();
	private Map<String, Integer> sheetName2index = new HashMap<String, Integer>(); // the name combine names of book, sheet 1 and sheet 2

	public ParsingBook(SBook book) {
		this.book = book;
	}

	// ZSS-661
	public void renameName(int sheetIndex, String oldName, String newName) {
		final String sidx = String.valueOf(sheetIndex);
		String oldkey = toKey(sidx, oldName);
		final Integer index = name2index.remove(oldkey);
		if (index != null) {
			String key = toKey(sidx, newName);
			name2index.put(key, index);
			index2name.set(index, newName);
		}
	}
	
	@Override
	public EvaluationName getName(String name, int sheetIndex) {
		return getOrCreateName(name, sheetIndex);
	}

	@Override
	public NameXPtg getNameXPtg(String name) {
		String key = toKey("", name);
		Integer index = name2index.get(key);
		if(index == null) {
			// formula function name
			index = index2name.size();
			index2name.add(name);
			name2index.put(key, index);
		}
		return new NameXPtg(0, index);
	}

	@Override
	public int getExternalSheetIndex(String sheetName) {
		return getExternalSheetIndex(null, sheetName);
	}

	@Override
	public int getExternalSheetIndex(String workbookName, String sheetName) {
		// directly get index if existed
		String key = toKey(workbookName, sheetName);
		Integer index = sheetName2index.get(key);
		if(index == null) {
			// create new index and check sheet name is 3D or not
			index = index2sheet.size();
			int p = sheetName.indexOf(':');
			String name = p < 0 ? sheetName : sheetName.substring(0, p);
			String lastName = p < 0 ? sheetName : sheetName.substring(p+1);
			index2sheet.add(new ExternalSheet(workbookName, name, lastName));
			sheetName2index.put(toKey(workbookName, sheetName), index);
		}
		return index;
	}
	
	/**
	 * @param sheetName sheet name or 3D sheet name (e.g "Sheet1:Sheet3")
	 * @return the external sheet index or -1 if not found
	 */
	public int findExternalSheetIndex(String sheetName) {
		return findExternalSheetIndex(null, sheetName);
	}

	/**
	 * @param workbookName book name or null
	 * @param sheetName sheet name or 3D sheet name (e.g "Sheet1:Sheet3")
	 * @return the external sheet index or -1 if not found
	 */
	public int findExternalSheetIndex(String workbookName, String sheetName) {
		Integer index = sheetName2index.get(toKey(workbookName, sheetName));
		return index != null ? index : -1;
	}

	private String toKey(String... strings) {
		return Arrays.toString(strings);
	}

	@Override
	public SpreadsheetVersion getSpreadsheetVersion() {
		// TODO zss 3.5
		return SpreadsheetVersion.EXCEL2007;
	}

	@Override
	public String getBookNameFromExternalLinkIndex(String externalLinkIndex) {

		try {
			// if external link index is really a index, convert it and find name from records
			int index = Integer.parseInt(externalLinkIndex) - 1; // zero based
			List<?> names = (List<?>)book.getAttribute(FormulaEngine.KEY_EXTERNAL_BOOK_NAMES);
			if(names != null) {
				return names.get(index).toString();
			}
		} catch(NumberFormatException e) {
			// do nothing
		} catch(IndexOutOfBoundsException e) {
			logger.warning(e.getMessage(), e);
		}

		// otherwise, it should be a book name already and just return itself.
		return externalLinkIndex;
	}

	@Override
	public EvaluationName getOrCreateName(String name, int sheetIndex) {
		String key = toKey(String.valueOf(sheetIndex), name);
		Integer index = name2index.get(key);
		if(index == null) {
			index = index2name.size();
			index2name.add(name);
			name2index.put(key, index);
		}
		EvaluationName n = new SimpleName(name, index, sheetIndex);
		return n;
	}
	
	/* FormulaRenderingWorkbook */

	@Override
	public String getNameText(NamePtg namePtg) {
		return index2name.get(namePtg.getIndex());
	}
	
	@Override
	public String resolveNameXText(NameXPtg nameXPtg) {
		return index2name.get(nameXPtg.getNameIndex());
	}
	
	/**
	 * @return internal or external sheet.
	 */
	public ExternalSheet getAnyExternalSheet(int externSheetIndex) {
		return index2sheet.get(externSheetIndex);
	}
	
	@Override
	public ExternalSheet getExternalSheet(int externSheetIndex) {
		// return external sheet object if only if the sheet is exact external
		ExternalSheet externalSheet = getAnyExternalSheet(externSheetIndex);
		return externalSheet.getWorkbookName() != null ? externalSheet : null;
	}
	
	@Override
	public String getSheetNameByExternSheet(int externSheetIndex) {
		// get sheet no matter external or internal, and covert to 3D ref. if any
		ExternalSheet sheet = getAnyExternalSheet(externSheetIndex);
		String name = sheet.getSheetName();
		String lastName = sheet.getLastSheetName();
		return name.equals(lastName) ? name : name + ":" + lastName;
	}

	@Override
	public String getExternalLinkIndexFromBookName(String bookname) {
		return bookname;
	}
	
	/**
	 * rename a sheet in this parsing book directly.
	 * if it can't find a sheet with old name, it won't create a sheet for the new name.
	 */
	public void renameSheet(String bookName, String oldName, String newName) {
		
		// null as current book
		if(book.getBookName().equals(bookName)) {
			bookName = null;
		}
		
		// check every external sheet data and rename sheet if necessary
		// rename as replacing by new external sheets (Note, the index should not be changed)
		List<ExternalSheet> temp = new ArrayList<ExternalSheet>(index2sheet.size()); 
		for(ExternalSheet extSheet : index2sheet) {
			if((bookName == null && extSheet.getWorkbookName() == null)
				|| (bookName != null && bookName.equals(extSheet.getWorkbookName()))) {
				String sheet1 = oldName.equals(extSheet.getSheetName()) ? newName : extSheet.getSheetName();
				String sheet2 = oldName.equals(extSheet.getLastSheetName()) ? newName : extSheet.getLastSheetName();
				temp.add(new ExternalSheet(extSheet.getWorkbookName(), sheet1, sheet2));
			} else {
				temp.add(extSheet);
			}
		}
		index2sheet = temp;
		
		// clear the map of external sheet name to index and rebuild it
		sheetName2index.clear();
		for(int i = 0; i < index2sheet.size(); ++i) {
			String book = index2sheet.get(i).getWorkbookName();
			String sheet1 = index2sheet.get(i).getSheetName();
			String sheet2 = index2sheet.get(i).getLastSheetName();
			String key = toKey(book, sheet1.equals(sheet2) ? (sheet1 + ":" + sheet2) : sheet1);
			sheetName2index.put(key, i);
		}
	}

	/**
	 * name to represent named range
	 * @author Pao
	 */
	private class SimpleName implements EvaluationName {

		private final String name;
		private final int nameIndex;
		private int sheetIndex;

		/**
		 * @param sheetIndex sheet index; if -1, indicates whole book.
		 */
		public SimpleName(String name, int nameIndex, int sheetIndex) {
			this.name = name;
			this.nameIndex = nameIndex;
			this.sheetIndex = sheetIndex;
		}

		public Ptg[] getNameDefinition() {
			return FormulaParser.parse(name, ParsingBook.this, FormulaType.NAMEDRANGE, sheetIndex);
		}

		public String getNameText() {
			return name;
		}

		public boolean hasFormula() {
			return false;
		}

		public boolean isFunctionName() {
			return false;
		}

		public boolean isRange() {
			return false;
		}

		public NamePtg createPtg() {
			return new NamePtg(nameIndex);
		}
	}
	
	//to compatible with zss-575 in 3.0
	@Override
	public boolean isAllowedDeferredNamePtg() {
		return true;
	}

}
