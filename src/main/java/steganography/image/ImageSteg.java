/*
 * Copyright (c) 2020
 * Contributed by Henk-Joas Lubig
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

package steganography.image;

import steganography.Steganography;
import steganography.image.encoders.GIFTableDecoder;
import steganography.exceptions.UnknownStegFormatException;
import steganography.image.encoders.PixelBit;
import steganography.image.encoders.PixelIndex;
import steganography.image.exceptions.ImageCapacityException;
import steganography.image.exceptions.ImageWritingException;
import steganography.image.exceptions.NoImageException;
import steganography.image.exceptions.UnsupportedImageTypeException;
import steganography.image.overlays.RemoveTransparentShuffleOverlay;
import steganography.image.overlays.ShuffleOverlay;
import steganography.image.encoders.BuffImgEncoder;
import steganography.image.overlays.BufferedImageCoordinateOverlay;
import steganography.image.overlays.TableOverlay;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class ImageSteg implements Steganography {

    public static final long DEFAULT_SEED = 1732341558;
    private static final int HEADER_SIGNATURE = 1349075561;
    private final boolean useTransparent;
    private final boolean useDefaultHeader;

    private static final Set<String> supportedFormats = new HashSet<>(
            Arrays.asList("bmp", "BMP", "gif", "GIF", "png", "PNG")
    );

    /**
     * Creates a new ImageSteg with settings:
     * <ul>
     *     <li>useDefaultHeader = true</li>
     *     <li>useTransparent = false</li>
     * </ul>
     *
     * This means, a default header will be encoded in the image to simplify decoding and
     * fully transparent pixels will not be used for encoding or decoding.
     *
     * Is equivalent to ImageSteg(true, false).
     */
    public ImageSteg() {
        this.useDefaultHeader = true;
        this.useTransparent = false;
    }

    /**
     * Creates a new ImageSteg with the given settings.
     * <ul>
     *     <li>
     *         useDefaultHeader - <br/>
     *         if true, the default header will be encoded in the image. The hidden message can then be
     *         decoded using ImageSteg.decode(...). <br/>
     *         if false, no header will be encoded in the image. The hidden message can only be decoded
     *         using ImageSteg.decodeRaw(length, ...)
     *     </li>
     *     <li>
     *         useTransparent - <br/>
     *         if true, fully transparent pixels will be used for encoding and decoding <br/>
     *         if false, fully transparent pixels will not be used for encoding and decoding <br/>
     *         This value must be equal while encoding and decoding to successfully decode the hidden message.
     *         This value can only affect PNGs that contain fully transparent pixels.
     *         If an image has no fully transparent pixels, this value will be ignored.
     *         If the image is a GIF, this value will be ignored.
     *         BMPs with transparent pixels are not supported by this class.
     *     </li>
     * </ul>
     * @param useDefaultHeader should the default header be used for encoding?
     * @param useTransparent should fully transparent pixels be used for encoding and decoding?
     */
    public ImageSteg(boolean useDefaultHeader, boolean useTransparent) {
        this.useDefaultHeader = useDefaultHeader;
        this.useTransparent = useTransparent;
    }

    // All formats: JPG, jpg, tiff, bmp, BMP, gif, GIF, WBMP, png, PNG, JPEG, tif, TIF, TIFF, wbmp, jpeg

    // @Override
    // public void useDefaultHeader(boolean useDefaultHeader) {
    //     // TODO: Might be problematic decoding, length has to be given from user
    //     // this.useDefaultHeader = useDefaultHeader;
    // }

    @Override
    public byte[] encode(byte[] carrier, byte[] payload)
            throws IOException, UnsupportedImageTypeException, NoImageException,
                    ImageWritingException, ImageCapacityException {

        return encode(carrier, payload, DEFAULT_SEED);
    }

    @Override
    public byte[] encode(byte[] carrier, byte[] payload, long seed)
            throws IOException, NoImageException, UnsupportedImageTypeException,
                    ImageWritingException, ImageCapacityException {

        if (carrier == null)
            throw new NullPointerException("Parameter 'carrier' must not be null");
        if (payload == null)
            throw new NullPointerException("Parameter 'payload' must not be null");

        BuffImgAndFormat buffImgAndFormat = carrier2BufferedImage(carrier);

        int type = buffImgAndFormat.getBufferedImage().getType();

        BuffImgEncoder encoder = getEncoder(buffImgAndFormat.getBufferedImage(), seed);

        if (this.useDefaultHeader) {
            encoder.encode(int2bytes(HEADER_SIGNATURE));
            encoder.encode(int2bytes(payload.length));
        }
        encoder.encode(payload);

        return bufferedImage2byteArray(encoder.getOverlay().getBufferedImage(), buffImgAndFormat.getFormat());
    }

    @Override
    public byte[] decode(byte[] steganographicData)
            throws IOException, UnsupportedImageTypeException, NoImageException, UnknownStegFormatException {

        return decode(steganographicData, DEFAULT_SEED);
    }

    /**
     * Retrieves hidden message from a steganographic file. This method will fail, if the message
     * was hidden without using the default header. Use ImageSteg.decodeRaw() for this purpose.
     * Reasons for failing with an UnknownStegFormatExceptions are:
     * <ul>
     *      <li>there is no hidden message</li>
     *      <li>the message was hidden with 'useDefaultHeader = false'</li>
     *      <li>the value for 'useTransparent' was different when hiding the message</li>
     *      <li>the message was hidden using an unknown algorithm</li>
     * </ul>
     * @param steganographicData Data containing data to extract
     * @param seed seed that was used to encode the given stenographicData
     * @return
     * @throws IOException if an error occurs during reading 'steganographicData'
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     * @throws UnknownStegFormatException if the default header could not be found
     */
    @Override
    public byte[] decode(byte[] steganographicData, long seed)
            throws IOException, NoImageException, UnsupportedImageTypeException, UnknownStegFormatException {

        if (steganographicData == null)
            throw new NullPointerException("Parameter 'steganographicData' must not be null");

        BuffImgAndFormat buffImgAndFormat = carrier2BufferedImage(steganographicData);

        BuffImgEncoder encoder = getEncoder(buffImgAndFormat.getBufferedImage(), seed);

        // TODO: only do this if useDefaultHeader == true, but length has to be given from user
        // decode 4 bytes and compare them to header signature
        if (bytesToInt(encoder.decode(4)) != HEADER_SIGNATURE) {
            throw new UnknownStegFormatException("No steganographic encoding found.");
        }

        // decode the next 4 bytes to get the amount of bytes to read
        int length = bytesToInt(encoder.decode(4));

        return encoder.decode(length);
    }

    /**
     * Retrieves hidden message from a steganographic file. This method will not search for a header
     * or validate the retrieved data in any form. If 'steganographicData' contains a supported image,
     * this method will always return a result. Whether this result is the hidden message, depends on the
     * settings used:
     * <ul>
     *     <li>'useTransparent' during encoding == 'useTransparent' during decoding</li>
     *     <li>'payload.length' during encoding == 'length' during decoding</li>
     *     <li>No seed used during encoding (thereby using ImageSteg.DEFAULT_SEED)</li>
     *     <li>'useDefaultHeader' == false during encoding</li>
     * </ul>
     * @param length Length (in bytes) of the hidden message
     * @param steganographicData Data containing data to extract
     * @return
     * @throws IOException if an error occurs during reading 'steganographicData'
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     */
    public byte[] decodeRaw(int length, byte[] steganographicData)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        return decodeRaw(length, steganographicData, DEFAULT_SEED);
    }

    /**
     * Retrieves hidden message from a steganographic file. This method will not search for a header
     * or validate the retrieved data in any form. If 'steganographicData' contains a supported image,
     * this method will always return a result. Whether this result is the hidden message, depends on the
     * settings used:
     * <ul>
     *     <li>'useTransparent' during encoding == 'useTransparent' during decoding</li>
     *     <li>'payload.length' during encoding == 'length' during decoding</li>
     *     <li>'seed' during encoding == 'seed' during decoding</li>
     *     <li>'useDefaultHeader' == false during encoding</li>
     * </ul>
     * @param length Length (in bytes) of the hidden message
     * @param steganographicData Data containing data to extract
     * @param seed seed that was used to encode the given stenographicData
     * @return
     * @throws IOException if an error occurs during reading 'steganographicData'
     * @throws NoImageException if no image could be read from 'steganographicData'
     * @throws UnsupportedImageTypeException if the type of the given image is not supported
     */
    public byte[] decodeRaw(int length, byte[] steganographicData, long seed)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        if (steganographicData == null)
            throw new NullPointerException("Parameter 'steganographicData' must not be null");

        BuffImgAndFormat buffImgAndFormat = carrier2BufferedImage(steganographicData);

        BuffImgEncoder encoder = getEncoder(buffImgAndFormat.getBufferedImage(), seed);

        return encoder.decode(length);
    }

    @Override
    public boolean isSteganographicData(byte[] data)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        return isSteganographicData(data, DEFAULT_SEED);
    }

    @Override
    public boolean isSteganographicData(byte[] data, long seed)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        if (data == null)
            throw new NullPointerException("Parameter 'data' must not be null");

        BuffImgAndFormat buffImgAndFormat = carrier2BufferedImage(data);

        BufferedImageCoordinateOverlay overlay = new ShuffleOverlay(buffImgAndFormat.getBufferedImage(), seed);
        BuffImgEncoder encoder = new PixelBit(overlay);

        return bytesToInt(encoder.decode(4)) == HEADER_SIGNATURE;
    }

    /**
     * Returns the maximum number of bytes that can be encoded in the given image using the settings
     * given to the constructor of ImageSteg.
     * @param image image to potentially encode bytes in
     * @return the payload-capacity of image
     */
    public int getImageCapacity(byte[] image)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        BufferedImage bufferedImage = carrier2BufferedImage(image).getBufferedImage();

        int capacity = getEncoder(bufferedImage, DEFAULT_SEED).getOverlay().available() / 8;

        return this.useDefaultHeader ? (capacity - 8) : capacity;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                                       UTIL
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Determines and returns the suitable encoder (and overlay) for the given bufferedImage according to its type.
     * @param bufferedImage image to get the encoder for
     * @param seed to hand to the overlay
     * @return BuffImgEncoder with set BufferedImageCoordinateOverlay, chosen accordingly to the images type
     * @throws UnsupportedImageTypeException if the images type is not supported by any known encoder / overlay
     */
    private BuffImgEncoder getEncoder(BufferedImage bufferedImage, long seed)
            throws UnsupportedImageTypeException {

        int type = bufferedImage.getType();

        switch (type) {

            // Types for PixelBit Algorithm
            //----------------------------------------------------------------------------------
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_BGR:
                return new PixelBit(getOverlay(bufferedImage, seed));

            // Type(s) for ColorCouple Algorithm
            //----------------------------------------------------------------------------------
            case BufferedImage.TYPE_BYTE_INDEXED:
                GIFTableDecoder tableDecoder = new GIFTableDecoder();
                try {
                    Map<Integer, List<Integer>> colorCouple = tableDecoder.getColorCouples(
                            tableDecoder.saveColorTable(bufferedImage2byteArray(bufferedImage,"gif"))
                    );
                    return new PixelIndex(new TableOverlay(bufferedImage, seed, colorCouple), colorCouple, seed);
                } catch (IOException | ImageWritingException e) {
                    e.printStackTrace();
                }
                // return overlay8Bit

            // Types that have not been tested, but are probably suitable for PixelBit Algorithm
            //----------------------------------------------------------------------------------
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            case BufferedImage.TYPE_INT_ARGB_PRE:
                // TODO: Test those types (could not find them)
                throw new UnsupportedImageTypeException("Image type is not supported because untested.");

            // Types that will (probably) not be supported - explicit for completion reasons
            //----------------------------------------------------------------------------------
            case BufferedImage.TYPE_BYTE_BINARY:
            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_CUSTOM:
            case BufferedImage.TYPE_USHORT_555_RGB:
            case BufferedImage.TYPE_USHORT_565_RGB:
            case BufferedImage.TYPE_USHORT_GRAY:
            default:
                throw new UnsupportedImageTypeException("Image type (BufferedImage.TYPE = " + type + ") is not supported");
        }
    }

    /**
     * Returns overlay according to global variable useTransparent
     * @param bufferedImage BufferedImage to hand to overlay
     * @param seed Seed to hand to overlay
     * @return ShuffleOverlay or RemoveTransparentShuffleOverlay
     * @throws UnsupportedImageTypeException if the image type is not supported by the overlay
     */
    private BufferedImageCoordinateOverlay getOverlay(BufferedImage bufferedImage, long seed) throws UnsupportedImageTypeException {
        return this.useTransparent ?
                new ShuffleOverlay(bufferedImage, seed) :
                new RemoveTransparentShuffleOverlay(bufferedImage, seed);
    }

    private BuffImgAndFormat carrier2BufferedImage(byte[] carrier)
            throws IOException, NoImageException, UnsupportedImageTypeException {

        BuffImgAndFormat buffImgAndFormat;

        try(ImageInputStream imageInputStream = new MemoryCacheImageInputStream(new ByteArrayInputStream(carrier))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);

            if (readers.hasNext()) {
                ImageReader reader = readers.next();

                if (!formatSupported(reader.getFormatName()))
                    throw new UnsupportedImageTypeException(
                                    "The Image format (" +
                                    reader.getFormatName() +
                                    ") is not supported."
                    );

                try {
                    reader.setInput(imageInputStream);

                    BufferedImage buffImg = reader.read(0);

                    if (reader.getFormatName().equalsIgnoreCase("bmp") && buffImg.getColorModel().hasAlpha())
                        throw new UnsupportedImageTypeException(
                                "Image format (bmp containing transparency) is not supported."
                        );

                    buffImgAndFormat = new BuffImgAndFormat(buffImg, reader.getFormatName());

                } finally {
                    reader.dispose();
                }
            } else {
                throw new NoImageException("No image could be read from input.");
            }
        }

        return buffImgAndFormat;
    }

    private byte[] bufferedImage2byteArray(BufferedImage image, String format)
            throws IOException, ImageWritingException {

        ByteArrayOutputStream resultImage = new ByteArrayOutputStream();

        if (!ImageIO.write(image, format, resultImage)) {
            throw new ImageWritingException("Could not write image. Unknown, internal error");
        }

        return resultImage.toByteArray();
    }

    private byte[] int2bytes(int integer) {
        return new byte[] {
                (byte) ((integer >> 24) & 0xFF),
                (byte) ((integer >> 16) & 0xFF),
                (byte) ((integer >> 8) & 0xFF),
                (byte) (integer & 0xFF)
        };
    }

    private int bytesToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    private boolean formatSupported(String formatName) {
        return ImageSteg.supportedFormats.contains(formatName);
    }

    private static class BuffImgAndFormat {

        private final BufferedImage bufferedImage;

        private final String format;

        public BuffImgAndFormat(BufferedImage bufferedImage, String format) {
            this.bufferedImage = bufferedImage;
            this.format = format;
        }

        public BufferedImage getBufferedImage() {
            return bufferedImage;
        }

        public String getFormat() {
            return format;
        }
    }
}
