package analysis;

import blobDetection.Blob;
import blobDetection.BlobDetection;
import blobDetection.EdgeVertex;
import oscP5.OscMessage;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * Blob analyzer. In this test version, move the mouse horizontally to set the
 * threshold.
 *
 * Recommended: in a dark room, move it right to about 80%. It should show
 * flashlights and mobile phone screens.
 *
 * To do: send more than one blob
 *
 * Cost: about 1 ms for the analysis.
 *
 * @author hamoid
 *
 */
public class BlobAnalysis extends BaseAnalysis {
	private final BlobDetection theBlobDetection;
	private final PImage img;

	/**
	 * @param p5
	 *            Receive the Processing context so this class can access the
	 *            Processing API and draw things on the screen
	 */
	public BlobAnalysis(PApplet p5) {
		super(p5);

		img = new PImage(80, 60);
		theBlobDetection = new BlobDetection(img.width, img.height);
		theBlobDetection.setPosDiscrimination(true);
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
	public void analyze(PImage cam) {
		img.copy(cam, 0, 0, cam.width, cam.height, 0, 0, img.width, img.height);
		fastblur(img, 2);
		theBlobDetection.setThreshold(PApplet.norm(p5.mouseX, 0, p5.width));
		theBlobDetection.computeBlobs(img.pixels);
	}

	// ==================================================
	// Super Fast Blur v1.1
	// by Mario Klingemann
	// <http://incubator.quasimondo.com>
	// aBe removed red & green
	// ==================================================
	private void fastblur(PImage img, int radius) {
		if (radius < 1) {
			return;
		}
		int w = img.width;
		int h = img.height;
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		// int r[]=new int[wh];
		// int g[]=new int[wh];
		int b[] = new int[wh];
		int /* rsum, gsum, */ bsum, x, y, i, p, p1, p2, yp, yi, yw;
		int vmin[] = new int[PApplet.max(w, h)];
		int vmax[] = new int[PApplet.max(w, h)];
		int[] pix = img.pixels;
		int dv[] = new int[256 * div];
		for (i = 0; i < 256 * div; i++) {
			dv[i] = (i / div);
		}

		yw = yi = 0;

		for (y = 0; y < h; y++) {
			/* rsum=gsum= */bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + PApplet.min(wm, PApplet.max(i, 0))];
				// rsum+=(p & 0xff0000)>>16;
				// gsum+=(p & 0x00ff00)>>8;
				bsum += p & 0x0000ff;
			}
			for (x = 0; x < w; x++) {

				// r[yi]=dv[rsum];
				// g[yi]=dv[gsum];
				b[yi] = dv[bsum];

				if (y == 0) {
					vmin[x] = PApplet.min(x + radius + 1, wm);
					vmax[x] = PApplet.max(x - radius, 0);
				}
				p1 = pix[yw + vmin[x]];
				p2 = pix[yw + vmax[x]];

				// rsum+=((p1 & 0xff0000)-(p2 & 0xff0000))>>16;
				// gsum+=((p1 & 0x00ff00)-(p2 & 0x00ff00))>>8;
				bsum += (p1 & 0x0000ff) - (p2 & 0x0000ff);
				yi++;
			}
			yw += w;
		}

		for (x = 0; x < w; x++) {
			/* rsum=gsum= */bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = PApplet.max(0, yp) + x;
				// rsum+=r[yi];
				// gsum+=g[yi];
				bsum += b[yi];
				yp += w;
			}
			yi = x;
			for (y = 0; y < h; y++) {
				pix[yi] = 0xff000000 | (dv[bsum] << 16) | (dv[bsum] << 8)
						| dv[bsum];
				if (x == 0) {
					vmin[y] = PApplet.min(y + radius + 1, hm) * w;
					vmax[y] = PApplet.max(y - radius, 0) * w;
				}
				p1 = x + vmin[y];
				p2 = x + vmax[y];

				// rsum+=r[p1]-r[p2];
				// gsum+=g[p1]-g[p2];
				bsum += b[p1] - b[p2];

				yi += w;
			}
		}
	}

	/**
	 * Draw for debugging purposes
	 */
	@Override
	public void draw() {
		p5.noFill();
		p5.strokeWeight(3);
		p5.stroke(0, 255, 0);
		Blob b;
		EdgeVertex eA;
		for (int n = 0; n < theBlobDetection.getBlobNb(); n++) {
			b = theBlobDetection.getBlob(n);
			if (b != null) {
				p5.beginShape();
				for (int m = 0; m < b.getEdgeNb(); m++) {
					eA = b.getEdgeVertexA(m);
					p5.vertex(eA.x * p5.width, eA.y * p5.height);
				}
				p5.endShape(PConstants.CLOSE);
			}
		}
	}

	/**
	 * Creates an OSC message containing the result of the analysis
	 *
	 * @return OscMessage with the coordinates of the first blob if found,
	 *         otherwise null
	 */
	@Override
	public OscMessage getOSCmsg() {
		if (theBlobDetection.getBlobNb() > 0) {
			Blob b = theBlobDetection.getBlob(0);
			int middleIndex = b.getEdgeNb() / 2;
			EdgeVertex v0 = b.getEdgeVertexA(0);
			EdgeVertex v1 = b.getEdgeVertexA(middleIndex);

			// send normalized coordinates of first found blob
			OscMessage msg = new OscMessage("/blob");
			msg.add((v0.x + v1.x) / 2);
			msg.add((v0.y + v1.y) / 2);
			return msg;
		}
		return null;
	}
}
