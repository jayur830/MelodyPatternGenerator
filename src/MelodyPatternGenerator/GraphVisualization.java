package MelodyPatternGenerator;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.List;

public class GraphVisualization extends Application {
    @FXML
    private NumberAxis xAxis = new NumberAxis();
    @FXML
    private NumberAxis yAxis = new NumberAxis();
    @FXML
    private LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

    public void initialize(String fileName, String artistName, String songName, int label, int dataNums) throws IOException {
        MelodyDescriptor.init();
        Pair<double[], double[]> dataPair = PatternGenerator.loadFile(fileName, label);
        List<double[]> list = new PatternGenerator(dataPair.getKey(), dataPair.getValue(), dataNums).getList();

        XYChart.Series[] melodyLine = new XYChart.Series[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            melodyLine[i] = new XYChart.Series();
            melodyLine[i].setName((i + 1) + " Melody");
            double[] data = list.get(i);
            for (int j = 0; j < data.length; ++j)
                if (Math.abs(data[j] - 0.0) >= 0.05) melodyLine[i].getData().add(new XYChart.Data<>(j + 1, data[j]));
            this.chart.getData().add(melodyLine[i]);
        }
        System.out.println();
        this.chart.setCreateSymbols(false);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initialize("BTS - IDOL.txt", "BTS", "IDOL", 0, 100);

        primaryStage.setTitle("Melody Feature");
        primaryStage.setScene(new Scene(this.chart, 700, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
