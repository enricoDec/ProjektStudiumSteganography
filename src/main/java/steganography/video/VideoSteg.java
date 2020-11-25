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

package steganography.video;

import steganography.Steganography;
import steganography.image.ImageSteg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author : Enrico Gamil Toros de Chadarevian
 * Project name : ProjektStudiumSteganography
 * @version : 1.0
 * @since : 23-11-2020
 **/
public class VideoSteg implements Steganography {
    private int maxEncodingThreads = 8;
    private int maxDecodingThreads = 8;
    private boolean logging = false;
    private long startTime = System.currentTimeMillis();
    private final File ffmpegBin = new File("S:\\code\\HTW\\ProjektStudiumSteganography\\src\\main\\resources");
    private final int seed = 1732341558;

    @Override
    public byte[] encode(byte[] carrier, byte[] payload) throws IOException {
        return encode(carrier, payload, this.seed);
    }

    @Override
    public byte[] encode(byte[] carrier, byte[] payload, long seed) throws IOException {
        //Decode Video to Single Frames
        Video video = new Video(carrier, ffmpegBin);
        VideoDecoder videoDecoder = new VideoDecoder(carrier, video, ffmpegBin, logging);
        //List used to save the single frames decoded from the carrier
        if (logging)
            System.out.println("Decoding Video Frames to images....");
        List<byte[]> imageList = videoDecoder.toPictureByteArray();
        if (logging)
            System.out.println("Video decoded in: " + (System.currentTimeMillis() - startTime) + "ms" + " (" + ((System.currentTimeMillis() - startTime) / 1000) + "s)");

        //TODO: Distribute Payload into all Frames
        if (logging)
            System.out.println("Encoding secret message into images...");
        startTime = System.currentTimeMillis();
        List<byte[]> stegImagesList = encodeUsingHenkAlgo(imageList, payload, seed);
        if (logging)
            System.out.println("All " + stegImagesList.size() + " images encoded in: " + (System.currentTimeMillis() - startTime) + "ms" + " (" + ((System.currentTimeMillis() - startTime) / 1000) + "s)");

        //Re-Encode Images to Video
        VideoEncoder videoEncoder = new VideoEncoder(video, ffmpegBin, logging);
        return videoEncoder.imagesToVideo(stegImagesList, videoDecoder.getPtsList());
    }

    @Override
    public byte[] decode(byte[] steganographicData) throws IOException {
        return decode(steganographicData, this.seed);
    }

    @Override
    public byte[] decode(byte[] steganographicData, long seed) throws IOException {
        Video video = new Video(steganographicData, ffmpegBin);

        //Decode Video Frames to pictures
        VideoDecoder videoDecoder = new VideoDecoder(steganographicData, video, ffmpegBin, logging);
        List<byte[]> imageList = videoDecoder.toPictureByteArray();

        List<byte[]> secretMessageList = decodeUsingHenkAlgo(imageList, seed);

        // TODO: What to do ?
        return secretMessageList.get(0);
    }

    @Override
    public boolean isSteganographicData(byte[] data) throws IOException {
        //List used to save the single frames decoded from the carrier
        List<byte[]> imageList;

        //Decode Video to Single Frames
        Video video = new Video(data, ffmpegBin);
        VideoDecoder videoDecoder = new VideoDecoder(data, video, ffmpegBin, logging);
        imageList = videoDecoder.toPictureByteArray();

        boolean isSteganographicData = true;
        for (byte[] image : imageList) {
            if (!new ImageSteg().isSteganographicData(image))
                isSteganographicData = false;
        }
        return isSteganographicData;
    }

    /**
     * Encodes a given list of byte[] of Pictures using the "Henk-Algo"
     * Each Picture is encoded in a own thread,
     * the max concurrent number of threads is set by maxEncodingThreads (by default 10)
     *
     * @param imageList list of byte[] of Pictures
     * @param payload   payload (secret)
     * @return Encoded list of Pictures
     */
    private List<byte[]> encodeUsingHenkAlgo(List<byte[]> imageList, byte[] payload, long seed) throws IOException {
        if (maxEncodingThreads == 1) {
            List<byte[]> resultList = new ArrayList<>();
            int i = 0;
            for (byte[] bytes : imageList) {
                Steganography steganography = new ImageSteg();
                resultList.add(steganography.encode(bytes, payload, seed));
                if (logging)
                    System.out.println("Encoded Frame (" + i + "/" + imageList.size() + ")");
                i++;
            }
            return resultList;
        } else {
            return multiThreadingCoding(imageList, payload, seed, false);
        }
    }

    private List<byte[]> decodeUsingHenkAlgo(List<byte[]> imageList, long seed) throws IOException {
        if (maxEncodingThreads == 1) {
            List<byte[]> resultList = new ArrayList<>();
            for (byte[] bytes : imageList) {
                Steganography steganography = new ImageSteg();
                resultList.add(steganography.decode(bytes, seed));
            }
            return resultList;
        } else {
            return multiThreadingCoding(imageList, null, seed, true);
        }
    }

    /**
     * Encode or Decode the secret message using multithreading
     *
     * @param imageList list of all the images to encode or decode
     * @param seed      seed used to encode or decode
     * @param decode    should decode?
     * @return byte[] of all images encoded or decoded
     */
    private List<byte[]> multiThreadingCoding(List<byte[]> imageList, byte[] payload, long seed, boolean decode) {
        // Create ExecutorService with fixed amount of threads
        // The pool of thread will execute the given runnable (void return) or callable (return)
        ExecutorService taskExecutor;
        if (!decode) {
            // Encoding
            //taskExecutor = new ThreadPoolExecutor(1, maxDecodingThreads, 5L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            taskExecutor = Executors.newFixedThreadPool(maxEncodingThreads);
        } else {
            //Decoding
            //taskExecutor = new ThreadPoolExecutor(1, maxEncodingThreads, 5L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            taskExecutor = Executors.newFixedThreadPool(maxDecodingThreads);
        }

        //Make list of callable tasks that will be run by threads
        List<Callable<byte[]>> taskList = new ArrayList<>();
        for (int i = 0; i < imageList.size(); i++) {
            int finalI = i;

            //Each thread decodes or encodes a single image and returns result
            if (decode) {
                taskList.add(() -> {
                            //ImageSteg is not threads safe yet, so need to make an instance for each thread
                            ImageSteg steganography = new ImageSteg();
                            return steganography.decode(imageList.get(finalI), seed);
                        }
                );
            } else {
                taskList.add(() -> {
                            //ImageSteg is not threads safe yet, so need to make an instance for each thread
                            ImageSteg steganography = new ImageSteg();
                            return steganography.encode(imageList.get(finalI), payload, seed);
                        }
                );
            }
        }

        if (logging) {
            System.out.println("All Callable tasks initialized" + System.lineSeparator() + "Running " + maxDecodingThreads + " Threads");
        }

        //Execute all tasks
        List<byte[]> resultList = new ArrayList<>();
        List<Future<byte[]>> futureList;         //I'm so ahead that I need a future List :3
        try {
            futureList = taskExecutor.invokeAll(taskList);
            //Wait for all results
            for (Future<byte[]> result : futureList) {
                resultList.add(result.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            taskExecutor.shutdown();
        }

        return resultList;
    }

    /**
     * Set the number of threads used to encode the pictures
     * WARNING very memory expensive might overflow max JVM heap
     * VM option: -Xmx will help
     * By default using 8 Threads
     * To not use multithreading set maxEncodingThreads to 1
     *
     * @param maxEncodingThreads max number of Threads used to encode
     */
    public void setMaxEncodingThreads(int maxEncodingThreads) {
        this.maxEncodingThreads = maxEncodingThreads;
    }

    /**
     * Set the number of threads used to decode the pictures
     * WARNING very memory expensive might overflow max JVM heap
     * VM option: -Xmx will help
     * By default using 8 Threads
     * To not use multithreading set maxDecodingThreads to 1
     *
     * @param maxDecodingThreads max number of Threads used to decode
     */
    public void setMaxDecodingThreads(int maxDecodingThreads) {
        this.maxDecodingThreads = maxDecodingThreads;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }
}