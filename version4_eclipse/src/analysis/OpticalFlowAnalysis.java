package analysis;

import oscP5.OscMessage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Optical flow analyzer
 *
 * @author hamoid
 *
 */
public class OpticalFlowAnalysis extends BaseAnalysis {
	// This setting greatly affects the result
	// More is less cpu intensive
	private final int GRID_SIZE_PX = 15;

	private int[] imgPixels;

	private int longestVectorId;
	private float longestVectorLength;

	// grid parameters
	private int avgWindowSize; // -avgWindowSize .. +avgWindowSize
	private int columnCount, rowCount;
	private int gridStepPx2;

	// just for drawing the vectors
	private float predictionTimeSec;
	private float predictionFrames;

	// regression vectors
	private float[] fx, fy, ft;
	private int vectorCount;

	// regularization term for regression
	private float fc = (float) Math.pow(10, 8); // larger values for noisy
												// video

	// smoothing parameters
	private float smoothness = 0.2f; // smaller value for longer smoothing

	// internally used variables
	private float ar, ag, ab; // used as return value of pixave
	private float[] dtr, dtg, dtb; // differentiation by t (red,gree,blue)
	private float[] dxr, dxg, dxb; // differentiation by x (red,gree,blue)
	private float[] dyr, dyg, dyb; // differentiation by y (red,gree,blue)
	private float[] par, pag, pab; // averaged grid values (red,gree,blue)
	private float[] flowx, flowy; // computed optical flow
	private float[] sflowx, sflowy; // slowly changing version of the flow

	/**
	 * @param p5
	 *            Receive the Processing context so this class can access the
	 *            Processing API and draw things on the screen
	 */
	public OpticalFlowAnalysis(PApplet p5) {
		super(p5);
	}

	@Override
	public void initialize(int w, int h, int fps) {
		setFPS(fps);

		avgWindowSize = GRID_SIZE_PX * 2;
		gridStepPx2 = GRID_SIZE_PX / 2;

		columnCount = w / GRID_SIZE_PX;
		rowCount = h / GRID_SIZE_PX;

		int cells = columnCount * rowCount;
		par = new float[cells];
		pag = new float[cells];
		pab = new float[cells];

		dtr = new float[cells];
		dtg = new float[cells];
		dtb = new float[cells];

		dxr = new float[cells];
		dxg = new float[cells];
		dxb = new float[cells];

		dyr = new float[cells];
		dyg = new float[cells];
		dyb = new float[cells];

		flowx = new float[cells];
		flowy = new float[cells];

		sflowx = new float[cells];
		sflowy = new float[cells];

		vectorCount = 3 * 9;
		fx = new float[vectorCount];
		fy = new float[vectorCount];

		ft = new float[vectorCount];

		super.initialize(w, h, fps);
	}

	public void setFPS(int fps) {
		predictionTimeSec = 1.0f; // larger for longer vector
		predictionFrames = predictionTimeSec * fps;
	}

	/**
	 * Analyze a video frame. Do calculations based on the received bitmap,
	 * which can be later drawn to the screen and/or sent as OSC to
	 * Supercollider.
	 *
	 * @param img
	 *            The video frame to be analyzed
	 */
	@Override
	public void analyze(PImage img) {
		imgPixels = img.pixels;

		// 1st sweep : differentiation by time
		for (int ix = 0; ix < columnCount; ix++) {
			int x0 = ix * GRID_SIZE_PX + gridStepPx2;
			for (int iy = 0; iy < rowCount; iy++) {
				int y0 = iy * GRID_SIZE_PX + gridStepPx2;
				int ig = iy * columnCount + ix;
				// compute average pixel at (x0,y0)
				pixave(x0 - avgWindowSize, y0 - avgWindowSize,
						x0 + avgWindowSize, y0 + avgWindowSize);
				// compute time difference
				dtr[ig] = ar - par[ig]; // red
				dtg[ig] = ag - pag[ig]; // green
				dtb[ig] = ab - pab[ig]; // blue
				// save the pixel
				par[ig] = ar;
				pag[ig] = ag;
				pab[ig] = ab;
			}
		}

		// 2nd sweep : differentiations by x and y
		for (int ix = 1; ix < columnCount - 1; ix++) {
			for (int iy = 1; iy < rowCount - 1; iy++) {
				int ig = iy * columnCount + ix;
				// compute x difference
				dxr[ig] = par[ig + 1] - par[ig - 1]; // red
				dxg[ig] = pag[ig + 1] - pag[ig - 1]; // green
				dxb[ig] = pab[ig + 1] - pab[ig - 1]; // blue
				// compute y difference
				dyr[ig] = par[ig + columnCount] - par[ig - columnCount]; // red
				dyg[ig] = pag[ig + columnCount] - pag[ig - columnCount]; // green
				dyb[ig] = pab[ig + columnCount] - pab[ig - columnCount]; // blue
			}
		}

		// 3rd sweep : solving optical flow
		for (int ix = 1; ix < columnCount - 1; ix++) {
			for (int iy = 1; iy < rowCount - 1; iy++) {
				int ig = iy * columnCount + ix;

				// prepare vectors fx, fy, ft
				getnext9(dxr, fx, ig, 0); // dx red
				getnext9(dxg, fx, ig, 9); // dx green
				getnext9(dxb, fx, ig, 18);// dx blue
				getnext9(dyr, fy, ig, 0); // dy red
				getnext9(dyg, fy, ig, 9); // dy green
				getnext9(dyb, fy, ig, 18);// dy blue
				getnext9(dtr, ft, ig, 0); // dt red
				getnext9(dtg, ft, ig, 9); // dt green
				getnext9(dtb, ft, ig, 18);// dt blue

				// solve for (flowx, flowy) such that
				// fx flowx + fy flowy + ft = 0
				solveflow(ig);

				// smoothing
				sflowx[ig] += (flowx[ig] - sflowx[ig]) * smoothness;
				sflowy[ig] += (flowy[ig] - sflowy[ig]) * smoothness;
			}
		}
		longestVectorLength = 0;
		int itemCount = columnCount * rowCount;
		// find longest vector
		for (int i = 0; i < itemCount; i++) {
			float u = sflowx[i];
			float v = sflowy[i];
			float newlen = u * u + v * v;
			if (newlen > longestVectorLength) {
				longestVectorLength = newlen;
				longestVectorId = i;
			}
		}

	}

	// calculate average pixel value (r,g,b) for rectangle region
	private void pixave(int x1, int y1, int x2, int y2) {
		float sumr, sumg, sumb;
		int pix;
		int r, g, b;
		int n;

		if (x1 < 0) {
			x1 = 0;
		}
		if (x2 >= width) {
			x2 = width - 1;
		}
		if (y1 < 0) {
			y1 = 0;
		}
		if (y2 >= height) {
			y2 = height - 1;
		}

		sumr = sumg = sumb = 0.0f;
		for (int y = y1; y <= y2; y++) {
			for (int i = width * y + x1; i <= width * y + x2; i++) {
				pix = imgPixels[i];
				b = pix & 0xFF; // blue
				pix = pix >>= 8;
				g = pix & 0xFF; // green
				pix = pix >> 8;
				r = pix & 0xFF; // red
				// averaging the values
				sumr += r;
				sumg += g;
				sumb += b;
			}
		}
		n = (x2 - x1 + 1) * (y2 - y1 + 1); // number of pixels
		// the results are stored in static variables
		ar = sumr / n;
		ag = sumg / n;
		ab = sumb / n;
	}

	// extract values from 9 neighbour grids
	void getnext9(float x[], float y[], int i, int j) {
		y[j + 0] = x[i + 0];
		y[j + 1] = x[i - 1];
		y[j + 2] = x[i + 1];
		y[j + 3] = x[i - columnCount];
		y[j + 4] = x[i + columnCount];
		y[j + 5] = x[i - columnCount - 1];
		y[j + 6] = x[i - columnCount + 1];
		y[j + 7] = x[i + columnCount - 1];
		y[j + 8] = x[i + columnCount + 1];
	}

	// solve optical flow by least squares (regression analysis)
	private void solveflow(int ig) {
		float xx, xy, yy, xt, yt;

		// prepare covariances
		xx = xy = yy = xt = yt = 0.0f;
		for (int i = 0; i < vectorCount; i++) {
			xx += fx[i] * fx[i];
			xy += fx[i] * fy[i];
			yy += fy[i] * fy[i];
			xt += fx[i] * ft[i];
			yt += fy[i] * ft[i];
		}

		// least squares computation
		final float a = xx * yy - xy * xy + fc; // fc is for stable computation
		final float u = yy * xt - xy * yt; // x direction
		final float v = xx * yt - xy * xt; // y direction

		// write back
		final float n = -2 * GRID_SIZE_PX / a;
		flowx[ig] = n * u; // optical flow x (pixel per frame)
		flowy[ig] = n * v; // optical flow y (pixel per frame)
	}

	@Override
	public void setParams(int id, float val) {
		switch (id) {
			case 0:
				fc = (float) Math.pow(10, 1 + val * 8);
				break;
			case 1:
				smoothness = val;
				break;
		}
	}

	/**
	 * Draw histogram for debugging purposes
	 */
	@Override
	public void draw() {
		float kx = p5.width / (float) width;
		float ky = p5.height / (float) height;

		for (int i = 0; i < columnCount * rowCount; i++) {
			float u = predictionFrames * sflowx[i];
			float v = predictionFrames * sflowy[i];

			float len = u * u + v * v; // avoid sqrt for better performance
			if (len >= 5 * 5) {
				p5.strokeWeight(i == longestVectorId ? 4 : 1);
				p5.stroke(0, 255, 0);
				float x = kx * ((i % columnCount) * GRID_SIZE_PX + gridStepPx2);
				float y = ky * ((i / columnCount) * GRID_SIZE_PX + gridStepPx2);
				p5.line(x, y, x + u, y + v);
			}
		}
	}

	/**
	 * Creates an OSC message contaning the result of the analysis
	 *
	 * @return Message including an OscMessage if there was a scene cut or null
	 *         otherwise
	 */
	@Override
	public OscMessage getOSCmsg() {
		// send longest vector info
		OscMessage msg = new OscMessage("/of");
		msg.add((float) Math.sqrt(longestVectorLength)); // len NOT normalized
		msg.add((float) Math.atan2(sflowy[longestVectorId],
				sflowx[longestVectorId])); // angle rad
		msg.add((longestVectorId % columnCount) / (float) columnCount); // x
																		// normalized
		msg.add((longestVectorId / columnCount) / (float) rowCount); // y
																		// normalized
		return msg;
	}
}
