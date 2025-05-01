package View;

import Exceptions.LoadDataException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.axis.NumberAxis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
     * @param title          The title of the chart
     * @param xLabel         The label for the x-axis
     * @param yLabel         The label for the y-axis
     * @param algorithmNames List of algorithm names
     * @param dataPoints     List of series data points for each algorithm
     * @param outputPath     The file path where the chart will be saved
     */
    public void createComparisonChart(String title, String xLabel, String yLabel,
                                      List<String> algorithmNames, List<List<double[]>> dataPoints, String outputPath) throws LoadDataException
    {

        XYSeriesCollection dataset = new XYSeriesCollection();

        // Validate input data
        if (algorithmNames.size() != dataPoints.size())
        {
            throw new LoadDataException("Mismatch between algorithm names and data points");
        }

        // Create series for each algorithm
        for (int i = 0; i < algorithmNames.size(); i++)
        {
            String algorithmName = algorithmNames.get(i);
            List<double[]> points = dataPoints.get(i);

            System.out.println("Algorithm: " + algorithmName + ", Points: " + points.size());

            XYSeries series = new XYSeries(algorithmName);
            boolean hasValidData = false;

            for (double[] point : points)
            {
                if (point.length < 2)
                {
                    throw new LoadDataException("Invalid data point for "+ algorithmName+ ": to few values at point "+ Arrays.toString(point));
                }

                double x = point[0];
                double y = point[1];

                if (!Double.isFinite(x) || !Double.isFinite(y))
                {
                   throw new LoadDataException("Non-finite value in data for " + algorithmName +
                            ": [" + x + ", " + y + "]");
                }

                // Add the point and mark that we have valid data
                series.add(x, y);
                hasValidData = true;
            }

            if (hasValidData)
            {
                dataset.addSeries(series);
            }
            else
            {
                throw new LoadDataException("No valid data points for algorithm " + algorithmName);
            }
        }

        try
        {
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

            for (int i = 0; i < dataset.getSeriesCount(); i++)
            {
                renderer.setSeriesPaint(i, colors[i % colors.length]);
                renderer.setSeriesStroke(i, new BasicStroke(2.0f));
                renderer.setSeriesShapesVisible(i, true);
            }

            plot.setRenderer(renderer);

            // Ensure the range doesn't include infinity or NaN
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRangeIncludesZero(true);
            rangeAxis.setAutoRange(true);

            // Ensure domain axis also has proper range
            NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
            domainAxis.setAutoRange(true);

            saveChart(outputPath, chart);
        }
        catch (Exception e)
        {
            throw new LoadDataException(e.getMessage());
        }

    }

    public void createComparisonChartWithVariability(String title, String xLabel, String yLabel,
                                                     List<String> algorithmNames,
                                                     List<List<double[]>> avgData,
                                                     List<List<double[]>> stdDevData,
                                                     String outputPath) throws LoadDataException
    {
        // Create dataset with main series
        XYSeriesCollection dataset = getXySeriesCollection(algorithmNames, avgData, stdDevData);

        // Create chart with shaded areas for standard deviation
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xLabel, yLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        // Customize renderer to show shaded areas
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // For each algorithm
        for (int i = 0; i < algorithmNames.size(); i++) {
            int mainIndex = i * 3;
            int upperIndex = mainIndex + 1;
            int lowerIndex = mainIndex + 2;

            // Main line
            renderer.setSeriesVisible(mainIndex, true);
            renderer.setSeriesShapesVisible(mainIndex, false);
            renderer.setSeriesStroke(mainIndex, new BasicStroke(2.0f));

            // Upper and lower bounds - make invisible
            renderer.setSeriesVisible(upperIndex, false);
            renderer.setSeriesVisible(lowerIndex, false);

            // Add shaded area between upper and lower
            plot.setRenderer(renderer);

            // Create dataset for area
            XYSeriesCollection areaDataset = new XYSeriesCollection();
            areaDataset.addSeries(dataset.getSeries(upperIndex));
            areaDataset.addSeries(dataset.getSeries(lowerIndex));

            // Add area renderer
            XYAreaRenderer areaRenderer = new XYAreaRenderer();
            areaRenderer.setOutline(true);
            plot.setDataset(i + 1, areaDataset);
            plot.setRenderer(i + 1, areaRenderer);
        }

        saveChart(outputPath, chart);
    }

    private XYSeriesCollection getXySeriesCollection(List<String> algorithmNames, List<List<double[]>> avgData, List<List<double[]>> stdDevData)
    {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < algorithmNames.size(); i++) {
            String algorithmName = algorithmNames.get(i);
            List<double[]> points = avgData.get(i);
            List<double[]> stdDev = stdDevData.get(i);

            XYSeries mainSeries = new XYSeries(algorithmName);
            XYSeries upperSeries = new XYSeries(algorithmName + " Upper");
            XYSeries lowerSeries = new XYSeries(algorithmName + " Lower");

            for (int j = 0; j < points.size(); j++) {
                double x = points.get(j)[0];
                double y = points.get(j)[1];
                double dev = stdDev.get(j)[1];

                mainSeries.add(x, y);
                upperSeries.add(x, y + dev);
                lowerSeries.add(x, y - dev);
            }

            dataset.addSeries(mainSeries);
            dataset.addSeries(upperSeries);
            dataset.addSeries(lowerSeries);
        }
        return dataset;
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
     * Creates a bar chart for comparing computational efficiency across algorithms.
     *
     * @param title          The title of the chart
     * @param xLabel         The label for the x-axis
     * @param yLabel         The label for the y-axis
     * @param algorithmNames List of algorithm names
     * @param runtimeValues  List of runtime values for each algorithm
     * @param outputPath     The file path where the chart will be saved
     */
    public void createEfficiencyBarChart(String title, String xLabel, String yLabel,
                                           List<String> algorithmNames, List<Double> runtimeValues, String outputPath) throws LoadDataException
    {
        // Print debug information
        System.out.println("Creating efficiency bar chart: " + title);
        System.out.println("Number of algorithms: " + algorithmNames.size());

        // Validate input
        if (algorithmNames.isEmpty() || runtimeValues.isEmpty())
        {
            throw new LoadDataException("Empty algorithm names or runtime values");
        }

        if (algorithmNames.size() != runtimeValues.size())
        {
            throw new LoadDataException("Mismatch between algorithm names and runtime values");
        }

        DefaultCategoryDataset dataset = getCategoryDataset(algorithmNames, runtimeValues);

        try
        {
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

            // Ensure range includes zero but avoids infinity
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRangeIncludesZero(true);
            rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 14));

            saveChart(outputPath, chart);
            System.out.println("Successfully created chart: " + outputPath);
        }
        catch (Exception e)
        {
            System.out.println("Error creating chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DefaultCategoryDataset getCategoryDataset(List<String> algorithmNames, List<Double> runtimeValues)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < algorithmNames.size(); i++)
        {
            String algorithm = algorithmNames.get(i);
            Double runtime = runtimeValues.get(i);


            if (runtime != null && Double.isFinite(runtime))
            {
                dataset.addValue(runtime, "Runtime", algorithm);
            }
            else
            {
                throw new LoadDataException("Non-finite runtime value for " + algorithm + ": " + runtime);
            }
        }
        return dataset;
    }

    private void saveChart(String outputPath,  JFreeChart chart) throws LoadDataException {
        try {
            File outputFile = new File(outputPath);
            // Create directories if they don't exist
            File parent = outputFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
        } catch (IOException e) {
            throw new LoadDataException("Error saving chart: " + e.getMessage());
        }
    }
}