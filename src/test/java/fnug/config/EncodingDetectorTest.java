package fnug.config;

import junit.framework.Assert;

import org.codehaus.jackson.JsonEncoding;
import org.junit.Test;

import fnug.resource.DefaultResource;

public class EncodingDetectorTest {

	/**
	 * Test that the detector can detect the encoding of a file
	 * encoded in UTF-8.
	 * @throws Exception
	 */
	@Test
	public void testDetectUTF8() throws Exception {
		DefaultResource res = new DefaultResource("/", "testencoding1-UTF8.js");
		EncodingDetector detector = new EncodingDetector(res.getBytes());
		JsonEncoding encoding = detector.detectEncoding();
		Assert.assertEquals("UTF-8", encoding.getJavaName());
	}	
	
	/**
	 * Test that the detector can detect the encoding of a file
	 * encoded in UTF-16LE (Little Endian).
	 * @throws Exception
	 */
	@Test
	public void testDetectUTF16LE() throws Exception {
		DefaultResource res = new DefaultResource("/", "testencoding2-UTF16LE.js");
		EncodingDetector detector = new EncodingDetector(res.getBytes());
		JsonEncoding encoding = detector.detectEncoding();
		Assert.assertEquals("UTF-16LE", encoding.getJavaName());
	}

	
}
