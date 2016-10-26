/**
 * 
 */
package edu.clemson.lph.dialogs;

/**
 * @author mmarti5
 * To use with ProgressDialog
 * 	class {ClassName} extends Thread implements ThreadCancelListener {
 *      ...
 *  	volatile boolean bCanceled = false;

		public {ClassName}( ProgressDialog prog, ... ) {
			this.prog = prog;
			...
			prog.setCancelListener(this);
		}
		
		@Override
		public void cancelThread() {
			bCanceled = true;
			interrupt();
		}
		
		public void run() {
		...
					while( ... && !bCanceled) {
						// Let USAHERDS catch up.
						try {
							Thread.sleep(500L);
						} catch (InterruptedException e1) { 
							if( bCanceled ) {
								exitThread(false);
								return;
							}
						}

		private void exitThread( boolean bSuccess ) {
			final boolean bDone = bSuccess;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					prog.setVisible(false);
					prog.dispose();
					// If implemented
					if( bDone )
						onThreadComplete({ClassName}.this);
				}
			});
		}

 */
public interface ThreadCancelListener {
	public void cancelThread();
}
