package com.aichess.chineseChess.data;

import com.aichess.chineseChess.R;

/**
 * The Class Data.
 */
public class Data {
	public static final int[] SOUND_ID = { R.raw.click, R.raw.illegal, R.raw.move,
			R.raw.move2, R.raw.capture, R.raw.capture2, R.raw.check,
			R.raw.check2, R.raw.win, R.raw.draw, R.raw.loss};	
	public static final int RESP_CLICK = 0;
	public static final int RESP_ILLEGAL = 1;
	public static final int RESP_MOVE = 2;
	public static final int RESP_MOVE2 = 3;
	public static final int RESP_CAPTURE = 4;
	public static final int RESP_CAPTURE2 = 5;
	public static final int RESP_CHECK = 6;
	public static final int RESP_CHECK2 = 7;
	public static final int RESP_WIN = 8;
	public static final int RESP_DRAW = 9;
	public static final int RESP_LOSS = 10;	
	/** The Constant LOW_SCREEN_WIDTH. */
	public static final int LOW_SCREEN_WIDTH = 240;
	/** The Constant RS_DATA_LEN. */
	public static final int RS_DATA_LEN = 512;

	/** Handler ������Ϣ. */
	public static final int WHAT_THINKING = 0;

	/** The Constant WHAT_OTHER. */
	public static final int WHAT_OTHER = 1;
	
	public static final int WHAT_SOUND = 2;

	/**
	 * 0: ��Ϸ״̬, 0Ϊ�����˳�, 1Ϊ��Ϸ�ѱ���<br>
	 * 16: ˭������, 0Ϊ����, 1Ϊ���� (��Flipped)<br>
	 * 17: ���Ӽ���, 0Ϊ����, 1Ϊ����, 2Ϊ��˫��, 3Ϊ�þ���<br>
	 * 18: ���Լ���, 0Ϊ����, 1Ϊ�м�, 2Ϊר�Ҽ�<br>
	 * 19: �����ȼ���0Ϊ������5Ϊ���ֵ<br>
	 * 20: ���ֵȼ���0Ϊ������5Ϊ���ֵ<br>
	 * 256-511: ����.
	 */
	public static byte[] rsData = new byte[RS_DATA_LEN];

	/** �Ƿ�������ߣ�true�������ߣ�false������. */
	public static boolean flipped;

	/** ���Ӽ��� ��0Ϊ����, 1Ϊ����, 2Ϊ��˫��, 3Ϊ�þ���. */
	public static int handicap = 0;

	/** ���Լ���: 0Ϊ����, 1Ϊ�м�, 2Ϊר�Ҽ�. */
	public static int level = 0;

	/**
	 * Re init.
	 */
	public static final void reInit() {
		rsData = new byte[RS_DATA_LEN];
	}
	public static int Amount = 0;
	public static double Price = 0;
}
