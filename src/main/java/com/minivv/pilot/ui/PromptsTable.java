package com.minivv.pilot.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.minivv.pilot.model.Prompt;
import com.minivv.pilot.model.AppSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PromptsTable extends JTable {
    private static final Logger LOG = Logger.getInstance(PromptsTable.class);
    private static final int NAME_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;
    private final PromptTableModel promptTableModel = new PromptTableModel();
    public final List<Prompt> prompts = new ArrayList<>();

    public PromptsTable() {
        setModel(promptTableModel);
        DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
        this.setCellEditor(editor);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void reset(AppSettings settings) {
        obtainPrompts(prompts, settings);
        promptTableModel.fireTableDataChanged();
    }

    public boolean isModified(AppSettings settings) {
        final ArrayList<Prompt> _prompts = new ArrayList<>();
        obtainPrompts(_prompts, settings);
        return !_prompts.equals(prompts);
    }

    private void obtainPrompts(@NotNull List<Prompt> prompts, AppSettings settings) {
        prompts.clear();
        prompts.addAll(settings.prompts.getPrompts());
    }

    public void addPrompt(Prompt prompt) {
        prompts.add(prompt);
        promptTableModel.fireTableRowsInserted(prompts.size() - 1, prompts.size() - 1);
    }

    public void commit(AppSettings settings) {
        settings.prompts.setPrompts(new ArrayList<>(this.prompts));
    }

    public void removeSelectedPrompts() {
        int[] selectedRows = getSelectedRows();
        if (selectedRows.length == 0) return;
        Arrays.sort(selectedRows);
        final int originalRow = selectedRows[0];
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            final int selectedRow = selectedRows[i];
            if (isValidRow(selectedRow)) {
                prompts.remove(selectedRow);
                promptTableModel.fireTableRowsDeleted(selectedRow, selectedRow);
            }
        }
        promptTableModel.fireTableDataChanged();
        if (originalRow < getRowCount()) {
            setRowSelectionInterval(originalRow, originalRow);
        } else if (getRowCount() > 0) {
            final int index = getRowCount() - 1;
            setRowSelectionInterval(index, index);
        }
    }

    private boolean isValidRow(int selectedRow) {
        return selectedRow >= 0 && selectedRow < prompts.size();
    }

    public void resetDefaultAliases() {
        AppSettings.resetDefaultPrompts(prompts);
        promptTableModel.fireTableDataChanged();
    }

    public boolean editPrompt() {
        if (getSelectedRowCount() != 1) {
            return false;
        }
        //进入行内编辑模式
        return editCellAt(getSelectedRow(), getSelectedColumn());
    }

    private class PromptTableModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return prompts.size();
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            String str = (String) value;
            if (str.length() != 0) {
                //修改prompts中的值
                if (columnIndex == NAME_COLUMN) {
                    prompts.get(rowIndex).setOption(str);
                } else if (columnIndex == VALUE_COLUMN) {
                    prompts.get(rowIndex).setSnippet(str);
                }
                promptTableModel.fireTableDataChanged();
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final Prompt pair = prompts.get(rowIndex);
            switch (columnIndex) {
                case NAME_COLUMN:
                    return pair.getOption();
                case VALUE_COLUMN:
                    return pair.getSnippet();
            }
            LOG.error("Wrong indices");
            return null;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case NAME_COLUMN:
                    return "Option";
                case VALUE_COLUMN:
                    return "Snippet";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
    }
}
