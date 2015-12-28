package facets.gui.components.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.dyno.visual.swing.layouts.Bilateral;
import org.dyno.visual.swing.layouts.Constraints;
import org.dyno.visual.swing.layouts.GroupLayout;
import org.dyno.visual.swing.layouts.Leading;
import org.dyno.visual.swing.layouts.Trailing;

import facets.gui.components.controller.FacetSearchController;
import facets.gui.components.models.ClassTypeHistoryDataModel;
import facets.gui.components.models.ClassTypeResultDataModel;
import facets.gui.components.models.FacetTypeResultDataModel;
import facets.gui.components.models.FacetValueRecordDataModel;
import facets.gui.components.models.QueryResultSetDataModel;

//VS4E -- DO NOT REMOVE THIS LINE!
public class FacetSearchView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel entryPanel;
	private JPanel historyPanelData;
	private JButton historyPanelRemove;
	private JScrollPane classScrollPanel;
	private JScrollPane facetScrollPane1;
	private JSplitPane facetSplitPanel;
	private JPanel facetPanel;
	private JList classList;
	private JList facetList;
	private JPanel facetValuePanel;
	private JPanel resultPanel;
	private JComboBox typeSelectionCombo;
	private JButton openButton;
	private JFileChooser directorychooser;
	private FacetSearchController controller;
	private JPanel busypanel;
	// private JXBusyLabel busylabel;
	private JButton previous;
	private JButton next;
	private JSplitPane jSplitPane0;
	private JPanel jPanel2;
	private JPanel jPanel0;
	private JScrollPane jScrollPane0;
	private JList facetvaluelist;
	private JButton recordFacetValueInterval;
	private JButton specificFacetValue;
	private JButton unknownFacetValue;
	private JButton removerecordedfacetvalue;
	private JList recordedfacetvaluelist;
	private JScrollPane jScrollPane2;
	private JTable currentStateResultSetTable;
	private JButton showcurrentresultquery;
	private JButton showcurrentresult;
	private JScrollPane currentResultSetTable;
	private static final String PREFERRED_LOOK_AND_FEEL = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

	public FacetSearchView() {
		// busylabel = new JXBusyLabel();
		initComponents();
		extraInitComponents();

	}

	private JPanel getHistoryPanelDataView() {
		if (historyPanelData == null) {
			historyPanelData = new JPanel();
			historyPanelData.setBackground(new Color(214, 217, 223));
			historyPanelData.setBorder(BorderFactory.createEtchedBorder(
					EtchedBorder.LOWERED, null, null));
			historyPanelData
					.setFont(new Font("Times New Roman", Font.PLAIN, 8));
			historyPanelData.setAutoscrolls(true);
			historyPanelData.setLayout(new BoxLayout(historyPanelData,
					BoxLayout.X_AXIS));
		}
		return historyPanelData;
	}

	public JPanel getHistoryPanelDataView(boolean toControl) {
		if (toControl)
			return historyPanelData;
		return null;

	}

	private void initComponents() {
		setTitle("Faceted Search Demo");
		setBackground(Color.white);
		setForeground(Color.white);
		setAlwaysOnTop(true);
		setLayout(new GroupLayout());
		add(getEntryPanel(), new Constraints(new Bilateral(9, 12, 0),
				new Leading(8, 41, 10, 10)));
		add(getFacetPanel(), new Constraints(new Leading(9, 284, 10, 10),
				new Bilateral(55, 12, 397)));
		add(getResultPanel(), new Constraints(new Bilateral(612, 12, 216),
				new Bilateral(117, 12, 0)));
		add(getHistoryPanelDataView(), new Constraints(
				new Bilateral(305, 6, 2), new Leading(52, 53, 445, 445)));
		add(getFacetValuePanel(), new Constraints(new Leading(299, 295, 234,
				508), new Bilateral(117, 12, 0)));
		setSize(913, 552);
	}

	private JScrollPane getCurrentResultSetTable() {
		if (currentResultSetTable == null) {
			currentResultSetTable = new JScrollPane();
			currentResultSetTable.setBackground(new Color(214, 217, 223));
			currentResultSetTable.setBorder(BorderFactory.createTitledBorder(
					null, "Results", TitledBorder.LEADING,
					TitledBorder.ABOVE_TOP, new Font("Bitstream Vera Sans",
							Font.BOLD, 12), new Color(59, 59, 59)));
			currentResultSetTable.setOpaque(true);
			currentResultSetTable.setAutoscrolls(true);
			currentResultSetTable
					.setViewportView(getCurrentQueryResultSetTable());
		}
		return currentResultSetTable;
	}

	private JButton getShowcurrentresultButton() {
		if (showcurrentresult == null) {
			showcurrentresult = new JButton();
			showcurrentresult.setText("result");
			showcurrentresult.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					showcurrentresultActionActionPerformed(event);
				}
			});
		}
		return showcurrentresult;
	}

	private JButton getShowcurrentresultqueryButton() {
		if (showcurrentresultquery == null) {
			showcurrentresultquery = new JButton();
			showcurrentresultquery.setText("query");
			showcurrentresultquery.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					showcurrentresultqueryActionActionPerformed(event);
				}
			});
		}
		return showcurrentresultquery;
	}

	private JTable getCurrentQueryResultSetTable() {
		if (currentStateResultSetTable == null) {
			currentStateResultSetTable = new JTable();
			currentStateResultSetTable.setModel(new DefaultTableModel());
		}
		return currentStateResultSetTable;
	}

	public JTable getCurrentQueryResultSetTable(boolean toControl) {

		if (toControl)
			return currentStateResultSetTable;
		return null;
	}

	private JList getRecordedfacetvaluelist() {
		if (recordedfacetvaluelist == null) {
			recordedfacetvaluelist = new JList();
			recordedfacetvaluelist.setBorder(BorderFactory.createEmptyBorder(0,
					0, 0, 0));
			recordedfacetvaluelist.setFont(new Font("Dialog", Font.PLAIN, 16));
			recordedfacetvaluelist
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			DefaultListModel listModel = new DefaultListModel();
			recordedfacetvaluelist.setModel(listModel);
		}
		return recordedfacetvaluelist;
	}

	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getRecordedfacetvaluelist());
		}
		return jScrollPane2;
	}

	private JButton getRemoverecordedfacetvalue() {
		if (removerecordedfacetvalue == null) {
			removerecordedfacetvalue = new JButton();
			removerecordedfacetvalue.setText("remove");
			removerecordedfacetvalue.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					removerecordedfacetvalueActionActionPerformed(event);
				}
			});
		}
		return removerecordedfacetvalue;
	}

	public JList getRecordedFacetValueList(boolean toControl) {

		if (toControl)
			return recordedfacetvaluelist;
		return null;
	}

	private JButton getSpecificFacetValue() {
		if (specificFacetValue == null) {
			specificFacetValue = new JButton();
			specificFacetValue.setText("specific");
			specificFacetValue.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					specificFacetValueActionActionPerformed(event);
				}
			});
		}
		return specificFacetValue;
	}

	private JButton getRecordFacetValueInterval() {
		if (recordFacetValueInterval == null) {
			recordFacetValueInterval = new JButton();
			recordFacetValueInterval.setText("interval");
			recordFacetValueInterval.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					recordFacetValueIntervalActionActionPerformed(event);
				}
			});
		}
		return recordFacetValueInterval;
	}

	private JButton getUnknownFacetValue() {
		if (unknownFacetValue == null) {
			unknownFacetValue = new JButton();
			unknownFacetValue.setText("?");
			unknownFacetValue.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					unknownFacetValueActionActionPerformed(event);
				}
			});
		}
		return unknownFacetValue;
	}

	private JList getFacetValueList() {
		if (facetvaluelist == null) {
			facetvaluelist = new JList();
			facetvaluelist.setBorder(BorderFactory.createTitledBorder(null,
					"Facet Values", TitledBorder.LEADING,
					TitledBorder.ABOVE_TOP, new Font("Bitstream Vera Sans",
							Font.BOLD, 12), new Color(59, 59, 59)));
			facetvaluelist.setFont(new Font("Dialog", Font.PLAIN, 16));
			facetvaluelist
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			DefaultListModel listModel = new DefaultListModel();
			facetvaluelist.setModel(listModel);
			facetvaluelist.setVisibleRowCount(0);
		}
		return facetvaluelist;
	}

	public JList getFacetValueList(boolean toControl) {
		if (toControl)
			return facetvaluelist;
		return null;

	}

	private JScrollPane getJScrollPane0() {
		if (jScrollPane0 == null) {
			jScrollPane0 = new JScrollPane();
			jScrollPane0.setAutoscrolls(true);
			jScrollPane0.setViewportView(getFacetValueList());
		}
		return jScrollPane0;
	}

	private JPanel getJPanel0() {
		if (jPanel0 == null) {
			jPanel0 = new JPanel();
			jPanel0.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			jPanel0.setOpaque(false);
			jPanel0.setAutoscrolls(true);
			jPanel0.setLayout(new GroupLayout());
			jPanel0.add(getJScrollPane0(), new Constraints(new Bilateral(5, 6,
					25), new Leading(2, 223, 10, 10)));
			jPanel0.add(getUnknownFacetValue(), new Constraints(new Leading(
					123, 52, 10, 10), new Leading(237, 6, 6)));
			jPanel0.add(getRecordFacetValueInterval(), new Constraints(
					new Leading(11, 103, 10, 10), new Trailing(6, 39, 237)));
			jPanel0.add(getSpecificFacetValue(), new Constraints(new Bilateral(
					181, 6, 75), new Leading(237, 6, 6)));
		}
		return jPanel0;
	}

	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setBackground(Color.white);
			jPanel2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			jPanel2.setOpaque(false);
			jPanel2.setAutoscrolls(true);
			jPanel2.setLayout(new GroupLayout());
			jPanel2.add(getRemoverecordedfacetvalue(), new Constraints(
					new Bilateral(6, 6, 74), new Trailing(6, 28, 65, 198)));
			jPanel2.add(getJScrollPane2(), new Constraints(new Bilateral(7, 6,
					25), new Leading(6, 91, 10, 10)));
		}
		return jPanel2;
	}

	private JSplitPane getJSplitPane0() {
		if (jSplitPane0 == null) {
			jSplitPane0 = new JSplitPane();
			jSplitPane0.setBackground(new Color(214, 217, 223));
			jSplitPane0.setDividerLocation(132);
			jSplitPane0.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane0.setOpaque(true);
			jSplitPane0.setAutoscrolls(true);
			jSplitPane0.setTopComponent(getJPanel2());
			jSplitPane0.setBottomComponent(getJPanel0());
		}
		return jSplitPane0;
	}

	private JButton getNext() {
		if (next == null) {
			next = new JButton();
			next.setText(">");
			next.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					nextActionActionPerformed(event);
				}
			});
		}
		return next;
	}

	private JButton getPrevious() {
		if (previous == null) {
			previous = new JButton();
			previous.setText("<");
			previous.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					previousActionActionPerformed(event);
				}
			});
		}
		return previous;
	}

	private JComboBox getTypeSelectionCombo() {
		if (typeSelectionCombo == null) {
			typeSelectionCombo = new JComboBox();
			typeSelectionCombo.setModel(new DefaultComboBoxModel());
			typeSelectionCombo.setEditable(true);
			typeSelectionCombo.setSelectedIndex(-1);
			typeSelectionCombo.setEnabled(false);
			typeSelectionCombo.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							onbusylabel();
							typeSelectionComboActionActionPerformed();
							offbusylabel();
						}
					});

				}
			});
		}
		return typeSelectionCombo;
	}

	private JFileChooser getDirectorychooser() {
		return directorychooser;
	}

	public void registerFacetSearchController(FacetSearchController con) {
		controller = con;
	}

	private FacetSearchController getFacetSearchController() {
		return controller;
	}

	private void extraInitComponents() {

		directorychooser = new JFileChooser() {

			@Override
			public void approveSelection() {
				if (getSelectedFile().isFile()) {
					return;
				} else
					super.approveSelection();
			}

		};
		directorychooser
				.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

	}

	private JButton getOpenButton() {
		if (openButton == null) {
			openButton = new JButton();
			openButton.setText("Load");
			openButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					onbusylabel();
					jButton0ActionActionPerformed(event);
					offbusylabel();
				}
			});
		}
		return openButton;
	}

	// private JComboBox getTypeSelectionComboBox() {
	// if (typeSelectionCombo == null) {
	// typeSelectionCombo = new JComboBox();
	// typeSelectionCombo.setModel(new DefaultComboBoxModel());
	// typeSelectionCombo.setEditable(true);
	// typeSelectionCombo.setSelectedIndex(-1);
	// typeSelectionCombo.setEnabled(false);
	// }
	// return typeSelectionCombo;
	// }

	public JComboBox getTypeSelectionComboBox(boolean toControl) {
		if (toControl)
			return typeSelectionCombo;
		else
			return null;
	}

	private JPanel getResultPanel() {
		if (resultPanel == null) {
			resultPanel = new JPanel();
			resultPanel.setBackground(new Color(214, 217, 223));
			resultPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			resultPanel.setLayout(new GroupLayout());
			resultPanel.add(getCurrentResultSetTable(), new Constraints(
					new Bilateral(6, 6, 25), new Leading(6, 384, 10, 10)));
			resultPanel.add(getShowcurrentresultqueryButton(), new Constraints(
					new Leading(6, 117, 10, 10), new Leading(390, 10, 10)));
			resultPanel.add(getShowcurrentresultButton(), new Constraints(
					new Bilateral(129, 6, 81), new Leading(390, 6, 6)));
		}
		return resultPanel;
	}

	private JPanel getFacetValuePanel() {
		if (facetValuePanel == null) {
			facetValuePanel = new JPanel();
			facetValuePanel.setBackground(new Color(214, 217, 223));
			facetValuePanel.setBorder(null);
			facetValuePanel.setLayout(new GroupLayout());
			facetValuePanel.add(getJSplitPane0(), new Constraints(
					new Bilateral(6, 6, 264), new Leading(6, 411, 6, 6)));
		}
		return facetValuePanel;
	}

	private JSplitPane getFacetSplitPanel() {
		if (facetSplitPanel == null) {
			facetSplitPanel = new JSplitPane();
			facetSplitPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
					0));
			facetSplitPanel.setDividerLocation(149);
			facetSplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
			facetSplitPanel.setAutoscrolls(true);
			facetSplitPanel.setInheritsPopupMenu(true);
			facetSplitPanel.setTopComponent(getClassScrollPanel());
			facetSplitPanel.setBottomComponent(getFacetScrollPane1());
		}
		return facetSplitPanel;
	}

	private JList getFacetList() {
		if (facetList == null) {
			facetList = new JList();
			facetList.setBorder(BorderFactory.createTitledBorder(null,
					"Facet Suggestions", TitledBorder.LEADING,
					TitledBorder.ABOVE_TOP, new Font("Bitstream Vera Sans",
							Font.BOLD, 12), new Color(59, 59, 59)));
			facetList.setFont(new Font("Dialog", Font.PLAIN, 16));
			facetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			DefaultListModel listModel = new DefaultListModel();
			facetList.setModel(listModel);
			facetList.setVisibleRowCount(0);
			facetList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent event) {
					if (event.getValueIsAdjusting() == false)
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								onbusylabel();
								facetListListSelectionValueChanged();
								offbusylabel();
							}
						});
				}
			});
		}
		return facetList;
	}

	public JList getFacetTypeList(boolean toControl) {
		if (toControl)
			return facetList;
		return facetList;
	}

	private JList getClassList() {
		if (classList == null) {
			classList = new JList();
			classList.setBorder(BorderFactory.createTitledBorder(null, "Types",
					TitledBorder.LEADING, TitledBorder.ABOVE_TOP, new Font(
							"Bitstream Vera Sans", Font.BOLD, 12), new Color(
							59, 59, 59)));
			classList.setFont(new Font("Dialog", Font.PLAIN, 16));
			classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			DefaultListModel listModel = new DefaultListModel();
			classList.setModel(listModel);
			classList.setVisibleRowCount(0);
			classList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent event) {
					if (event.getValueIsAdjusting() == false)
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								onbusylabel();
								classListListSelectionValueChanged();
								offbusylabel();
							}
						});
				}
			});
		}
		return classList;
	}

	public JList getClassTypeList(boolean toControl) {
		if (toControl)
			return classList;
		return null;
	}

	private JPanel getFacetPanel() {
		if (facetPanel == null) {
			facetPanel = new JPanel();
			facetPanel.setBackground(new Color(214, 217, 223));
			facetPanel.setBorder(null);
			facetPanel.setLayout(new GroupLayout());
			facetPanel.add(getNext(), new Constraints(new Trailing(6, 131, 10,
					10), new Trailing(6, 83, 399)));
			facetPanel.add(getPrevious(), new Constraints(new Bilateral(6, 143,
					38), new Trailing(6, 83, 399)));
			facetPanel.add(getFacetSplitPanel(), new Constraints(new Bilateral(
					6, 6, 27), new Bilateral(9, 39, 60)));
		}
		return facetPanel;
	}

	private JScrollPane getFacetScrollPane1() {
		if (facetScrollPane1 == null) {
			facetScrollPane1 = new JScrollPane();
			facetScrollPane1.setViewportView(getFacetList());
		}
		return facetScrollPane1;
	}

	private JScrollPane getClassScrollPanel() {
		if (classScrollPanel == null) {
			classScrollPanel = new JScrollPane();
			classScrollPanel.setViewportView(getClassList());
		}
		return classScrollPanel;
	}

	private JPanel getEntryPanel() {
		if (entryPanel == null) {
			entryPanel = new JPanel();
			entryPanel.setBackground(new Color(214, 217, 223));
			entryPanel.setBorder(null);
			entryPanel.setLayout(new GroupLayout());
			entryPanel.add(getOpenButton(), new Constraints(new Leading(19,
					106, 10, 10), new Leading(6, 6, 6)));
			entryPanel.add(getTypeSelectionCombo(), new Constraints(
					new Leading(145, 626, 10, 10), new Leading(8, 6, 6)));
		}
		return entryPanel;
	}

	private static void installLnF() {
		try {
			String lnfClassname = PREFERRED_LOOK_AND_FEEL;
			if (lnfClassname == null)
				lnfClassname = UIManager.getCrossPlatformLookAndFeelClassName();
			UIManager.setLookAndFeel(lnfClassname);
		} catch (Exception e) {
			System.err.println("Cannot install " + PREFERRED_LOOK_AND_FEEL
					+ " on this platform:" + e.getMessage());
		}
	}

	/**
	 * Main entry of the class. Note: This class is only created so that you can
	 * easily preview the result at runtime. It is not expected to be managed by
	 * the designer. You can modify it as you like.
	 */
	public static void main(String[] args) {
		installLnF();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				FacetSearchView frame = new FacetSearchView();

				FacetSearchController controller = new FacetSearchController();

				ClassTypeResultDataModel classmodel = new ClassTypeResultDataModel(
						controller);

				ClassTypeHistoryDataModel classhistorymodel = new ClassTypeHistoryDataModel(
						controller);

				FacetTypeResultDataModel facetmodel = new FacetTypeResultDataModel(
						controller);

				FacetValueRecordDataModel facetvaluemodel = new FacetValueRecordDataModel(
						controller);

				QueryResultSetDataModel resultmodel = new QueryResultSetDataModel(
						controller);

				controller.registerClassTypeResultDataModel(classmodel);

				controller.registerClassTypeHistoryDataModel(classhistorymodel);

				controller.registerFacetTypeResultDataModel(facetmodel);

				controller.registerFacetValueRecordDataModel(facetvaluemodel);

				controller.registerQueryResultSetDataModel(resultmodel);

				controller.registerFacetSearchView(frame);

				frame.registerFacetSearchController(controller);

				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
				frame.setTitle("FacetSearchView");
				frame.getContentPane().setPreferredSize(frame.getSize());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	private void jButton0ActionActionPerformed(ActionEvent event) {

		if (event.getSource() == openButton) {
			int returnVal = directorychooser
					.showOpenDialog(FacetSearchView.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = directorychooser.getSelectedFile();
				if (file.isDirectory())
					controller.performDataFileUpload(file);
			} else {

			}

		}
	}

	private void typeSelectionComboActionActionPerformed() {

		if (!typeSelectionCombo.isPopupVisible()
				&& typeSelectionCombo.getSelectedIndex() >= 0) {
			controller.performComboBoxSelection(typeSelectionCombo
					.getSelectedItem().toString());
			openButton.setEnabled(false);
		}

	}

	private void classListListSelectionValueChanged() {

		if (classList.getSelectedIndex() >= 0) {

			controller.performClassTypeSelection(classList.getSelectedIndex(),
					classList.getSelectedValue().toString());

		}

	}

	private void facetListListSelectionValueChanged() {

		if (facetList.getSelectedIndex() >= 0) {

			controller.performFacetTypeSelection(facetList.getSelectedIndex(),
					facetList.getSelectedValue().toString());

		}

	}

	private void onbusylabel() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// busylabel.setBusy(true);

			}
		});
	}

	private void offbusylabel() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// busylabel.setBusy(false);
			}
		});

	}

	private void previousActionActionPerformed(ActionEvent event) {

		if (historyPanelData.getComponentCount() > 0) {

			controller.previousActionPerformed();
		}

	}

	private void nextActionActionPerformed(ActionEvent event) {

		if (historyPanelData.getComponentCount() > 0) {

			controller.nextActionPerformed();
		}
	}

	private void recordFacetValueIntervalActionActionPerformed(ActionEvent event) {

		if (historyPanelData.getComponentCount() > 0
				&& facetvaluelist.getModel().getSize() > 0) {

			controller.performFacetValueIntervalSelection();

		}
	}

	private void specificFacetValueActionActionPerformed(ActionEvent event) {

		if (historyPanelData.getComponentCount() > 0
				&& facetvaluelist.getModel().getSize() > 0
				&& facetvaluelist.getSelectedIndex() >= 0) {

			controller.performFacetValueSpecificSelection(facetvaluelist
					.getSelectedIndex());

		}

	}

	private void unknownFacetValueActionActionPerformed(ActionEvent event) {

		if (historyPanelData.getComponentCount() > 0
				&& facetvaluelist.getModel().getSize() > 0) {

			controller.performFacetValueUnknownSelection();

		}

	}

	private void removerecordedfacetvalueActionActionPerformed(ActionEvent event) {

		if (recordedfacetvaluelist.getSelectedIndex() >= 0)
			controller.performRemoveRecordedFacetValue(recordedfacetvaluelist.getSelectedValue());

	}

	private void showcurrentresultActionActionPerformed(ActionEvent event) {

		if (historyPanelData.getComponentCount() > 0)
			controller.performShowCurrentStatusResultSet();

	}

	private void showcurrentresultqueryActionActionPerformed(ActionEvent event) {
		if (historyPanelData.getComponentCount() > 0)
			controller.performShowCurrentStatusResultQuery();

	}
}
