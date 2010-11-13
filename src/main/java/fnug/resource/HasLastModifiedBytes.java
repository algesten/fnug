package fnug.resource;

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
 * Implementors have bytes that are time stamped.
 * 
 * @author Martin Algesten
 * 
 */
public interface HasLastModifiedBytes {

    /**
     * The bytes.
     * 
     * @return the timestamped bytes.
     */
    byte[] getBytes();

    /**
     * The timestamp when the bytes were last changed.
     * 
     * @return the last modified of the {@link #getBytes()}.
     */
    long getLastModified();

}
