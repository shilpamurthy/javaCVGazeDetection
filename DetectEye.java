
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;

import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.awt.image.BufferedImage;

import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
/**
 * 
 * @author shilpamurthy
 * Class to detect gaze direction
 * To use this class just instantiate the class with 
 * you haarcascade xml files from openCV with file 1 as the
 * face detection file and the second one as eye detection 
 * xml files. Then call isLeftOrRight(img) where img is the 
 * image in IplImage format
 */
public class DetectEye {

	/**
	 * Cascade files for face and eyes
	 * Can be downloaded. 
	 */
	private static String CASCADE_FILE = "";
	private static String CASCADE_FILE2 = "";
	
	public DetectEye(String p1, String p2)
	{
		CASCADE_FILE = p1;
		CASCADE_FILE2 = p2;
	}

	/**
	 * Function to detect gaze direction
	 * @param originalImage
	 * @return 1 if the eye is looking right 0 if the eye is looking left
	 * @throws Exception
	 */
	public int isLeftOrRight(IplImage originalImage) throws Exception {

		IplImage grayImage = IplImage.create(originalImage.width(),
				originalImage.height(), IPL_DEPTH_8U, 1);

		// We convert the original image to grayscale.
		cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);

		CvMemStorage storage = CvMemStorage.create();

		// We instantiate a classifier cascade to be used for detection,
		// using
		// the cascade definition.
		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(
				cvLoad(CASCADE_FILE));

		// We detect the faces.
		CvSeq faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 1,
				0);

		CvRect r = new CvRect(cvGetSeqElem(faces, 0));
		BufferedImage im = (originalImage.getBufferedImage()).getSubimage(
				r.x(), r.y(), r.width(), r.height());
		return getEyeSide(im);

	}

	/**
	 * Helper function to get the direction of gaze
	 * @param bufferedImg Image for gaze detection
	 * @return 1 if the eye is looking right or 0 if the eye is looking left
	 * @throws Exception
	 */
	public int getEyeSide(BufferedImage bufferedImg) throws Exception {

		IplImage originalImage = IplImage.createFrom(bufferedImg);
		CvMemStorage storage = CvMemStorage.create();

		// We instantiate a classifier cascade to be used for detection,
		// using
		// the cascade definition.
		CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(
				cvLoad(CASCADE_FILE2));

		// We detect the faces.
		CvSeq faces = cvHaarDetectObjects(originalImage, cascade, storage, 1.1,
				1, 0);
		CvRect max1 = new CvRect(1, 1, 1, 1);
		CvRect max2 = new CvRect(1, 1, 1, 1);
		for (int i = 0; i < faces.total(); i++) {
			CvRect r = new CvRect(cvGetSeqElem(faces, i));
			if ((r.width() * r.height() > max1.width() * max1.height())
					|| (r.width() * r.height() > max2.width() * max2.height())) {
				if (max1.width() * max1.height() > max2.width() * max2.height()) {
					max2 = r;
				} else {
					max1 = r;
				}
			}

		}
		Tuple leftRight = new Tuple();
		mask(bufferedImg, max1, leftRight);
		mask(bufferedImg, max2, leftRight);
		if (leftRight.getX() > leftRight.getY()) {
			return 0;
		}
		return 1;

	}

	public static void mask(BufferedImage bufferedImg, CvRect r, Tuple leftRight) {
		for (int j = r.x(); j < r.x() + r.width() - 1; j++) {
			for (int k = r.y(); k < r.y() + r.height() - 1; k++) {
				if (bufferedImg.getRGB(j, k) <= 0xFF333333) {
					if (j < (r.x() + r.width() / 2)) {
						leftRight.incrX();
					} else {
						leftRight.incrY();
					}
				}
			}
		}
	}
}