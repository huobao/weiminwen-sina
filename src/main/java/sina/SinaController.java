package sina;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>类描述</p>
 * 创建日期 2017/11/12
 *
 * @author tianshangdeyun(wangbo@eefung.com)
 * @since 1.0.1
 */
@Controller
@RequestMapping("/")
public class SinaController {
    @Autowired
    ExcelMaker excelMaker;
    @Autowired
    Parser parser;

    @ResponseBody
    @RequestMapping(value = "/sina", method = RequestMethod.GET)
    public byte[] sina(HttpServletResponse response, @RequestParam("url") String url) {
        if (url == null || url.trim().equals("")) {
            response.addHeader("Content-Disposition", "attachment; filename=error.txt");
            return "没有输入微博url!".getBytes();
        }
        try {
            SinaData sinaData = parser.getMblog(url);
            List<SinaData> bigVSinaDatas = parser.getBigVSinaDataOfTransmit(sinaData.getId());
            String[][] excelData = new String[bigVSinaDatas.size() + 1][7];
            excelData[0][0] = "转发日期";
            excelData[0][1] = "转发时间";
            excelData[0][2] = "微博名称";
            excelData[0][3] = "粉丝数";
            excelData[0][4] = "平均转发";
            excelData[0][5] = "平均评论";
            excelData[0][6] = "平均点赞";
            int index = 1;
            for (SinaData bigSd : bigVSinaDatas) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");
                    excelData[index][0] = dateFormat.format(new Date(bigSd.getTime()));
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH时mm分");
                    excelData[index][1] = timeFormat.format(new Date(bigSd.getTime()));
                    excelData[index][2] = bigSd.getUsername();

                    HomeData homeData = parser.getSinaDataByUserId(bigSd.getUserId());
                    excelData[index][3] = homeData.getFlowwerNum() + "";
                    int transmitNum = 0;
                    int commentNum = 0;
                    int likeNum = 0;
                    for (SinaData sd : homeData.getSinaDatas()) {
                        transmitNum += sd.getTransmitNum();
                        commentNum += sd.getCommentNum();
                        likeNum += sd.getLikeNum();
                    }
                    int size = homeData.getSinaDatas().size();
                    excelData[index][4] = (transmitNum / size) + "";
                    excelData[index][5] = (commentNum / size) + "";
                    excelData[index][6] = (likeNum / size) + "";
                    index++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //设置返回头
            String format = "attachment; filename=sina.xls";
            response.addHeader("Content-Disposition", format);
            byte[] bytes = excelMaker.createExcel("微博", excelData);
            return bytes;
        } catch (Throwable e) {
            e.printStackTrace();
            response.addHeader("Content-Disposition", "attachment; filename=error.txt");
            return "方才出现了网络错误，请重试！".getBytes();
        }
    }
}
