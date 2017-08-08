
import java.util.prefs.Preferences;
import javax.swing.SpinnerNumberModel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ManualSync.java
 *
 * Created on Jul 20, 2011, 1:25:34 PM
 */
/**
 *
 * @author ilan
 */
public class ManualSync extends javax.swing.JDialog {
	PetCtFrame parent;
	JFijiPipe refPipe, setPipe;
	Preferences jPrefer;
	int currStoreIndx = 0;

	/** Creates new form ManualSync
	 * @param parent
	 * @param modal */
	public ManualSync(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		this.parent = (PetCtFrame) parent;
		initComponents();
		init();
	}
	
	private void init() {
		buttonGroup1 = new javax.swing.ButtonGroup();
		buttonGroup1.add(jRadioMRI);
		buttonGroup1.add(jRadioPET);
		jRadioMRI.setSelected(true);
		jPrefer = parent.jPrefer;
		setPipes();
		getStoreValues();
	}
	
	void resetValues() {
		double reflo, refhi, setlo, sethi;
		int xShft, yShft, zShft;
		setPipes();
		refPipe.data1.mriOffZ = 0;
		refPipe.mriOffX = refPipe.mriOffY = 0;
		zShft = setPipe.data1.mriOffZ;
		if( jRadioMRI.isSelected() && zShft != 0) {
			parent.getPetCtPanel1().maybeSetMriOffset();
			zShft = 0;
			xShft = setPipe.mriOffX;
			yShft = setPipe.mriOffY;
		} else {
			xShft = yShft = 0;
			reflo = refPipe.data1.zpos.get(0);
			setlo = setPipe.data1.zpos.get(0);
			int n = refPipe.data1.numFrms;
			refhi = refPipe.data1.zpos.get(n-1);
			n = setPipe.data1.numFrms;
			sethi = setPipe.data1.zpos.get(n-1);
			zShft = (int) ((refhi - sethi + reflo - setlo)/(2*setPipe.data1.spacingBetweenSlices));
		}
		jSpinOffset.setValue(zShft);
		jSpinOffY.setValue(yShft);
		jSpinOffX.setValue(xShft);
		jSpinOffSag.setValue(0);
		jCheckIgnore.setSelected(false);
		setPipeIgnore(false);
		parent.repaint();
	}
	
	void offsetChanged(int indx) {
		SpinnerNumberModel spin1 = (SpinnerNumberModel) jSpinOffset.getModel();
		int i = spin1.getNumber().intValue();
		setPipe.data1.mriOffZ = i;
		setPipe.corSagShift = parent.getPetCtPanel1().getCorSagShift(setPipe);
		int saveVal = i;
		spin1 = (SpinnerNumberModel) jSpinOffY.getModel();
		i = spin1.getNumber().intValue();
		int wid2 = setPipe.data1.width / 2;
		if( i < -wid2 || i > wid2) {
			if( i < 0) i = -wid2;
			else i = wid2;
			jSpinOffY.setValue(i);
		}
		if(indx == 2) saveVal = i;
		setPipe.mriOffY = i;
		spin1 = (SpinnerNumberModel) jSpinOffX.getModel();
		i = spin1.getNumber().intValue();
		if( i < -wid2 || i > wid2) {
			if( i < 0) i = -wid2;
			else i = wid2;
			jSpinOffX.setValue(i);
		}
		setPipe.mriOffX = i;
		if(indx == 1) saveVal = i;

		spin1 = (SpinnerNumberModel) jSpinOffSag.getModel();
		i = spin1.getNumber().intValue();
		setPipe.mriOffSag = i;
		if(indx == 4) saveVal = i;
		// now save value to registry
		i = currStoreIndx;
		String tmp1 = "sync store ";
		if(indx ==1) tmp1 += "x";
		if(indx ==2) tmp1 += "y";
		if(indx ==3) tmp1 += "z";
		if(indx ==4) tmp1 += "sag";
		if(indx ==5) tmp1 += "ignore";
		tmp1 = tmp1 + i;
		if(indx ==5) {
			boolean ignorXY = jCheckIgnore.isSelected();
			jPrefer.putBoolean(tmp1, ignorXY);
			setPipeIgnore(ignorXY);
		} else
			jPrefer.putInt(tmp1, saveVal);
		tmp1 = "sync store label" + i;
		jPrefer.put(tmp1,jTextStore.getText());
		parent.getPetCtPanel1().updateDisp3Value(jRadioMRI.isSelected());
		parent.repaint();
	}
	
	private void setPipeIgnore(boolean ignoreXY) {
		parent.getPetCtPanel1().maybeChangeUseXYShift(!ignoreXY);
	}
	
	void storeChanged() {
		SpinnerNumberModel spin1 = (SpinnerNumberModel) jSpinStore.getModel();
		currStoreIndx = spin1.getNumber().intValue();
		getStoreValues();
		parent.repaint();
	}
	
	void lableChanged() {
		String tmp1 = "sync store label" + currStoreIndx;
		jPrefer.put(tmp1,jTextStore.getText());
		
	}
	
	void getStoreValues() {
		int indx = currStoreIndx;
		String defVal = "default position";
		if(indx > 0) defVal = "Please set to a better label";
		String indxStr = "sync store label" + indx;
		String val1 = jPrefer.get(indxStr, null);
		if( val1 == null) val1 = defVal;
		jTextStore.setText(val1);
		indxStr = "sync store z" + indx;
		int ival1 = jPrefer.getInt(indxStr, 0);
		jSpinOffset.setValue(ival1);
		indxStr = "sync store x" + indx;
		ival1 = jPrefer.getInt(indxStr, 0);
		jSpinOffX.setValue(ival1);
		indxStr = "sync store y" + indx;
		ival1 = jPrefer.getInt(indxStr, 0);
		jSpinOffY.setValue(ival1);
		indxStr = "sync store sag" + indx;
		ival1 = jPrefer.getInt(indxStr, 0);
		jSpinOffSag.setValue(ival1);
		indxStr = "sync store ignore" + indx;
		boolean ignore = jPrefer.getBoolean(indxStr, false);
		jCheckIgnore.setSelected(ignore);
		setPipeIgnore(ignore);
	}
	
	private void setPipes() {
		PetCtPanel panel1 = parent.getPetCtPanel1();
		if( jRadioMRI.isSelected()) {
			refPipe = panel1.getCorrectedOrUncorrectedPipe(false);
			setPipe = panel1.getMriOrCtPipe();
		} else {
			setPipe = panel1.getCorrectedOrUncorrectedPipe(false);
			refPipe = panel1.getMriOrCtPipe();
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jRadioMRI = new javax.swing.JRadioButton();
        jRadioPET = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButReset = new javax.swing.JButton();
        jLabZ = new javax.swing.JLabel();
        jSpinOffset = new javax.swing.JSpinner();
        jLabX = new javax.swing.JLabel();
        jSpinOffX = new javax.swing.JSpinner();
        jLabY = new javax.swing.JLabel();
        jSpinOffY = new javax.swing.JSpinner();
        jLabSag = new javax.swing.JLabel();
        jSpinOffSag = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jSpinStore = new javax.swing.JSpinner();
        jTextStore = new javax.swing.JTextField();
        jButHelp = new javax.swing.JButton();
        jCheckIgnore = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sync MRI data");

        jLabel1.setText("When using MRI data, there can be an alignment problem");

        jLabel2.setText("compared to the PET-CT study.");

        jLabel3.setText("This program allows a manual correction to be applied.");

        jLabel4.setText("It should most likely be applied to the CT-MRI data but can");

        jLabel5.setText("also be used to align uncorrected PET to corrected PET.");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("apply correction to"));

        jRadioMRI.setText("CT-MRI");

        jRadioPET.setText("PET-SPECT");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jRadioMRI)
                .addGap(18, 18, 18)
                .addComponent(jRadioPET)
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jRadioMRI)
                .addComponent(jRadioPET))
        );

        jLabel6.setText("Reset gives an initial estimate for the Z value.");

        jButReset.setText("Reset");
        jButReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButResetActionPerformed(evt);
            }
        });

        jLabZ.setText("Z:");

        jSpinOffset.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinOffsetStateChanged(evt);
            }
        });

        jLabX.setText("X:");

        jSpinOffX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinOffXStateChanged(evt);
            }
        });

        jLabY.setText("Y:");

        jSpinOffY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinOffYStateChanged(evt);
            }
        });

        jLabSag.setText("sag:");

        jSpinOffSag.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinOffSagStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jButReset)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabZ)
                .addGap(1, 1, 1)
                .addComponent(jSpinOffset, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabX)
                .addGap(3, 3, 3)
                .addComponent(jSpinOffX, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabY)
                .addGap(1, 1, 1)
                .addComponent(jSpinOffY, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabSag)
                .addGap(1, 1, 1)
                .addComponent(jSpinOffSag, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButReset)
                .addComponent(jLabZ)
                .addComponent(jSpinOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabX)
                .addComponent(jSpinOffX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabY)
                .addComponent(jSpinOffY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabSag)
                .addComponent(jSpinOffSag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel7.setText("Choose where to store data and set label:");

        jSpinStore.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));
        jSpinStore.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinStoreStateChanged(evt);
            }
        });

        jTextStore.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextStoreKeyReleased(evt);
            }
        });

        jButHelp.setText("Help");
        jButHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButHelpActionPerformed(evt);
            }
        });

        jCheckIgnore.setText("ignore XY");
        jCheckIgnore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckIgnoreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jSpinStore)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextStore, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCheckIgnore))
                            .addComponent(jLabel7))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButHelp)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jButHelp)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinStore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextStore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jCheckIgnore))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSpinStoreStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinStoreStateChanged
		storeChanged();
    }//GEN-LAST:event_jSpinStoreStateChanged

    private void jTextStoreKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextStoreKeyReleased
		lableChanged();
    }//GEN-LAST:event_jTextStoreKeyReleased

    private void jSpinOffSagStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinOffSagStateChanged
		offsetChanged(4);
    }//GEN-LAST:event_jSpinOffSagStateChanged

    private void jSpinOffYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinOffYStateChanged
		offsetChanged(2);
    }//GEN-LAST:event_jSpinOffYStateChanged

    private void jSpinOffXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinOffXStateChanged
		offsetChanged(1);
    }//GEN-LAST:event_jSpinOffXStateChanged

    private void jSpinOffsetStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinOffsetStateChanged
        offsetChanged(3);
    }//GEN-LAST:event_jSpinOffsetStateChanged

    private void jButResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButResetActionPerformed
		resetValues();
    }//GEN-LAST:event_jButResetActionPerformed

    private void jButHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButHelpActionPerformed
		ChoosePetCt.openHelp("Sync MRI data");
    }//GEN-LAST:event_jButHelpActionPerformed

    private void jCheckIgnoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckIgnoreActionPerformed
		offsetChanged(5);
    }//GEN-LAST:event_jCheckIgnoreActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButHelp;
    private javax.swing.JButton jButReset;
    private javax.swing.JCheckBox jCheckIgnore;
    private javax.swing.JLabel jLabSag;
    private javax.swing.JLabel jLabX;
    private javax.swing.JLabel jLabY;
    private javax.swing.JLabel jLabZ;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton jRadioMRI;
    private javax.swing.JRadioButton jRadioPET;
    private javax.swing.JSpinner jSpinOffSag;
    private javax.swing.JSpinner jSpinOffX;
    private javax.swing.JSpinner jSpinOffY;
    private javax.swing.JSpinner jSpinOffset;
    private javax.swing.JSpinner jSpinStore;
    private javax.swing.JTextField jTextStore;
    // End of variables declaration//GEN-END:variables
}
