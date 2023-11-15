import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ij.IJ;
import net.imagej.ops.OpService;

public class DelivrDialog extends JDialog {


	private static final long serialVersionUID = -8193447523141761456L;

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
	
	
	
	private JTextField textFieldRawData;
	private JTextField textFieldOutputLocation;
	private JCheckBox chckbxMaskDetection;
	private JButton btnStart;
	private JLabel lblPerformOperation;
	private JCheckBox chckbxCellDetection;
	private JCheckBox chckbxPostprocessing;
	private JCheckBox chckbxAtlasAlignment;
	private JCheckBox chckbxRegionAssignment;
	private JCheckBox chckbxVisualization;
	private JLabel lblSaveOutput;
	private JCheckBox chckbxSaveMask;
	private JLabel lblMaskDetection;
	private JLabel lblCellDetection;
	private JLabel lblPostprocessing;
	private JLabel lblAtlasAlignment;
	private JLabel lblRegionAssignment;
	private JLabel lblVisualization;
	private JCheckBox chckbxSaveCellDetection;
	private JCheckBox chckbxSavePostprocessing;
	private JCheckBox chckbxSaveAtlasAlignment;
	private JLabel lblTestTimeAugmentation;
	private JCheckBox chckbxTestTimeAugmentation;
	private JButton btnAdvanced;
	
	private ObjectNode rootNode;

	/**
	 * Create the application.
	 */
	public DelivrDialog(final Context ctx) {
		ctx.inject(this);
		this.rootNode = initializeJSON();
		setBounds(100, 100, 550, 380);
		setTitle("DELIVR Pipeline");
		
		int running_y = 0;
		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		//getContentPane().setLayout(gridBagLayout);
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblDELIVRTitle = new JLabel(" ");
		GridBagConstraints gbc_lblDELIVRTitle = new GridBagConstraints();
		gbc_lblDELIVRTitle.insets = new Insets(0, 0, 5, 5);
		//gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblDELIVRTitle.gridx = 0;
		gbc_lblDELIVRTitle.gridy = running_y++;
		gbc_lblDELIVRTitle.gridwidth = 2;
		getContentPane().add(lblDELIVRTitle, gbc_lblDELIVRTitle);
		
		//TODO: Daraus mal so Filepicker machen
		JLabel lblRawDataLocation = new JLabel("Raw Data location");
		GridBagConstraints gbc_lblRawDataLocation = new GridBagConstraints();
		gbc_lblRawDataLocation.anchor = GridBagConstraints.EAST;
		gbc_lblRawDataLocation.insets = new Insets(0, 0, 5, 5);
		//gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblRawDataLocation.gridx = 0;
		gbc_lblRawDataLocation.gridy = running_y;
		getContentPane().add(lblRawDataLocation, gbc_lblRawDataLocation);
		
		textFieldRawData = new JTextField();
		GridBagConstraints gbc_textFieldRawData = new GridBagConstraints();
		gbc_textFieldRawData.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldRawData.gridx = 1;
		gbc_textFieldRawData.gridy = 1;
		gbc_textFieldRawData.weightx = running_y++;
		gbc_textFieldRawData.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(textFieldRawData, gbc_textFieldRawData);
		textFieldRawData.setColumns(10);
		
		JLabel lblOutputLocation = new JLabel("Output location");
		GridBagConstraints gbc_lblOutputLocation = new GridBagConstraints();
		gbc_lblOutputLocation.anchor = GridBagConstraints.EAST;
		gbc_lblOutputLocation.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputLocation.gridx = 0;
		gbc_lblOutputLocation.gridy = running_y;
		getContentPane().add(lblOutputLocation, gbc_lblOutputLocation);
		
		textFieldOutputLocation = new JTextField();
		GridBagConstraints gbc_textFieldOutputLocation = new GridBagConstraints();
		gbc_textFieldOutputLocation.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldOutputLocation.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldOutputLocation.gridx = 1;
		gbc_textFieldOutputLocation.gridy = running_y++;
		getContentPane().add(textFieldOutputLocation, gbc_textFieldOutputLocation);
		textFieldOutputLocation.setColumns(10);
		
		
		lblPerformOperation = new JLabel("Perform Operation:");
		GridBagConstraints gbc_lblPerformOperation = new GridBagConstraints();
		gbc_lblPerformOperation.insets = new Insets(0, 0, 5, 5);
		gbc_lblPerformOperation.gridx = 1;
		gbc_lblPerformOperation.gridy = running_y;
		getContentPane().add(lblPerformOperation, gbc_lblPerformOperation);
		
		lblSaveOutput = new JLabel("Save Output  ");
		GridBagConstraints gbc_lblSaveOutput = new GridBagConstraints();
		gbc_lblSaveOutput.insets = new Insets(0, 0, 5, 0);
		gbc_lblSaveOutput.gridx = 2;
		gbc_lblSaveOutput.gridy = running_y++;
		getContentPane().add(lblSaveOutput, gbc_lblSaveOutput);
		
		//Mask Detection
		addLabelAndText(getContentPane(), rootNode, "Mask Detection", "MASK_DOWNSAMPLE", "SAVE_MASK_OUTPUT", 0, running_y++);

		//addLabelAndTextSmall(getContentPane(), rootNode, "mask_detection", "Mask with Ilastik", "mask_with_Ilastik", 0, running_y++);

		//Cell Detection
		addLabelAndText(getContentPane(), rootNode, "Cell Detection", "BLOB_DETECTION", "SAVE_NETWORK_OUTPUT", 0, running_y++);
		
		//Postprocessing
		addLabelAndText(getContentPane(), rootNode, "Postprocessing", "POSTPROCESSING", "SAVE_POSTPROCESSING_OUTPUT", 0, running_y++);
		
		//Atlas Alignment
		addLabelAndText(getContentPane(), rootNode, "Atlas Alignment", "ATLAS_ALIGNMENT", "SAVE_ATLAS_OUTPUT", 0, running_y++);

		//addLabelAndTextSmall(getContentPane(), rootNode, "atlas_alignment", "Parallel process masks", "parallel_processing", 0, running_y++);
		
		//Region Assignment
		addLabelAndTextSmallFlags(getContentPane(), rootNode, "Region Assignment", "REGION_ASSIGNMENT", 0, running_y++);
		
		//Visualization
		addLabelAndTextSmallFlags(getContentPane(), rootNode, "Visualization", "VISUALIZATION", 0, running_y++);

		//addLabelAndTextSmall(getContentPane(), rootNode, "visualization", "Visualize Region IDs by RGB", "region_id_rgb", 0, running_y++);

		//addLabelAndTextSmall(getContentPane(), rootNode, "visualization", "Visualize Region IDs by Grayvalues", "region_id_grayvalues", 0, running_y++);

		//addLabelAndTextSmall(getContentPane(), rootNode, "visualization", "Atlas Depthmap", "no_atlas_depthmap", 0, running_y++);
		
		//Test Time Augmentation
		// addLabelAndTextSmallFlags(getContentPane(), rootNode, "Test Time Augmentation", "TEST_TIME_AUGMENTATION", 0, running_y++);
		
		// Process everything in RAM
		// addLabelAndTextSmallFlags(getContentPane(), rootNode, "Load everything into RAM", "LOAD_ALL_RAM", 0, running_y++);
						
		class ButtonAction extends AbstractAction {
			private static final long serialVersionUID = -3047248626537144126L;
			private final JTextField raw_location_text;
			private final JTextField output_location_text;
			private final JButton sender;
			private final ObjectNode rootNode;
			private final Context ctx;
			
			
			public ButtonAction(JTextField raw, JTextField output, JButton sender, ObjectNode rootNode, Context ctx) {
				this.raw_location_text = raw;
				this.output_location_text = output;
				this.sender = sender;
				this.rootNode = rootNode;
				this.ctx = ctx;
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				sender.setEnabled(false);

				
				String raw_attached_path = this.raw_location_text.getText();
				String output_attached_path = this.output_location_text.getText();
				
				//Add path limiter if neccessary
				if(raw_attached_path.substring(raw_attached_path.length() - 1) != "/") {
					raw_attached_path += "/";
				}
				
				if(output_attached_path.substring(output_attached_path.length() - 1) != "/") {
					output_attached_path += "/";
				}
				
								
				//IJ.log(resultsring);
				ObjectMapper objectMapper = new ObjectMapper();
				String prettyJson = "";
				try {
					prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
				} catch (JsonProcessingException e1) {
					e1.printStackTrace();
				}
				IJ.log("Loaded following JSON:");
				IJ.log(prettyJson);
				
				String command = "";
				//URL config_url = this.getClass().getResource("/config.json");
				
				ObjectMapper mapper = new ObjectMapper();
				ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
				String config_location = output_attached_path.replaceAll("\"", "") + "config.json";
				IJ.log(config_location);
				try {
					//TODO check if raw location ends with \
					writer.writeValue(new File(config_location), this.rootNode);
				} catch (JsonGenerationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (JsonMappingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				

				if(System.getProperty("os.name").contains("Linux")) {
					command = "docker run --rm -i --runtime=nvidia -v " + sanitizePath(raw_attached_path, true) +
							":/data/raw/ -v " + sanitizePath(output_attached_path, true) + ":/data/output delivr:12 python3 __main__.py /data/output/config.json";
				}else {
					//TODO: Replace / with \ in windows
					command = "powershell.exe docker run --rm -i --gpus=all " + 
							"--mount type=bind,src=\"" + sanitizePath(raw_attached_path, false) +
							"\",target=/data/raw/" + 
							" --mount type=bind,src=\"" + sanitizePath(output_attached_path, false) + 
							"\",target=/data/output delivr:12 " +
							"python3 __main__.py /data/output/config.json";
				}
				//TODO: Mockup command
				IJ.log("Running " + command);
				//command = "sh /home/ramial-maskari/Downloads/bash.sh";
				//command = "ping -c 100 8.8.8.8";
				String regionPath = ((ObjectNode) rootNode.get("region_assignment")).get("output_location").toString();
				String outputPath = this.output_location_text.getText().replace("\"", "");
				IJ.log(outputPath);
				if(outputPath.substring(outputPath.length() - 1).equals("/")) {
					regionPath = outputPath + regionPath.split("/")[3] + "/" + regionPath.split("/")[4];
				}else {
					regionPath = outputPath + "/" + regionPath.split("/")[3] + "/" + regionPath.split("/")[4];
				}
				IJ.log(outputPath);

				boolean regionAssignment = ((ObjectNode) rootNode.get("FLAGS")).get("REGION_ASSIGNMENT").asBoolean();
				IJ.log(Boolean.toString(regionAssignment));
				DelivrProgress progress = new DelivrProgress(ctx, command, sender, regionAssignment, regionPath);
				progress.setVisible(true);
		    }
			private String sanitizePath(String path, Boolean linux) {
				path.replaceAll("\"", "");
				path.replaceAll("'", "");
				path.replaceAll("\n", "");
				path.replaceAll("\r", "");
				if(!linux) {
					path.replaceAll("//","\\");
				}
				return path;
			};
		}
		
		btnStart = new JButton("Start");
		btnStart.setIcon(new ImageIcon(DelivrDialog.class.getResource("/microscope.gif")));
		btnStart.addActionListener(new ButtonAction(textFieldRawData, textFieldOutputLocation, btnStart, rootNode, ctx));
		
		
		/* This button should open up another JFrame in which the user can select:
		 * - Resolutions for Imaging
		 * - Models for masking, processing, alignment etc
		 * - Individual paths for input/output 
		 */
		class OpenAdvancedOptionsAction extends AbstractAction{
			private static final long serialVersionUID = 408832199792821906L;
			private final Context ctx;
			private final ObjectNode rootNode;
			public OpenAdvancedOptionsAction(Context ctx, ObjectNode rootNode) {
				this.ctx = ctx;
				this.rootNode = rootNode;
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//AdvancedOptions ao = new AdvancedOptions(ctx, rootNode);
				Options ao = new Options(ctx, rootNode);
				ao.setVisible(true);
			}
		}
		btnAdvanced = new JButton("Advanced Options");
		btnAdvanced.setEnabled(true);
		btnAdvanced.setIcon(new ImageIcon(DelivrDialog.class.getResource("/META-INF/jruby.home/lib/ruby/stdlib/rdoc/generator/template/darkfish/images/wrench.png")));
		GridBagConstraints gbc_btnAdvanced = new GridBagConstraints();
		gbc_btnAdvanced.insets = new Insets(0, 0, 0, 5);
		gbc_btnAdvanced.gridx = 0;
		gbc_btnAdvanced.gridy = running_y;
		getContentPane().add(btnAdvanced, gbc_btnAdvanced);
		btnAdvanced.addActionListener(new OpenAdvancedOptionsAction(ctx, this.rootNode));
		
		

		
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.insets = new Insets(0, 0, 0, 5);
		gbc_btnStart.gridx = 1;
		gbc_btnStart.gridy = running_y;
		getContentPane().add(btnStart, gbc_btnStart);
		

	}
	
	class CheckboxJSONAction extends AbstractAction {
		private static final long serialVersionUID = -856491804336454639L;
		private final JCheckBox cbx;
		private final ObjectNode rootNode;
		private final String name;
		private final String associated_name;
	    public CheckboxJSONAction(ObjectNode rootNode, String name, JCheckBox cbx, String associated_name) {
	        this.cbx = cbx;
	        this.rootNode = rootNode;
			this.name = name;
			this.associated_name = associated_name;
	    }
	 
	    @Override
	    public void actionPerformed(ActionEvent e) {
	        JCheckBox cbLog = (JCheckBox) e.getSource();
	        ((ObjectNode)this.rootNode.get("FLAGS")).put(this.name,cbLog.isSelected());
	        if (cbLog.isSelected()) {
	            cbx.setEnabled(true);
	        } else {
		        ((ObjectNode)this.rootNode.get("FLAGS")).put(this.associated_name,cbLog.isSelected());
	            cbx.setEnabled(false);
	            cbx.setSelected(false);
	        }
	    }
	}
	
	class CheckboxSaveJSONAction extends AbstractAction{
		private static final long serialVersionUID = 992921L;
		private final ObjectNode rootNode;
		private final String name;
	    public CheckboxSaveJSONAction(ObjectNode rootNode, String name) {
	        this.rootNode = rootNode;
			this.name = name;
	    }
	 
	    @Override
	    public void actionPerformed(ActionEvent e) {
	        JCheckBox cbLog = (JCheckBox) e.getSource();
	        ((ObjectNode)this.rootNode.get("FLAGS")).put(this.name,cbLog.isSelected());
	    }
	}
	
	private void addLabelAndText(Container panel, ObjectNode rootNode, String label, String valueMapKey, String saveKey, int gridx, int gridy) {
		
		JLabel lbl = new JLabel(label);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(0, 0, 5, 5);
		gbc_lbl.gridx = gridx;
		gbc_lbl.gridy = gridy;
		panel.add(lbl, gbc_lbl);

		Boolean bool_save = ((ObjectNode) rootNode.get("FLAGS")).get(saveKey).asBoolean();
		Boolean bool_val = ((ObjectNode) rootNode.get("FLAGS")).get(valueMapKey).asBoolean();
		
		JCheckBox chckbxSave = new JCheckBox(new CheckboxSaveJSONAction(rootNode, saveKey));
		//TODO: get boolean val from rootNode
		chckbxSave.setSelected(bool_save);
		GridBagConstraints gbc_chckbxSave= new GridBagConstraints();
		gbc_chckbxSave.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxSave.gridx = gridx + 2;
		gbc_chckbxSave.gridy = gridy;
		panel.add(chckbxSave, gbc_chckbxSave);

		
		JCheckBox chckbx = new JCheckBox(new CheckboxJSONAction(rootNode, valueMapKey, chckbxSave, saveKey));
		//TODO: get boolean val from rootNode
		chckbx.setSelected(bool_val);
		GridBagConstraints gbc_chckbx = new GridBagConstraints();
		gbc_chckbx.insets = new Insets(0, 0, 5, 5);
		gbc_chckbx.gridx = gridx + 1;
		gbc_chckbx.gridy = gridy;
		panel.add(chckbx, gbc_chckbx);
		
	}
	
	private void addLabelAndTextSmall(Container panel, ObjectNode rootNode, String parentNode, String label, String valueMapKey, int gridx, int gridy) {
		
		JLabel lbl = new JLabel(label);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(0, 0, 5, 5);
		gbc_lbl.gridx = gridx;
		gbc_lbl.gridy = gridy;
		panel.add(lbl, gbc_lbl);

		Boolean bool_val = ((ObjectNode) rootNode.get(parentNode)).get(valueMapKey).asBoolean();
		
		JCheckBox chckbx = new JCheckBox(new CheckboxSaveJSONAction(rootNode, valueMapKey));
		//TODO: get boolean val from rootNode
		chckbx.setSelected(bool_val);
		GridBagConstraints gbc_chckbx = new GridBagConstraints();
		gbc_chckbx.insets = new Insets(0, 0, 5, 5);
		gbc_chckbx.gridx = gridx + 1;
		gbc_chckbx.gridy = gridy;
		panel.add(chckbx, gbc_chckbx);
		
	}
	
	private void addLabelAndTextSmallFlags(Container panel, ObjectNode rootNode, String label, String valueMapKey, int gridx, int gridy) {
		addLabelAndTextSmall(panel, rootNode, "FLAGS", label, valueMapKey, gridx, gridy);		
	}
	
	private ObjectNode initializeJSON() {
		testResource();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
		try {
			InputStream inputStream = getClass().getResourceAsStream("/config.json");
			rootNode = (ObjectNode) objectMapper.readTree(inputStream);
		}catch (IOException e1) {
			System.out.println("Config json not readable");
		}catch (NullPointerException e2) {
			System.out.println("Nulli");
		} catch (IllegalArgumentException e2) {
			System.out.println("IllegalArgument Exception Error in InputStream\n");
			e2.printStackTrace();
		}
		return rootNode;
	}
	
	private void testResource() {
		URL url = this.getClass().getResource("/config.json");
		IJ.log("Resource is in" + url.toString());
	}
	
}
