package application.menu.analyse;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import konten.Bestandskonto;
import konten.Konto;
/**
 * EröffnungsbilanzEinsehen dient zum Einsehen der bei der Eröffnungsbilanz erstellten Konten mit deren Anfangsbeständen.
 */
public class EroeffnungsbilanzEinsehenController implements Initializable {

	@FXML
	private VBox container;
	private GridPane eroeffnungsbilanzContainer;
	private ArrayList<Bestandskonto> bKontoListe;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		bKontoListe = new ArrayList<>();
		eroeffnungsbilanzContainer = new GridPane();
		eroeffnungsbilanzContainer.setVgap(10);
		eroeffnungsbilanzContainer.setHgap(35);
		eroeffnungsbilanzContainer.setPadding(new Insets(20, 0, 0, 28));
		container.getChildren().add(eroeffnungsbilanzContainer);
	}
	/**
	 * <i><b>Übergabe/Setzen der Konten</b></i><br>
	 * <br>
	 * Alle Bestandskonten einer Kontenliste werden dem Controller übergeben. <br>
	 * 
	 * @param kntList
	 * 			- ist eine Liste von Konten
	 */
	public void setKonten(ArrayList<Konto> kntList) {
		for (Konto konto : kntList) {
			// "1" entspricht der Kontoart Bestandskonto
			if (konto.getKontoart() == 1) {
				bKontoListe.add((Bestandskonto) konto);
			}
		}
		befuelleGP();
	}

	/**
	 * <i><b>Befüllen des GridPanes mit Kontennamen und Anganfsbeständen</b></i><br>
	 * <br>
	 * Bestandskonten werden in einem GridPane nach Aktiva und Passiva aufgeteilt und mit jeweiligen Kontonamen und Anfangsbestand ausgegeben  <br>
	 * 
	 */
	public void befuelleGP() {
		int rowAktiv = 1, rowPassiv = 1;
		for (Bestandskonto konto : bKontoListe) {
			if (konto.isAktivkonto()) {
				Label kontoName = new Label(konto.getTitel());
				eroeffnungsbilanzContainer.add(kontoName, 0, rowAktiv);

				Label kontoAB = new Label(Double.toString(konto.getAnfangsbestand()));
				eroeffnungsbilanzContainer.add(kontoAB, 1, rowAktiv++);
			} else {
				Label kontoName = new Label(konto.getTitel());
				eroeffnungsbilanzContainer.add(kontoName, 3, rowPassiv);

				Label kontoAB = new Label(Double.toString(konto.getAnfangsbestand()));
				eroeffnungsbilanzContainer.add(kontoAB, 4, rowPassiv++);
			}
		}
		final int maxValue = Integer.max(rowAktiv, rowPassiv);
		Line aktivLine = new Line(0, 0, 70, 0);
		eroeffnungsbilanzContainer.add(aktivLine, 1, maxValue);
		Line passivLine = new Line(0, 0, 70, 0);
		eroeffnungsbilanzContainer.add(passivLine, 4, maxValue);
		Label aktivBilanzwert = new Label(getBilanzsumme(true) + "");
		eroeffnungsbilanzContainer.add(aktivBilanzwert, 1, maxValue + 1);
		Label passivBilanzwert = new Label(getBilanzsumme(false) + "");
		eroeffnungsbilanzContainer.add(passivBilanzwert, 4, maxValue + 1);

		Platform.runLater(() -> {
			eroeffnungsbilanzContainer.autosize();
			Line hLine = new Line(0, 0, eroeffnungsbilanzContainer.getWidth(), 0);
			eroeffnungsbilanzContainer.add(hLine, 0, 0, 5, 1);
			Line vLine = new Line(0, 0, 0, eroeffnungsbilanzContainer.getHeight());
			eroeffnungsbilanzContainer.add(vLine, 2, 0, 1, maxValue+3);
		});

	}
	
	/**
	 * <i><b>Berechnen der Bilanzsumme</b></i><br>
	 * <br>
	 * Es wird die Bilanzsumme aller Aktiv- bzw. Passivkonten aus den Anfangsbeständen berechnet.  <br>
	 * 
	 * @param getAktiv
	 *            - gibt an, ob es sich um die Aktiva- oder Passiva handelt
	 * @return Bilanzsumme
	 */
	private double getBilanzsumme(boolean getAktiv) {
		double summe = 0;
		for (Bestandskonto konto : bKontoListe) {
			if (konto.isAktivkonto() && getAktiv) {
				summe += konto.getAnfangsbestand();
			} else if (!konto.isAktivkonto() && !getAktiv) {
				summe += konto.getAnfangsbestand();
			}
		}
		return summe;
	}
	/**
	 * <i><b>Prüfung nach ausgeglichener Bilanz</b></i><br>
	 * <br>
	 * Es wird geprüft, ob die Bilanz ausgeglichen ist. Dafür werden die Anfangsbestände von Aktiv-und Passivkonten summiert. Ergibt die Differenz der beiden Summen gleich 0, dann ist die Bilanz ausgeglichen. <br>
	 * 

	 * @return Ob Bilanz ausgeglichen ist
	 */
	public boolean isBilanzAusgeglichen() {
		double aktiv = 0, passiv = 0;
		for (Bestandskonto konto : bKontoListe) {
			if (konto.isAktivkonto()) {
				aktiv += konto.getAnfangsbestand();
			} else {
				passiv += konto.getAnfangsbestand();
			}
		}
		if (aktiv == passiv) {
			return true;
		}
		return false;
	}

}
