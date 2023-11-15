/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the Unlicense for details:
 *     https://unlicense.org/
 */

import javax.swing.SwingUtilities;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true,
	menuPath = "DELIVR>DELIVR Pipeline")
public class DelivrCommand implements Command {

	@Parameter
	private Context ctx;

	private static DelivrDialog dialog = null;
	/**
	 * show a dialog and give the dialog access to required IJ2 Services
	 */
	@Override
	public void run() {
		SwingUtilities.invokeLater(() -> {
			if (dialog == null) {
				dialog = new DelivrDialog(ctx);
			}
			dialog.setVisible(true);
		});
	}
}
