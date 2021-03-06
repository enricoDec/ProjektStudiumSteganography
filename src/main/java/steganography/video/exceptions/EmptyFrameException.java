package steganography.video.exceptions;

import steganography.exceptions.UnknownStegFormatException;

/**
 * @author : Enrico Gamil Toros
 * Project name : ProjektSteganography
 * @version : 1.0
 * @since : 05.01.21
 * <p>
 * Thrown if if the Frame is Empty
 **/
public class EmptyFrameException extends UnknownStegFormatException {

    public EmptyFrameException() {
        super();
    }

    public EmptyFrameException(String message) {
        super(message);
    }
}
