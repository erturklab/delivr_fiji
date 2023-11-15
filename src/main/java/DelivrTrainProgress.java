import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.ZProjector;
import net.imagej.ops.OpService;

public class DelivrTrainProgress extends JDialog {

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
	public DelivrTrainProgress(final Context ctx, String command, JButton sender, String outputPath) {
		setTitle("Progress");
		setBounds(100, 100, 350, 120);
		setLocationRelativeTo(null); //Set centered
		
		IJ.log("We are now running " + command);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblDELIVRTitle = new JLabel(" ");
		GridBagConstraints gbc_lblDELIVRTitle = new GridBagConstraints();
		gbc_lblDELIVRTitle.insets = new Insets(0, 0, 5, 5);
		//gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblDELIVRTitle.gridx = 0;
		gbc_lblDELIVRTitle.gridy = 0;
		gbc_lblDELIVRTitle.gridwidth = 2;
		getContentPane().add(lblDELIVRTitle, gbc_lblDELIVRTitle);
		
		JProgressBar progressValidation = addProgress(getContentPane(), "  Validation Fold", 0, 1);

		JProgressBar progressEpoch = addProgress(getContentPane(), "  Epoch", 0, 2);
		
		class LogPuller extends SwingWorker{
			private JProgressBar progressValidation;
			private JProgressBar progressEpoch;
			private JLabel lblOutput;
			private JButton sender;
			private String command;
			private DelivrTrainProgress daddy;
			private int maxepochs = 1;
			private String outputPath;
			@Override
			public String doInBackground() {
				try {

					Runtime rt = Runtime.getRuntime();
					IJ.log("Now in Pulli");
					Process docker_process = rt.exec(command);
					IJ.log("Pulli donni");
					String line = null;
	
					BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(docker_process.getInputStream()));
					while ((line = inputStreamReader.readLine()) != null) {	
						IJ.log(line);
						handleLine(line);
					}
	
					BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(docker_process.getErrorStream()));
					while ((line = errorStreamReader.readLine()) != null) {
						IJ.log("ERROR ::::::" + line);
					}
					
				} catch (IOException e1) {
					e1.printStackTrace();
					IJ.log("IOERROR ::::::" + e1.getMessage());
				} catch (NullPointerException ne1) {
					ne1.printStackTrace();
					IJ.log("NullPointerException ::::::" + ne1.getMessage());
					
				}
			IJ.log("Done!");
			progressValidation.setValue(100);
			progressEpoch.setValue(100);
			training_summary();
			return "";
		 	}
			
			private void training_summary() {
			/** Open Training output folder
			 * 	Display csv
			 */
				try {
					Desktop.getDesktop().open(new File(this.outputPath));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			private void handleLine(String line) {
				if(line.contains("MAXEPOCHS")) {
					this.maxepochs = Integer.valueOf(line.split(":")[1]);
					
				}else if(line.contains("Train_Val Fold:")) {
					//Test Fold :0:1| Train_Val Fold:0:5 
					try {
						String[] lineArray = line.split(":");
																		
						int progressValidationValue 	= Math.round(Float.valueOf(lineArray[3]) / Float.valueOf(lineArray[4]) * 100);
						IJ.log("Progress Val Fold: " + String.valueOf(progressValidationValue));
						progressValidation.setValue(progressValidationValue);
					} catch (Exception e2) {
						IJ.log(e2.getMessage());
					}
				}else if(line.contains("Epoch ")){
					String progressEpochValue = line.split(":")[0].substring(5);
					int progressValidationValue 	= Math.round(Float.valueOf(progressEpochValue) / Float.valueOf(this.maxepochs) * 100);
					progressEpoch.setValue(progressValidationValue);
				
				}else {
					IJ.log(line);
				}
			}
		   @Override
		   protected void done() {
		      sender.setEnabled(true);
		      daddy.dispose();
		   }
		    
		   public void set_command(String command) {
			   this.command = command;
		   }
		   
		   public void set_sender(JButton sender) {
			   this.sender = sender;
		   }
		   
		   public void set_progress_validation(JProgressBar progressValidation) {
			   this.progressValidation = progressValidation;
		   }
		   
		   public void set_progress_epoch(JProgressBar progressEpoch) {
			   this.progressEpoch = progressEpoch;
		   }
		   
		   public void set_daddy(DelivrTrainProgress daddy) {
			   this.daddy = daddy;
		   }
		   public void set_outputPath(String outputPath) {
			   this.outputPath = outputPath;
		   }
		}
		
		LogPuller pulli = new LogPuller();
		pulli.set_progress_validation(progressValidation);
		pulli.set_progress_epoch(progressEpoch);
		pulli.set_command(command);
		pulli.set_sender(sender);
		pulli.set_daddy(this);
		pulli.set_outputPath(outputPath);
		pulli.execute();
		
	}
	
	private JProgressBar addProgress(Container contentPane, String label, int x, int y) {
		JLabel lbl = new JLabel(label);
		GridBagConstraints gbc_lbl = new GridBagConstraints();
		gbc_lbl.insets = new Insets(0, 0, 5, 0);
		gbc_lbl.gridx = x;
		gbc_lbl.gridy = y;
		contentPane.add(lbl, gbc_lbl);
		
		JProgressBar progress = new JProgressBar(0, 100);
		progress.setOrientation(SwingConstants.HORIZONTAL);
		progress.setStringPainted(true);
		GridBagConstraints gbc_progress = new GridBagConstraints();
		gbc_progress.insets = new Insets(0, 0, 5, 0);
		gbc_progress.gridx = x + 1;
		gbc_progress.gridy = y;
		gbc_progress.gridwidth = 10;
		contentPane.add(progress, gbc_progress);
		
		return progress;		
	}
}
