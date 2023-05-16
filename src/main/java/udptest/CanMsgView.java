package udptest;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class CanMsgView extends TableView<CanMsg> {

    protected interface ICellFunction {
        String cellFunction(CanMsg canMsg) throws Exception;
    }

    protected class CellFactoryInterface
            implements Callback<TableColumn.CellDataFeatures<CanMsg, String>, ObservableValue<String>> {

        final ICellFunction cf;

        CellFactoryInterface(final ICellFunction cf) {
            this.cf = cf;
        }

        @Override
        public ObservableValue<String> call(TableColumn.CellDataFeatures<CanMsg, String> param) {
            final CanMsg canMsg = param.getValue();
            String s;
            try {
                s = cf.cellFunction(canMsg);
            } catch (Exception e) {
                s = e.getMessage();
            }
            return new SimpleStringProperty(s);
        }
    }

    protected abstract class CellFactoryBase
            implements Callback<TableColumn.CellDataFeatures<CanMsg, String>, ObservableValue<String>> {
        abstract String cellFunction(final CanMsg canMsg) throws Exception;

        @Override
        public ObservableValue<String> call(TableColumn.CellDataFeatures<CanMsg, String> param) {
            final CanMsg canMsg = param.getValue();
            String s;
            try {
                s = cellFunction(canMsg);
            } catch (Exception e) {
                s = e.getMessage();
            }
            return new SimpleStringProperty(s);
        }
    }

    protected void addColumn(final String name, final ICellFunction cf) {
        TableColumn<CanMsg, String> column = new TableColumn<>();
        column.setText(name);
        column.setCellValueFactory(new CellFactoryInterface(cf));
        getColumns().add(column);
    }

    protected void addColumn(final String name, final CellFactoryBase cfni) {
        TableColumn<CanMsg, String> column = new TableColumn<>();
        column.setText(name);
        column.setCellValueFactory(cfni);
        getColumns().add(column);
    }

}
