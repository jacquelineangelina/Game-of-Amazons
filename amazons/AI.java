package amazons;

import java.util.Iterator;
import static amazons.Piece.*;

/** A Player that automatically generates moves.
 *  @author Jacqueline Angelina
 */
class AI extends Player {

    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;
    /** Maximum number of moves for one depth search. */
    private static final int MAX = 40;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        int value = 0;
        Iterator<Move> legal;
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        } else if (sense == 1) {
            legal = board.legalMoves(WHITE);
            value = Integer.MIN_VALUE;
            while (legal.hasNext()) {
                Move saved = legal.next();
                board.makeMove(saved);
                alpha = Math.max(alpha, value);
                value = Math.max(value, findMove(board, depth - 1,
                        false, -1, alpha, beta));
                board.undo();
                if (saveMove && value > alpha) {
                    _lastFoundMove = saved;
                }
            }
            return value;
        } else {
            legal = board.legalMoves(BLACK);
            value = Integer.MAX_VALUE;
            while (legal.hasNext()) {
                Move saved = legal.next();
                board.makeMove(saved);
                beta = Math.min(beta, value);
                value = Math.min(value, findMove(board, depth - 1,
                        false, 1, alpha, beta));
                board.undo();
                if (saveMove && value < beta) {
                    _lastFoundMove = saved;
                }
            }
            return value;
        }
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        if (N < MAX) {
            return 1;
        } else {
            return 1 + N / MAX;
        }
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }
        int whitescore = 0;
        Iterator<Move> whitemoves = board.legalMoves(WHITE);
        while (whitemoves.hasNext()) {
            whitemoves.next();
            whitescore += 1;
        }
        int blackscore = 0;
        Iterator<Move> blackmoves = board.legalMoves(BLACK);
        while (blackmoves.hasNext()) {
            blackmoves.next();
            blackscore += 1;
        }
        return whitescore - blackscore;
    }
}
