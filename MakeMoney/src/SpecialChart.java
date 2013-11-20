import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nampq
 * Date: 10/1/12
 * Time: 8:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpecialChart {

    public static void main(final String[] args) {

        //1. Number
        //2. startDate
        //3. endDate

        String num = args[0];
        String startDate = args[1];
        String endDate = args[2];

//        String startDate = "20120701";
//        String endDate = "20121001";

//        String num = "42";

        ReadData rData = new ReadData();
        Map<String, String> data = rData.readData(startDate, endDate, "db.csv");
        double xAxisLength = data.size();
        XYDataset dataset = LineChart.caculateXYSeries(num, data, true);


        final LineChart demo = new LineChart("Make money", dataset , xAxisLength);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }


}
