/*
 * Copyright (c) 2020
 * Contributed by NAME HERE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package steganography;

import steganography.exceptions.*;

import java.io.IOException;

public interface Steganography {
    /**
     * Encodes the given payload in the given carrier (image, mp3, ...) and returns the result.
     * The format of the returned media will be the same as carrier. Carrier needs to be an exact media representation
     * as it would be read from a file by an InputStream.
     * @param carrier media used to hide the payload
     * @param payload data to hide
     * @return steganographic data - exact media representation. Can be stored as it is to a file to open externally
     * @throws IOException if a problem occurs during reading of carrier or payload
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from carrier
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     * @throws MediaReassemblingException if a problem occurred during writing of the result media
     * @throws MediaCapacityException if the payload doesn't fit in the carrier
     */
    byte[] encode(byte[] carrier, byte[] payload)
            throws IOException, MediaNotFoundException, UnsupportedMediaTypeException,
                    MediaReassemblingException, MediaCapacityException;

    /**
     * <p>Encodes the given payload in the given carrier (image, mp3, ...) and returns the result.
     * The format of the returned media will be the same as carrier. Carrier needs to be an exact media representation
     * as it would be read from a file by an InputStream.</p>
     * <p>The Seed changes the way the payload is encoded. When decoding the result, the exact same Seed needs to be
     * given to decode()</p>
     * @param carrier carrier used to hide the data
     * @param payload data to hide
     * @param seed affects the resulting steganographic data (similar to a password)
     * @return steganographic data
     * @throws IOException if a problem occurs during reading of carrier or payload
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from carrier
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     * @throws MediaReassemblingException if a problem occurred during writing of the result media
     * @throws MediaCapacityException if the payload doesn't fit in the carrier
     */
    byte[] encode(byte[] carrier, byte[] payload, long seed)
            throws IOException, MediaNotFoundException, UnsupportedMediaTypeException,
                    MediaReassemblingException, MediaCapacityException;

    /**
     * Retrieves hidden message from a steganographic file.
     * @param steganographicData Data containing data to extract
     * @return hidden message
     * @throws IOException if a problem occurs during reading of steganographicData
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from steganographicData
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     */
    byte[] decode(byte[] steganographicData)
            throws IOException, MediaNotFoundException, UnsupportedMediaTypeException, UnknownStegFormatException;

    /**
     * Retrieves hidden message from a steganographic file.
     * @param steganographicData Data containing data to extract
     * @param seed seed that was used to encode the given stenographicData
     * @return hidden message
     * @throws IOException if a problem occurs during reading of steganographicData
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from steganographicData
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     */
    byte[] decode(byte[] steganographicData, long seed)
            throws IOException, MediaNotFoundException, UnsupportedMediaTypeException, UnknownStegFormatException;

    /**
     * Tests if the given data has a hidden message encoded in it
     * @param data data to test
     * @return true if the given data has a hidden message encoded in it
     * @throws IOException if a problem occurs during reading of data
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from data
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     */
    boolean isSteganographicData(byte[] data)
            throws IOException, MediaNotFoundException, UnsupportedMediaTypeException;

    /**
     * Tests if the given data has a hidden message encoded in it, using the given seed
     * @param data data to test
     * @param seed seed the hidden message was encoded with
     * @return true if the given data has a hidden message encoded in it
     * @throws IOException if a problem occurs during reading of data
     * @throws MediaNotFoundException if the intended media (e.g. Image, Video, ...) could not be read from data
     * @throws UnsupportedMediaTypeException if the Media Type (e.g. JPG) is not supported
     */
    boolean isSteganographicData(byte[] data, long seed)
            throws IOException, MediaNotFoundException, UnsupportedMediaTypeException;
}
