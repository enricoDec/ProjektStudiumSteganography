/*
 * Copyright (c) 2020
 * Contributed by Selina Wernike
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
/**
 * @author Selina Wernike
 * This Klass will Split an animated gif into individual frames and vice versa
 * https://stackoverflow.com/questions/8933893/convert-each-animated-gif-frame-to-a-separate-bufferedimage
 */
package steganography.image;

import steganography.image.encoders.GifDecoder;
import steganography.util.ByteArrayUtils;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author Selina Wernike
 * The class splits an animated gif into several single frame gifs or vice versa
 */
public class AnimatedGif {
        private static String  path = "src/main/resources/";
        private static IIOMetadata[] metadataArray;
        private static int noi = 0;
        private int[] delay;
        private int nop = 0;
        private IIOMetadata[] metadataForImages;


    /**
     *
     * @param animatedGif a byte array with the animated gif, that needs to be splitted
     * @return {byte[][]} output an two dimensional array which contains all gif data
     * @throws IOException
     */
        public byte[][] splitGif(byte[] animatedGif) throws IOException {
            ImageReader reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
            ByteArrayInputStream input = new ByteArrayInputStream(animatedGif);
            ImageInputStream ciis = ImageIO.createImageInputStream(input);
            reader.setInput(ciis, false);
            nop = reader.getNumImages(true);
            metadataForImages = new IIOMetadata[nop];
            byte[][] output = new byte[nop][];
            BufferedImage master = null;

            for (int i = 0; i < nop; i++) {
                BufferedImage image = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);
                metadataForImages[i] = metadata;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(image, "GIF", bos);
                output[i] = bos.toByteArray();
            }
            return output;
        }

        public byte[][] splitGifDecoder(byte[] animatedGif) throws NullPointerException{
            try {
                GifDecoder.GifImage gif = GifDecoder.read(animatedGif);
                nop = gif.getFrameCount();
                delay = new int[nop];
                byte[][] output = new byte[nop][];
                for (int i = 0; i < nop; i++) {
                    delay[i] = gif.getDelay(i);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    BufferedImage image = gif.getFrame(i);
                    ImageIO.write(image, "GIF", bos);
                    ImageIO.write(image, "GIF", new File(path + "/gifFrames/" + i + ".gif"));
                    output[i] = bos.toByteArray();

                }
                return output;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public byte[] sequenceGifDecoder(byte[][] gifs) {
            ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName("gif").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageReader reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();

            try {

                ImageOutputStream out = new FileImageOutputStream(new File(path + "doggy.gif"));
                writer.setOutput(out);
                writer.prepareWriteSequence(null);

                for (int i = 0; i < nop; i++) {
                    ByteArrayInputStream readInput = new ByteArrayInputStream(gifs[i]);
                    BufferedImage bufferedImage = ImageIO.read(readInput);
                    ImageTypeSpecifier specifier = ImageTypeSpecifier.createFromBufferedImageType(bufferedImage.getType());
                    IIOMetadata newMetadata = writer.getDefaultImageMetadata(specifier,param);
                    ByteArrayInputStream ioInput = new ByteArrayInputStream(gifs[i]);
                    ImageInputStream ciis = ImageIO.createImageInputStream(ioInput);
                    reader.setInput(ciis, false);
                    IIOImage frame = reader.readAll(0,null);
                    newMetadata = createMetadata(delay[i], frame, newMetadata);
                    // Cannot change Metadata since its read-only at the moment.
                    writer.writeToSequence(new IIOImage(bufferedImage,null,newMetadata),param);

                }
                writer.endWriteSequence();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
            return null;
        }

        private  IIOMetadata createMetadata(int delay, IIOImage gifFrame, IIOMetadata newMetadata) throws IIOInvalidTreeException {
            IIOMetadata metadata = gifFrame.getMetadata();
            String name = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(name);

            IIOMetadataNode graphicsControlExtensionNote = getNode(root,"GraphicControlExtension");
            graphicsControlExtensionNote.setAttribute("delayTime", Integer.toString(delay));
            newMetadata.setFromTree(name, root);
            return newMetadata;
        }

        private static IIOMetadataNode getNode(IIOMetadataNode root, String nodeName) {
            int nNodes = root.getLength();
            for (int i = 0; i < nNodes; i++) {
                if(root.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                    return (IIOMetadataNode) root.item(i);
                }
            }
            IIOMetadataNode node = new IIOMetadataNode(nodeName);
            root.appendChild(node);
            return (node);
        }

        public byte[] sequenzGIF(byte[][] gifs)  {
            ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName("gif").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {


                ImageOutputStream out = ImageIO.createImageOutputStream(bos);
                writer.setOutput(out);
                writer.prepareWriteSequence(null);
                for (int i = 0; i < nop; i++) {
                    ByteArrayInputStream input = new ByteArrayInputStream(gifs[i]);
                    BufferedImage next = ImageIO.read(input);
                    writer.writeToSequence(new IIOImage(next,null,metadataForImages[i]),param);

                }
                writer.endWriteSequence();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bos.toByteArray();
        }
    public static void main(String[] args) {
            AnimatedGif giffer = new AnimatedGif();
            File input =new File(path + "doggy.gif");
        try {
            byte[] gif = ByteArrayUtils.read(input);
            giffer.sequenceGifDecoder(giffer.splitGifDecoder(gif));

        } catch (IOException e) {
            e.printStackTrace();
        }

        }

    /**
     * Splits a gif into single frames
     * @param name Name of the gif
     * @return {byte[][]} Byte array with all frames as byte[]
     */
    public static byte[][] splitGifTest(String name) {
        try {
            String[] imgatt = new String[]{
                    "imageLeftPosition",
                    "imageTopPosition",
                    "imageWidth",
                    "imageHeight"
            };

            ImageReader reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
            ImageInputStream ciis = ImageIO.createImageInputStream(new File(path + name));
            reader.setInput(ciis,false);
            noi = reader.getNumImages(true);
            metadataArray = new IIOMetadata[noi];
            System.out.println(noi);
            BufferedImage master = null;

            for (int i = 0; i < noi; i++) {
                BufferedImage image;
                   image = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);
                metadataArray[i] = metadata;
                ImageIO.write(image, "GIF", new File(path + "/gifFrames/" + i + ".gif"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] mergeGIF() {
        ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName("gif").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        try {
            ImageOutputStream out = new FileImageOutputStream(new File(path + "bubbles2.gif"));
            writer.setOutput(out);
            writer.prepareWriteSequence(null);

        for (int i = 0; i < noi; i++) {
            BufferedImage next = ImageIO.read(new File(path + "/gifFrames/" + i + ".gif"));
            writer.writeToSequence(new IIOImage(next,null,metadataArray[i]),param);

        }
        writer.endWriteSequence();
        out.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

        return null;
    }
}
