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

package socialmediasteganography;


import apis.MediaType;
import apis.SocialMedia;
import apis.models.Token;
import apis.imgur.Imgur;
import apis.reddit.Reddit;
import apis.tumblr.Tumblr;
import apis.utils.BlobConverterImpl;
import persistence.JSONPersistentManager;
import persistence.PersistenceDummy;
import steganography.image.ImageSteg;
import steganography.util.ByteArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mario Teklic
 */


public class App {
    public static void main(String[] args) throws IOException {
        //JSONPersistentManager.getInstance().setJsonPersistentHelper(new PersistenceDummy());

        /**
         * Setup Tumblr Client
         */
        SocialMedia socialMedia = new Tumblr();
        socialMedia.setBlogName("mariofenzl");

        /**
         * encode and post png on tumblr
         */
        /*socialMedia.setMediaType(MediaType.PNG);
        SocialMediaSteganography sms = new SocialMediaSteganographyImpl(new ImageSteg());
        //Carrier, Payload
        byte[] bytes = BlobConverterImpl.downloadToByte("https://compress-or-die.com/public/understanding-png/assets/lena-dirty-transparency-corrected-cv.png");
        String payload = "hallo";
        //Encode and Post
        sms.encodeAndPost(socialMedia, bytes, payload.getBytes());*/


        /**
         * encode and post gif on Tumblr
         */
        socialMedia.setMediaType(MediaType.GIF);
        SocialMediaSteganography sms = new SocialMediaSteganographyImpl(new ImageSteg());
        //Carrier, Payload
        File gifFile = new File("src/main/java/apis/tumblr/medias/simpson.gif");
        byte[] gifByte = ByteArrayUtils.read(gifFile);
        String payload = "hallo";
        //Encode and Post
        sms.encodeAndPost(socialMedia, gifByte, payload.getBytes());


        /**
         * encode and post mp3 on Tumblr
         */
        /*socialMedia.setMediaType(MediaType.MP3);
        SocialMediaSteganography sms = new SocialMediaSteganographyImpl(new MP3Steganography());
        //Carrier, Payload
        File gifFile = new File("src/main/java/apis/tumblr/medias/audiotest.mp3");
        BufferedImage bufferedImage = ImageIO.read(gifFile);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "gif", byteArrayOutputStream);
        byte[] gifData = byteArrayOutputStream.toByteArray();
        String payload = "hallo";
        //Encode and Post
        sms.encodeAndPost(socialMedia, gifData, payload.getBytes());
*/

        /**
         * encode and post mp4 on Tumblr
         */
        /*socialMedia.setMediaType(MediaType.MP4);
        SocialMediaSteganography sms = new SocialMediaSteganographyImpl(new VideoSteg());
        //Carrier, Payload
        File videoFile = new File("src/main/java/apis/tumblr/medias/test.mp4");
        FileInputStream fileInputStream = null;
        fileInputStream = new FileInputStream(videoFile);
        byte[] videoData = IOUtils.toByteArray(fileInputStream);
        String payload = "hallo";
        //Encode and Post
        sms.encodeAndPost(socialMedia, videoData, payload.getBytes());*/

        /**
         * test to decode uploaded picture
         */





        //Search in social media for pictures and try to decode
        //List<byte[]> results = sms.searchForHiddenMessages(socialMedia, "test");
        //socialMedia.subscribeToKeyword("nature");



        //Setup
        /*SocialMedia socialMedia = new Imgur();
        socialMedia.setToken(new Token("e1e5f0ff327cb45aa5440f456297317cbcb77859", 100));
        SocialMediaSteganography sms = new SocialMediaSteganographyImpl(new ImageSteg());*/

        //Carrier, Payload
       /* byte[] byts = BlobConverterImpl.downloadToByte("https://compress-or-die.com/public/understanding-png/assets/lena-dirty-transparency-corrected-cv.png");
        String payload = "This is a secret message which was built by ProjectStudiumSteganography";*/

        //Encode and Post
        //sms.encodeAndPost(socialMedia, byts, payload.getBytes());

        //Search in social media for pictures and try to decode
        //List<byte[]> results = sms.searchForHiddenMessages(socialMedia, "test");
        //socialMedia.subscribeToKeyword("nature");

/*
        //Auswertung als Strings
        List<String> messages = new ArrayList<>();
        for (byte[] b : results) {
            if (b.length > 0) {
                String msg = new String(b);
                messages.add(msg);
                System.out.println(msg);
            } else {
                System.out.println("No bytes for message type found");
            }
        }
*/

        /*
         //Zum testen, ob dieses Bild korrekt runtergeladen wurde.

        InputStream is = new ByteArrayInputStream(byts);
        BufferedImage bImg = ImageIO.read(is);
        ImageIO.write(bImg, "png", new File("myfile.png"));
        */

    /*
        //Upload on Imgur

         SocialMedia imgur = new Imgur();
         imgur.setToken(new Token("0d5ce353c61cbb597df3497669e7c4e85f072e2a", 123));
         byte[] byts = BlobConverterImpl.downloadToByte("https://i.imgur.com/SJZyZQ1.png");
         imgur.postToSocialNetwork(byts, "testword");
*/

        
/*
        //Upload on Reddit

        SocialMedia reddit = new Reddit();
        List<byte[]> resultList = reddit.getRecentMediaForKeyword("nature");
        byte[] tmpImage = resultList.get(resultList.size()-1);
        reddit.setToken(new Token("668533834712-q_itL79dEvxBQgWCRsktUkpTBScVQw", 123124));
        reddit.postToSocialNetwork(tmpImage, "test");

*/

        /**
         //Subscribe Imgur & Reddit

         SocialMedia imgur = new Imgur();
         imgur.changeSubscriptionInterval(TimeUnit.MINUTES, 5);
         imgur.subscribeToKeyword("test");

         SocialMedia reddit = new Reddit();
         reddit.subscribeToKeyword("nature");
         */
    }
}
