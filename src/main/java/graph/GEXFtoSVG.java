package graph;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.gephi.io.importer.api.Container;



public class GEXFtoSVG {

    public void script(String gexfName, String svgName) throws Exception {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        Container container;
        try {
            String string = String.format("src\\main\\resources\\gexf\\%s.gexf", gexfName);
            System.out.printf("Opening: %s%n", string);
            File file = new File(string);
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // import the container into the workspace. The workspace contains the graph model and model
        importController.process(container, new DefaultProcessor(), workspace);

        //Layout for 1 second
        AutoLayout autoLayout = new AutoLayout(1, TimeUnit.SECONDS);
        autoLayout.setGraphModel(graphModel);
        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout
                .createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);
        //True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout
                .createDynamicProperty("forceAtlas.repulsionStrength.name", 500., 0f);
        //500 for the complete period
        autoLayout.addLayout(firstLayout, 0.5f);
        autoLayout.addLayout(secondLayout, 0.5f, new AutoLayout
                .DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        autoLayout.execute();

        // Preview
        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.BLACK));
        model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, 0.1f);
        model.getProperties().putValue(PreviewProperty.SHOW_EDGES, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.DIRECTED, true);
        model.getProperties().putValue(PreviewProperty.ARROW_SIZE, 20.0f);
        model.getProperties().putValue(PreviewProperty.EDGE_CURVED, false);
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, new Font("Arial", Font.PLAIN, 2));

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File(String.format("src\\main\\resources\\svg\\%s.svg", svgName)));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        container.closeLoader();
    }
}