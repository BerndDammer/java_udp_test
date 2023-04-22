package udptest;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;

public class GridPane2 extends GridPane {
	protected enum INSERTING {
		HGROW, CENTER, FILL;
	}
	protected GridPane2(boolean e)
	{
		if(e)extend();
	}
	private static final Insets insets = new Insets(1.0);

	protected void add(Node node, int column, int row, INSERTING inserting) {
		add(node, column, row);
		insert(node, inserting);
	}

	protected void add(Node node, int column, int row, int colspan, int rowspan, INSERTING inserting)
	{	
		add(node, column,row,colspan,rowspan);
		insert(node, inserting);
	}
	private final void insert(Node node, INSERTING inserting)
	{
		switch (inserting) {
		case CENTER:
			setHalignment(node, HPos.CENTER);
			setValignment(node, VPos.CENTER);
			break;
		case HGROW:
			setHalignment(node, HPos.CENTER);
			setValignment(node, VPos.CENTER);
			setHgrow(node, Priority.ALWAYS);
			setFillWidth(node, true);
			break;
		case FILL:
			setHalignment(node, HPos.CENTER);
			setValignment(node, VPos.CENTER);
			setHgrow(node, Priority.ALWAYS);
			setFillWidth(node, true);
			setVgrow(node, Priority.ALWAYS);
			setFillHeight(node, true);
			break;
		default:
			break;
		}
		setMargin( node, insets);
	}

	protected void extend() {
		setVgap(4.0);
		setHgap(4.0);

		ColumnConstraints c = new ColumnConstraints();
		c.setHgrow(Priority.SOMETIMES);
		for (int i = 0; i < 3; i++) {
			getColumnConstraints().add(c);
		}
		RowConstraints r = new RowConstraints();
		r.setVgrow(Priority.SOMETIMES);
		for (int i = 0; i < 6; i++) {
			getRowConstraints().add(r);
		}
		setBorder(new Border(new BorderStroke(Color.DARKGRAY,
				  BorderStrokeStyle.SOLID,
				  CornerRadii.EMPTY, new BorderWidths(3.0))));

	}
}
