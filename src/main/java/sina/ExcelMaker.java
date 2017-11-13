package sina;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * <p>类描述</p>
 * 创建日期 2017/11/12
 *
 * @author tianshangdeyun(wangbo@eefung.com)
 * @since 1.0.1
 */
@Service
public class ExcelMaker {
    public byte[] createExcel(String sheetName, String[][] datas) {
        HSSFWorkbook wb = createWorkBook(sheetName, datas);
        byte[] bytesData = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(5000000)) {
            wb.write(byteArrayOutputStream);
            bytesData = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytesData;
    }

    public HSSFWorkbook createWorkBook(String sheetName, String[][] datas) {
        // 第一步，创建一个webbook，对应一个Excel文件
        HSSFWorkbook wb = new HSSFWorkbook();
        // 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
        HSSFSheet sheet = wb.createSheet("新浪微博");
        // 第三步，添加
        for (int i = 0; i < datas.length; i++) {
            HSSFRow row = sheet.createRow(i);
            for (int j = 0; j < datas[i].length; j++) {
                HSSFCell cell = row.createCell(j);
                cell.setCellValue(datas[i][j]);
            }
        }
        return wb;
    }
}
