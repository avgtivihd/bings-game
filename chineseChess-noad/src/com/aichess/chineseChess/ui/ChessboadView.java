package com.aichess.chineseChess.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.aichess.chineseChess.R;
import com.aichess.chineseChess.data.Data;
import com.aichess.chineseChess.utils.Debug;
import com.aichess.chineseChess.utils.Position;
import com.aichess.chineseChess.utils.Search;
import com.aichess.chineseChess.utils.Util;

/**
 * The Class ChessboadView.
 */
public class ChessboadView extends SurfaceView implements Callback {
	/** The m holder. */
	private SurfaceHolder mHolder;

	/** The m canvas. */
	private Canvas mCanvas;

	/** The m paint. */
	private Paint mPaint;

	/** ����X���꣬�����ʾ�� ������Ϊ0. */
	private int mCursorX;

	/** ����Y���� �������ʾ��������Ϊ0. */
	private int mCursorY;

	/** The mv last. */
	private int sqSelected, mvLast;

	/** The message. */
	private String message;

	/** ��ǰ����Ϸ�׶�. */
	protected static final int PHASE_LOADING = 0;

	/** The Constant PHASE_WAITING. */
	protected static final int PHASE_WAITING = 1;

	/** The Constant PHASE_THINKING. */
	protected static final int PHASE_THINKING = 2;

	/** The Constant PHASE_EXITTING. */
	protected static final int PHASE_EXITTING = 3;

	/** The phase. */
	protected volatile int phase = PHASE_LOADING;

	/** The Constant IMAGE_NAME. */
	private static final int[] IMAGE_NAME = { -1, -1, -1, -1, -1, -1, -1, -1,
			R.drawable.rk, R.drawable.ra, R.drawable.rb, R.drawable.rn,
			R.drawable.rr, R.drawable.rc, R.drawable.rp, -1, R.drawable.bk,
			R.drawable.ba, R.drawable.bb, R.drawable.bn, R.drawable.br,
			R.drawable.bc, R.drawable.bp, -1, };

	/** ��Ϸ״̬. */
	byte[] retractData = new byte[Data.RS_DATA_LEN];

	/** The top. */
	private int squareSize, width, height, left, top;

	/** The pos. */
	private Position pos = new Position();

	/** The search. */
	private Search search = new Search(pos, 12);

	/** ��ϷͼƬ. */
	private Bitmap mBoardBitmap;

	/** The img cursor2. */
	private Bitmap imgSelected, imgSelected2, imgCursor, imgCursor2;

	/** The img pieces. */
	private Bitmap[] imgPieces = new Bitmap[24];

	/** The m handler. */
	private Handler mHandler;

	/**
	 * Instantiates a new chessboad view.
	 * 
	 * @param context
	 *            the context
	 * @param han
	 *            the han
	 */
	public ChessboadView(Context context, Handler han) {
		super(context);
		mHandler = han;
		mBoardBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.board);
		width = mBoardBitmap.getWidth();
		height = mBoardBitmap.getHeight();

		// ����ͼƬ��Դ
		try {
			imgSelected = BitmapFactory.decodeResource(getResources(),
					R.drawable.selected);
			imgSelected2 = BitmapFactory.decodeResource(getResources(),
					R.drawable.selected2);
			imgCursor = imgSelected;
			imgCursor2 = imgSelected2;
			for (int pc = 0; pc < 24; pc++) {
				if (IMAGE_NAME[pc] == -1) {
					imgPieces[pc] = null;
				} else {
					imgPieces[pc] = BitmapFactory.decodeResource(
							getResources(), IMAGE_NAME[pc]);
				}
			}
		} catch (Exception e) {
			Debug.e(e.getMessage());
		}

		setLayoutParams(new ViewGroup.LayoutParams(width, height));
		mHolder = getHolder();
		mHolder.addCallback(this);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		init();
		load();
		Debug.d("load finished.");
	}

	/**
	 * Inits the.
	 */
	protected void init() {
		phase = PHASE_LOADING;
		Data.flipped = (Data.rsData[16] != 0);
		Data.handicap = Util.MIN_MAX(0, Data.rsData[17], 3);
		Data.level = Util.MIN_MAX(0, Data.rsData[18], 2);

		squareSize = Math.min(width / 9, height / 10);
		int boardWidth = squareSize * 9;
		int boardHeight = squareSize * 10;
		left = (width - boardWidth) / 2;
		top = (height - boardHeight) / 2;

		// ���ù���ʼλ�ã�7��7Ϊ����λ��
		mCursorX = 7;
		mCursorY = 7;
	}

	/**
	 * Load.
	 */
	protected void load() {

		sqSelected = mvLast = 0;
		if (Data.rsData[0] == 0) {
			// ��ʼ������
			pos.fromFen(Position.STARTUP_FEN[Data.handicap]);
		} else { // ����Ͷ�ȡ�ϴε���Ϸ����״̬
			// Restore Record-Score Data
			pos.clearBoard();
			for (int sq = 0; sq < 256; sq++) {
				int pc = Data.rsData[sq + 256];
				if (pc > 0) {
					pos.addPiece(sq, pc);
				}
			}
			if (Data.flipped) {
				pos.changeSide();
			}
			pos.setIrrev();
		}
		// ���泷��״̬
		System.arraycopy(Data.rsData, 0, retractData, 0, Data.RS_DATA_LEN);
		// ����ǵ������ߣ���ִ��responseMove()����
		phase = PHASE_LOADING;
		if (pos.sdPlayer == 0 ? Data.flipped : !Data.flipped) {
			new Thread() {
				public void run() {
					while (phase == PHASE_LOADING) {
						try {
							Debug.d("load-thread: waitting");
							sleep(100);
						} catch (InterruptedException e) {
							// Ignored
						}
					}
					responseMove();
				}
			}.start();
		}
	}

	/**
	 * ����View.
	 */
	protected synchronized void paint() {
		if (phase == PHASE_LOADING) {
			phase = PHASE_WAITING;
		}
		try {
			// ���Canvas����
			mCanvas = mHolder.lockCanvas();
			// ˢ��
			mCanvas.drawColor(Color.WHITE);
			// ������
			mCanvas.drawBitmap(mBoardBitmap, 0, 0, mPaint);

			// ������
			for (int sq = 0; sq < 256; sq++) {
				if (Position.IN_BOARD(sq)) {
					int pc = pos.squares[sq];
					if (pc > 0) {
						drawSquare(mCanvas, imgPieces[pc], mPaint, sq);
					}
				}
			}

			int sqSrc = 0;
			int sqDst = 0;
			if (mvLast > 0) {
				sqSrc = Position.SRC(mvLast);
				sqDst = Position.DST(mvLast);
				drawSquare(mCanvas, (pos.squares[sqSrc] & 8) == 0 ? imgSelected
						: imgSelected2, mPaint, sqSrc);
				drawSquare(mCanvas, (pos.squares[sqDst] & 8) == 0 ? imgSelected
						: imgSelected2, mPaint, sqDst);
			} else if (sqSelected > 0) {
				drawSquare(mCanvas,
						(pos.squares[sqSelected] & 8) == 0 ? imgSelected
								: imgSelected2, mPaint, sqSelected);
			}

			int sq = Position.COORD_XY(mCursorX + Position.FILE_LEFT, mCursorY
					+ Position.RANK_TOP);
			if (Data.flipped) {
				sq = Position.SQUARE_FLIP(sq);
			}
			if (sq == sqSrc || sq == sqDst || sq == sqSelected) {
				drawSquare(mCanvas, (pos.squares[sq] & 8) == 0 ? imgCursor2
						: imgCursor, mPaint, sq);
			} else {
				drawSquare(mCanvas, (pos.squares[sq] & 8) == 0 ? imgCursor
						: imgCursor2, mPaint, sq);
			}
		} catch (Exception ex) {
		} finally {
			if (mCanvas != null)
				mHolder.unlockCanvasAndPost(mCanvas);
		}

		// ����View�ĸ��»ᵼ��TextView�����ã�ֻ��������ٷ�һ�������ˡ�����
		// TODO: ��Ƿ���Ϣ
		if (phase == PHASE_WAITING) {
			Message msg = new Message();
			msg.what = Data.WHAT_OTHER;
			msg.obj = getContext().getResources().getString(
					R.string.info_please);
			mHandler.sendMessage(msg);
		} else if (phase == PHASE_EXITTING) {
			Message msg = new Message();
			msg.what = Data.WHAT_OTHER;
			msg.obj = message;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * Draw square.
	 * 
	 * @param c
	 *            the c
	 * @param b
	 *            the b
	 * @param p
	 *            the p
	 * @param sq
	 *            the sq
	 */
	private void drawSquare(Canvas c, Bitmap b, Paint p, int sq) {
		int sqFlipped = (Data.flipped ? Position.SQUARE_FLIP(sq) : sq);
		int sqX = left + (Position.FILE_X(sqFlipped) - Position.FILE_LEFT)
				* squareSize;
		int sqY = top + (Position.RANK_Y(sqFlipped) - Position.RANK_TOP)
				* squareSize;
		c.drawBitmap(b, sqX, sqY, p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// �÷�������Ӧ���Σ��ֱ�ΪDown��UP��Move
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (phase == PHASE_THINKING) {
				return true;
			}
			mCursorX = Util.MIN_MAX(0, (x - left) / squareSize, 8);
			mCursorY = Util.MIN_MAX(0, (y - top) / squareSize, 9);

			// �����߳���ȥִ�в�����������ɺ��ػ�View���Ӷ��������ANR��
			new Thread(new Runnable() {
				public void run() {
					clickSquare();
				}
			}).start();
			paint();
		}
		return true;
	}

	/**
	 * Click square.
	 */
	private void clickSquare() {
		int sq = Position.COORD_XY(mCursorX + Position.FILE_LEFT, mCursorY
				+ Position.RANK_TOP);
		if (Data.flipped) {
			sq = Position.SQUARE_FLIP(sq);
		}
		int pc = pos.squares[sq];
		if ((pc & Position.SIDE_TAG(pos.sdPlayer)) != 0) {
			// TODO: ��Ƿ���Ϣ
			Message msg = new Message();
			msg.what = Data.WHAT_SOUND;
			msg.arg1 = Data.RESP_CLICK;
			mHandler.sendMessage(msg);
			mvLast = 0;
			sqSelected = sq;
		} else {
			if (sqSelected > 0 && addMove(Position.MOVE(sqSelected, sq))
					&& !responseMove()) {
				Data.rsData[0] = 0;
				phase = PHASE_EXITTING;
				// TODO: ��Ƿ���Ϣ
				Message msg = new Message();
				msg.what = Data.WHAT_OTHER;
				msg.obj = message;
				mHandler.sendMessage(msg);
			}
		}
	}

	/**
	 * Adds the move.
	 * 
	 * @param mv
	 *            the mv
	 * @return true, if successful
	 */
	private boolean addMove(int mv) {
		if (pos.legalMove(mv)) {
			if (pos.makeMove(mv)) {
				// TODO: ��Ƿ���Ϣ
				Message msg = new Message();
				msg.what = Data.WHAT_SOUND;
				msg.arg1 = pos.inCheck() ? Data.RESP_CHECK
						: pos.captured() ? Data.RESP_CAPTURE : Data.RESP_MOVE;
				mHandler.sendMessage(msg);
				if (pos.captured()) {
					pos.setIrrev();
				}
				sqSelected = 0;
				mvLast = mv;
				return true;
			}
			// TODO: ��Ƿ���Ϣ
			Message msg = new Message();
			msg.what = Data.WHAT_SOUND;
			msg.arg1 = Data.RESP_ILLEGAL;
			mHandler.sendMessage(msg);
		}
		return false;
	}

	/**
	 * ����.
	 */
	protected void retract() {
		if (phase == PHASE_THINKING) {
			// ����������˼���ͱ���Űɣ�������������˵��Ҫ��Ȼ������������
			return;
		}
		// Restore Retract Status
		System.arraycopy(retractData, 0, Data.rsData, 0, Data.RS_DATA_LEN);
		load();
		paint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder
	 * )
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Debug.d("surfaceCreated");
		paint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
	 * , int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Debug.d("surfaceChanged");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.
	 * SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Debug.d("surfaceDestroyed");
	}

	/**
	 * Response move.
	 * 
	 * @return true, if successful
	 */
	boolean responseMove() {
		Debug.d("Computer's turn.");
		if (getResult()) {
			return false;
		}
		phase = PHASE_THINKING;
		mHandler.sendEmptyMessage(Data.WHAT_THINKING);
		paint();
		mvLast = search.searchMain(1000 << (Data.level << 1));
		pos.makeMove(mvLast);
		int response = pos.inCheck() ? Data.RESP_CHECK2
				: pos.captured() ? Data.RESP_CAPTURE2 : Data.RESP_MOVE2;
		if (pos.captured()) {
			pos.setIrrev();
		}
		phase = PHASE_WAITING;
		// TODO: ����������ػ�
		paint();
		return !getResult(response);
	}

	/**
	 * Player Move Result.
	 * 
	 * @return the result
	 */
	private boolean getResult() {
		return getResult(-1);
	}

	/**
	 * Computer Move Result.
	 * 
	 * @param response
	 *            the response
	 * @return the result
	 */
	private boolean getResult(int response) {
		// TODO: ���ػ��ٷ���Ϣ�������ػ�����TextView������
		paint();
		if (pos.isMate()) {
			// TODO: ��Ƿ���Ϣ
			Message msg = new Message();
			msg.what = Data.WHAT_SOUND;
			msg.arg1 = response < 0 ? Data.RESP_WIN : Data.RESP_LOSS;
			mHandler.sendMessage(msg);
			message = (response < 0 ? getResources().getString(R.string.text_congraduation) : getResources().getString(R.string.text_retry));
			Debug.d(getResources().getString(R.string.text_gameover) + message);
			saveRetractData();
			return true;
		}
		int vlRep = pos.repStatus(3);
		if (vlRep > 0) {
			vlRep = (response < 0 ? pos.repValue(vlRep) : -pos.repValue(vlRep));
			// TODO: ��Ƿ���Ϣ
			Message msg = new Message();
			msg.what = Data.WHAT_SOUND;
			msg.arg1 = vlRep > Position.WIN_VALUE ? Data.RESP_LOSS
					: vlRep < -Position.WIN_VALUE ? Data.RESP_WIN
							: Data.RESP_DRAW;
			mHandler.sendMessage(msg);
			message = (vlRep > Position.WIN_VALUE ? getResources().getString(R.string.text_longchess)
					: vlRep < -Position.WIN_VALUE ? getResources().getString(R.string.text_computerlongchess)
							: getResources().getString(R.string.text_equal));
			saveRetractData();
			return true;
		}
		if (pos.moveNum > 100) {
			// TODO: ��Ƿ���Ϣ
			Message msg = new Message();
			msg.what = Data.WHAT_SOUND;
			msg.arg1 = Data.RESP_DRAW;
			mHandler.sendMessage(msg);
			message = getResources().getString(R.string.text_longequal);
			saveRetractData();
			return true;
		}
		if (response >= 0) {
			// TODO: ��Ƿ���Ϣ
			Message msg = new Message();
			msg.what = Data.WHAT_SOUND;
			msg.arg1 = response;
			msg.obj = getContext().getResources().getString(
					R.string.info_please);
			mHandler.sendMessage(msg);
			saveRetractData();
		}
		return false;
	}
	
	void saveRetractData() {
		// Backup Retract Status
		System.arraycopy(Data.rsData, 0, retractData, 0, Data.RS_DATA_LEN);
		// Backup Record-Score Data
		Data.rsData[0] = 1;
		System.arraycopy(pos.squares, 0, Data.rsData, 256, 256);
	}
}
