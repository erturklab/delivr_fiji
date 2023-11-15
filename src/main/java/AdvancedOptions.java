import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.imagej.ops.OpService;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class AdvancedOptions extends JDialog {

	@Parameter
	private OpService ops;

	@Parameter
	private LogService log;

	@Parameter
	private StatusService status;

	@Parameter
	private CommandService cmd;

	@Parameter
	private ThreadService thread;

	@Parameter
	private UIService ui;

	private ObjectNode rootNode;
	
	private HashMap<String, JTextField> valueMap;
	
	public AdvancedOptions(final Context ctx, ObjectNode rootNode) {
		ctx.inject(this);
		setBounds(100, 100, 700, 400);
		
		this.rootNode = rootNode;
		this.valueMap = new HashMap<String, JTextField>();
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		class SaveAndQuitAction extends AbstractAction{
			private ObjectNode rootNode;
			private HashMap<String, JTextField> valueMap;
			
			public SaveAndQuitAction(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
				this.rootNode = rootNode;
				this.valueMap = valueMap;
			}

			@Override
			public void actionPerformed(ActionEvent arg0) {
				valueMapToJson(this.valueMap, this.rootNode);
				dispose();				
			}
			
		}
		JButton saveAndQuitButton = new JButton("Save and Return");
		saveAndQuitButton.addActionListener(new SaveAndQuitAction(rootNode, valueMap));
		getContentPane().add(saveAndQuitButton, BorderLayout.SOUTH);
		 
		JPanel tabMasking = createMaskingPanel(rootNode, valueMap);
		JPanel tabInference = createInferencePanel(rootNode, valueMap);
		JPanel tabPostprocessing = createPostprocessingPanel(rootNode, valueMap);
		JPanel tabAtlasAlignment = createAtlasAlignmentPanel(rootNode, valueMap);
		JPanel tabRegionAssignment = createRegionAssignmentPanel(rootNode, valueMap);
		JPanel tabVisualization = createVisualizationPanel(rootNode, valueMap);
        // Add the tabs to the tabbed pane
        tabbedPane.addTab("Masking", tabMasking);
        tabbedPane.addTab("Inference", tabInference);
        tabbedPane.addTab("Postprocessing", tabPostprocessing);
        tabbedPane.addTab("Atlas Alignment", tabAtlasAlignment);
        tabbedPane.addTab("Region Assignment", tabRegionAssignment);
        tabbedPane.addTab("Visualization", tabVisualization);
	}
	

	private void addLabelAndText(JPanel panel, HashMap<String, JTextField> valueMap, ObjectNode rootNode, String label, String valueMapKey, int gridx, int gridy) {
		JLabel lbl = new JLabel(label);
		lbl.setHorizontalAlignment(JTextField.LEFT);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(0, 0, 5, 5);
		gbc_lbl.gridx = gridx;
		gbc_lbl.gridy = gridy;
		gbc_lbl.weightx = 0.5;
		panel.add(lbl, gbc_lbl);
		
		String[] valueMapKeyArray = valueMapKey.split("\\+");
		String parent = valueMapKeyArray[0];
		String child = valueMapKeyArray[1];
				
		String text_input = ((ObjectNode) rootNode.get(parent)).get(child).asText();
		JTextField txt = new JTextField(text_input);
		GridBagConstraints gbc_txt = new GridBagConstraints();
		gbc_txt.insets = new Insets(0, 0, 5, 5);
		gbc_txt.gridx = gridx + 1;
		gbc_txt.gridy = gridy;
		//gbc_txt.gridwidth = 3;
		gbc_txt.weightx = 1;
		panel.add(txt, gbc_txt);
		valueMap.put(parent + "+" + child, txt);
	}
	
	private JPanel createMaskingPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
        // create panel with GridBagLayout
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		
        JPanel panel = new JPanel(gridBagLayout);
        

        addLabelAndText(panel, valueMap, rootNode, "Ilastik Path", "mask_detection+ilastik_location", 0, 0);
        addLabelAndText(panel, valueMap, rootNode, "Output Location", "mask_detection+output_location", 0, 2);

        
        return panel;
    }

	private JPanel createInferencePanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
        // create panel with GridBagLayout
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		
        JPanel panel = new JPanel(gridBagLayout);

        addLabelAndText(panel, valueMap, rootNode, "Input Location", "blob_detection+input_location", 0, 0);
        
        addLabelAndText(panel, valueMap, rootNode, "Model Location", "blob_detection+model_location", 0, 2);
        
        addLabelAndText(panel, valueMap, rootNode, "Output Location", "blob_detection+output_location", 0, 4);

        
        return panel;
    }
	
	private JPanel createPostprocessingPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
        // create panel with GridBagLayout
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		
        JPanel panel = new JPanel(gridBagLayout);
        
        addLabelAndText(panel, valueMap, rootNode, "Input Location", "postprocessing+input_location", 0, 0);
        
        addLabelAndText(panel, valueMap, rootNode, "Output Location", "postprocessing+output_location", 0, 2);
        
        return panel;
    }

	private JPanel createAtlasAlignmentPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
		// create panel with GridBagLayout
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		
	    JPanel panel = new JPanel(gridBagLayout);
	    
	    addLabelAndText(panel, valueMap, rootNode, "Input Location", "atlas_alignment+input_location", 0, 0);
	    
	    addLabelAndText(panel, valueMap, rootNode, "Output Location", "atlas_alignment+output_location", 0, 2);

	    addLabelAndText(panel, valueMap, rootNode, "mBrainAligner Location", "atlas_alignment+mBrainAligner_location", 0, 4);

	    addLabelAndText(panel, valueMap, rootNode, "Collection Folder", "atlas_alignment+collection_folder", 0, 6);
	    
	    //TODO Make this a checkbox instead
	    addLabelAndText(panel, valueMap, rootNode, "Parallel Processing", "atlas_alignment+parallel_processing", 0, 8);
	    
	    return panel;
    }

	private JPanel createRegionAssignmentPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
		// create panel with GridBagLayout
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		
        JPanel panel = new JPanel(gridBagLayout);
        
        addLabelAndText(panel, valueMap, rootNode, "Input Location", "region_assignment+input_location", 0, 0);

        addLabelAndText(panel, valueMap, rootNode, "CCF3 Atlas File", "region_assignment+CCF3_atlasfile", 0, 2);
        
        addLabelAndText(panel, valueMap, rootNode, "CCF3 Ontology", "region_assignment+CCF3_ontology", 0, 4);
        
        addLabelAndText(panel, valueMap, rootNode, "Output Location", "region_assignment+output_location", 0, 6);
        
        return panel;
	}

	private JPanel createVisualizationPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
		// create panel with GridBagLayout
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		
        JPanel panel = new JPanel(gridBagLayout);
        
        addLabelAndText(panel, valueMap, rootNode, "Input CSV Location", "visualization+input_csv_location", 0, 0);

        addLabelAndText(panel, valueMap, rootNode, "Input Size Location", "visualization+input_size_location", 0, 2);
        
        addLabelAndText(panel, valueMap, rootNode, "Input Prediction Location", "visualization+input_prediction_location", 0, 4);
        
        addLabelAndText(panel, valueMap, rootNode, "Cache Location", "visualization+cache_location", 0, 6);

        addLabelAndText(panel, valueMap, rootNode, "Output Location", "visualization+output_location", 0, 8);
        
        return panel;
    }

	
	public void valueMapToJson(HashMap<String, JTextField> valueMap, ObjectNode rootNode) {
		for(String key : valueMap.keySet()) {
			String value = valueMap.get(key).getText();
			String[] location = key.split("\\+");
			if(location.length > 2) {
				//TODO Da mussma nochamal rann
			}else {
				String parent_location = location[0];
				String child_location = location[1];
				ObjectNode parent_node = (ObjectNode) rootNode.get(parent_location);
				parent_node.put(child_location, value);
			}
		}
		
	}
}
