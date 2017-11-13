package sina;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>类描述</p>
 * 创建日期 2017/11/11
 *
 * @author tianshangdeyun(wangbo@eefung.com)
 * @since 1.0.1
 */
@Service
public class Parser {
    /**
     * 获取博主主页的微博信息
     *
     * @param userId
     * @return
     */
    public HomeData getSinaDataByUserId(String userId) {
        String hostUrl = "https://weibo.com/u/" + userId;
        String result = null;
        String cookie = getCookie();
        HomeData homeData = new HomeData();
        try {
            HttpResponse response = Request.Get(hostUrl)
                                           .connectTimeout(1000)
                                           .socketTimeout(1000)
                                           .setHeader("Host", "weibo.com")
                                           .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                                           .setHeader("Content-Type", "text/html; charset=utf-8")
                                           .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                           .setHeader("Accept-Language", "zh-CN,zh;q=0.8")
                                           .setHeader("Upgrade-Insecure-Requests", "1")
                                           .setHeader("Accept-Encoding", "gzip, deflate, br")
                                           .setHeader("Accept-Language", "zh-CN,zh;q=0.8")
                                           .setHeader("Cookie", cookie)
                                           .execute().returnResponse();
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() < 300) {
                InputStream input = response.getEntity().getContent();
                result = IOUtils.toString(input);
            } else {
                System.out.println("error code is : " + status.getStatusCode());
                InputStream input = response.getEntity().getContent();
                System.out.println("error message : \n" + IOUtils.toString(input));
                return null;
            }

            //解析
            Pattern pattern = Pattern.compile("<script>FM.view\\((.+)\\)</script>");
            Matcher matcherScript = pattern.matcher(result);
            while (matcherScript.find()) {
                String scriptStr = matcherScript.group(1);
                Matcher matcher = null;
                //取粉丝数
                if (scriptStr.contains("Pl_Core_T8CustomTriColumn")) {
                    JSONObject elem = JSON.parseObject(scriptStr);
                    String domid = elem.getString("domid");
                    if (domid.contains("Pl_Core_T8CustomTriColumn")) {
                        String content = elem.getString("html");
                        Pattern numPattern = Pattern.compile(">(\\d+)<");
                        matcher = numPattern.matcher(content);
                        int i = 1;
                        while (matcher.find()) {
                            if (i == 2) {
                                homeData.setFlowwerNum(Integer.valueOf(matcher.group(1)));
                                break;
                            }
                            i++;
                        }
                    }
                }
                //取转发，点赞，评论，时间
                if (scriptStr.contains("pl.content.homeFeed.index") && scriptStr.contains("Pl_Official_MyProfileFeed")) {
                    List<SinaData> sinaDatas = new ArrayList<SinaData>();

                    String contentStr = JSON.parseObject(scriptStr).getString("html");
                    Document doc = Jsoup.parse(contentStr);
                    Elements elements = doc.select("[action-type=feed_list_item]");

                    for (Element element : elements) {
                        //转发，评论，点赞
                        Element blowEle = element.select("div.WB_feed_handle").get(0);
                        SinaData sinaData = new SinaData();
                        Element forwordEle = blowEle.select("span[node-type=forward_btn_text]").get(0);
                        Pattern numPattern = Pattern.compile("(\\d+)");
                        matcher = numPattern.matcher(forwordEle.text());
                        if (matcher.find()) {
                            sinaData.setTransmitNum(Integer.valueOf(matcher.group(1)));
                        }
                        Element commentEle = blowEle.select("span[node-type=comment_btn_text]").get(0);
                        matcher = numPattern.matcher(commentEle.text());
                        if (matcher.find()) {
                            sinaData.setCommentNum(Integer.valueOf(matcher.group(1)));
                        }
                        Element likeEle = blowEle.select("span[node-type=like_status]").get(0);
                        matcher = numPattern.matcher(likeEle.text());
                        if (matcher.find()) {
                            sinaData.setLikeNum(Integer.valueOf(matcher.group(1)));
                        }
                        //获取时间
                        Element upElem = element.select("div.WB_from.S_txt2 [date]").get(0);
                        long date = Long.valueOf(upElem.attr("date"));
                        sinaData.setTime(date);
                        sinaDatas.add(sinaData);
                    }
                    homeData.setSinaDatas(sinaDatas);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return homeData;
    }

    /**
     * 获取转发的大V博主的名称和id
     *
     * @return
     */
    public List<SinaData> getBigVSinaDataOfTransmit(String id) {
        //https://weibo.com/aj/v6/mblog/info/big?ajwvr=6&id=4169268117776485&__rnd=%id&page=%d
        Set<String> bigVUserIds = new HashSet<String>();

        String urlF = "https://weibo.com/aj/v6/mblog/info/big?ajwvr=6&id=%s&__rnd=1510409112486&page=%s";
        int page = 1;
        int maxTimes = 5;
        int pageMax = 1;
        List<SinaData> sinaDatas = new ArrayList<SinaData>();
        String cookie = getCookie();
        while (page <= pageMax && page <= maxTimes) {
            String result = null;
            try {
                String url = String.format(urlF, id, page);
                HttpResponse response = Request.Get(url)
                                               .connectTimeout(1000)
                                               .socketTimeout(1000)
                                               .setHeader("Host", "weibo.com")
                                               .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                                               .setHeader("Content-Type", "text/html; charset=utf-8")
                                               .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                               .setHeader("Accept-Language", "zh-CN,zh;q=0.8")
                                               .setHeader("Upgrade-Insecure-Requests", "1")
                                               .setHeader("Accept-Encoding", "gzip, deflate, br")
                                               .setHeader("Accept-Language", "zh-CN,zh;q=0.8")
                                               .setHeader("Cookie", cookie)
                                               .execute().returnResponse();
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() < 300) {
                    InputStream input = response.getEntity().getContent();
                    result = IOUtils.toString(input);
                } else {
                    System.out.println("error code is : " + status.getStatusCode());
                    InputStream input = response.getEntity().getContent();
                    System.out.println("error message : \n" + IOUtils.toString(input));
                    continue;
                }

                //解析
                JSONObject resultj = JSON.parseObject(result);
                JSONObject dataj = resultj.getJSONObject("data");
                JSONObject pagej = dataj.getJSONObject("page");
                Integer maxpagej = pagej.getInteger("totalpage");
                if (maxpagej != null) {
                    pageMax = maxpagej;
                }
                String htmlj = dataj.getString("html");
                Document doc = Jsoup.parse(htmlj);
                Elements elements = doc.select("[action-type=feed_list_item]");
                for (Element element : elements) {
                    SinaData sinaData = new SinaData();
                    //获取认证信息
                    Element verifyEle = element.select("div.WB_text").get(0);
                    boolean isBigV = verifyEle.select("a").html().contains("认证");
                    Element userInfoEle = verifyEle.select("a[href]").get(0);
                    String userId = userInfoEle.attr("usercard").substring("id=".length()).trim();
                    String username = userInfoEle.text();
                    if (isBigV && bigVUserIds.add(userId)) {
                        sinaData.setUserId(userId);
                        sinaData.setUsername(username);
                    } else {
                        continue;
                    }
                    //获取时间信息
                    Element timeEle = element.select("a[date]").get(0);
                    sinaData.setTime(Long.valueOf(timeEle.attr("date")));
                    sinaDatas.add(sinaData);
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            } finally {
                page++;
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return sinaDatas;
    }

    /**
     * 获取微博的内容
     *
     * @param weiboUrl
     * @return
     */

    public SinaData getMblog(String weiboUrl) {
        String mblogStr = "";
        String cookie = getCookie();
        try {
            HttpResponse response = Request.Get(weiboUrl)
                                           .connectTimeout(1000)
                                           .socketTimeout(1000)
                                           .setHeader("Host", "weibo.com")
                                           .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                                           .setHeader("Content-Type", "text/html; charset=utf-8")
                                           .setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                           .setHeader("Accept-Language", "zh-CN,zh;q=0.8")
                                           .setHeader("Upgrade-Insecure-Requests", "1")
                                           .setHeader("Accept-Encoding", "gzip, deflate, br")
                                           .setHeader("Accept-Language", "zh-CN,zh;q=0.8")
                                           .setHeader("Cookie", cookie)
                                           .execute().returnResponse();
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() < 300) {
                InputStream input = response.getEntity().getContent();
                mblogStr = IOUtils.toString(input);
            } else {
                System.out.println("error code is : " + status.getStatusCode());
                InputStream input = response.getEntity().getContent();
                System.out.println("error message : \n" + IOUtils.toString(input));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        SinaData sinaData = new SinaData();
        Pattern pattern = Pattern.compile("<script>FM.view\\((.+)\\)</script>");
        Matcher matcherScript = pattern.matcher(mblogStr);
        while (matcherScript.find()) {
            String scriptStr = matcherScript.group(1);
            if (scriptStr.contains("pl.content.weiboDetail.index") && scriptStr.contains("Pl_Official_WeiboDetail")) {
                String content = JSON.parseObject(scriptStr).getString("html");
                Document doc = Jsoup.parse(content);
                Element elem = doc.select("div.WB_detail div.WB_from.S_txt2 a[name]").get(0);
                String id = elem.attr("name");
                sinaData.setId(id);
            }
        }


        Pattern userIdPattern = Pattern.compile("/(\\d+)/");
        Matcher matcher = userIdPattern.matcher(weiboUrl);
        while (matcher.find()) {
            String userId = matcher.group(1);
            sinaData.setUserId(userId);
            break;
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sinaData;
    }

    public static void main(String[] args) {
        String url = "https://weibo.com/1574684061/FuBnhx4T5?from=page_1003061574684061_profile&wvr=6&mod=weibotime&type=comment#_rnd1510485402164";
        Parser parser = new Parser();
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
        ExcelMaker excelMaker = new ExcelMaker();
        byte[] bytes = excelMaker.createExcel("微博", excelData);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(new File("/Users/tianshangdeyun/Documents/eefung/test/1.xls"));
            IOUtils.write(bytes, output);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getCookie() {
        String result = "";

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream("cookie"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("获取cookie失败！");
        }

        return result;
    }
}
