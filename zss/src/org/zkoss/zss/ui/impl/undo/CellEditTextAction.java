/* CellEditTextAction.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/7/25, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl.undo;

import org.zkoss.zss.api.IllegalFormulaException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.Sheet;
/**
 * 
 * @author dennis
 *
 */
public class CellEditTextAction extends AbstractEditTextAction {
	
	private final String _editText;
	private final String[][] _editTexts;
	
	public CellEditTextAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,String editText){
		super(label,sheet,row,column,lastRow,lastColumn);
		this._editText = editText;
		_editTexts = null;
	}
	public CellEditTextAction(String label,Sheet sheet,int row, int column, int lastRow,int lastColumn,String[][] editTexts){
		super(label,sheet,row,column,lastRow,lastColumn);
		this._editTexts = editTexts;
		_editText = null;
	}

	protected void applyAction(){
		boolean protect = isSheetProtected();
		if(_editText!=null && !protect){
			Range r = Ranges.range(_sheet,_row,_column,_lastRow,_lastColumn);
			r.setCellEditText(_editText);
		}else{
			for(int i=_row;i<=_lastRow;i++){
				for(int j=_column;j<=_lastColumn;j++){
					Range r = Ranges.range(_sheet,i,j);
					boolean lock = r.getCellStyle().isLocked();
					if(protect && lock)
						continue;
					
					try{
						r.setCellEditText(_editText!=null?_editText:_editTexts[i][j]);
					}catch(IllegalFormulaException x){};//eat in this mode
				}
			}
		}
	}
}
