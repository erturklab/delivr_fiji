/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the Unlicense for details:
 *     https://unlicense.org/
 */

import javax.swing.WindowConstants;

import net.imagej.ImageJ;

public class SwingDELIVR {

	public static void main(final String[] args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		try {
			final DelivrDialog dialog = new DelivrDialog(ij.context());
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
