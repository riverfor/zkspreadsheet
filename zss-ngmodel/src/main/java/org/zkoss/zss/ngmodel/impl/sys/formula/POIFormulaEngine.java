/* ZPOIEngine.java

	Purpose:
		
	Description:
		
	History:
		Dec 10, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.ngmodel.impl.sys.formula;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.poi.ss.formula.CollaboratingWorkbooksEnvironment;
import org.zkoss.poi.ss.formula.EvaluationCell;
import org.zkoss.poi.ss.formula.FormulaParseException;
import org.zkoss.poi.ss.formula.FormulaParser;
import org.zkoss.poi.ss.formula.FormulaType;
import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.eval.ValuesEval;
import org.zkoss.poi.ss.formula.ptg.Area3DPtg;
import org.zkoss.poi.ss.formula.ptg.AreaPtg;
import org.zkoss.poi.ss.formula.ptg.FuncPtg;
import org.zkoss.poi.ss.formula.ptg.NamePtg;
import org.zkoss.poi.ss.formula.ptg.NameXPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.Ref3DPtg;
import org.zkoss.poi.ss.formula.ptg.RefPtg;
import org.zkoss.zss.ngmodel.CellRegion;
import org.zkoss.zss.ngmodel.ErrorValue;
import org.zkoss.zss.ngmodel.NBook;
import org.zkoss.zss.ngmodel.NCell;
import org.zkoss.zss.ngmodel.NSheet;
import org.zkoss.zss.ngmodel.impl.BookSeriesAdv;
import org.zkoss.zss.ngmodel.impl.NameRefImpl;
import org.zkoss.zss.ngmodel.impl.RefImpl;
import org.zkoss.zss.ngmodel.impl.sys.DependencyTableAdv;
import org.zkoss.zss.ngmodel.sys.dependency.Ref;
import org.zkoss.zss.ngmodel.sys.dependency.Ref.RefType;
import org.zkoss.zss.ngmodel.sys.formula.EvaluationResult;
import org.zkoss.zss.ngmodel.sys.formula.EvaluationResult.ResultType;
import org.zkoss.zss.ngmodel.sys.formula.FormulaEngine;
import org.zkoss.zss.ngmodel.sys.formula.FormulaEvaluationContext;
import org.zkoss.zss.ngmodel.sys.formula.FormulaExpression;
import org.zkoss.zss.ngmodel.sys.formula.FormulaParseContext;

/**
 * A formula engine implemented by ZPOI
 * @author Pao
 */
public class POIFormulaEngine implements FormulaEngine {
	private final static Logger logger = Logger.getLogger(POIFormulaEngine.class.getName());

	@Override
	public FormulaExpression parse(String formula, FormulaParseContext context) {
		FormulaExpression expr = null;
		try {
			// adapt and parse
			NBook book = context.getBook();
			ParsingBook parsingBook = new ParsingBook(context.getSheet().getSheetName());
			Ptg[] tokens = FormulaParser.parse(formula, parsingBook, FormulaType.CELL, 0); // current sheet index in parsing is always 0

			// dependency tracking
			BookSeriesAdv series = (BookSeriesAdv)book.getBookSeries();
			DependencyTableAdv table = (DependencyTableAdv)series.getDependencyTable();
			Ref dependant = context.getDependent();
			for(Ptg ptg : tokens) {
				Ref precedent = toDenpendRef(context, parsingBook, ptg);
				if(precedent != null) {
					table.add(dependant, precedent);
				}
			}

			// create result
			Ref singleRef = tokens.length == 1 ? toDenpendRef(context, parsingBook, tokens[0]) : null;
			expr = new FormulaExpressionImpl(formula, singleRef, false);
		} catch(FormulaParseException e) {
			logger.log(Level.INFO, e.getMessage(), e);
			expr = new FormulaExpressionImpl(formula, null, true);
		}
		return expr;
	}

	private Ref toDenpendRef(FormulaParseContext ctx, ParsingBook parsingBook, Ptg ptg) {
		try {
			NSheet sheet = ctx.getSheet();

			if(ptg instanceof NamePtg) {
				NamePtg namePtg = (NamePtg)ptg;
				String bookName = sheet.getBook().getBookName();
				String name = parsingBook.getName(namePtg.getIndex());
				return new NameRefImpl(bookName, null, name); // assume name is book-scope
			} else if(ptg instanceof NameXPtg) {
				// TODO name in external book
				// return ec.getNameXEval(((NameXPtg)ptg));
			} else if(ptg instanceof Ref3DPtg) {
				Ref3DPtg rptg = (Ref3DPtg)ptg;
				String bookName = sheet.getBook().getBookName();
				String[] tokens = parsingBook.getName(rptg.getExternSheetIndex()).split(":");
				String sheetName = tokens[0];
				String lastSheetName = tokens.length >= 2 ? tokens[1] : null;
				return new RefImpl(bookName, sheetName, lastSheetName, rptg.getRow(), rptg.getColumn());
			} else if(ptg instanceof Area3DPtg) {
				Area3DPtg aptg = (Area3DPtg)ptg;
				String bookName = sheet.getBook().getBookName();
				String[] tokens = parsingBook.getName(aptg.getExternSheetIndex()).split(":");
				String sheetName = tokens[0];
				String lastSheetName = tokens.length >= 2 ? tokens[1] : null;
				return new RefImpl(bookName, sheetName, lastSheetName, aptg.getFirstRow(),
						aptg.getFirstColumn(), aptg.getLastRow(), aptg.getLastColumn());
			} else if(ptg instanceof RefPtg) {
				RefPtg rptg = (RefPtg)ptg;
				String bookName = sheet.getBook().getBookName();
				String sheetName = sheet.getSheetName();
				return new RefImpl(bookName, sheetName, rptg.getRow(), rptg.getColumn());
			} else if(ptg instanceof AreaPtg) {
				AreaPtg aptg = (AreaPtg)ptg;
				String sheetName = sheet.getSheetName();
				String bookName = sheet.getBook().getBookName();
				return new RefImpl(bookName, sheetName, aptg.getFirstRow(), aptg.getFirstColumn(),
						aptg.getLastRow(), aptg.getLastColumn());
			} else if(ptg instanceof FuncPtg) {
				// TODO consider function-type dependency
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}

	@Override
	public EvaluationResult evaluate(FormulaExpression expr, FormulaEvaluationContext context) {

		// by pass if expression is invalid format
		if(expr.hasError()) {
			return new EvaluationResultImpl(ResultType.ERROR, new ErrorValue(ErrorValue.INVALID_FORMULA));
		}

		EvaluationResult result = null;
		try {
			NBook book = context.getBook();
			EvalBook evalBook = null;
			WorkbookEvaluator evaluator = null;

			// book series
			BookSeriesAdv bookSeries = (BookSeriesAdv)book.getBookSeries();
			NBook[] books = bookSeries.getBooks().toArray(new NBook[0]);
			String[] bookNames = new String[books.length];
			WorkbookEvaluator[] evaluators = new WorkbookEvaluator[books.length];
			for(int i = 0; i < books.length; ++i) {
				bookNames[i] = books[i].getBookName();
				EvalBook eb = new EvalBook(books[i]);
				evaluators[i] = new WorkbookEvaluator(eb, null, null);
				if(context.getBook() == books[i]) {
					evalBook = eb;
					evaluator = evaluators[i];
				}
			}
			CollaboratingWorkbooksEnvironment.setup(bookNames, evaluators);
			if(evalBook == null || evaluator == null) { // just in case
				return new EvaluationResultImpl(ResultType.ERROR, "The book isn't in the book series.");
			}

			// evaluation formula
			ValueEval value = null;
			int currentSheetIndex = book.getSheetIndex(context.getSheet());
			if(context.getCell().isNull()) {
				// evaluation formula directly
				value = evaluator.evaluate(currentSheetIndex, expr.getFormulaString(), false);
			} else {
				NCell cell = context.getCell();
				EvaluationCell evalCell = evalBook.getSheet(currentSheetIndex).getCell(cell.getRowIndex(),
						cell.getColumnIndex());
				value = evaluator.evaluate(evalCell);
			}

			// convert to result
			if(value instanceof ErrorEval) {
				int code = ((ErrorEval)value).getErrorCode();
				result = new EvaluationResultImpl(ResultType.ERROR, new ErrorValue((byte)code));
			} else if(value instanceof StringEval) {
				result = new EvaluationResultImpl(ResultType.SUCCESS, ((StringEval)value).getStringValue());
			} else if(value instanceof NumberEval) {
				result = new EvaluationResultImpl(ResultType.SUCCESS, ((NumberEval)value).getNumberValue());
			} else if(value instanceof ValuesEval) {
				ValueEval[] values = ((ValuesEval)value).getValueEvals();
				Object[] array = new Object[values.length];
				for(int i = 0; i < values.length; ++i) {
					if(value instanceof StringEval) {
						array[i] = ((StringEval)values[i]).getStringValue();
					} else if(value instanceof NumberEval) {
						array[i] = ((NumberEval)values[i]).getNumberValue();
					} else {
						throw new Exception("no matched type: " + array[i]); // FIXME
					}
				}
				return new EvaluationResultImpl(ResultType.SUCCESS, array);
			} else {
				throw new Exception("no matched type: " + value); // FIXME
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			result = new EvaluationResultImpl(ResultType.ERROR, new ErrorValue(ErrorValue.INVALID_FORMULA));
		}
		return result;
	}

	private static class FormulaExpressionImpl implements FormulaExpression, Serializable {
		private static final long serialVersionUID = -8532826169759927711L;
		private String formula;
		private Ref ref;
		private boolean error;

		/**
		 * @param ref resolved reference if formula has only one parsed token
		 */
		public FormulaExpressionImpl(String formula, Ref ref, boolean error) {
			this.formula = formula;
			this.ref = ref;
			this.error = error;
		}

		@Override
		public boolean hasError() {
			return error;
		}

		@Override
		public String getFormulaString() {
			return formula;
		}

		@Override
		public String reformSheetNameChanged(String oldName, String newName) {
			// TODO
			return formula;
		}

		@Override
		public boolean isRefersTo() {
			return ref != null && (ref.getType() == RefType.AREA || ref.getType() == RefType.CELL);
		}

		@Override
		public String getRefersToSheetName() {
			// FIXME 3D sheets
			return isRefersTo() ? ref.getSheetName() : null;
		}

		@Override
		public CellRegion getRefersToCellRegion() {
			return isRefersTo() ? new CellRegion(ref.getRow(), ref.getColumn(), ref.getLastRow(),
					ref.getLastColumn()) : null;
		}

	}

	private static class EvaluationResultImpl implements EvaluationResult {

		private ResultType type;
		private Object value;

		public EvaluationResultImpl(ResultType type, Object value) {
			this.type = type;
			this.value = value;
		}

		@Override
		public ResultType getType() {
			return type;
		}

		@Override
		public Object getValue() {
			return value;
		}

	}

}
