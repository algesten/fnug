package fnug.resource;

import junit.framework.Assert;

import org.junit.Test;

import fnug.resource.JsCompilationFailedException;
import fnug.resource.JsCompressor;

public class JsCompressorTest {

    @Test
    public void testJsCompress() throws Exception {

        JsCompressor compressor = new JsCompressor();

        String s = new String(
                compressor.compress(("var a = function() { \n\n\n\n alert('this is a test'); };     " +
                        "var b = function() { a(); };").getBytes()));

        Assert.assertEquals("var a=function(){alert(\"this is a test\")},b=function(){a()};", s);

    }


    @Test
    public void testCompressBadJs() throws Exception {

        JsCompressor compressor = new JsCompressor();

        try {
            compressor.compress(("var a = function() { + };").getBytes());
            Assert.fail();
        } catch (JsCompilationFailedException jse) {
            // expected with such rubbish code.
            Assert.assertEquals("Compilation failed\n" +
                    "JSC_PARSE_ERROR. Parse error. syntax error at [concatenated] line 1 : 23\n" +
                    "JSC_PARSE_ERROR. Parse error. missing } after function body at [concatenated] line 1 : 24",
                    jse.getMessage());
        }

    }

}
