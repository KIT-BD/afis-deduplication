package com.neurotec.samples.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.text.Highlighter;


public final class NPropertyGrid
        extends JPanel
        implements ListSelectionListener {
    private static final long serialVersionUID = 1L;
    private static final Map<Class<?>, Class<?>> BOXED_TO_PRIMITIVE = new HashMap<>();

    static {
        BOXED_TO_PRIMITIVE.put(Integer.class, int.class);
        BOXED_TO_PRIMITIVE.put(Short.class, short.class);
        BOXED_TO_PRIMITIVE.put(Byte.class, byte.class);
        BOXED_TO_PRIMITIVE.put(Long.class, long.class);
        BOXED_TO_PRIMITIVE.put(Float.class, float.class);
        BOXED_TO_PRIMITIVE.put(Double.class, double.class);
        BOXED_TO_PRIMITIVE.put(Boolean.class, boolean.class);
    }


    private final boolean isEditable;


    private List<String> supportedProperties;


    private final JTable table;


    private final JScrollPane scroll;


    private final JLabel propertyLabel;


    private final JTextArea txtDescription;

    private final JSplitPane gridSplitPane;


    public NPropertyGrid(boolean isEditable, List<String> supportedProperties) {
        setLayout(new BorderLayout());
        this.isEditable = isEditable;
        this.supportedProperties = supportedProperties;
        setMinimumSize(new Dimension(0, 0));
        this.table = new PropertyTable();
        this.table.setCellSelectionEnabled(true);
        this.table.getSelectionModel().setSelectionMode(0);
        this.scroll = new JScrollPane(this.table, 20, 31);
        this.gridSplitPane = new JSplitPane(0);

        JPanel descriptionPanel = new JPanel(new BorderLayout());
        this.propertyLabel = new JLabel();
        this.txtDescription = new JTextArea();
        this.txtDescription.setOpaque(false);
        this.txtDescription.setEditable(false);
        this.txtDescription.setLineWrap(true);
        this.txtDescription.setWrapStyleWord(true);
        this.txtDescription.setHighlighter((Highlighter) null);
        this.txtDescription.setBorder(BorderFactory.createEmptyBorder());
        descriptionPanel.add(this.propertyLabel, "First");
        descriptionPanel.add(this.txtDescription, "Center");

        this.gridSplitPane.setLeftComponent(this.scroll);
        this.gridSplitPane.setRightComponent(descriptionPanel);
        this.gridSplitPane.setDividerLocation(250);
        this.gridSplitPane.setDividerSize(1);
        add(this.gridSplitPane);

        this.table.getSelectionModel().addListSelectionListener(this);
    }


    public void setSource(Object bean) {
        if (bean == null) {
            return;
        }

        BeanTableModel model = new BeanTableModel(bean);
        this.table.setModel(model);
        this.table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;


            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                PropertyDescriptor prop = ((NPropertyGrid.BeanTableModel) table.getModel()).getDescriptor(row);
                if (prop.getWriteMethod() != null) {
                    Font currentFont = c.getFont();
                    c.setFont(new Font(currentFont.getName(), 1, currentFont.getSize()));
                }
                return c;
            }
        });
    }


    public void setSupportedProperties(List<String> supportedProperties) {
        this.supportedProperties = supportedProperties;
    }

    public void stopEditing() {
        if (this.table.getCellEditor() != null) {
            this.table.getCellEditor().stopCellEditing();
        }
    }


    public void valueChanged(ListSelectionEvent e) {
        int row = this.table.getSelectedRow();
        if (row > -1) {
            this.propertyLabel.setText((String) this.table.getValueAt(row, 0));
            Object value = this.table.getValueAt(row, 1);
            if (value == null) {
                this.txtDescription.setText("");
            } else {
                this.txtDescription.setText(value.toString());
            }
        }
    }


    private class BeanTableModel
            implements TableModel {
        private BeanInfo info;


        private Object object;


        private List<PropertyDescriptor> properties;


        private boolean showExpert;


        BeanTableModel(Object bean) {
            try {
                this.object = bean;
                this.info = Introspector.getBeanInfo(bean.getClass());
                this.properties = Collections.synchronizedList(new ArrayList<>());
                this.showExpert = false;
                refreshProperties();
            } catch (IntrospectionException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(NPropertyGrid.this, e.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void refreshProperties() {
            for (PropertyDescriptor prop : this.info.getPropertyDescriptors()) {
                if (NPropertyGrid.this.supportedProperties.contains(prop.getName()) && (!prop.isExpert() || this.showExpert)) {
                    this.properties.add(prop);
                }
            }
        }


        private Object getValueObject(PropertyDescriptor prop, Object obj, String newValue) {
            try {
                Object returnValue, readValue = prop.getReadMethod().invoke(obj, new Object[0]);
                if (readValue instanceof Integer) {
                    returnValue = Integer.valueOf(newValue);
                } else if (readValue instanceof Double) {
                    returnValue = Double.valueOf(newValue);
                } else if (readValue instanceof Long) {
                    returnValue = Long.valueOf(newValue);
                } else if (readValue instanceof Short) {
                    returnValue = Short.valueOf(newValue);
                } else if (readValue instanceof Byte) {
                    returnValue = Byte.valueOf(newValue);
                } else if (readValue instanceof Float) {
                    returnValue = Float.valueOf(newValue);
                } else if (readValue instanceof Boolean) {
                    returnValue = Boolean.valueOf(newValue);
                } else {
                    returnValue = newValue;
                }
                return returnValue;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(NPropertyGrid.this, e.getCause().toString()));
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(NPropertyGrid.this, e.toString()));
            }
            return null;
        }

        private PropertyDescriptor getDescriptor(int row) {
            return this.properties.get(row);
        }

        private boolean isAssignable(Class<?> to, Class<?> from) {
            if (to.isAssignableFrom(from)) {
                return true;
            }
            Class<?> primitiveFrom = (Class) NPropertyGrid.BOXED_TO_PRIMITIVE.get(from);
            if (primitiveFrom == null) {
                return false;
            }
            return to.isAssignableFrom(primitiveFrom);
        }


        public int getRowCount() {
            if (this.info == null || this.info.getPropertyDescriptors() == null) {
                return 0;
            }
            return this.properties.size();
        }


        public int getColumnCount() {
            return 2;
        }


        public String getColumnName(int columnIndex) {
            return (columnIndex == 0) ? "Property" : "Value";
        }


        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            return Object.class;
        }


        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (NPropertyGrid.this.isEditable) {
                if (columnIndex == 0) {
                    return false;
                }
                PropertyDescriptor desc = this.properties.get(rowIndex);
                return (desc.getWriteMethod() != null);
            }

            return false;
        }


        public Object getValueAt(int rowIndex, int columnIndex) {
            PropertyDescriptor prop = this.properties.get(rowIndex);
            if (columnIndex == 0) {
                return prop.getDisplayName();
            }
            try {
                return prop.getReadMethod().invoke(this.object, new Object[0]);
            } catch (Exception e) {
                return (e.getMessage() != null) ? e.getMessage() : e.getCause().getMessage();
            }
        }


        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            try {
                PropertyDescriptor prop = this.properties.get(rowIndex);
                if (aValue instanceof String) {
                    Object value = getValueObject(prop, this.object, (String) aValue);
                    if (value != null && isAssignable(prop.getPropertyType(), value.getClass())) {
                        prop.getWriteMethod().invoke(this.object, new Object[]{value});
                    }
                } else {
                    prop.getWriteMethod().invoke(this.object, new Object[]{aValue});
                }
                prop.getReadMethod().invoke(this.object, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void addTableModelListener(TableModelListener l) {
        }


        public void removeTableModelListener(TableModelListener l) {
        }
    }


    private final class PropertyTable
            extends JTable {
        private static final long serialVersionUID = 1L;


        private PropertyTable() {
        }


        public TableCellEditor getCellEditor(int row, int column) {
            if (NPropertyGrid.this.table.getModel() instanceof NPropertyGrid.BeanTableModel) {
                PropertyDescriptor prop = ((NPropertyGrid.BeanTableModel) NPropertyGrid.this.table.getModel()).getDescriptor(row);
                Class<?> propertyType = prop.getPropertyType();
                if (propertyType != null) {
                    if (propertyType.equals(boolean.class)) {
                        JComboBox<Boolean> booleanCmb = new JComboBox();
                        booleanCmb.addItem(Boolean.TRUE);
                        booleanCmb.addItem(Boolean.FALSE);
                        return new DefaultCellEditor(booleanCmb);
                    }
                    Object[] enumConstansts = propertyType.getEnumConstants();
                    if (enumConstansts != null && enumConstansts.length > 0) {
                        return new DefaultCellEditor(new JComboBox(enumConstansts));
                    }
                }
            }


            return super.getCellEditor(row, column);
        }
    }
}
