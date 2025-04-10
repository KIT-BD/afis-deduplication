/*     */ package com.neurotec.samples.swing;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.beans.BeanInfo;
/*     */ import java.beans.IntrospectionException;
/*     */ import java.beans.Introspector;
/*     */ import java.beans.PropertyDescriptor;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.DefaultCellEditor;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JSplitPane;
/*     */ import javax.swing.JTable;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.SwingUtilities;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ import javax.swing.event.TableModelListener;
/*     */ import javax.swing.table.DefaultTableCellRenderer;
/*     */ import javax.swing.table.TableCellEditor;
/*     */ import javax.swing.table.TableModel;
/*     */ import javax.swing.text.Highlighter;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class NPropertyGrid
/*     */   extends JPanel
/*     */   implements ListSelectionListener
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*  52 */   private static final Map<Class<?>, Class<?>> BOXED_TO_PRIMITIVE = new HashMap<>(); static {
/*  53 */     BOXED_TO_PRIMITIVE.put(Integer.class, int.class);
/*  54 */     BOXED_TO_PRIMITIVE.put(Short.class, short.class);
/*  55 */     BOXED_TO_PRIMITIVE.put(Byte.class, byte.class);
/*  56 */     BOXED_TO_PRIMITIVE.put(Long.class, long.class);
/*  57 */     BOXED_TO_PRIMITIVE.put(Float.class, float.class);
/*  58 */     BOXED_TO_PRIMITIVE.put(Double.class, double.class);
/*  59 */     BOXED_TO_PRIMITIVE.put(Boolean.class, boolean.class);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private final boolean isEditable;
/*     */ 
/*     */   
/*     */   private List<String> supportedProperties;
/*     */ 
/*     */   
/*     */   private final JTable table;
/*     */ 
/*     */   
/*     */   private final JScrollPane scroll;
/*     */ 
/*     */   
/*     */   private final JLabel propertyLabel;
/*     */ 
/*     */   
/*     */   private final JTextArea txtDescription;
/*     */   
/*     */   private final JSplitPane gridSplitPane;
/*     */ 
/*     */   
/*     */   public NPropertyGrid(boolean isEditable, List<String> supportedProperties) {
/*  85 */     setLayout(new BorderLayout());
/*  86 */     this.isEditable = isEditable;
/*  87 */     this.supportedProperties = supportedProperties;
/*  88 */     setMinimumSize(new Dimension(0, 0));
/*  89 */     this.table = new PropertyTable();
/*  90 */     this.table.setCellSelectionEnabled(true);
/*  91 */     this.table.getSelectionModel().setSelectionMode(0);
/*  92 */     this.scroll = new JScrollPane(this.table, 20, 31);
/*  93 */     this.gridSplitPane = new JSplitPane(0);
/*     */     
/*  95 */     JPanel descriptionPanel = new JPanel(new BorderLayout());
/*  96 */     this.propertyLabel = new JLabel();
/*  97 */     this.txtDescription = new JTextArea();
/*  98 */     this.txtDescription.setOpaque(false);
/*  99 */     this.txtDescription.setEditable(false);
/* 100 */     this.txtDescription.setLineWrap(true);
/* 101 */     this.txtDescription.setWrapStyleWord(true);
/* 102 */     this.txtDescription.setHighlighter((Highlighter)null);
/* 103 */     this.txtDescription.setBorder(BorderFactory.createEmptyBorder());
/* 104 */     descriptionPanel.add(this.propertyLabel, "First");
/* 105 */     descriptionPanel.add(this.txtDescription, "Center");
/*     */     
/* 107 */     this.gridSplitPane.setLeftComponent(this.scroll);
/* 108 */     this.gridSplitPane.setRightComponent(descriptionPanel);
/* 109 */     this.gridSplitPane.setDividerLocation(250);
/* 110 */     this.gridSplitPane.setDividerSize(1);
/* 111 */     add(this.gridSplitPane);
/*     */     
/* 113 */     this.table.getSelectionModel().addListSelectionListener(this);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setSource(Object bean) {
/* 121 */     if (bean == null) {
/*     */       return;
/*     */     }
/*     */     
/* 125 */     BeanTableModel model = new BeanTableModel(bean);
/* 126 */     this.table.setModel(model);
/* 127 */     this.table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer()
/*     */         {
/*     */           private static final long serialVersionUID = 1L;
/*     */ 
/*     */           
/*     */           public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
/* 133 */             Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
/* 134 */             PropertyDescriptor prop = ((NPropertyGrid.BeanTableModel)table.getModel()).getDescriptor(row);
/* 135 */             if (prop.getWriteMethod() != null) {
/* 136 */               Font currentFont = c.getFont();
/* 137 */               c.setFont(new Font(currentFont.getName(), 1, currentFont.getSize()));
/*     */             } 
/* 139 */             return c;
/*     */           }
/*     */         });
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void setSupportedProperties(List<String> supportedProperties) {
/* 147 */     this.supportedProperties = supportedProperties;
/*     */   }
/*     */   
/*     */   public void stopEditing() {
/* 151 */     if (this.table.getCellEditor() != null) {
/* 152 */       this.table.getCellEditor().stopCellEditing();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void valueChanged(ListSelectionEvent e) {
/* 162 */     int row = this.table.getSelectedRow();
/* 163 */     if (row > -1) {
/* 164 */       this.propertyLabel.setText((String)this.table.getValueAt(row, 0));
/* 165 */       Object value = this.table.getValueAt(row, 1);
/* 166 */       if (value == null) {
/* 167 */         this.txtDescription.setText("");
/*     */       } else {
/* 169 */         this.txtDescription.setText(value.toString());
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private class BeanTableModel
/*     */     implements TableModel
/*     */   {
/*     */     private BeanInfo info;
/*     */ 
/*     */     
/*     */     private Object object;
/*     */ 
/*     */     
/*     */     private List<PropertyDescriptor> properties;
/*     */ 
/*     */     
/*     */     private boolean showExpert;
/*     */ 
/*     */ 
/*     */     
/*     */     BeanTableModel(Object bean) {
/*     */       try {
/* 195 */         this.object = bean;
/* 196 */         this.info = Introspector.getBeanInfo(bean.getClass());
/* 197 */         this.properties = Collections.synchronizedList(new ArrayList<>());
/* 198 */         this.showExpert = false;
/* 199 */         refreshProperties();
/* 200 */       } catch (IntrospectionException e) {
/* 201 */         e.printStackTrace();
/* 202 */         SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(NPropertyGrid.this, e.toString()));
/* 203 */       } catch (Exception e) {
/* 204 */         e.printStackTrace();
/*     */       } 
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private void refreshProperties() {
/* 213 */       for (PropertyDescriptor prop : this.info.getPropertyDescriptors()) {
/* 214 */         if (NPropertyGrid.this.supportedProperties.contains(prop.getName()) && (!prop.isExpert() || this.showExpert)) {
/* 215 */           this.properties.add(prop);
/*     */         }
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     private Object getValueObject(PropertyDescriptor prop, Object obj, String newValue) {
/*     */       try {
/* 223 */         Object returnValue, readValue = prop.getReadMethod().invoke(obj, new Object[0]);
/* 224 */         if (readValue instanceof Integer) {
/* 225 */           returnValue = Integer.valueOf(newValue);
/* 226 */         } else if (readValue instanceof Double) {
/* 227 */           returnValue = Double.valueOf(newValue);
/* 228 */         } else if (readValue instanceof Long) {
/* 229 */           returnValue = Long.valueOf(newValue);
/* 230 */         } else if (readValue instanceof Short) {
/* 231 */           returnValue = Short.valueOf(newValue);
/* 232 */         } else if (readValue instanceof Byte) {
/* 233 */           returnValue = Byte.valueOf(newValue);
/* 234 */         } else if (readValue instanceof Float) {
/* 235 */           returnValue = Float.valueOf(newValue);
/* 236 */         } else if (readValue instanceof Boolean) {
/* 237 */           returnValue = Boolean.valueOf(newValue);
/*     */         } else {
/* 239 */           returnValue = newValue;
/*     */         } 
/* 241 */         return returnValue;
/* 242 */       } catch (InvocationTargetException e) {
/* 243 */         e.printStackTrace();
/* 244 */         SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(NPropertyGrid.this, e.getCause().toString()));
/* 245 */       } catch (Exception e) {
/* 246 */         e.printStackTrace();
/* 247 */         SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(NPropertyGrid.this, e.toString()));
/*     */       } 
/* 249 */       return null;
/*     */     }
/*     */     
/*     */     private PropertyDescriptor getDescriptor(int row) {
/* 253 */       return this.properties.get(row);
/*     */     }
/*     */     
/*     */     private boolean isAssignable(Class<?> to, Class<?> from) {
/* 257 */       if (to.isAssignableFrom(from)) {
/* 258 */         return true;
/*     */       }
/* 260 */       Class<?> primitiveFrom = (Class)NPropertyGrid.BOXED_TO_PRIMITIVE.get(from);
/* 261 */       if (primitiveFrom == null) {
/* 262 */         return false;
/*     */       }
/* 264 */       return to.isAssignableFrom(primitiveFrom);
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public int getRowCount() {
/* 274 */       if (this.info == null || this.info.getPropertyDescriptors() == null) {
/* 275 */         return 0;
/*     */       }
/* 277 */       return this.properties.size();
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     public int getColumnCount() {
/* 283 */       return 2;
/*     */     }
/*     */ 
/*     */     
/*     */     public String getColumnName(int columnIndex) {
/* 288 */       return (columnIndex == 0) ? "Property" : "Value";
/*     */     }
/*     */ 
/*     */     
/*     */     public Class<?> getColumnClass(int columnIndex) {
/* 293 */       if (columnIndex == 0) {
/* 294 */         return String.class;
/*     */       }
/* 296 */       return Object.class;
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     public boolean isCellEditable(int rowIndex, int columnIndex) {
/* 302 */       if (NPropertyGrid.this.isEditable) {
/* 303 */         if (columnIndex == 0) {
/* 304 */           return false;
/*     */         }
/* 306 */         PropertyDescriptor desc = this.properties.get(rowIndex);
/* 307 */         return (desc.getWriteMethod() != null);
/*     */       } 
/*     */       
/* 310 */       return false;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public Object getValueAt(int rowIndex, int columnIndex) {
/* 317 */       PropertyDescriptor prop = this.properties.get(rowIndex);
/* 318 */       if (columnIndex == 0) {
/* 319 */         return prop.getDisplayName();
/*     */       }
/*     */       try {
/* 322 */         return prop.getReadMethod().invoke(this.object, new Object[0]);
/* 323 */       } catch (Exception e) {
/* 324 */         return (e.getMessage() != null) ? e.getMessage() : e.getCause().getMessage();
/*     */       } 
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*     */     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
/*     */       try {
/* 332 */         PropertyDescriptor prop = this.properties.get(rowIndex);
/* 333 */         if (aValue instanceof String) {
/* 334 */           Object value = getValueObject(prop, this.object, (String)aValue);
/* 335 */           if (value != null && isAssignable(prop.getPropertyType(), value.getClass())) {
/* 336 */             prop.getWriteMethod().invoke(this.object, new Object[] { value });
/*     */           }
/*     */         } else {
/* 339 */           prop.getWriteMethod().invoke(this.object, new Object[] { aValue });
/*     */         } 
/* 341 */         prop.getReadMethod().invoke(this.object, new Object[0]);
/* 342 */       } catch (Exception e) {
/* 343 */         e.printStackTrace();
/*     */       } 
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void addTableModelListener(TableModelListener l) {}
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void removeTableModelListener(TableModelListener l) {}
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final class PropertyTable
/*     */     extends JTable
/*     */   {
/*     */     private static final long serialVersionUID = 1L;
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     private PropertyTable() {}
/*     */ 
/*     */ 
/*     */     
/*     */     public TableCellEditor getCellEditor(int row, int column) {
/* 374 */       if (NPropertyGrid.this.table.getModel() instanceof NPropertyGrid.BeanTableModel) {
/* 375 */         PropertyDescriptor prop = ((NPropertyGrid.BeanTableModel)NPropertyGrid.this.table.getModel()).getDescriptor(row);
/* 376 */         Class<?> propertyType = prop.getPropertyType();
/* 377 */         if (propertyType != null) {
/* 378 */           if (propertyType.equals(boolean.class)) {
/* 379 */             JComboBox<Boolean> booleanCmb = new JComboBox();
/* 380 */             booleanCmb.addItem(Boolean.TRUE);
/* 381 */             booleanCmb.addItem(Boolean.FALSE);
/* 382 */             return new DefaultCellEditor(booleanCmb);
/*     */           } 
/* 384 */           Object[] enumConstansts = propertyType.getEnumConstants();
/* 385 */           if (enumConstansts != null && enumConstansts.length > 0) {
/* 386 */             return new DefaultCellEditor(new JComboBox(enumConstansts));
/*     */           }
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/* 392 */       return super.getCellEditor(row, column);
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\swing\NPropertyGrid.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */