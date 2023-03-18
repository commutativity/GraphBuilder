package graph;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SVGtoPNG {
    String svgDirAndName;
    String pngDirAndName;

    SVGtoPNG(String svgDirAndName, String pngDirAndName) throws Exception {
        this.svgDirAndName =svgDirAndName;
        this.pngDirAndName =pngDirAndName;
        createImage();
    }

    public void createImage() throws Exception {
        String svg_URI_input = new File(String.format("%s", svgDirAndName)).toURI().toString();
        TranscoderInput input_svg_image = new TranscoderInput(svg_URI_input);

        // define OutputStream to PNG Image and attach to TranscoderOutput
        OutputStream pngOutStream = Files.newOutputStream(Paths.get(pngDirAndName));
        TranscoderOutput output_png_image = new TranscoderOutput(pngOutStream);

        // create PNGTranscoder and define hints if required
        PNGTranscoder my_converter = new PNGTranscoder();

        // convert and Write output
//        System.out.println("It will print");
        my_converter.transcode(input_svg_image, output_png_image);
//        System.out.println("It will not print");
        pngOutStream.flush();
        pngOutStream.close();
    }
}