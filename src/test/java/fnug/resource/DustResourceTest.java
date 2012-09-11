package fnug.resource;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

public class DustResourceTest {

    @Test
    public void testCompile() throws UnsupportedEncodingException {

        DustCompiledResource r = new DustCompiledResource("/", "dusttest.dust");

        String s = new String(r.getBytes(), "utf-8");

        Assert.assertEquals(
                "(function(){dust.register(\"dusttest\",body_0);function body_0(chk,ctx){return chk.write" +
                        "(\"<div>Hello there \").reference(ctx.get(\"name\"),ctx,\"h\")" +
                        ".write(\"!</div>\");}return body_0;})();",
                s);

    }


    @Test
    public void testCompile2() throws UnsupportedEncodingException {

        DustCompiledResource r = new DustCompiledResource("/test/", "bbee.dusttest2.dust");

        String s = new String(r.getBytes(), "utf-8");

        Assert.assertEquals("(function(){dust.register(\"bbee.dusttest2\"," +
                "body_0);function body_0(chk,ctx){return chk.write(\"<div>Second hello there \")." +
                "reference(ctx.get(\"name\"),ctx,\"h\").write(\"!</div>\");}return body_0;})();", s);

    }

}
