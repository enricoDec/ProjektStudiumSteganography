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

package steganography.image.encoders;

import steganography.util.ByteHex;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Selina Wernike
 * These Class is mainly used to decode and change a GIF color table.
 * It's based on the official GIF source
 */
public class GIFTableDecoder {
    private int[] colorTable;
    //private static final byte MASK_LENGTH = Byte.parseByte("11111000",2);
    byte[] header = {ByteHex.hexToByte("47"),ByteHex.hexToByte("49"), ByteHex.hexToByte("46"),
                        ByteHex.hexToByte("38"), ByteHex.hexToByte("39"), ByteHex.hexToByte("61")};
    public void changeAllPixels() {
        try {
            File file = new File("src/main/resources/changed.gif");
           /* BufferedImage test = ImageIO.read(file);
            System.out.println(test.getType());
            for (int i = 0; i < test.getHeight(); i++) {
                for (int j = 0; j < test.getWidth(); j++) {
                    int color = 0x0000FF00;
                    color = test.getRGB(j,i);
                    System.out.print(Integer.toHexString(color) + ", ");
                    if(color == 0) {
                        test.setRGB(j,i,(0x00FF0000));
                    }

                }
            }
            FileOutputStream out = new FileOutputStream("src/main/resources/changed.gif");
            ImageIO.write(test, "gif",out);
            out.close(); */
            byte[] gifByte = Files.readAllBytes(file.toPath());
            saveColorTable(gifByte);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This Method extracts the color table of a gif and transforms it into an int-array.
     * Each entry contains an Integer representing Alpha, red, green, blue channel with 8 bit each
     * @param gif Not decoded byte-Array of a gif
     * @return {int[]} colorTable The
     */
    public int[] saveColorTable(byte[] gif) {
        if (gif.length <= header.length) {
            throw new IllegalArgumentException("Data is not a gif89a");
        }
        //check for gif Header
        for (int i = 0; i < header.length; i++) {
            if (header[i] != gif[i]) {
                throw new IllegalArgumentException("Data is not a gif89a");
            }
        }
        //check if globalcolorTable exists
        System.out.println(ByteHex.byteToHex(gif[10]));
        if((gif[10] & 0x80) == 0) {
            System.out.println("No globalcolorTable");
        }  else {
            System.out.println("GlobalColorTabel exists");
            System.out.println(ByteHex.byteToHex(gif[9]));
            int length =  (gif[10] & 0x7);
            colorTable = globalColorTable(gif,length);
        }
        return colorTable;
    }

    private int[] globalColorTable(byte[] gif, int length) {
        int i = 13;
        int[] table = new int[(int) Math.pow(2,length + 1)];
        for (int j = 0; j < table.length;j++) {
            int color = 0 | 0xFF;
            System.out.print("red: " + ByteHex.byteToHex(gif[i]) + ", ");
            color = (color << 8) | gif[i];
            i++;
            System.out.print("green: " + ByteHex.byteToHex(gif[i]) + ",");
            color = (color << 8) | gif[i];
            i++;
            System.out.print("blue: " + ByteHex.byteToHex(gif[i]) + "\n");
            color = (color << 8) | gif[i];
            i++;
            table[j] = color;
        }
        return table;
    }

    /**
     * Unterteilt die Farbtabelle in Farbpaare die einen ähnlichen Farbwert haben.
     * @return ColorCoupel[] HashMap mit allen Farbwerten, die min. ein Farbpaar haben.
     */
    public Map<Integer,List<Integer>> getColorCouples(int[] colorTable) {
        Map<Integer, List<Integer>> colorCouples = new HashMap<>();
        for (int i = 0; i < colorTable.length;i++) {
            List<Integer> couples = new ArrayList<>();
            boolean pixelIsOne = PixelBit.pixelIsOne(colorTable[i]);
            for (int j = 0; j < colorTable.length; j++) {
                if (i != j) {
                    //Red, green and blue Value;
                    if (Math.abs(getRed(colorTable[i]) - getRed(colorTable[j])) <= 8) {
                        if (Math.abs(getGreen(colorTable[i]) - getGreen(colorTable[j])) <= 8) {
                            if (Math.abs(getBlue(colorTable[i]) - getBlue(colorTable[j])) <= 8) {
                                if (pixelIsOne != PixelBit.pixelIsOne(colorTable[j])) {
                                    couples.add(colorTable[j]);
                                }
                            }
                        }
                    }

                }
            }
            if (couples.size() > 0) {
                colorCouples.put(colorTable[i],couples);
            }
        }
        return colorCouples;
    }

    public static void main(String[] args) {
        GIFTableDecoder test = new GIFTableDecoder();
        test.changeAllPixels();
    }

    private int getRed(int color) {
        int red = color << 8;
        return (red >> 24);
    }

    private int getGreen(int color) {
        int green = color << 16;
        return (green >> 24);
    }

    private int getBlue(int color) {
        int blue = color << 24;
        return  (blue >> 24);
    }
}