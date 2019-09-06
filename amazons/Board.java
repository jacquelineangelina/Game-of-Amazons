package amazons;

import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import static amazons.Piece.*;
import static amazons.Move.mv;

/** The state of an Amazons Game.
 *  @author Jacqueline Angelina
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        this._turn = model._turn;
        this._winner = model._winner;
        pieces = new Piece[SIZE][SIZE];
        for (int i = 0; i < 10; i += 1) {
            for (int j = 0; j < 10; j += 1) {
                this.pieces[i][j] = model.pieces[i][j];
            }
        }
        moves = new Stack<Move>();
    }

    /** Clears the board to the initial position. */
    void init() {
        _turn = WHITE;
        _winner = EMPTY;
        pieces = new Piece[SIZE][SIZE];
        for (int i = 0; i < 10; i += 1) {
            for (int j = 0; j < 10; j += 1) {
                pieces[i][j] = EMPTY;
            }
        }
        pieces[0][6] = BLACK;
        pieces[3][9] = BLACK;
        pieces[6][9] = BLACK;
        pieces[9][6] = BLACK;
        pieces[0][3] = WHITE;
        pieces[3][0] = WHITE;
        pieces[6][0] = WHITE;
        pieces[9][3] = WHITE;
        moves = new Stack<Move>();
    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return moves.size();
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        if (!legalMoves(_turn).hasNext()) {
            _winner = turn().opponent();
            return _winner;
        } else {
            return null;
        }
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return pieces[s.col()][s.row()];
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return pieces[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        pieces[s.col()][s.row()] = p;
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {
        pieces[col][row] = p;
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, col - 'a', row - '1');
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        if (!from.isQueenMove(to)) {
            return false;
        } else {
            Square newfrom = null;
            int steps = 1;
            while (newfrom != to) {
                newfrom = from.queenMove(from.direction(to), steps);
                if (get(newfrom) != EMPTY && newfrom != asEmpty) {
                    return false;
                }
                steps += 1;
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return this.get(from) == _turn;
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {
        return isLegal(from) && isUnblockedMove(from, to, null);
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        return isLegal(from, to) && isUnblockedMove(to, spear, from);
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return move != null && isLegal(move.from(), move.to(), move.spear());
    }

    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {
        makeMove(mv(from, to, spear));
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        if (isLegal(move.from(), move.to(), move.spear())) {
            put(get(move.from()), move.to());
            put(EMPTY, move.from());
            put(SPEAR, move.spear());
            _turn = turn().opponent();
            moves.push(move);
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        Move a = moves.pop();
        put(EMPTY, a.spear());
        put(get(a.to()), a.from());
        put(EMPTY, a.to());
        _turn = turn().opponent();
    }

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {
        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = -1;
            _steps = 0;
            _asEmpty = asEmpty;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _dir < 8;
        }

        @Override
        public Square next() {
            if (hasNext()) {
                Square next = _from.queenMove(_dir, _steps);
                toNext();
                return next;
            }
            return null;
        }

        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {
            if (_dir == -1 && _steps == 0) {
                _dir = 0;
                _steps = 1;
            } else {
                _steps += 1;
            }
            Square nextmove = _from.queenMove(_dir, _steps);
            while (!isUnblockedMove(_from, nextmove, _asEmpty)) {
                _dir += 1;
                if (_dir == 8) {
                    return;
                }
                _steps = 1;
                nextmove = _from.queenMove(_dir, _steps);
            }
        }

        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;
    }

    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<Move> {

        /** All legal moves for SIDE (WHITE or BLACK). */
        LegalMoveIterator(Piece side) {
            _startingSquares = Square.iterator();
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            _fromPiece = side;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _spearThrows.hasNext();
        }

        @Override
        public Move next() {
            if (hasNext()) {
                Move next = mv(_start, _nextSquare, _spearThrows.next());
                toNext();
                return next;
            }
            return null;
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
            if (_spearThrows.hasNext()) {
                return;
            }
            if (_pieceMoves.hasNext()) {
                _nextSquare = _pieceMoves.next();
                _spearThrows = reachableFrom(_nextSquare, _start);
                return;
            }
            while (_startingSquares.hasNext()) {
                Square nextstart = _startingSquares.next();
                if (get(nextstart) == _fromPiece) {
                    _start = nextstart;
                    _pieceMoves = reachableFrom(_start, null);
                    if (_pieceMoves.hasNext()) {
                        _nextSquare = _pieceMoves.next();
                        _spearThrows = reachableFrom(_nextSquare, _start);
                        return;
                    }
                }
            }
        }

        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Current starting square. */
        private Square _start;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Square _nextSquare;
        /** Remaining moves from _start to consider. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
    }

    @Override
    public String toString() {
        String result = "   ";
        for (int i = 9; i > -1; i -= 1) {
            for (int j = 0; j < 10; j += 1) {
                if (this.get(j, i) == EMPTY) {
                    if (j != 9) {
                        result += "-";
                        result += " ";
                    } else if (i == 0 && j == 9) {
                        result += "-";
                        result += "\n";
                    } else if (j == 9) {
                        result += "-";
                        result += "\n   ";
                    }
                } else if (this.get(j, i) == BLACK) {
                    if (j != 9) {
                        result += "B";
                        result += " ";
                    } else if (i == 0 && j == 9) {
                        result += "B";
                        result += "\n";
                    } else if (j == 9) {
                        result += "B";
                        result += "\n";
                        result += "   ";
                    }
                } else if (this.get(j, i) == WHITE) {
                    if (j != 9) {
                        result += "W";
                        result += " ";
                    } else if (i == 0 && j == 9) {
                        result += "W";
                        result += "\n";
                    } else if (j == 9) {
                        result += "W";
                        result += "\n";
                        result += "   ";
                    }
                } else if (this.get(j, i) == SPEAR) {
                    if (j != 9) {
                        result += "S";
                        result += " ";
                    } else if (i == 0 && j == 9) {
                        result += "S";
                        result += "\n";
                    } else if (j == 9) {
                        result += "S";
                        result += "\n";
                        result += "   ";
                    }
                }
            }
        }
        return result;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQUARES =
        Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Array list of pieces on the board. */
    private Piece[][] pieces;
    /** Stack of all moves made. */
    private Stack<Move> moves;

}
