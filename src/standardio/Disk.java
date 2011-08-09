/*********************************************************
 * * *
 * 
 * @author Vinay Vittal *
 * @created October 31, 2004 * * *
 *********************************************************/

package standardio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Disk {

	private final File		file;
	private final String	filePath;

	public Disk() {

		filePath = ("." + File.separator + "simdisk.data");
		file = new File(filePath);

		try {
			if (!file.exists()) {
				file.createNewFile();
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				raf.seek(0);
				for (int i = 0; i < 2000 * 1000; i++) {
					raf.writeByte(0x00);
				}
				raf.close();
			}
		} catch (IOException e) {
			System.err.println("Exception: " + e);
			e.printStackTrace();
		}
	}

	public int get_block(int blknum, byte[] buf)
			throws DiskException {

		RandomAccessFile raf;
		int length = buf.length;
		int count = 0;

		if ((blknum < 0) || (blknum > 1999)) {
			throw new DiskException("DiskException - Illegal Block Number "
					+ blknum);
		}

		if (length > 64) {
			length = 64;
		} else if (length < 64) {
			throw new DiskException("DiskException - Illegal Buffer Size "
					+ length);
		}

		try {
			raf = new RandomAccessFile(file, "r");
			raf.seek(blknum * 64);
			for (int i = 0; i < length; i++) {
				buf[i] = raf.readByte();
				count++;
			}
			raf.close();
		} catch (IOException e) {
			System.err.println("Exception: " + e);
			e.printStackTrace();
		}
		return count;
	}

	public int put_block(int blknum, byte[] buf)
			throws DiskException {

		RandomAccessFile raf;
		int length = buf.length;
		int count = 0;

		if ((blknum < 0) || (blknum > 1999)) {
			throw new DiskException("DiskException - Illegal Block Number "
					+ blknum);
		}

		if (length > 64) {
			length = 64;
		} else if (length < 64) {
			throw new DiskException("DiskException - Illegal Buffer Size "
					+ length);
		}

		try {
			raf = new RandomAccessFile(file, "rw");
			raf.seek(blknum * 64);
			for (int i = 0; i < length; i++) {
				raf.writeByte(buf[i]);
				count++;
			}
			raf.close();
		} catch (IOException e) {
			System.err.println("Exception: " + e);
			e.printStackTrace();
		}
		return count;
	}
}

class DiskException extends Exception {

	private final String	exceptionMessage;

	DiskException(String message) {

		exceptionMessage = message;
	}

	@Override
	public String toString() {

		return exceptionMessage;
	}
}