import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.io.File;

public class DelivrProgress extends JDialog {

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

	public DelivrProgress(final Context ctx, String command, JButton sender, boolean regionAssignment, String regionPath) {

		setTitle("Progress");
		setBounds(100, 100, 250, 120);
		setLocationRelativeTo(null); //Set centered
		
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
		
		JProgressBar progressOverall = addProgress(getContentPane(), "  Overall", 0, 1);

		JProgressBar progressBrain = addProgress(getContentPane(), "  Task", 0, 2);
		
		class LogPuller extends SwingWorker{
			private JProgressBar progressOverall;
			private JProgressBar progressBrain;
			private JLabel lblOutput;
			private JButton sender;
			private String command;
			private boolean regionAssignment;
			private String regionPath;
			private DelivrProgress daddy;
			@Override
			public String doInBackground() {
				try {

					Runtime rt = Runtime.getRuntime();
					Process docker_process = rt.exec(command);
					
					String line = null;
	
					BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(docker_process.getInputStream()));
					while ((line = inputStreamReader.readLine()) != null) {	
						//IJ.log(line);
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
			progressOverall.setValue(100);
			progressBrain.setValue(100);
			regionPath = regionPath.replace("\"", "");
			if(!regionPath.substring(regionPath.length() - 1).equals("/")) {
				regionPath += "/";
			}
			if(!System.getProperty("os.name").contains("Linux")) {
				regionPath.replace("/", "\\");
			}
			//Open all the heatmaps, assign a cute lookup table
			IJ.log("REGION PATH: " + regionPath);
			if(this.regionAssignment) {
				File dir = new File(regionPath);
		        File[] files = dir.listFiles();
		        IJ.log("Opening Heatmaps...");
		        for(File file: files) {
		        	if(file.getAbsolutePath().contains(".tif")) {
						IJ.run("Bio-Formats Importer", "open=" + file.getAbsolutePath() + " autoscale color_mode=Default rois_import=[ROI manager] view=Hyperstack stack_order=XYCZT");
						ImagePlus imp = WindowManager.getCurrentImage();
						IJ.run(imp, "Fire", "");
						imp.setSlice(200);
						IJ.run(imp, "Enhance Contrast", "saturated=0.35");
						ImagePlus mip = ZProjector.run(imp,"max");
						IJ.run(mip, "Fire", "");

		        	}
		        }
			}
			return "";
		 	}

			private void handleLine(String line) {
				//DEBUG Case
				if(line.contains("icmp_seq")) {
					String[] lineArray = line.split(" ");
					int icmp = Integer.valueOf(lineArray[4].split("=")[1]);
					//IJ.log(Integer.toString(icmp));
					//IJ.log(Integer.toString((icmp % 10) * 10));
					progressOverall.setValue(icmp);
					progressBrain.setValue((icmp % 10) * 10);
				}
				//Working case
				else if(line.contains("HOOK")) {
					//IJ.log("HANDLING LINE");
					//HOOK:{hookoverall}:{hookfactor}:{brain_i}:{len(postprocessed_files)}
					//TODO If region assignment: open Images! (MIP?) (use regex)
					//TODO If visualization: open Images! (use regex, too)
					try {
						String[] lineArray = line.split(":");
																		
						int progressValueOverall 	= Math.round(Float.valueOf(lineArray[1]) / Float.valueOf(lineArray[2]) * 100);
						int progressValueTask 		= Math.round((1 + Float.valueOf(lineArray[3])) / (1 + Float.valueOf(lineArray[4])) * 100);
						IJ.log("Progress Overall: " + String.valueOf(progressValueOverall));
						IJ.log("Progress Task: " + String.valueOf(progressValueTask));
						progressOverall.setValue(progressValueOverall);
						progressBrain.setValue(progressValueTask);
					} catch (Exception e2) {
						IJ.log(e2.getMessage());
					}
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
		   
		   public void set_progress_overall(JProgressBar overall) {
			   this.progressOverall = overall;
		   }
		   
		   public void set_progress_brain(JProgressBar brain) {
			   this.progressBrain = brain;
		   }
		   
		   public void set_regionAssignment(boolean regionAssignment) {
			   this.regionAssignment = regionAssignment;
		   }
		   public void set_regionPath(String regionPath) {
			   this.regionPath = regionPath;
		   }
		   public void set_daddy(DelivrProgress daddy) {
			   this.daddy = daddy;
		   }
		}
		
		LogPuller pulli = new LogPuller();
		pulli.set_progress_overall(progressOverall);
		pulli.set_progress_brain(progressBrain);
		pulli.set_regionAssignment(regionAssignment);
		pulli.set_regionPath(regionPath);
		pulli.set_command(command);
		pulli.set_sender(sender);
		pulli.set_daddy(this);
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
