package sina;

import java.util.List;

/**
 * <p>类描述</p>
 * 创建日期 2017/11/12
 *
 * @author tianshangdeyun(wangbo@eefung.com)
 * @since 1.0.1
 */
public class HomeData {
    int flowwerNum;
    List<SinaData> sinaDatas;

    public int getFlowwerNum() {
        return flowwerNum;
    }

    public void setFlowwerNum(int flowwerNum) {
        this.flowwerNum = flowwerNum;
    }

    public List<SinaData> getSinaDatas() {
        return sinaDatas;
    }

    public void setSinaDatas(List<SinaData> sinaDatas) {
        this.sinaDatas = sinaDatas;
    }
}
