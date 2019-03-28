import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class Toolbox {
	private static byte[] createChecksum(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		byte[] buffer;
		int numRead;
		FileInputStream fis;
		MessageDigest md;
		
		buffer = new byte[1024];
		fis =  new FileInputStream(file);
		md = MessageDigest.getInstance("MD5");
		
		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				md.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return md.digest();
	}

	public static String getMD5Checksum(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		byte[] b;
		String result;

		b = createChecksum(file);
		result = "";
		for (int i=0; i < b.length; i++) {
			result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}
}