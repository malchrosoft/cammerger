/*
 * Copyright MalchroSoft - Aymeric MALCHROWICZ. All right reserved.
 * The source code that contains this comment is an intellectual property
 * of MalchroSoft [Aymeric MALCHROWICZ]. Use is subject to licence terms.
 */
package com.malchrosoft.cammerger.test.barcodescanner;

/**
 *
 * @author Aymeric Malchrowicz / MalchroSoft
 */
public class BarcodeScanLauncher
{
	private static BarcodeScannerFrameController controller;

	public static void main(String... args)
	{
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try
		{
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
			javax.swing.UnsupportedLookAndFeelException ex)
		{
			java.util.logging.Logger.getLogger(BarcodeScannerFrame.class.getName()).log(java.util.logging.Level.SEVERE,
				null, ex);
		}
		//</editor-fold>

		controller = new BarcodeScannerFrameController();
		controller.launchRefreshLoop();
	}

}
