import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		PicrossAffichage frame = new PicrossAffichage("XInput.txt");
		long t0 = System.currentTimeMillis();
		//PossibleRows.backtrack(frame);
		//PossibleRows.solving(frame);
		PossibleRows.solvingBacktrackwithCombination(frame);
		long tf = System.currentTimeMillis();
		System.out.println("Computation time : " + (tf - t0) + " ms");
	}
}

class PossibleRows {
	LinkedList<byte[]> list;
	boolean flag;

	private PossibleRows(byte[] rowClue, byte numberColumn) {
		list = new LinkedList<byte[]>();
		this.allPossible(new byte[numberColumn], 0, 0, rowClue, lastPossibleCell(rowClue, numberColumn));
		flag = true;
	}
	
	private PossibleRows(PossibleRows that) {
		list = new LinkedList<byte[]>();
		if (that != null && !that.list.isEmpty())
			this.list.addAll(that.list);
		flag = that.flag;
	}

	private void allPossible(byte[] arrayInConstruction, int currentCell, int numberRectanglePut, byte[] rowClue,
			byte[] lastPossibleCell) {
		for (int j = currentCell; j < lastPossibleCell[numberRectanglePut] + 1; j++) {
			byte[] tmp = new byte[arrayInConstruction.length];
			for (int i = 0; i < currentCell; i++)
				tmp[i] = arrayInConstruction[i];
			int currentCellTmp = currentCell;
			while (currentCellTmp < j)
				tmp[currentCellTmp++] = PicrossGrid.AWHITE;
			while (currentCellTmp < j + rowClue[numberRectanglePut])
				tmp[currentCellTmp++] = PicrossGrid.ABLACK;
			if (numberRectanglePut + 1 == rowClue.length) {
				while (currentCellTmp < tmp.length)
					tmp[currentCellTmp++] = PicrossGrid.AWHITE;
				this.list.add(tmp);
			} else {
				tmp[currentCellTmp++] = PicrossGrid.AWHITE;
				this.allPossible(tmp, currentCellTmp, numberRectanglePut + 1, rowClue, lastPossibleCell);
			}
		}
	}

	private static byte[] lastPossibleCell(byte[] rowClue, byte numberColumn) {
		int n = rowClue.length;
		byte[] c = new byte[n];
		c[n - 1] = (byte) (numberColumn - rowClue[n - 1]);
		for (int i = n - 2; i > -1; i--) {
			c[i] = (byte) (c[i + 1] - 1 - rowClue[i]);
		}
		return c;
	}
	
	private static boolean check(byte[][] columnsClue, byte[][] grid) {
		for (int i = 0; i < columnsClue.length; i++)
			if (!checkAux(columnsClue[i], grid, i))
				return false;
		return true;
	}

	private static boolean checkAux(byte[] columnsClue, byte[][] grid, int q) {
		byte[] reste = PossibleRows.lastPossibleCell(columnsClue, (byte) grid[0].length);
		int i = 0, j = 1; // indice du rectangle noir et indice de parcours
		int m = grid[0][q]; // couleur de la premiere case
		int l = (m == PicrossGrid.BLACK || m == PicrossGrid.ABLACK) ? 1 : 0;
		while (j < grid.length) {
			if (grid[j][q] == PicrossGrid.UNKNOWN)
				return true;
			else if (grid[j][q] == PicrossGrid.WHITE || grid[j][q] == PicrossGrid.AWHITE) {
				if (m == PicrossGrid.BLACK || m == PicrossGrid.ABLACK) {
					if (columnsClue[i] != l) { // Respecte-t-on le nb de carré
												// noir
						return false;
					}
					i++; // On regarde le rectangle noir suivant
					l = 0;
				}
				if (i < reste.length && !(j < reste[i])) { // reste place?
					return false;
				}
			} else if (grid[j][q] == PicrossGrid.BLACK || grid[j][q] == PicrossGrid.ABLACK) {
				if (!(i < reste.length)) // trop de rectangle noir
					return false;
				l++;
				if (l > columnsClue[i]) // trop de case noire
					return false;
			}
			m = grid[j][q];
			j++;
		}
		return true;
	}
	
	public static void backtrack(PicrossAffichage frame) {
		PicrossGrid picross = frame.picross;
		PossibleRows[] c = new PossibleRows[picross.numberRow];
		for (int i = 0; i < picross.numberRow; i++)
			c[i] = new PossibleRows(picross.rowClue[i], picross.numberColumn);
		backtrack(c, frame);
	}

	private static void backtrack(PossibleRows[] rows, PicrossAffichage frame) {
		PicrossGrid picross = frame.picross;
		for (int i = 0; i < picross.numberRow; i++)
			rows[i].exclusion(picross.grid[i]);
		byte[][] gridBeforeBacktrack = copyGrid(frame.picross);
		PossibleRows[] rowBeforeBacktrack = copyArrayRows(rows);
		backtrack(rows, rowBeforeBacktrack, frame, gridBeforeBacktrack);
	}

	private static void backtrack(PossibleRows[] rows, PossibleRows[] rowBeforeBacktrack, PicrossAffichage frame,
			byte[][] gridBeforeBacktrack) {
		PicrossGrid picross = frame.picross;
		int i = 0;
		while (i < picross.numberRow) {
			//slow();
			byte[] d = rows[i].list.pollFirst();
			if (d == null) {
				picross.grid[i] = gridBeforeBacktrack[i];
				rows[i] = new PossibleRows(rowBeforeBacktrack[i--]);
				continue;
			}
			picross.grid[i] = d;
			if (check(picross.columnClue, picross.grid))
				i++;
			frame.repaint();
		}
	}

	public static byte[][] copyGrid(PicrossGrid picross) {
		byte[][] gridBeforeBacktrack = new byte[picross.numberRow][picross.numberColumn];
		for (int i = 0; i < gridBeforeBacktrack.length; i++)
			for (int j = 0; j < gridBeforeBacktrack[i].length; j++)
				gridBeforeBacktrack[i][j] = picross.grid[i][j];
		return gridBeforeBacktrack;
	}
	
	private static PossibleRows[] copyArrayRows(PossibleRows[] rows) {
		PossibleRows[] rowBeforeBacktrack = new PossibleRows[rows.length];
		for (int i = 0; i < rows.length; i++)
			rowBeforeBacktrack[i] = new PossibleRows(rows[i]);
		return rowBeforeBacktrack;
	}
	
	private byte[] combination() {
		int j = 1, n = this.list.size();
		byte[] result = copyFirstElement();
		while (j++ < n) {
			byte[] array = this.list.pop();
			this.list.addLast(array);
			combinationAux(result, array);
		}
		return result;
	}

	private byte[] copyFirstElement() {
		byte[] first = this.list.pop();
		this.list.addLast(first);
		byte[] result = new byte[first.length];
		for (int i = 0; i < result.length; i++) {
			if (first[i] == PicrossGrid.ABLACK)
				result[i] = PicrossGrid.BLACK;
			else if (first[i] == PicrossGrid.AWHITE)
				result[i] = PicrossGrid.WHITE;
			else
				result[i] = first[i];
		}
		return result;
	}

	private static void combinationAux(byte[] result, byte[] array) {
		for (int i = 0; i < result.length; i++)
			if (result[i] != PicrossGrid.UNKNOWN)
				if (array[i] == PicrossGrid.ABLACK || array[i] == PicrossGrid.BLACK) {
					if (result[i] == PicrossGrid.WHITE)
						result[i] = PicrossGrid.UNKNOWN;
				} else if (array[i] == PicrossGrid.AWHITE || array[i] == PicrossGrid.WHITE)
					if (result[i] == PicrossGrid.BLACK)
						result[i] = PicrossGrid.UNKNOWN;
	}
	
	private boolean exclusion(byte[] partialSolution) {
		int j = 0, n = this.list.size();
		while (j++ < n) {
			byte[] possibleArray = this.list.pop();
			if (isCompatible(possibleArray, partialSolution))
				this.list.addLast(possibleArray);
		}
		return n != this.list.size();
	}

	private static boolean isCompatible(byte[] possibleArray, byte[] partialSolution) {
		boolean c = true;
		int i = 0;
		while (i < partialSolution.length && c) {
			if (partialSolution[i] != PicrossGrid.UNKNOWN) {
				if (partialSolution[i] == PicrossGrid.BLACK || partialSolution[i] == PicrossGrid.ABLACK) {
					if (possibleArray[i] == PicrossGrid.AWHITE || possibleArray[i] == PicrossGrid.WHITE)
						c = false;
				} else if (possibleArray[i] == PicrossGrid.ABLACK || possibleArray[i] == PicrossGrid.BLACK)
					c = false;
				possibleArray[i] = partialSolution[i];
			}
			i++;
		}
		return c;
	}
	
	public static void solving(PicrossAffichage frame) {
		PicrossGrid picross = frame.picross;
		PossibleRows[] rows = initializeRows(picross);
		PossibleRows[] columns = initializeColumns(picross);
		boolean continueCombination = true;
		while (continueCombination) {
			continueCombination = exclusionCombinationRows(rows, columns, picross);
			continueCombination = exclusionCombinationColumns(rows, columns, picross) || continueCombination;
			frame.repaint();
		}
		backtrack(rows, frame);
	}

	private static PossibleRows[] initializeRows(PicrossGrid picross) {
		PossibleRows[] lig = new PossibleRows[picross.numberRow];
		for (int i = 0; i < picross.numberRow; i++)
			lig[i] = new PossibleRows(picross.rowClue[i], picross.numberColumn);
		return lig;
	}

	private static PossibleRows[] initializeColumns(PicrossGrid picross) {
		PossibleRows[] col = new PossibleRows[picross.numberColumn];
		for (int i = 0; i < picross.numberColumn; i++)
			col[i] = new PossibleRows(picross.columnClue[i], picross.numberRow);
		return col;
	}

	private static boolean exclusionCombinationRows(PossibleRows[] rows, PossibleRows[] columns, PicrossGrid picross) {
		boolean result = false;
		for (int i = 0; i < picross.numberRow; i++) {
			//slow();
			if (rows[i].flag) {
				//slow();
				result = rows[i].exclusion(picross.grid[i]) || result;
				byte[] tmp = rows[i].combination();
				for (int j = 0; j < picross.numberColumn; j++)
					if (!columns[j].flag)
						columns[j].flag = (picross.grid[i][j] != tmp[j]);
				picross.grid[i] = tmp;
			}
			rows[i].flag = false;
		}
		return result;
	}

	private static boolean exclusionCombinationColumns(PossibleRows[] rows, PossibleRows[] columns,
			PicrossGrid picross) {
		boolean result = false;
		for (int i = 0; i < picross.numberColumn; i++) {
			byte[] tmp = new byte[picross.numberRow];
			for (int j = 0; j < picross.numberRow; j++)
				tmp[j] = picross.grid[j][i];
			result = columns[i].exclusion(tmp) || result;
			tmp = columns[i].combination();
			for (int j = 0; j < picross.numberColumn; j++)
				if (!rows[j].flag)
					rows[j].flag = (picross.grid[j][i] != tmp[j]);
			for (int k = 0; k < picross.numberRow; k++)
				picross.grid[k][i] = tmp[k];
			columns[i].flag = false;
		}
		return result;
	}

	public static void solvingBacktrackwithCombination(PicrossAffichage frame) {
		PicrossGrid picross = frame.picross;
		PossibleRows[] rows = initializeRows(picross);
		PossibleRows[] columns = initializeColumns(picross);
		boolean continueCombination = true;
		while (continueCombination) {
			continueCombination = exclusionCombinationRows(rows, columns, picross);
			continueCombination = exclusionCombinationColumns(rows, columns, picross) || continueCombination;
			frame.repaint();
		}
		backtrackWithCombinationAux(frame, picross, rows, columns);
	}

	private static void backtrackWithCombinationAux(PicrossAffichage frame, PicrossGrid picross, PossibleRows[] rows,
			PossibleRows[] columns) {
		for (int i = 0; i < picross.numberRow; i++)
			rows[i].exclusion(picross.grid[i]);
		for (int i = 0; i < picross.numberRow; i++) {
			byte[] a = rows[i].list.pop();
			rows[i].list.clear();
			rows[i].list.add(a);
			picross.grid[i] = a;
			boolean continueCombination = true;
			for (int j = 0; j < picross.numberColumn; j++)
				columns[j].flag = true;
			while (continueCombination) {
				continueCombination = exclusionCombinationRows(rows, columns, picross);
				continueCombination = exclusionCombinationColumns(rows, columns, picross) || continueCombination;
				frame.repaint();
			}
		}
	}

	private static void slow() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
	}
}

class PicrossGrid {
	public static final byte BLACK = 1, WHITE = -1, UNKNOWN = 0, ABLACK = 2, AWHITE = -2;
	public byte numberRow, numberColumn;
	public byte[][] rowClue, columnClue;
	public byte[][] grid;
	public int rowClueLength, columnClueLength;

	PicrossGrid(PicrossGrid picross) {
		numberRow = picross.numberRow;
		numberColumn = picross.numberColumn;
		rowClue = picross.rowClue;
		columnClue = picross.columnClue;
		grid = PossibleRows.copyGrid(picross);
		rowClueLength = picross.rowClueLength;
		columnClueLength = picross.columnClueLength;
	}

	PicrossGrid(String nomFichier) throws FileNotFoundException {
		Scanner s = new Scanner(new FileReader(nomFichier));
		numberRow = Byte.parseByte(s.nextLine());
		numberColumn = Byte.parseByte(s.nextLine());
		this.intializeGrid();
		this.intializeRowClue(s);
		this.intializeColumnClue(s);
		s.close();
	}

	private void intializeGrid() {
		grid = new byte[numberRow][numberColumn];
		for (int i = 0; i < numberRow; i++)
			for (int j = 0; j < numberColumn; j++)
				grid[i][j] = UNKNOWN;
	}

	private void intializeRowClue(Scanner s) {
		String current = "";
		rowClue = new byte[numberRow][];
		rowClueLength = 0;
		for (byte i = 0; i < numberRow; i++) {
			current = s.nextLine();
			rowClueLength = (rowClueLength > current.length()) ? rowClueLength : current.length();
			int k = 1, j = 1;
			while (k < current.length() - 1) {
				char a = current.charAt(k);
				if (a == ',') {
					j++;
				}
				k++;
			}
			rowClue[i] = new byte[j];
			j = 0;
			k = 1;
			while (k < current.length() - 1) {
				char a = current.charAt(k);
				if (a == ',') {
					j++;
				} else {
					rowClue[i][j] *= 10;
					rowClue[i][j] += Character.getNumericValue(current.charAt(k));
				}
				k++;
			}
		}
	}

	private void intializeColumnClue(Scanner s) {
		String current = "";
		columnClue = new byte[numberColumn][];
		columnClueLength = 0;
		for (int i = 0; i < numberColumn; i++) {
			current = s.nextLine();
			columnClueLength = (columnClueLength > current.length()) ? columnClueLength : current.length();
			int k = 1, j = 1;
			while (k < current.length() - 1) {
				char a = current.charAt(k);
				if (a == ',') {
					j++;
				}
				k++;
			}
			columnClue[i] = new byte[j];
			j = 0;
			k = 1;
			while (k < current.length() - 1) {
				char a = current.charAt(k);
				if (a == ',') {
					j++;
				} else {
					columnClue[i][j] *= 10;
					columnClue[i][j] += Character.getNumericValue(current.charAt(k));
				}
				k++;
			}
		}
	}
}

class PicrossAffichage extends Canvas {
	private static final long serialVersionUID = 1L;
	static final int cellSize = 20;
	PicrossGrid picross;

	PicrossAffichage(String nomFichier) throws FileNotFoundException {
		picross = new PicrossGrid(nomFichier);
		Frame f = new Frame("Picross from " + nomFichier);
		initialize(f);
	}

	public void initialize(Frame f) {
		f.setBounds(400, 100, cellSize * picross.numberColumn + 10 * picross.rowClueLength,
				cellSize * picross.numberRow + picross.columnClueLength * 10);

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.add(this);
		f.setVisible(true);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		for (int i = 0; i < picross.numberRow; i++) {
			for (int j = 0; j < picross.numberColumn; j++) {
				if (picross.grid[i][j] == PicrossGrid.UNKNOWN)
					g.setColor(Color.lightGray);
				else if (picross.grid[i][j] == PicrossGrid.BLACK)
					g.setColor(Color.black);
				else if (picross.grid[i][j] == PicrossGrid.WHITE)
					g.setColor(Color.white);
				else if (picross.grid[i][j] == PicrossGrid.ABLACK)
					g.setColor(Color.blue);
				else if (picross.grid[i][j] == PicrossGrid.AWHITE)
					g.setColor(Color.cyan);
				g.fillRect(j * cellSize + 10 * picross.rowClueLength + cellSize / 16,
						i * cellSize + picross.columnClueLength * 10 + cellSize / 16, cellSize - cellSize / 8,
						cellSize - cellSize / 8);
			}
		}
		g.setColor(Color.BLACK);
		g.setFont(new Font("Serif", Font.BOLD, 17));
		for (int i = 0; i < picross.numberRow; i++) {
			for (int j = 0; j < picross.rowClue[i].length - 1; j++) {
				g.drawString(picross.rowClue[i][j] + ", ", 5 + j * 20,
						16 + picross.columnClueLength * 10 + i * cellSize);
			}
			g.drawString(picross.rowClue[i][picross.rowClue[i].length - 1] + "",
					5 + (picross.rowClue[i].length - 1) * 20, 16 + picross.columnClueLength * 10 + i * cellSize);
		}
		for (int i = 0; i < picross.numberColumn; i++) {
			for (int j = 0; j < picross.columnClue[i].length - 1; j++) {
				g.drawString(picross.columnClue[i][j] + "", 5 + picross.rowClueLength * 10 + i * cellSize, 16 + j * 20);
			}
			g.drawString(picross.columnClue[i][picross.columnClue[i].length - 1] + "",
					5 + picross.rowClueLength * 10 + i * cellSize, 16 + (picross.columnClue[i].length - 1) * 20);
		}
	}
}