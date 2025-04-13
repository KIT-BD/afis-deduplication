package com.neurotec.samples.server.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JPanel;


public final class GridBagUtils {
    private GridBagConstraints gridBagConstraints;

    public GridBagUtils(int fill) {
        this.gridBagConstraints = new GridBagConstraints();
        this.gridBagConstraints.fill = fill;
    }

    public GridBagUtils(int fill, Insets insets) {
        this.gridBagConstraints = new GridBagConstraints();
        this.gridBagConstraints.fill = fill;
        this.gridBagConstraints.insets = insets;
    }

    public void setInsets(Insets insets) {
        this.gridBagConstraints.insets = insets;
    }

    public void addToGridBagLayout(int x, int y, JPanel parent, JComponent component) {
        this.gridBagConstraints.gridx = x;
        this.gridBagConstraints.gridy = y;
        parent.add(component, this.gridBagConstraints);
    }

    public void addToGridBagLayout(int x, int y, int width, int height, JPanel parent, JComponent component) {
        this.gridBagConstraints.gridx = x;
        this.gridBagConstraints.gridy = y;
        this.gridBagConstraints.gridwidth = width;
        this.gridBagConstraints.gridheight = height;
        parent.add(component, this.gridBagConstraints);
    }

    public void addToGridBagLayout(int x, int y, int width, int height, int weightX, int weightY, JPanel parent, JComponent component) {
        this.gridBagConstraints.gridx = x;
        this.gridBagConstraints.gridy = y;
        this.gridBagConstraints.gridwidth = width;
        this.gridBagConstraints.gridheight = height;
        this.gridBagConstraints.weightx = weightX;
        this.gridBagConstraints.weighty = weightY;
        parent.add(component, this.gridBagConstraints);
    }

    public void clearGridBagConstraints() {
        this.gridBagConstraints.gridwidth = 1;
        this.gridBagConstraints.gridheight = 1;
        this.gridBagConstraints.weightx = 0.0D;
        this.gridBagConstraints.weighty = 0.0D;
    }
}
