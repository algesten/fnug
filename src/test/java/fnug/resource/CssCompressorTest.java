package fnug.resource;

import org.junit.Assert;
import org.junit.Test;

import fnug.resource.CssCompressor;

public class CssCompressorTest {

    @Test
    public void testCompressCss() throws Exception {

        String css = "body { background: black; \n\n\n\n     color: white; margin:   14px 14px    14px 14px; }";

        CssCompressor comp = new CssCompressor();
        String c = new String(comp.compress(css.getBytes()));

        Assert.assertEquals("body{background:black;color:white;margin:14px 14px 14px 14px}", c);

    }

    @Test
    public void testCompressBadCss() throws Exception {

        String css = "body { background: ";

        CssCompressor comp = new CssCompressor();
        String c = new String(comp.compress(css.getBytes()));

        Assert.assertEquals("body{background:", c);

        css = "body { background: }";

        c = new String(comp.compress(css.getBytes()));

        Assert.assertEquals("body{background:}", c);

    }

}
