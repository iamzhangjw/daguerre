package pers.zjw.daguerre.utils;

import pers.zjw.daguerre.oss.domain.ThumbImageObjectName;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacv.*;
import org.imgscalr.Scalr;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

/**
 * thumbnail tailor
 *
 * @author zhangjw
 * @date 2022/8/13 0013 14:06
 */
@Slf4j
public class ThumbnailTailor {

    public static InputStream videoCover(java.io.File file, String filename) throws IOException {
        // https://stackoverflow.com/questions/37163978/how-to-get-a-thumbnail-of-an-uploaded-video-file
        long start = System.currentTimeMillis();
        int i;
        BufferedImage firstFrameImage;
        try (FrameGrabber grabber = new FFmpegFrameGrabber(file);
             FrameConverter<BufferedImage> converter = new Java2DFrameConverter()) {
            String ext = FilenameUtils.getExtension(filename);
            grabber.setFormat((StringUtils.hasText(ext) ? ext : "mp4"));
            grabber.start();
            Frame frame = grabber.grabFrame();
            i = 0;
            while (Objects.isNull(frame) || Objects.isNull(frame.image)) {
                frame = grabber.grabFrame();
                i++;
            }
            firstFrameImage = converter.convert(frame);
            grabber.stop();
        }

        InputStream is = imageThumbnail(firstFrameImage, filename);
        log.debug("file {} grab {} frame generate cover image cost {}ms.",
                filename, i, System.currentTimeMillis() - start);
        return is;
    }

    public static boolean needResize(long length) {
        // if image file greater than 128k
        return length >= 131_072;
    }

    public static InputStream imageThumbnail(java.io.File file, String filename) throws IOException {
        return imageThumbnail(ImageIO.read(file), filename);
    }

    public static InputStream imageThumbnail(BufferedImage img, String filename) throws IOException {
        long start = System.currentTimeMillis();
        // 压缩模式为宽度自适应，width = 500，一般大小不超过 20kb
        BufferedImage thumbnail = Scalr.resize(img, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH,
                500, Scalr.OP_ANTIALIAS);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, ThumbImageObjectName.THUMB_FILE_EXTENSION, os);
        log.debug("file {} generate thumbnail image cost {}ms.",
                filename, System.currentTimeMillis() - start);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
