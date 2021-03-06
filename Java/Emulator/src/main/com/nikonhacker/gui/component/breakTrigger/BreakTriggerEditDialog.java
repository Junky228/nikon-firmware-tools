package com.nikonhacker.gui.component.breakTrigger;

import com.nikonhacker.Constants;
import com.nikonhacker.Format;
import com.nikonhacker.disassembly.fr.FrCPUState;
import com.nikonhacker.disassembly.tx.TxCPUState;
import com.nikonhacker.emu.trigger.BreakTrigger;
import com.nikonhacker.emu.trigger.condition.MemoryValueBreakCondition;
import com.nikonhacker.gui.component.cpu.CPUStateComponent;
import com.nikonhacker.gui.component.cpu.FrCPUStateComponent;
import com.nikonhacker.gui.component.cpu.TxCPUStateComponent;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class BreakTriggerEditDialog extends JDialog {
    private final CPUStateComponent cpuStateComponent;
    private final BreakTrigger trigger;
    private final JTextField nameField;
    private final JCheckBox enabledCheckBox;
    private final JCheckBox enableLogCheckBox;
    private final JCheckBox enableBreakCheckBox;
    private final JTextField interruptToTriggerField;
    private final JTextField interruptToWithdramField;
    private final JCheckBox startLoggingCheckBox;
    private final JCheckBox stopLoggingCheckBox;
    private final CPUStateComponent newCpuStateComponent;

    public BreakTriggerEditDialog(JDialog owner, int chip, final BreakTrigger trigger, String title) {
        super(owner, title, true);
        this.trigger = trigger;

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Name: "));
        nameField = new JTextField(20);
        nameField.setText(trigger.getName());
        topPanel.add(nameField);

        topPanel.add(new JLabel("Enabled: "));
        enabledCheckBox = new JCheckBox();
        enabledCheckBox.setSelected(trigger.isEnabled());
        topPanel.add(enabledCheckBox);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        // CPU condition
        if (chip == Constants.CHIP_FR) {
            cpuStateComponent = new FrCPUStateComponent((FrCPUState)trigger.getCpuStateValues(), (FrCPUState)trigger.getCpuStateFlags(), true);
        }
        else {
            cpuStateComponent = new TxCPUStateComponent((TxCPUState)trigger.getCpuStateValues(), (TxCPUState)trigger.getCpuStateFlags(), true);
        }
        cpuStateComponent.refresh();
        tabbedPane.addTab("Register conditions", null, cpuStateComponent);

        // CPU modifications
        if (chip == Constants.CHIP_FR) {
            newCpuStateComponent = new FrCPUStateComponent((FrCPUState)trigger.getNewCpuStateValues(), (FrCPUState)trigger.getNewCpuStateFlags(), true);
        }
        else {
            newCpuStateComponent = new TxCPUStateComponent((TxCPUState)trigger.getNewCpuStateValues(), (TxCPUState)trigger.getNewCpuStateFlags(), true);
        }
        newCpuStateComponent.refresh();
        tabbedPane.addTab("Register modifications", null, newCpuStateComponent);

        // Memory conditions
        tabbedPane.addTab("Memory conditions", null, new MemoryConditionListComponent());

        // Resulting actions
        JPanel actionPanel = new JPanel(new MigLayout());

        actionPanel.add(new JLabel("Enable break"));
        enableBreakCheckBox = new JCheckBox();
        enableBreakCheckBox.setSelected(trigger.getMustBreak());
        actionPanel.add(enableBreakCheckBox, "wrap");

        actionPanel.add(new JLabel("Enable log"));
        enableLogCheckBox = new JCheckBox();
        enableLogCheckBox.setSelected(trigger.getMustBeLogged());
        actionPanel.add(enableLogCheckBox, "wrap");

        actionPanel.add(new JLabel("Trigger interrupt  0x"));
        interruptToTriggerField = new JTextField(10);
        if (trigger.getInterruptToRequest() != null) {
            interruptToTriggerField.setText(Format.asHex(trigger.getInterruptToRequest(), 2));
        }
        actionPanel.add(interruptToTriggerField, "wrap");

        actionPanel.add(new JLabel("Withdraw interrupt  0x"));
        interruptToWithdramField = new JTextField(10);
        if (trigger.getInterruptToWithdraw() != null) {
            interruptToWithdramField.setText(Format.asHex(trigger.getInterruptToWithdraw(), 2));
        }
        actionPanel.add(interruptToWithdramField, "wrap");

        actionPanel.add(new JLabel("Start logging"));
        startLoggingCheckBox = new JCheckBox();
        startLoggingCheckBox.setSelected(trigger.getMustStartLogging());
        actionPanel.add(startLoggingCheckBox, "wrap");

        actionPanel.add(new JLabel("Stop logging"));
        stopLoggingCheckBox = new JCheckBox();
        stopLoggingCheckBox.setSelected(trigger.getMustStopLogging());
        actionPanel.add(stopLoggingCheckBox, "wrap");

        tabbedPane.addTab("Actions", null, actionPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        bottomPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(cancelButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void save() {
        trigger.setName(nameField.getText());
        trigger.setEnabled(enabledCheckBox.isSelected());
        cpuStateComponent.saveValuesAndFlags();
        trigger.setMustBeLogged(enableLogCheckBox.isSelected());
        trigger.setMustBreak(enableBreakCheckBox.isSelected());
        trigger.setInterruptToRequest(StringUtils.isBlank(interruptToTriggerField.getText())?null:Format.parseIntHexField(interruptToTriggerField));
        trigger.setInterruptToWithdraw(StringUtils.isBlank(interruptToWithdramField.getText())?null:Format.parseIntHexField(interruptToWithdramField));
        trigger.setMustStartLogging(startLoggingCheckBox.isSelected());
        trigger.setMustStopLogging(stopLoggingCheckBox.isSelected());
        newCpuStateComponent.saveValuesAndFlags();
        dispose();
    }

    private class MemoryConditionListComponent extends JComponent {
        final JList memoryConditionJList;

        public MemoryConditionListComponent() {
            setLayout(new BorderLayout());
            final List<MemoryValueBreakCondition> conditions = trigger.getMemoryValueBreakConditions();

            memoryConditionJList = new JList(createListModel(conditions));
            memoryConditionJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            memoryConditionJList.setLayoutOrientation(JList.VERTICAL);
            memoryConditionJList.setVisibleRowCount(10);
            JScrollPane listScroller = new JScrollPane(memoryConditionJList);
            listScroller.setPreferredSize(new Dimension(250, 300));
            add(listScroller, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new GridLayout(3, 1));

            JButton addButton = new JButton("Add");
            addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addCondition();
                }
            });
            rightPanel.add(addButton);

            JButton editButton = new JButton("Edit");
            editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editCondition(memoryConditionJList.getSelectedIndex());
                }
            });
            rightPanel.add(editButton);

            JButton deleteButton = new JButton("Delete");
            deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteCondition(memoryConditionJList.getSelectedIndex());
                }
            });
            rightPanel.add(deleteButton);

            add(rightPanel, BorderLayout.EAST);
        }

        private void editCondition(int index) {
            if (index != -1) {
                editCondition(trigger.getMemoryValueBreakConditions().get(index));
                memoryConditionJList.setSelectedIndex(index);
            }
        }

        private DefaultListModel createListModel(List<MemoryValueBreakCondition> conditions) {
            DefaultListModel listModel = new DefaultListModel();
            for (MemoryValueBreakCondition condition : conditions) {
                listModel.addElement(condition);
            }
            return listModel;
        }

        private void deleteCondition(int index) {
            if (index != -1) {
                trigger.getMemoryValueBreakConditions().remove(index);
                updateConditions();
            }
        }

        void addCondition() {
            MemoryValueBreakCondition condition = new MemoryValueBreakCondition(trigger);
            trigger.getMemoryValueBreakConditions().add(condition);
            memoryConditionJList.setSelectedIndex(trigger.getMemoryValueBreakConditions().size() - 1);
            editCondition(condition);
        }

        void editCondition(MemoryValueBreakCondition condition) {
            new MemoryConditionEditDialog(BreakTriggerEditDialog.this, condition, "Edit memory value condition").setVisible(true);
            updateConditions();
        }

        private void updateConditions() {
            memoryConditionJList.setModel(createListModel(trigger.getMemoryValueBreakConditions()));
        }

    }
}
