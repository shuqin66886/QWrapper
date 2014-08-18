import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import com.travelco.rdf.infocenter.InfoCenter;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.httpclient.NameValuePair;

import java.text.SimpleDateFormat;
import java.util.*;

public class Wrapper_gjdairat001 implements QunarCrawler {

    public static void main(String[] args) {
        FlightSearchParam searchParam = new FlightSearchParam();
        searchParam.setDep("BEY");
        searchParam.setArr("ACC");
//        searchParam.setDep("Paris-Orly (ORY)");
//        searchParam.setArr("Accra (ACC)");
        searchParam.setDepDate("2014-08-25");
        searchParam.setTimeOut("60000");
        searchParam.setWrapperid("Wrapper_gjdairat001");
        searchParam.setToken("");

        String html = new Wrapper_gjdairat001().getHtml(searchParam);      //得到最终结果

        ProcessResultInfo result = new ProcessResultInfo();
        result = new Wrapper_gjdairat001().process(html, searchParam);
        String jieguo=JSON.toJSONString(result);
        if (result.isRet() && result.getStatus().equals(Constants.SUCCESS)) {
            List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result.getData();
            for (OneWayFlightInfo in : flightList) {
                System.out.println("************" + in.getInfo().toString());
                System.out.println("++++++++++++" + in.getDetail().toString());
            }
        } else {
            System.out.println(result.getStatus());
        }
    }

    public String getHtml(FlightSearchParam arg0) {                 //第一次跳转
        QFHttpClient httpClient = new QFHttpClient(arg0, false);
        QFPostMethod postMethod = new QFPostMethod("http://www.royalairmaroc.com/int-en/ezjscore/call/");
        postMethod.setRequestHeader("Connection", "close");
        try {
            NameValuePair[] data = {
                    new NameValuePair("language_code", "EN")
                    , new NameValuePair("countryCode", "GB")
                    , new NameValuePair("depart", getCity(arg0.getDep()))
                    , new NameValuePair("arrivee", getCity(arg0.getArr()))
//                    , new NameValuePair("depart", arg0.getDep())
//                    , new NameValuePair("arrivee", arg0.getArr())
                    , new NameValuePair("date_depart", getDate(arg0.getDepDate()))
                    , new NameValuePair("type_classe", "RAMALL")
                    , new NameValuePair("radio-type-aller", "O")
                    , new NameValuePair("nbre_adulte", "1")
                    , new NameValuePair("nbre_enfant", "0")
                    , new NameValuePair("nbre_bebe", "0")
                    , new NameValuePair("ezjscServer_function_arguments", "amadeus::getData")
                    , new NameValuePair("ezxform_token", "")
            };
            postMethod.setRequestBody(data);
            int status = httpClient.executeMethod(postMethod);
            String html = postMethod.getResponseBodyAsString();

            String body = postInfo(arg0,html);       //第2次
            return postDateInfo(arg0,body);        //第3次
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
                ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
            }
        }

    }

    public static String postInfo(FlightSearchParam arg0,String html) {                //第2次跳转
        String url = StringUtils.substringBetween(html, "url&quot;:&quot;", "&quot;,&quot;EMBEDDED_TRANSACTION").replaceAll("\\\\", "");
        String ENC = StringUtils.substringBetween(html, "ENC&quot;:&quot;", "&quot;}");
        QFHttpClient httpClient = new QFHttpClient(arg0, false);
        QFPostMethod postMethod = new QFPostMethod(url);
        postMethod.setRequestHeader("Connection", "close");
        try {
            NameValuePair[] data = {
                    new NameValuePair("EMBEDDED_TRANSACTION", "FlexPricerAvailability")
                    , new NameValuePair("SITE", "BBDGBBDG")
                    , new NameValuePair("LANGUAGE", "US")
                    , new NameValuePair("ENCT", "1")
                    , new NameValuePair("ENC", ENC)
            };
            postMethod.setRequestBody(data);
            int status = httpClient.executeMethod(postMethod);
            String body = postMethod.getResponseBodyAsString();
            return body;
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
                ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
            }
        }
    }

    public static String postDateInfo(FlightSearchParam arg0,String body) {                      //第3次跳转
        QFHttpClient httpClient = new QFHttpClient(arg0, false);
        String jsessionid = StringUtils.substringBetween(body, "FlexPricerAvailabilityDispatcherPui.action", "\" method=\"POST\" class=\"transparentForm\">");
        QFPostMethod postMethod = new QFPostMethod("https://www.royalairmaroc.e-retail.amadeus.com/plnext/5APHOneWay/FlexPricerAvailabilityDispatcherPui.action" + jsessionid);
        postMethod.setRequestHeader("Connection", "close");
        try {
            NameValuePair[] data = {
                    new NameValuePair("B_LOCATION_1", arg0.getDep())
                    , new NameValuePair("E_LOCATION_1", arg0.getArr())
//                     new NameValuePair("B_LOCATION_1", arg0.getDep().substring(arg0.getDep().indexOf("(")+1,arg0.getDep().length()-1))
//                    , new NameValuePair("E_LOCATION_1",arg0.getArr().substring(arg0.getArr().indexOf("(")+1,arg0.getArr().length()-1))
                    , new NameValuePair("DATE_RANGE_VALUE_2", "0")
                    , new NameValuePair("AIRLINE_2_1", "AT")
                    , new NameValuePair("DATE_RANGE_VALUE_1", "0")
                    , new NameValuePair("REAL_ARRANGE_BY", "N")
                    , new NameValuePair("ENCT", "1")
                    , new NameValuePair("TRAVELLER_TYPE_1", "ADT")
                    , new NameValuePair("SITE", "BBDGBBDG")
                    , new NameValuePair("DISPLAY_TYPE", "1")
                    , new NameValuePair("EXTERNAL_ID", "AT")
                    , new NameValuePair("TRIP_TYPE", "O")
                    , new NameValuePair("DATE_RANGE_QUALIFIER_1", "C")
                    , new NameValuePair("DATE_RANGE_QUALIFIER_2", "C")
                    , new NameValuePair("OFFICE_ID", "PARAT08WA")
                    , new NameValuePair("B_DATE_1", arg0.getDepDate().replaceAll("-", "") + "0000")
                    , new NameValuePair("COMMERCIAL_FARE_FAMILY_1", "RAMALL")
                    , new NameValuePair("PRICING_TYPE", "O")
                    , new NameValuePair("B_ANY_TIME_1", "TRUE")
                    , new NameValuePair("LANGUAGE", "US")
                    , new NameValuePair("ARRANGE_BY", "")
                    , new NameValuePair("B_ANY_TIME_2", "TRUE")
                    , new NameValuePair("AIRLINE_1_1", "AT")
                    , new NameValuePair("PLTG_IS_UPSELL", "true")
                    , new NameValuePair("PAGE_TICKET", "0")
            };
            postMethod.setRequestBody(data);
            int status = httpClient.executeMethod(postMethod);
            String result = postMethod.getResponseBodyAsString();
            return result;
        } catch (Exception e) {
            return "Exception";
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
                ((SimpleHttpConnectionManager)httpClient.getHttpConnectionManager()).shutdown();
            }
        }
    }


    public ProcessResultInfo process(String arg0, FlightSearchParam arg1) {                 //对返回结果的处理，得到指定格式
        String html = arg0;

        /* ProcessResultInfo中，
           * ret为true时，status可以为：SUCCESS(抓取到机票价格)|NO_RESULT(无结果，没有可卖的机票)
           * ret为false时，status可以为:CONNECTION_FAIL|INVALID_DATE|INVALID_AIRLINE|PARSING_FAIL|PARAM_ERROR
           */
        ProcessResultInfo result = new ProcessResultInfo();
        if ("Exception".equals(html)) {
            result.setRet(false);
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }
        //需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
        if (html.contains("Today Flight is full, select an other day or check later for any seat released. ")) {
            result.setRet(false);
            result.setStatus(Constants.INVALID_DATE);
            return result;
        }

        try {
            String priceStr = StringUtils.substringBetween(html, "\"list_recommendation\":", ",\"list_date\"");      //根据flight_id计算票价
            JSONArray price = JSON.parseArray(priceStr);
            List<HashMap> priceMapList = new ArrayList<HashMap>();
            HashMap priceMap = new HashMap();
            for (int p = 0; p < price.size(); p++) {
                JSONObject jsb = price.getJSONObject(p);
                JSONArray pob = jsb.getJSONArray("list_bound");
                JSONArray flight = pob.getJSONObject(0).getJSONArray("list_flight");
                for (int f = 0; f < flight.size(); f++) {
                    float value1 = 0;
                    float value2 = 0;
                    if (priceMap != null && priceMap.containsKey(flight.getJSONObject(f).getString("flight_id"))) {
                        value1 = Float.parseFloat(priceMap.get(flight.getJSONObject(f).getString("flight_id")).toString());
                        value2 = jsb.getFloat("price");
                        if (value1 >= value2)
                            priceMap.put(flight.getJSONObject(f).getString("flight_id"), jsb.getString("price"));
                    } else
                        priceMap.put(flight.getJSONObject(f).getString("flight_id"), jsb.getString("price"));
                }
            }

            List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();                                                //获得具体航班信息
            String jsonStr = StringUtils.substringBetween(html, "list_flight\":", "}],\"list_recommendation\"");
            JSONArray ajson = JSON.parseArray(jsonStr);
            for (int i = 0; i < ajson.size(); i++) {
                OneWayFlightInfo baseFlight = new OneWayFlightInfo();
                List<FlightSegement> segs = new ArrayList<FlightSegement>();
                FlightDetail flightDetail = new FlightDetail();
                List<String> flightNoList = new ArrayList<String>();
                JSONObject ojson = ajson.getJSONObject(i);
                String flightid = ojson.getString("flight_id");
                JSONArray segmentArray = ojson.getJSONArray("list_segment");
                for (int j = 0; j < segmentArray.size(); j++) {
                    FlightSegement seg = new FlightSegement();
                    JSONObject object = (JSONObject) segmentArray.get(j);
                    String flightNo = "AT" + object.getString("flight_number");
                    flightNoList.add(flightNo);
                    seg.setFlightno(flightNo);
                    seg.setDepDate(setDate(object.getString("b_date_date")));
                    seg.setArrDate(setDate(object.getString("e_date_date")));
                    JSONObject blocationObject = object.getJSONObject("b_location");
                    seg.setDepairport(blocationObject.getString("location_code"));
                    JSONObject elocationObject = object.getJSONObject("e_location");
                    seg.setArrairport(elocationObject.getString("location_code"));
                    seg.setDeptime(setTime(object.getString("b_date_formatted_time")));
                    seg.setArrtime(setTime(object.getString("e_date_formatted_time")));
                    segs.add(seg);
                }
                JSONObject ob = (JSONObject) segmentArray.get(0);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                flightDetail.setDepdate(sdf.parse(setDate(ob.getString("b_date_date"))));
                flightDetail.setFlightno(flightNoList);
                flightDetail.setMonetaryunit("EUR");
                flightDetail.setPrice(Double.parseDouble(priceMap.get(flightid).toString()));
                flightDetail.setDepcity(arg1.getDep());
                flightDetail.setArrcity(arg1.getArr());
                flightDetail.setWrapperid(arg1.getWrapperid());
                baseFlight.setDetail(flightDetail);
                baseFlight.setInfo(segs);
                flightList.add(baseFlight);
            }
            result.setRet(true);
            result.setStatus(Constants.SUCCESS);
            result.setData(flightList);
            return result;
        } catch (Exception e) {
            result.setRet(false);
            result.setStatus(Constants.PARSING_FAIL);
            return result;
        }
    }

    public static String getCity(String code) {
        String city = new InfoCenter().getCityFromAirportCode(code) + '(' + code + ')';
        return city;
    }

    public String setDate(String arg) {
        String date = arg.substring(0, 4) + '-' + arg.substring(4, 6) + '-' + arg.substring(6, 8);
        return date;
    }

    public static String getDate(String arg) {
        String date = arg.substring(8, 10) + '/' + arg.substring(5, 7) + '/' + arg.substring(0, 4);
        return date;
    }

    public String setTime(String arg) {
        String time="";
        if(arg.contains("PM"))
            time=(Integer.parseInt(arg.substring(0,2))+12)+arg.substring(2,5);
        else
            time=arg.substring(0,5);
        return time;

    }

    public BookingResult getBookingInfo(FlightSearchParam arg0) {
        String bookingUrlPre = "http://www.royalairmaroc.com/int-en";
        BookingResult bookingResult = new BookingResult();
        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction(bookingUrlPre);
        bookingInfo.setMethod("post");
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("language_code", "EN");
        map.put("countryCode", "GB");
        map.put("depart", getCity(arg0.getDep()));
        map.put("arrivee",getCity(arg0.getArr()));
        map.put("date_depart", getDate(arg0.getDepDate()));
        map.put("date_arrivee", "");
        map.put("type_classe", "RAMALL");
        map.put("radio-type-aller", "O");
        map.put("nbre_adulte", "1");
        map.put("nbre_enfant", "0");
        map.put("nbre_bebe", "0");
        map.put("ezjscServer_function_arguments", "amadeus::getData");
        map.put("ezxform_token", "");
        bookingInfo.setInputs(map);
        bookingResult.setData(bookingInfo);
        bookingResult.setRet(true);
        return bookingResult;
    }
}

