package org.example.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
@Data // getter, setter, toString
@NoArgsConstructor // 디폴트 생성자
public class HttpClient {
    private Double latitude; // 위도
    private Double longitude; // 경도
    private String location_keyword;
    private Double radius;

    private static final String AUTHORIZATION = "KakaoAK f4f51cafe50aa5d6202e2b117fa39315";
    private static final String REST_KEY = "f4f51cafe50aa5d6202e2b117fa39315";

    public HttpClient(String location_keyword, Double radius){
        this.location_keyword = location_keyword;
        this.radius = radius;
    }

    // 키워드로 장소 주소 좌표 변환
    public void saveLocationInfo(String location_keyword) throws ClientProtocolException, IOException{
        String GET_URL="http://dapi.kakao.com/v2/local/search/address.json?query=" + location_keyword;

        // http client 생성
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // get 메서드와 URL 설정
        HttpGet httpGet = new HttpGet(GET_URL);

        // 헤더 추가, 정보 설정
        httpGet.addHeader("Authorization", AUTHORIZATION); //헤더 추가
        httpGet.addHeader("Content-type", "application/json"); //JSON 타입 헤더 추가

        // get 요청
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        //response의 status 코드 출력
        System.out.println("::GET Response Status::");
        System.out.println(httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }

        reader.close();

        // 결과 출력
        // System.out.println(response.toString());
        httpClient.close();

        JSONObject location_info = new JSONObject(response.toString());
        JSONArray location_info_array = location_info.getJSONArray("documents");
        JSONObject address = location_info_array.getJSONObject(0);
        String y = address.getString("y"); //address에서 가져온 x값 가져오기
        String x = address.getString("x"); //address에서 가져온 y값 가져오기

        this.latitude = Double.parseDouble(y);
        this.longitude = Double.parseDouble(x);

        System.out.println("입력한 위치 키워드:" + address.getString("address_name"));
        System.out.println("검색 반경: " + this.radius / 1000.0 + "km");
    }

    // 반경 내의 약국
    public void findPharmacy() throws ClientProtocolException, IOException {
        String GET_URL= "https://dapi.kakao.com/v2/local/search/keyword.json?y="+this.latitude+"&x="+this.longitude+"&radius="+this.radius+"&query=약국";

        // http client 생성
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // get 메서드와 URL 설정
        HttpGet httpGet = new HttpGet(GET_URL);

        // 헤더 추가, 정보 설정
        httpGet.addHeader("Authorization", AUTHORIZATION); //헤더 추가
        httpGet.addHeader("Content-type", "application/json"); //JSON 타입 헤더 추가

        // get 요청
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        // response의 status 코드 출력
        // System.out.println("::GET Response Status::");
        // System.out.println(httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }

        reader.close();

        // 결과 출력
        // System.out.println(response.toString());
        httpClient.close();

        JSONObject location_info = new JSONObject(response.toString());
        JSONArray location_info_array = location_info.getJSONArray("documents");

        if(location_info_array.length() == 0){ // 반경 내에 약국이 없다면
            System.out.println("반경 내에 약국이 존재하지 않습니다.");
            System.exit(0);
        }else{
            // 상위 최대 10개 결과에 대한 추출된 데이터를 표시합니다.
            for(int i=0; i<location_info_array.length() % 10; i++){
                JSONObject address = location_info_array.getJSONObject(i);

                System.out.println("**약국 검색 결과**");
                System.out.println("장소 URL(지도 위치): " + address.getString("place_url"));
                System.out.println("상호명: " + address.getString("place_name"));
                System.out.println("주소: " + address.getString("address_name"));
                System.out.println("전화번호: " + address.getString("phone"));
                String dist = address.getString("distance");
                System.out.println("거리(km): " + Double.parseDouble(dist) / 1000.0 + "km");
                System.out.println("-------------------------------");
            }
        }

        // 검색된 결과에서 장소 URL을 입력하면 브라우저에 해당 kakaomap이 출력되도록 한다.
        System.out.println("kakaomap URL(장소 URL):");
        // 키보드로부터 문자열을 계속 입력받다가 exit 를 입력하면 종료하시오
        InputStream is = System.in;
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            while((line = br.readLine()) != null){ // 더 이상 읽을 데이터가 없으면 Null
                if(line.equals("exit")){
                    System.out.println("프로그램 종료");
                    System.exit(0);
                }else{
                    try {
                        URI url = new URI(line);
                        desktop.browse(url);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    System.out.println("kakaomap URL(장소 URL):");
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 반경 내의 주유소
    public void findGasStation() throws ClientProtocolException, IOException {
        String GET_URL= "https://dapi.kakao.com/v2/local/search/keyword.json?y="+this.latitude+"&x="+this.longitude+"&radius="+this.radius+"&query=주유소";

        // http client 생성
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // get 메서드와 URL 설정
        HttpGet httpGet = new HttpGet(GET_URL);

        // 헤더 추가, 정보 설정
        httpGet.addHeader("Authorization", AUTHORIZATION); //헤더 추가
        httpGet.addHeader("Content-type", "application/json"); //JSON 타입 헤더 추가

        // get 요청
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        // response의 status 코드 출력
        // System.out.println("::GET Response Status::");
        // System.out.println(httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }

        reader.close();

        // 결과 출력
        // System.out.println(response.toString());
        httpClient.close();

        JSONObject location_info = new JSONObject(response.toString());
        JSONArray location_info_array = location_info.getJSONArray("documents");

        if(location_info_array.length() == 0){ // 반경 내에 약국이 없다면
            System.out.println("반경 내에 주유소가 존재하지 않습니다.");
            System.exit(0);
        }else{
            // 상위 최대 10개 결과에 대한 추출된 데이터를 표시합니다.
            for(int i=0; i<location_info_array.length() % 10; i++){
                JSONObject address = location_info_array.getJSONObject(i);

                System.out.println("**주유소 검색 결과**");
                System.out.println("장소 URL(지도 위치): " + address.getString("place_url"));
                System.out.println("상호명: " + address.getString("place_name"));
                System.out.println("주소: " + address.getString("address_name"));
                System.out.println("전화번호: " + address.getString("phone"));
                String dist = address.getString("distance");
                System.out.println("거리(km): " + Double.parseDouble(dist) / 1000.0 + "km");
                System.out.println("-------------------------------");
            }
        }

        // 검색된 결과에서 장소 URL을 입력하면 브라우저에 해당 kakaomap이 출력되도록 한다.
        System.out.println("kakaomap URL(장소 URL):");
        InputStream is = System.in;
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            while((line = br.readLine()) != null){ // 더 이상 읽을 데이터가 없으면 Null
                // 키보드로부터 문자열을 계속 입력받다가 exit 를 입력하면 종료하시오
                if(line.equals("exit")){
                    System.out.println("프로그램 종료");
                    System.exit(0);
                }else{
                    try {
                        URI url = new URI(line);
                        desktop.browse(url);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    System.out.println("kakaomap URL(장소 URL):");
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
