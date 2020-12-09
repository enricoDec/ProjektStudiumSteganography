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

import steganography.audio.mp3.MP3File;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class returns the data bytes of an MP3 file in order.
 * @author Richard Rudek
 */
public class MP3SequenceOverlay implements AudioOverlay {

    protected byte[] mp3Bytes;
    protected List<Integer> dataByteOrder;
    protected int currentPosition = -1;

    /**
     * Adds a sequence overlay to a given MP3 file. This overlay retrieves only the data bytes of the MP3 file and
     * returns the bytes in order.
     * @param bytes byte array containing an MP§ file
     * @param seed would normally be used to influence the overlay (e.g. shuffling).
     *             Obviously, this cannot be used in the a sequence overlay.
     * @throws UnsupportedAudioFileException if the given byte array does not contain an MP3 file
     */
    public MP3SequenceOverlay(byte[] bytes, long seed) throws UnsupportedAudioFileException {
        MP3File mp3File = new MP3File(bytes);
        if (!mp3File.findAllFrames())
            throw new UnsupportedAudioFileException("The given byte array is not a valid MP3 file.");
        this.mp3Bytes = mp3File.getMP3Bytes();
        this.dataByteOrder = mp3File.getModifiablePositions();

        createOverlay(seed);
    }

    protected void createOverlay(long seed) {
    }

    @Override
    public byte next() throws NoSuchElementException {
        if (++this.currentPosition >= this.dataByteOrder.size())
            throw new NoSuchElementException("No more bytes left.");

        return this.mp3Bytes[this.dataByteOrder.get(this.currentPosition)];
    }

    @Override
    public int available() {
        return this.dataByteOrder.size() - this.currentPosition - 1;
    }

    @Override
    public void setByte(byte value) throws NoSuchElementException {
        this.mp3Bytes[this.dataByteOrder.get(this.currentPosition)] = value;
    }

    @Override
    public byte[] getBytes() {
        return this.mp3Bytes;
    }
}