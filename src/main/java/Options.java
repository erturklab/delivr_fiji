import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import DelivrDialog.CheckboxSaveJSONAction;
import ij.IJ;
import net.imagej.ops.OpService;

public class Options extends JDialog {

	private static final long serialVersionUID = -5339283865474467540L;

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
	
	
	public Options(final Context ctx, ObjectNode rootNode) {
		setTitle("Advanced Options");
		setBounds(100, 100, 800, 400);		

		this.valueMap = new HashMap<String, JTextField>();
		
		this.rootNode = rootNode;
		
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
		
		JPanel tabDatasetProperties = createDatasetPanel(rootNode, valueMap);		
		JPanel tabMasking = createMaskingPanel(rootNode, valueMap);
		JPanel tabModels = createModelPanel(rootNode, valueMap);
		JPanel tabTraining = createTrainingPanel(rootNode);
		JPanel tabVisualization = createVisualizationPanel(rootNode);

		tabbedPane.addTab("Dataset Properties", tabDatasetProperties);
		tabbedPane.addTab("Masking Properties", tabMasking);
		tabbedPane.addTab("Model Locations", tabModels);
		tabbedPane.addTab("Trainig, Postprocessing, Alignment", tabTraining);
		tabbedPane.addTab("Visualization", tabVisualization);
		
	}
	
	private JPanel setupPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		
        JPanel panel = new JPanel(gridBagLayout);
        return panel;
	}
	
	private JPanel createDatasetPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
        // create panel with GridBagLayout
		JPanel panel = setupPanel();

        JLabel lblDatasetProperties = new JLabel("Dataset properties");
		GridBagConstraints gbc_lblDatasetProperties = new GridBagConstraints();
		gbc_lblDatasetProperties.insets = new Insets(5, 5, 5, 5);
		gbc_lblDatasetProperties.gridx = 0;
		gbc_lblDatasetProperties.gridy = 0;
		gbc_lblDatasetProperties.gridwidth = 2;
		panel.add(lblDatasetProperties, gbc_lblDatasetProperties);

		addLabelAndTextDataset(panel, rootNode, "  Original X resolution in μm", "original_um_x", valueMap, 0, 1);
		addLabelAndTextDataset(panel, rootNode, "  Original Y resolution in μm", "original_um_y", valueMap, 0, 2);
		addLabelAndTextDataset(panel, rootNode, "  Original Z resolution in μm", "original_um_z", valueMap, 0, 3);
		
		JLabel lblDownSampleProperties = new JLabel("Downsample properties");
		GridBagConstraints gbc_lblDownSampleProperties = new GridBagConstraints();
		gbc_lblDownSampleProperties.insets = new Insets(5, 5, 5, 5);
		gbc_lblDownSampleProperties.gridx = 0;
		gbc_lblDownSampleProperties.gridy = 4;
		gbc_lblDownSampleProperties.gridwidth = 2;
		panel.add(lblDownSampleProperties, gbc_lblDownSampleProperties);

		addLabelAndTextDataset(panel, rootNode, "  Downsample factor X", "downsample_um_x", valueMap, 0, 5);
		addLabelAndTextDataset(panel, rootNode, "  Downsample factor Y", "downsample_um_y", valueMap, 0, 6);
		addLabelAndTextDataset(panel, rootNode, "  Downsample factor Z", "downsample_um_z", valueMap, 0, 7);

        
        return panel;
    }
	
	private JPanel createRegionVisualizationPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
        // create panel with GridBagLayout
		JPanel panel = setupPanel();
        
        //TODO refactor how valueMap works to make it work with Checkboxes

  //      addLabelAndBelowText(panel, rootNode, "Ventricle detection model location", "mask_detection+ilastik_model", valueMap, 0, 0);

//        addLabelAndBelowText(panel, rootNode, "Cell detection model location", "blob_detection+model_location", valueMap, 0, 2);
 
        return panel;
	}
	
	private JPanel createModelPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
        // create panel with GridBagLayout
		JPanel panel = setupPanel();

        addLabelAndBelowText(panel, rootNode, "Ventricle detection model location", "mask_detection+ilastik_model", valueMap, 0, 0);

        addLabelAndBelowText(panel, rootNode, "Cell detection model location", "blob_detection+model_location", valueMap, 0, 2);

        addLabelAndBelowText(panel, rootNode, "CCF3 atlas file location", "region_assignment+CCF3_atlasfile", valueMap, 0, 4);

        addLabelAndBelowText(panel, rootNode, "CCF3 ontology location", "region_assignment+CCF3_ontology", valueMap, 0, 6);
        
        return panel;
	}
	
	private JPanel createTrainingPanel(ObjectNode rootNode) {
        // create panel with GridBagLayout
		JPanel panel = setupPanel();
        
        /*
        JLabel lblDatasetProperties = new JLabel("Cell detection and Postprocessing");
		GridBagConstraints gbc_lblDatasetProperties = new GridBagConstraints();
		gbc_lblDatasetProperties.insets = new Insets(5, 5, 5, 5);
		gbc_lblDatasetProperties.gridx = 0;
		gbc_lblDatasetProperties.gridy = 0;
		gbc_lblDatasetProperties.gridwidth = 2;
		panel.add(lblDatasetProperties, gbc_lblDatasetProperties);
		*/
		
		int running_y = 0;
		
		addLabelAndCheckbox(panel, rootNode, "Test Time Augmentation", "FLAGS","TEST_TIME_AUGMENTATION",  0, running_y++);
		addLabelAndCheckbox(panel, rootNode, "Load full dataset into RAM", "FLAGS","LOAD_ALL_RAM",  0, running_y++);
		addLabelAndCheckbox(panel, rootNode, "Parallel Processing of Atlas Alignment", "atlas_alignment","parallel_processing",  0, running_y++);
        
        
		return panel;
	}
	
	private JPanel createVisualizationPanel(ObjectNode rootNode){
        // create panel with GridBagLayout
		JPanel panel = setupPanel();
  
		int running_y = 0;
		
		addLabelAndCheckbox(panel, rootNode, "Color Region IDs in RGB", "visualization","region_id_rgb",  0, running_y++);
		addLabelAndCheckbox(panel, rootNode, "Color Region IDs in Grayscales", "visualization","region_id_grayvalues",  0, running_y++);
		addLabelAndCheckbox(panel, rootNode, "No Atlas Depthmap", "visualization","no_atlas_depthmap",  0, running_y++);
        
        
		return panel;
	}
	
	private JPanel createMaskingPanel(ObjectNode rootNode, HashMap<String, JTextField> valueMap) {
        // create panel with GridBagLayout
		JPanel panel = setupPanel();

        JTextField textfield = addLabelAndTextGrownup(panel, rootNode, "Simple Threshold Value", "simple_threshold_value", "mask_detection", valueMap, 0, 1);
        textfield.setEnabled(false);
        addLabelAndCheckboxWithAction(panel, rootNode, "Mask with Ilastik", "mask_detection", "mask_with_Ilastik", 0, 0, textfield);
		       
        return panel;
	}
	
	class CheckboxSaveJSONAction extends AbstractAction{
		private static final long serialVersionUID = 992921L;
		private final ObjectNode rootNode;
		private final String parentNode;
		private final String childNode;
	    public CheckboxSaveJSONAction(ObjectNode rootNode, String parentNode, String childNode) {
	        this.rootNode = rootNode;
			this.parentNode = parentNode;
			this.childNode = childNode;
	    }
	 
	    @Override
	    public void actionPerformed(ActionEvent e) {
	        JCheckBox cbLog = (JCheckBox) e.getSource();
	        ((ObjectNode)this.rootNode.get(parentNode)).put(this.childNode,cbLog.isSelected());
	        
	    }
	}
	
	class CheckboxSaveAndEnableJSONAction extends AbstractAction{
		private static final long serialVersionUID = 992921L;
		private final ObjectNode rootNode;
		private final String parentNode;
		private final String childNode;
		private final JComponent component;
	    public CheckboxSaveAndEnableJSONAction(ObjectNode rootNode, String parentNode, String childNode,JComponent component) {
	        this.rootNode = rootNode;
			this.parentNode = parentNode;
			this.childNode = childNode;
	        this.component = component;
	    }
	 
	    @Override
	    public void actionPerformed(ActionEvent e) {
	        JCheckBox cbLog = (JCheckBox) e.getSource();
	        ((ObjectNode)this.rootNode.get(parentNode)).put(this.childNode,cbLog.isSelected());
	        if (cbLog.isSelected()) {
	        	component.setEnabled(false);
	        } else {
	        	component.setEnabled(true);
	        }
	    }
	}
	
	private void addLabelAndTextDataset(Container panel, ObjectNode rootNode, String label, String key, HashMap<String, JTextField> valueMap, int gridx, int gridy) {
		addLabelAndText(panel, rootNode, label, key, "mask_detection", "downsample_steps", valueMap, gridx, gridy);
	}
	
	private JTextField addLabelAndTextGrownup(Container panel, ObjectNode rootNode, String label, String key, String parentNode, HashMap<String, JTextField> valueMap, int gridx, int gridy) {
		//Like the others, but no grandchildren
		JLabel lbl = new JLabel("   " + label);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(5, 5, 5, 5);
		gbc_lbl.gridx = gridx;
		gbc_lbl.gridy = gridy;
		panel.add(lbl, gbc_lbl);
		
		String default_input = ((ObjectNode) rootNode.get(parentNode)).get(key).toString();
		JTextField textfield = new JTextField(default_input);
		GridBagConstraints gbc_textfield= new GridBagConstraints();
		gbc_textfield.insets = new Insets(0, 0, 5, 0);
		gbc_textfield.gridx = gridx + 1;
		gbc_textfield.gridy = gridy;
		panel.add(textfield, gbc_textfield);

		valueMap.put(parentNode + "+" + key, textfield);
		return textfield;
	}
	
	private void addLabelAndText(Container panel, ObjectNode rootNode, String label, String key, String parentNode, String childNode, HashMap<String, JTextField> valueMap, int gridx, int gridy) {
		JLabel lbl = new JLabel("   " + label);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(5, 5, 5, 5);
		gbc_lbl.gridx = gridx;
		gbc_lbl.gridy = gridy;
		panel.add(lbl, gbc_lbl);
		
		String default_input = ((ObjectNode) ((ObjectNode) rootNode.get(parentNode)).get(childNode)).get(key).toString();
		JTextField textfield = new JTextField(default_input);
		GridBagConstraints gbc_textfield= new GridBagConstraints();
		gbc_textfield.insets = new Insets(0, 0, 5, 0);
		gbc_textfield.gridx = gridx + 1;
		gbc_textfield.gridy = gridy;
		panel.add(textfield, gbc_textfield);

		valueMap.put(key, textfield);
	}
	
	private void addLabelAndCheckbox(Container panel, ObjectNode rootNode, String label, String parentNode, String childNode, int gridx, int gridy) {
		JLabel lbl = new JLabel("   " + label);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(5, 5, 5, 5);
		gbc_lbl.gridx = gridx;
		gbc_lbl.gridy = gridy;
		panel.add(lbl, gbc_lbl);

		Boolean bool_val = ((ObjectNode) rootNode.get(parentNode)).get(childNode).asBoolean();
		
		JCheckBox chckbx = new JCheckBox(new CheckboxSaveJSONAction(rootNode, parentNode, childNode));
		//TODO: get boolean val from rootNode
		chckbx.setSelected(bool_val);
		GridBagConstraints gbc_chckbx = new GridBagConstraints();
		gbc_chckbx.insets = new Insets(5, 5, 5, 5);
		gbc_chckbx.gridx = gridx + 1;
		gbc_chckbx.gridy = gridy;
		panel.add(chckbx, gbc_chckbx);
	}
	
	
	
	
	private void addLabelAndCheckboxWithAction(Container panel, ObjectNode rootNode, String label, String parentNode, String childNode, int gridx, int gridy, JComponent relatedComponent) {
		JLabel lbl = new JLabel("   " + label);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(5, 5, 5, 5);
		gbc_lbl.gridx = gridx;
		gbc_lbl.gridy = gridy;
		panel.add(lbl, gbc_lbl);

		Boolean bool_val = ((ObjectNode) rootNode.get(parentNode)).get(childNode).asBoolean();
		
		JCheckBox chckbx = new JCheckBox(new CheckboxSaveAndEnableJSONAction(rootNode, parentNode, childNode, relatedComponent));
		//TODO: get boolean val from rootNode
		chckbx.setSelected(bool_val);
		GridBagConstraints gbc_chckbx = new GridBagConstraints();
		gbc_chckbx.insets = new Insets(5, 5, 5, 5);
		gbc_chckbx.gridx = gridx + 1;
		gbc_chckbx.gridy = gridy;
		panel.add(chckbx, gbc_chckbx);
	}
	
	private void addLabelAndBelowText(Container panel, ObjectNode rootNode, String label, String key, HashMap<String, JTextField> valueMap, int gridx, int gridy) {
		JLabel lbl = new JLabel(" " + label);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(5, 5, 5, 5);
		gbc_lbl.gridx = gridx;
		gbc_lbl.gridy = gridy;
		gbc_lbl.gridwidth = 2;
		gbc_lbl.anchor = GridBagConstraints.WEST;
		panel.add(lbl, gbc_lbl);

		String[] location = key.split("\\+");
		String parent_location = location[0];
		String child_location = location[1];
		String default_input = ((ObjectNode) rootNode.get(parent_location)).get(child_location).toString().replace("\"","");
		JTextField textfield = new JTextField(default_input);
		GridBagConstraints gbc_textfield= new GridBagConstraints();
		gbc_textfield.insets = new Insets(0, 0, 5, 0);
		gbc_textfield.gridx = gridx;
		gbc_textfield.gridy = gridy + 1;
		gbc_textfield.gridwidth = 2;
		gbc_textfield.weightx = 1;
		gbc_textfield.fill = GridBagConstraints.HORIZONTAL;
		//gbc_textfield.anchor = GridBagConstraints.WEST;
		panel.add(textfield, gbc_textfield);

		valueMap.put(key, textfield);
	}
	
	public void valueMapToJson(HashMap<String, JTextField> valueMap, ObjectNode rootNode) {
		for(String key : valueMap.keySet()) {
			String value = valueMap.get(key).getText();
			if(key.contains("+")){
				String[] location = key.split("\\+");
				String parent_location = location[0];
				String child_location = location[1];
				ObjectNode parent_node = (ObjectNode) rootNode.get(parent_location);
				parent_node.put(child_location, value);
			}else{
				DoubleNode doubleNode = new DoubleNode(Double.parseDouble(value));
				ObjectNode parent_node = (ObjectNode) ((ObjectNode) rootNode.get("mask_detection")).get("downsample_steps");
				parent_node.replace(key, doubleNode);
			}
		}
		
	}
}
