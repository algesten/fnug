package fnug.config;

import java.io.CharConversionException;
import java.io.IOException;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonParseException;

/*
                                  Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."

      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or
          Derivative Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.

      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   APPENDIX: How to apply the Apache License to your work.

      To apply the Apache License to your work, attach the following
      boilerplate notice, with the fields enclosed by brackets "[]"
      replaced with your own identifying information. (Don't include
      the brackets!)  The text should be enclosed in the appropriate
      comment syntax for the file format. We also recommend that a
      file or class name and description of purpose be included on the
      same "printed page" as the copyright notice for easier
      identification within third-party archives.

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/**
 * Built on parts of the org.codehaus.jackson.impl.ByteSourceBootstrapper, 
 * created by Tatu Saloranta, to be able to find the encoding of byte arrays.
 * 
 * @author chilmers, based on original by Tatu Saloranta.
 * 
 * Information from NOTICE file in Jackson distribution (jackson-core-asl-1.6.1) 
 * regarding the Jackson distribution:
 * "This product currently only contains code developed by authors
 * of specific components, as identified by the source code files;
 * if such notes are missing files have been created by
 * Tatu Saloranta.
 * 
 * For additional credits (generally to people who reported problems)
 * see CREDITS file."
 * 
 * Information from LICENSE file in Jackson distribution (jackson-core-asl-1.6.1)
 * regarding the Jackson distribution:
 * "This copy of Jackson JSON processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 * 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/
 * 
 * A copy is also included with both the the downloadable source code package
 * and jar that contains class bytecodes, as file "ASL 2.0". In both cases,
 * that file should be located next to this file: in source distribution
 * the location should be "release-notes/asl"; and in jar "META-INF/"
 * "
 * Both this text and the ASL 2.0 are given within this source file.
 *
 */
public class EncodingDetector {

    /*
    /**********************************************************
    /* Input buffering
    /**********************************************************
     */

    final byte[] _inputBuffer;

    private int _inputPtr; 
    
    /*
    /**********************************************************
    /* Input location
    /**********************************************************
     */

    /**
     * Current number of input units (bytes or chars) that were processed in
     * previous blocks,
     * before contents of current input buffer.
     *<p>
     * Note: includes possible BOMs, if those were part of the input.
     */
    protected int _inputProcessed;    
	

    boolean _bigEndian = true;
    int _bytesPerChar = 0; // 0 means "dunno yet"    

    public EncodingDetector(byte[] inputBuffer)    
    {
        _inputBuffer = inputBuffer;
        _inputPtr = 0;
        // Need to offset this for correct location info
        _inputProcessed = -0;
    }	    
	
    /**
     * Method that should be called after constructing an instace.
     * It will figure out encoding that content uses, to allow
     * for instantiating a proper scanner object.
     */
    public JsonEncoding detectEncoding()
        throws IOException, JsonParseException
    {
        boolean foundEncoding = false;

        // First things first: BOM handling
        /* Note: we can require 4 bytes to be read, since no
         * combination of BOM + valid JSON content can have
         * shorter length (shortest valid JSON content is single
         * digit char, but BOMs are chosen such that combination
         * is always at least 4 chars long)
         */
        int quad =  (_inputBuffer[_inputPtr] << 24)
            | ((_inputBuffer[_inputPtr+1] & 0xFF) << 16)
            | ((_inputBuffer[_inputPtr+2] & 0xFF) << 8)
            | (_inputBuffer[_inputPtr+3] & 0xFF);
        
        if (handleBOM(quad)) {
            foundEncoding = true;
        } else {
            /* If no BOM, need to auto-detect based on first char;
             * this works since it must be 7-bit ascii (wrt. unicode
             * compatible encodings, only ones JSON can be transferred
             * over)
             */
            // UTF-32?
            if (checkUTF32(quad)) {
                foundEncoding = true;
            } else if (checkUTF16(quad >>> 16)) {
                foundEncoding = true;
            }
        }

        JsonEncoding enc;

        /* Not found yet? As per specs, this means it must be UTF-8. */
        if (!foundEncoding) {
            enc = JsonEncoding.UTF8;
        } else if (_bytesPerChar == 2) {
            enc = _bigEndian ? JsonEncoding.UTF16_BE : JsonEncoding.UTF16_LE;
        } else if (_bytesPerChar == 4) {
            enc = _bigEndian ? JsonEncoding.UTF32_BE : JsonEncoding.UTF32_LE;
        } else {
            throw new RuntimeException("Internal error"); // should never get here
        }
        return enc;
    }
    
    /*
    /**********************************************************
    /* Internal methods, parsing
    /**********************************************************
     */

    /**
     * @return True if a BOM was succesfully found, and encoding
     *   thereby recognized.
     */
    private boolean handleBOM(int quad)
        throws IOException
    {
        /* Handling of (usually) optional BOM (required for
         * multi-byte formats); first 32-bit charsets:
         */
        switch (quad) {
        case 0x0000FEFF:
            _bigEndian = true;
            _inputPtr += 4;
            _bytesPerChar = 4;
            return true;
        case 0xFFFE0000: // UCS-4, LE?
            _inputPtr += 4;
            _bytesPerChar = 4;
            _bigEndian = false;
            return true;
        case 0x0000FFFE: // UCS-4, in-order...
            reportWeirdUCS4("2143"); // throws exception
        case 0xFEFF0000: // UCS-4, in-order...
            reportWeirdUCS4("3412"); // throws exception
        }
        // Ok, if not, how about 16-bit encoding BOMs?
        int msw = quad >>> 16;
        if (msw == 0xFEFF) { // UTF-16, BE
            _inputPtr += 2;
            _bytesPerChar = 2;
            _bigEndian = true;
            return true;
        }
        if (msw == 0xFFFE) { // UTF-16, LE
            _inputPtr += 2;
            _bytesPerChar = 2;
            _bigEndian = false;
            return true;
        }
        // And if not, then UTF-8 BOM?
        if ((quad >>> 8) == 0xEFBBBF) { // UTF-8
            _inputPtr += 3;
            _bytesPerChar = 1;
            _bigEndian = true; // doesn't really matter
            return true;
        }
        return false;
    }

    private boolean checkUTF32(int quad)
            throws IOException
        {
            /* Handling of (usually) optional BOM (required for
             * multi-byte formats); first 32-bit charsets:
             */
            if ((quad >> 8) == 0) { // 0x000000?? -> UTF32-BE
                _bigEndian = true;
            } else if ((quad & 0x00FFFFFF) == 0) { // 0x??000000 -> UTF32-LE
                _bigEndian = false;
            } else if ((quad & ~0x00FF0000) == 0) { // 0x00??0000 -> UTF32-in-order
                reportWeirdUCS4("3412");
            } else if ((quad & ~0x0000FF00) == 0) { // 0x0000??00 -> UTF32-in-order
                reportWeirdUCS4("2143");
            } else {
                // Can not be valid UTF-32 encoded JSON...
                return false;
            }
            // Not BOM (just regular content), nothing to skip past:
            //_inputPtr += 4;
            _bytesPerChar = 4;
            return true;
        }

        private boolean checkUTF16(int i16)
        {
            if ((i16 & 0xFF00) == 0) { // UTF-16BE
                _bigEndian = true;
            } else if ((i16 & 0x00FF) == 0) { // UTF-16LE
                _bigEndian = false;
            } else { // nope, not  UTF-16
                return false;
            }
            // Not BOM (just regular content), nothing to skip past:
            //_inputPtr += 2;
            _bytesPerChar = 2;
            return true;
        }
        
        /*
        /**********************************************************
        /* Internal methods, problem reporting
        /**********************************************************
         */

        private void reportWeirdUCS4(String type)
            throws IOException
        {
            throw new CharConversionException("Unsupported UCS-4 endianness ("+type+") detected");
        }        
	
}
