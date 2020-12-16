/*
 * Copyright (c) 2020
 * Contributed by Enrico de Chadarevian
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

package steganography.video.encoders;

import java.io.IOException;
import java.util.List;

public interface IEncoder {

    /**
     * Encodes a List of images to a Video
     * Current implementation uses a lossless codec
     *
     * @param stegImages list of Stenographic images
     * @return Encoded Video byte[]
     * @throws IOException IOException
     */
    byte[] imagesToVideo(List<byte[]> stegImages) throws IOException;
}