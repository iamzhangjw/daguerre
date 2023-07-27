package io.ooze.daguerre;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 压缩图片 test case
 *
 * @author zhangjw
 * @date 2022/12/12 0012 11:26
 */
public class ResizeTest {
    public static void main(String[] args) throws IOException {
        File file = new File("E:\\downloads\\sh.png");
        BufferedImage thumbnail = Scalr.resize(ImageIO.read(file), Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, 688, 430, Scalr.OP_ANTIALIAS);
        FileOutputStream os = new FileOutputStream(new File("E:\\downloads\\sh_688x430.jpg"));
        ImageIO.write(thumbnail, "jpg", os);
    }
}
