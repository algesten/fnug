package fnug.config;

import org.codehaus.jackson.JsonLocation;

/*
 Copyright 2010 Martin Algesten

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
 * Specialisation of {@link ConfigParseException} for JsonConfigParser.
 * 
 * @author Martin Algesten
 * 
 */
@SuppressWarnings("serial")
public class JsonConfigParseException extends ConfigParseException {

    private JsonConfigParseException(String msg, JsonLocation loc, Exception ex) {
        super("At line " + loc.getLineNr() + " col " + loc.getColumnNr() + ": " + msg, ex);
    }

    /**
     * Constructs with message and location.
     * 
     * @param msg
     *            Message
     * @param loc
     *            Location
     */
    public JsonConfigParseException(String msg, JsonLocation loc) {
        this(msg, loc, null);
    }

    /**
     * Constructs with location and wrapped exception.
     * 
     * @param loc
     *            Location
     * @param ex
     *            Wrapped exception.
     */
    public JsonConfigParseException(JsonLocation loc, Exception ex) {
        this(ex.getMessage(), loc, ex);
    }

}
