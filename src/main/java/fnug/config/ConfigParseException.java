package fnug.config;

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
 * Exception thrown when parsing a config.
 * 
 * @author Martin Algesten
 * 
 */
@SuppressWarnings("serial")
public class ConfigParseException extends RuntimeException {

    /**
     * Constructs with message.
     * 
     * @param msg
     *            Message.
     */
    public ConfigParseException(String msg) {
        super(msg);
    }

    /**
     * Constructs with message and wrapped exception.
     * 
     * @param msg
     *            Message
     * @param ex
     *            Wrapped exception.
     */
    public ConfigParseException(String msg, Exception ex) {
        super(msg, ex);
    }

}
