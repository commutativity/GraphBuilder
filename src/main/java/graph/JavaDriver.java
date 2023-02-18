package graph;


public class JavaDriver {

    public void runGEXFtoSVG(String gexfName, String svgName) throws Exception {
        GEXFtoSVG graph = new GEXFtoSVG();
        graph.script(gexfName, svgName);
    }

    public void runSVGtoPNG(String svgName, String pngName) throws Exception {
        new SVGtoPNG(svgName, pngName);
    }
}
