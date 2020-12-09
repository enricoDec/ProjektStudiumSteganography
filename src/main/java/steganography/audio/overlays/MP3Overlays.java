/*
 * Copyright (c) 2020
 * Contributed by Richard Rudek
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

package steganography.audio.overlays;

/**
 * This enum contains every overlay that can be used to encode or decode messages into or from MP3 files.
 * @author Richard Rudek
 */
public enum MP3Overlays {
    /**
     * Goes through MP3 files and reads/writes from/to data bytes in order
     */
    SEQUENCE_OVERLAY,

    /**
     * Goes through MP3 files and reads/writes from/to shuffled data bytes according to a seed
     */
    SHUFFLE_OVERLAY
}