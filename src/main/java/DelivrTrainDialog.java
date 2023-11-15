import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.io.FileUtils;
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

public class DelivrTrainDialog extends JDialog {
		private static final long serialVersionUID = 1L;

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
		private JTextField textOutputPath;
		
		//JSON config file
		private ObjectNode rootNode;
		
		/*TODO:
			- Load JSON file
			- Initialize everything with file
			- Update json file
			- Start training (python code) 
			-> Train Progress Class, read out tqdm
			- Save model, enable plugging it into pytorch again (how)
		*/
		
		public DelivrTrainDialog(final Context ctx) {
			ctx.inject(this);
			this.rootNode = initializeJSON();
			setBounds(100, 100, 400, 520);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{0, 0};
			gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			gridBagLayout.columnWeights = new double[]{1.0, 1.0};
			gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			getContentPane().setLayout(gridBagLayout);
			
			int running_y = 0;
			
			JLabel lblDelivrTraining = new JLabel("<html><B>DELIVR Training</B></html>");
			GridBagConstraints gbc_lblDelivrTraining = new GridBagConstraints();
			gbc_lblDelivrTraining.insets = new Insets(0, 0, 5, 5);
			gbc_lblDelivrTraining.gridx = 0;
			gbc_lblDelivrTraining.gridy = running_y++;
			gbc_lblDelivrTraining.gridwidth = 2;
			getContentPane().add(lblDelivrTraining, gbc_lblDelivrTraining);
			

			JTextField txt_input  = addLabelAndText(getContentPane(), rootNode, "Raw path", "dataset", "raw_path", 0, running_y++);
			JTextField txt_gt  = addLabelAndText(getContentPane(), rootNode, "Annotation path", "dataset", "gt_path", 0, running_y++);
			JTextField txt_output = addLabelAndText(getContentPane(), rootNode, "Output path", "dataset", "output_path", 0, running_y++);
			
			//+-----Training------+
			
			JLabel lblTrainig = new JLabel("<html><B>Training</B></html>");
			GridBagConstraints gbc_lblTraining = new GridBagConstraints();
			gbc_lblTraining.insets = new Insets(0, 0, 5, 5);
			gbc_lblTraining.gridx = 0;
			gbc_lblTraining.gridy = running_y++;
			gbc_lblTraining.gridwidth = 2;
			getContentPane().add(lblTrainig, gbc_lblTraining);

			JCheckBox chckbx_retrain = addLabelAndCheckBox(getContentPane(), rootNode, "Retrain", "training", "retrain", 0, running_y++);
			JTextField txt_model_path = addLabelAndText(getContentPane(),rootNode, "Model Path", "dataset", "delivr_model_path", 0, running_y++);
			link_checkbox_textfield(txt_model_path, chckbx_retrain);
			
			
			JCheckBox chckbx_tta = addLabelAndCheckBox(getContentPane(), rootNode, "Test Time Augmentation", "training", "tta", 0, running_y++);
			JCheckBox chckbx_normalization = addLabelAndCheckBox(getContentPane(), rootNode, "Normalization", "training", "normalization", 0, running_y++);

			JTextField txt_epochs = addLabelAndText(getContentPane(), rootNode, "Epochs", "training", "epochs", 0, running_y++);
			JTextField txt_learningrate = addLabelAndText(getContentPane(), rootNode, "Learning Rate", "training", "learning_rate", 0, running_y++);
			
			
			
			//+-----Network------+
			
			JLabel lblNetwork = new JLabel("<html><B>Network</B></html>");
			GridBagConstraints gbc_lblNetwork= new GridBagConstraints();
			gbc_lblNetwork.insets = new Insets(0, 0, 5, 5);
			gbc_lblNetwork.gridx = 0;
			gbc_lblNetwork.gridy = running_y++;
			gbc_lblNetwork.gridwidth = 2;
			getContentPane().add(lblNetwork, gbc_lblNetwork);

			JTextField txt_batchsize = addLabelAndText(getContentPane(), rootNode, "Batch Size", "network", "batch_size", 0, running_y++);
			JTextField txt_numworkers = addLabelAndText(getContentPane(), rootNode, "Number of Workers", "network", "num_workers", 0, running_y++);
			
			//+-----Testing------+
			
			JLabel lblTest = new JLabel("<html><B>Testing</B></html>");
			GridBagConstraints gbc_lblTest= new GridBagConstraints();
			gbc_lblTest.insets = new Insets(0, 0, 5, 5);
			gbc_lblTest.gridx = 0;
			gbc_lblTest.gridy = running_y++;
			gbc_lblTest.gridwidth = 2;
			getContentPane().add(lblTest, gbc_lblTest);

			//TODO Add Checkbox for testing
			//TODO: Fixed Test Set
			JTextField txt_test = addLabelAndText(getContentPane(), rootNode, "Test set path", "training", "test_list", 0, running_y++);
//			GridBagConstraints gbc_txt_test= new GridBagConstraints();
//			gbc_txt_test.insets = new Insets(0, 0, 5, 5);
//			gbc_txt_test.gridx = 0;
//			gbc_txt_test.gridy = 13;
//			gbc_txt_test.gridwidth = 2;
//			getContentPane().add(txt_test, gbc_txt_test);
			
			class ButtonAction extends AbstractAction {
				private final Context ctx;
				
				private final ObjectNode rootNode;
				
				private static final long serialVersionUID = -10230100123L;
				
				private final JButton sender;
				
				private final JTextField txt_input;
				private final JTextField txt_gt;
				private final JTextField txt_output;
				private final JTextField txt_test;

				private final JCheckBox chckbx_retrain;
				private final JTextField txt_model_path;
				private final JCheckBox chckbx_tta;
				private final JCheckBox chckbx_normalization;

				private final JTextField txt_epochs;
				private final JTextField txt_learningrate;

				private final JTextField txt_batchsize;
				private final JTextField txt_numworkers;
				
				private final JTextField txt_command;
				
				public ButtonAction(
						JButton sender,
						JTextField txt_input,
						JTextField txt_gt,
						JTextField txt_output,
						JTextField txt_test,
						JCheckBox chckbx_retrain,
						JTextField txt_model_path,
						JCheckBox chckbx_tta,
						JCheckBox chckbx_normalization,
						JTextField txt_epochs,
						JTextField txt_learningrate,
						JTextField txt_batchsize,
						JTextField txt_numworkers,
						JTextField txt_command,
						ObjectNode rootNode,
						Context ctx) {
					this.sender = sender;
					this.txt_input = txt_input;
					this.txt_gt = txt_gt;
					this.txt_output = txt_output;
					this.txt_test = txt_test;
					this.chckbx_retrain = chckbx_retrain;
					this.txt_model_path = txt_model_path;
					this.chckbx_tta = chckbx_tta;
					this.chckbx_normalization = chckbx_normalization;
					this.txt_epochs = txt_epochs;
					this.txt_learningrate = txt_learningrate;
					this.txt_batchsize = txt_batchsize;
					this.txt_numworkers = txt_numworkers;
					
					this.txt_command = txt_command;
					
					this.rootNode = rootNode;
					this.ctx = ctx;
				}
				
				private void copyFolder(String input_path, String output_path, String foldername) {
					String final_path = output_path + foldername;
					File src_dir = new File(input_path);
					File dest_dir= new File(final_path);
					try {
						FileUtils.copyDirectory(src_dir, dest_dir);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				@Override
				public void actionPerformed(ActionEvent e) {
					sender.setEnabled(false);
					//We know which element contains which JSON information
					//Just backtrace it haha
					
					Boolean linux = false;
					if(System.getProperty("os.name").contains("Linux")) {
						linux = true;
					}
					
					//TODO Sanitize everything:
					//TODO: Figure this out
					//TODO Copy everything into the output file under a specific folder, rename everything so that it fits the docker naming convention, do stuff when finished
					String raw_path = sanitizePath(txt_input.getText(), linux);
					String gt_path =  sanitizePath(txt_gt.getText(), linux);
					String test_path = sanitizePath(txt_test.getText(), linux);
					String output_path = sanitizePath(txt_output.getText(), linux);
					
					copyFolder(raw_path, output_path, "raw_new");
					copyFolder(gt_path, output_path, "gt_new_no_empty_patches_binary");
					if(test_path != "") {
						copyFolder(test_path, output_path, "test");
					}
					
					
					((ObjectNode)this.rootNode.get("dataset")).put("output_path", "/data/");

					((ObjectNode)this.rootNode.get("training")).put("retrain", chckbx_retrain.isSelected());
					if(chckbx_retrain.isSelected()) {
						((ObjectNode)this.rootNode.get("dataset")).put("delivr_model_path", sanitizePath(txt_model_path.getText(), linux));						
					}
					((ObjectNode)this.rootNode.get("training")).put("tta", chckbx_tta.isSelected());
					((ObjectNode)this.rootNode.get("training")).put("normalization", chckbx_normalization.isSelected());

					((ObjectNode)this.rootNode.get("training")).put("epochs", Integer.parseInt(txt_epochs.getText()));
					((ObjectNode)this.rootNode.get("training")).put("learning_rate", Float.parseFloat(txt_learningrate.getText()));
					
					((ObjectNode)this.rootNode.get("network")).put("batch_size", Integer.parseInt(txt_batchsize.getText()));
					((ObjectNode)this.rootNode.get("network")).put("num_workers", Integer.parseInt(txt_numworkers.getText()));
					
					ObjectMapper objectMapper = new ObjectMapper();
					String prettyJson = "";
					try {
						prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
					} catch (JsonProcessingException e1) {
						e1.printStackTrace();
					}
					IJ.log("Loaded following JSON:");
					IJ.log(prettyJson);
					
					ObjectMapper mapper = new ObjectMapper();
					ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
					String config_location = output_path.replaceAll("\"", "") + "config_train.json";
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
					
					String command = "";
					if(this.txt_command.getText().equals("")) {
						if(linux) {
							command = "docker run -i --rm --runtime=nvidia --ipc=host -v "+
									sanitizePath(txt_output.getText(), linux) + ":/data/ delivr_cfos_train:06 /usr/bin/python3 __main__.py /data/config_train.json";
						}else {
						//	command = "powershell.exe docker run -i --rm --gpus=all --ipc=host -v "+
						//			sanitizePath(txt_output.getText(), linux) + ":/data/ delivr_cfos_train:06";
							command = "powershell -c \"\"docker run -t --rm --gpus=all --ipc=host -v " + sanitizePath(txt_output.getText(), linux) + ":/data/ delivr_cfos_train:06 python3 __main__.py /data/config_train.json \"\"";
						}
					}else {
						command = this.txt_command.getText();
					}
					
					//String env = "/home/ramial-maskari/Documents/cFos/delivr_cfos_train/TRAIN_ENV/bin/activate";
					
					//command = "" + env + " && python3 /home/ramial-maskari/Documents/cFos/delivr_cfos_train/__main__.py " + config_location;
					//command = "python3 /home/ramial-maskari/Documents/cFos/delivr_cfos_train/__main__.py " + config_location;
					
					IJ.log("Running " + command);
					
					DelivrTrainProgress progress = new DelivrTrainProgress(ctx, command, sender);
					progress.setVisible(true);
					
				}
				
				private String sanitizePath(String path, Boolean linux) {
					path.replaceAll("\"", "");
					path.replaceAll("'", "");
					path.replaceAll("\n", "");
					path.replaceAll("\r", "");
					if(!linux) {
						path.replaceAll("//","\\");
						if(!path.endsWith("\\")) {
							path += "\\";
						}
					}else{
						if(!path.endsWith("/")) {
							path += "/";
						}
						
					}
					return path;
				};
				
			}
			
			JTextField txt_command = new JTextField("");
			GridBagConstraints gbc_txt_command = new GridBagConstraints();
			gbc_txt_command.insets = new Insets(0, 0, 0, 5);
			gbc_txt_command.gridx = 0;
			gbc_txt_command.gridy = running_y++;
			gbc_txt_command.fill = GridBagConstraints.HORIZONTAL;
			gbc_txt_command.anchor = GridBagConstraints.WEST;
			gbc_txt_command.gridwidth = 2;
			//TODO: Enable for Debugging
			//getContentPane().add(txt_command, gbc_txt_command);
			
			//Run
			JButton btnStart = new JButton("Start");
			btnStart.addActionListener(new ButtonAction(
					btnStart,
					txt_input,
					txt_gt,
					txt_output,
					txt_test,
					chckbx_retrain,
					txt_model_path,
					chckbx_tta,
					chckbx_normalization,
					txt_epochs,
					txt_learningrate,
					txt_batchsize,
					txt_numworkers,
					txt_command,
					rootNode,
					ctx
					));
			btnStart.setIcon(new ImageIcon(DelivrDialog.class.getResource("/microscope.gif")));
			GridBagConstraints gbc_btnStart = new GridBagConstraints();
			gbc_btnStart.insets = new Insets(0, 0, 0, 5);
			gbc_btnStart.gridx = 1;
			gbc_btnStart.gridy = running_y++;
			getContentPane().add(btnStart, gbc_btnStart);
			
			JLabel lblMargin = new JLabel(" ");
			GridBagConstraints gbc_lblMargin= new GridBagConstraints();
			gbc_lblMargin.insets = new Insets(0, 0, 5, 5);
			gbc_lblMargin.gridx = 0;
			gbc_lblMargin.gridy = running_y++;
			gbc_lblMargin.gridwidth = 2;
			getContentPane().add(lblMargin, gbc_lblMargin);
			
		}
		
		//TODO:
		// +--- Add Progress bar
		
		private JTextField addLabelAndText(Container panel, ObjectNode rootNode, String label, String k0, String k1, int gridx, int gridy) {
			JLabel lbl = new JLabel(" " + label);
			GridBagConstraints gbc_lbl = new GridBagConstraints();
			gbc_lbl.insets = new Insets(0, 0, 5, 5);
			gbc_lbl.gridx = gridx;
			gbc_lbl.gridy = gridy;
			gbc_lbl.anchor = GridBagConstraints.WEST;
			panel.add(lbl, gbc_lbl);
			
			String text = ((ObjectNode) rootNode.get(k0)).get(k1).toString().replaceAll("\"", "");
			JTextField textfield = new JTextField(text);
			textfield.setHorizontalAlignment(SwingConstants.RIGHT);
			GridBagConstraints gbc_txt = new GridBagConstraints();
			gbc_txt.insets = new Insets(0, 0, 5, 5);
			gbc_txt.gridx = gridx + 1;
			gbc_txt.gridy = gridy;
			gbc_txt.fill = GridBagConstraints.HORIZONTAL;
			gbc_txt.anchor = GridBagConstraints.EAST;
			panel.add(textfield, gbc_txt);
			
			return textfield;
		}
		
		private void link_checkbox_textfield(JTextField textfield, JCheckBox checkbox) {
			class CheckBoxLinkAction extends AbstractAction{
				private static final long serialVersionUID = 1L;
				private final JTextField textfield;
				private final JCheckBox checkbox;
				public CheckBoxLinkAction(JTextField textfield, JCheckBox checkbox) {
					this.textfield = textfield;
					this.checkbox = checkbox;
					this.textfield.setEnabled(this.checkbox.isSelected());
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					boolean selection = ((JCheckBox) e.getSource()).isSelected();
					this.textfield.setEnabled(selection);
				}
			}
			checkbox.setAction(new CheckBoxLinkAction(textfield, checkbox));
		}
		
		
		private JCheckBox addLabelAndCheckBox(Container panel, ObjectNode rootNode, String label, String k0, String k1, int gridx, int gridy) {
			JLabel lbl = new JLabel(" " + label);
			GridBagConstraints gbc_lbl = new GridBagConstraints();
			gbc_lbl.insets = new Insets(0, 0, 5, 5);
			gbc_lbl.gridx = gridx;
			gbc_lbl.gridy = gridy;
			gbc_lbl.anchor = GridBagConstraints.WEST;
			panel.add(lbl, gbc_lbl);
			
			Boolean bool_val = ((ObjectNode) rootNode.get(k0)).get(k1).asBoolean();
			
			JCheckBox chckbx = new JCheckBox();
			chckbx.setSelected(bool_val);
			GridBagConstraints gbc_chckbx = new GridBagConstraints();
			gbc_chckbx.insets = new Insets(0, 0, 5, 5);
			gbc_chckbx.gridx = gridx + 1;
			gbc_chckbx.gridy = gridy;
			gbc_chckbx.anchor = GridBagConstraints.EAST;
			panel.add(chckbx, gbc_chckbx);
			
			return chckbx;
		}
		
		
		
		private ObjectNode initializeJSON() {
			testResource();
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
			try {
				InputStream inputStream = getClass().getResourceAsStream("/config_train.json");
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
			URL url = this.getClass().getResource("/config_train.json");
			IJ.log("Resource is in" + url.toString());
		}
}
