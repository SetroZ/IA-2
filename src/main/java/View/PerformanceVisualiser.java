package View;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.axis.NumberAxis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class for visualizing algorithm performance metrics.
 * Used for generating charts and graphs for the comparative analysis
 * of different optimization algorithms.
 */
public class PerformanceVisualiser {

    public PerformanceVisualiser() {
        // Set the headless property to true - no gui needed
        System.setProperty("java.awt.headless", "true");
    }


    /**
     * Creates a comparative performance chart for multiple algorithms.
     *
     * @param title The title of the chart
     * @param xLabel The label for the x-axis
     * @param yLabel The label for the y-axis
     * @param algorithmNames List of algorithm names
     * @param dataPoints List of series data points for each algorithm
     * @param outputPath The file path where the chart will be saved
     */
    public void createComparisonChart(String title, String xLabel, String yLabel,
                                      List<String> algorithmNames, List<List<double[]>> dataPoints, String outputPath) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < algorithmNames.size(); i++) {
            XYSeries series = new XYSeries(algorithmNames.get(i));
            List<double[]> points = dataPoints.get(i);

            for (double[] point : points) {
                series.add(point[0], point[1]);
            }

            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        // Customize line colors for different algorithms
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Define colors for different algorithms
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN};

        for (int i = 0; i < algorithmNames.size(); i++) {
            renderer.setSeriesPaint(i, colors[i % colors.length]);
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(i, true);
        }

        plot.setRenderer(renderer);

        saveChart(outputPath, chart);
    }

    /**
     * Customizes the appearance of a chart.
     *
     * @param chart The JFreeChart to customize
     */
    private void customizeChart(JFreeChart chart) {
        // Set the background color
        chart.setBackgroundPaint(Color.WHITE);

        // Customize the title
        chart.setTitle(new TextTitle(chart.getTitle().getText(),
                new Font("Serif", Font.BOLD, 18)));

        // Customize the plot
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(240, 240, 240));
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Customize the axes
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));
    }

    /**
     *
     * Creates a bar chart for comparing computational efficiency across algorithms.
     *
     * @param title The title of the chart
     * @param xLabel The label for the x-axis
     * @param yLabel The label for the y-axis
     * @param algorithmNames List of algorithm names
     * @param runtimeValues List of runtime values for each algorithm
     * @param outputPath The file path where the chart will be saved
     */
    public void createEfficiencyBarChart(String title, String xLabel, String yLabel,
                                         List<String> algorithmNames, List<Double> runtimeValues, String outputPath) {
        // Create dataset
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();

        // Add data to dataset
        for (int i = 0; i < algorithmNames.size(); i++) {
            dataset.addValue(runtimeValues.get(i), "Runtime", algorithmNames.get(i));
        }

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Customize the chart
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Serif", Font.BOLD, 18));

        // Customize the plot
        NumberAxis rangeAxis = getRangeAxis(chart);
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));

        saveChart(outputPath, chart);
    }

    private NumberAxis getRangeAxis(JFreeChart chart)
    {
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(240, 240, 240));
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Customize bars
        org.jfree.chart.renderer.category.BarRenderer renderer = (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.1);

        // Customize axis
        org.jfree.chart.axis.CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.2);
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));

        return (NumberAxis) plot.getRangeAxis();
    }

    private void saveChart(String outputPath, JFreeChart chart)
    {
        try {
            File outputFile = new File(outputPath);
            // Create directories if they don't exist
            File parent = outputFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}