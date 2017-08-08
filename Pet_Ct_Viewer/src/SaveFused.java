
import ij.IJ;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

/**
 *
 * @author ilan
 */
public class SaveFused extends javax.swing.JDialog {

	/**
	 * Creates new form SaveFused
	 * @param parent - PetCtFrame
	 * @param modal
	 */
	public SaveFused(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		petFrm1 = (PetCtFrame) parent;
		initComponents();
		init();
	}

	private void init() {
		int offType;
		setLocationRelativeTo(petFrm1);
		petPanel1 = petFrm1.getPetCtPanel1();
		petPipe = petPanel1.petPipe;
		offType = petPipe.offscrMode;
		if(offType == 0) {
			jLabInfo0.setText("No fused images displayed.");
			jLabInfo1.setText("Press Cancel and then release MIP.");
			jButOk.setEnabled(false);
			jSpinLo.setEnabled(false);
			jSpinHi.setEnabled(false);
			return;
		}
		isInitializing = true;
		Dimension sz1 = petPanel1.getSize();
		areaFactor = sz1.height*sz1.width/(330.0*885.);
		maxSlice = petPipe.data1.width;
		if(petPanel1.m_sliceType == JFijiPipe.DSP_AXIAL) maxSlice = petPipe.getNormalizedNumFrms();
		getSpinModel(0).setValue(1);
		getSpinModel(1).setValue(maxSlice);
		setTimeEst();
		jLabInfo1.setText("To reduce time required use limits or shrink window.");
		isInitializing = false;
	}

	void setTimeEst() {
		int dif = Math.abs(getSpinInt(1) - getSpinInt(0)) + 1;
		double timEst = dif * (0.2 + 0.18*areaFactor);
		int min = 0, sec = 0;
		if( timEst > 100) min = (int)((timEst + 30)/60);
		else sec = (int) (timEst + 0.5);
		String out = "Press OK and do not disturb for ";
		if( min == 0) out += sec + " sec";
		else out += min + " min";
		jLabInfo0.setText(out);
	}

	void spinnerChanged(int type) {
		if( isInitializing) return;
		int val1 = getSpinInt(type);
		if( val1 <= 0) val1 = 1;
		if( val1 > maxSlice) val1 = maxSlice;
		val1--;
		setTimeEst();
		setSlice(val1);
	}
	
	void setSlice(int val1) {
		switch(petPanel1.m_sliceType) {
			case JFijiPipe.DSP_AXIAL:
				petPanel1.petAxial = val1;
				break;

			case JFijiPipe.DSP_CORONAL:
				petPanel1.petCoronal = val1;
				break;

			case JFijiPipe.DSP_SAGITAL:
				petPanel1.petSagital = val1;
				break;
		}
		petPanel1.repaint();
	}

	int getSpinInt(int type) {
		SpinnerNumberModel spin1 = getSpinModel(type);
		return spin1.getNumber().intValue();
	}

	SpinnerNumberModel getSpinModel(int type) {
		JSpinner jspin;
		switch(type) {
			case 1:
				jspin = jSpinHi;
				break;

			default:
				jspin = jSpinLo;
				break;
		}
		SpinnerNumberModel spin1 = (SpinnerNumberModel) jspin.getModel();
		return spin1;
	}
		
	void bkgCalc() {
		work2 = new bkgdSaveFused();
		work2.addPropertyChangeListener(new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
			String propertyName = evt.getPropertyName();
			if( propertyName.equals("state")) {
				SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
				if( state == SwingWorker.StateValue.DONE) {
					work2 = null;
				}
			}
			}
		});
		work2.execute();
	}

	void storeFusedSlices() {
		int slice1, slice2, i, num1, robot=3;
		boolean saveAutoResize = petFrm1.autoResize;
		setVisible(false);	// hide the dialog
		if(petPanel1.isMIPdisplay()) robot=1;
		slice1 = getSpinInt(0);
		slice2 = getSpinInt(1);
		if( slice1 <= 0) slice1 = 1;
		if( slice1 > maxSlice) slice1 = maxSlice;
		if( slice2 <= 0) slice2 = 1;
		if( slice2 > maxSlice) slice2 = maxSlice;
		if( slice1 > slice2) {
			i = slice1;
			slice1 = slice2;
			slice2 = i;
		}
		slice1--;	// count from zero
		num1 = slice2 - slice1;
		try {
			myWriteDicom dcm1 = new myWriteDicom(petFrm1, null);
			dcm1.m_delay = 200;
			if( !saveAutoResize) {
				petFrm1.autoResize = true;
				petPanel1.updatePipeInfo();
				Thread.sleep(400);
			}
			for( i=0; i<num1; i++) {
				setSlice(i+slice1);
				dcm1.writeImmediateDicomHeader( petPanel1.m_sliceType, robot);
				IJ.showProgress(i, num1);
			}
		} catch (Exception e) { ChoosePetCt.stackTrace2Log(e);}
		if( !saveAutoResize) {
			petFrm1.autoResize = false;
			petPanel1.updatePipeInfo();
			petFrm1.fitWindow();
		}
		setSlice(slice1);
		dispose();
	}

	protected class bkgdSaveFused extends SwingWorker {

		@Override
		protected Void doInBackground() {
			storeFusedSlices();
			return null;
		}
		
	}
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabInfo0 = new javax.swing.JLabel();
        jButOk = new javax.swing.JButton();
        jButCancel = new javax.swing.JButton();
        jLabInfo1 = new javax.swing.JLabel();
        jSpinLo = new javax.swing.JSpinner();
        jLabLimits = new javax.swing.JLabel();
        jSpinHi = new javax.swing.JSpinner();
        jButHelp = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Save Fused Images");

        jLabInfo0.setText("0");

        jButOk.setText("OK");
        jButOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButOkActionPerformed(evt);
            }
        });

        jButCancel.setText("Cancel");
        jButCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButCancelActionPerformed(evt);
            }
        });

        jLabInfo1.setText("1");

        jSpinLo.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        jSpinLo.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinLoStateChanged(evt);
            }
        });

        jLabLimits.setText("<- limits ->");

        jSpinHi.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        jSpinHi.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinHiStateChanged(evt);
            }
        });

        jButHelp.setText("Help");
        jButHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButHelpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabInfo0)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButOk)
                        .addGap(18, 18, 18)
                        .addComponent(jButCancel)
                        .addGap(18, 18, 18)
                        .addComponent(jButHelp))
                    .addComponent(jLabInfo1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSpinLo, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(jLabLimits)
                        .addGap(4, 4, 4)
                        .addComponent(jSpinHi, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(181, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabInfo0)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabInfo1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinLo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabLimits)
                    .addComponent(jSpinHi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButOk)
                    .addComponent(jButCancel)
                    .addComponent(jButHelp))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButCancelActionPerformed
		dispose();
    }//GEN-LAST:event_jButCancelActionPerformed

    private void jButOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButOkActionPerformed
		bkgCalc();
    }//GEN-LAST:event_jButOkActionPerformed

    private void jSpinLoStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinLoStateChanged
		spinnerChanged(0);
    }//GEN-LAST:event_jSpinLoStateChanged

    private void jSpinHiStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinHiStateChanged
		spinnerChanged(1);
    }//GEN-LAST:event_jSpinHiStateChanged

    private void jButHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButHelpActionPerformed
		ChoosePetCt.openHelp("Save Fused Images");
    }//GEN-LAST:event_jButHelpActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButCancel;
    private javax.swing.JButton jButHelp;
    private javax.swing.JButton jButOk;
    private javax.swing.JLabel jLabInfo0;
    private javax.swing.JLabel jLabInfo1;
    private javax.swing.JLabel jLabLimits;
    private javax.swing.JSpinner jSpinHi;
    private javax.swing.JSpinner jSpinLo;
    // End of variables declaration//GEN-END:variables
	PetCtFrame petFrm1;
	PetCtPanel petPanel1;
	double areaFactor = 1.0;
	JFijiPipe petPipe;
	int maxSlice;
	boolean isInitializing;
	bkgdSaveFused work2 = null;
}
