package org.example;

import org.example.model.HttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try{
            while(true){
                System.out.println("위치 키워드를 입력하세요: ");
                String location_keyword = br.readLine();
                System.out.println("검색 반경을 입력하세요(1000:1km): ");
                double radius = Double.parseDouble(br.readLine());

                // 해당 위치 키워드의 위도와 경도 받아 client 인스턴스에 저장하기
                HttpClient client = new HttpClient(location_keyword, radius);
                client.saveLocationInfo(location_keyword);

                // 반경 내 약국 찾기
                client.findPharmacy();
                // 반경 내 주유소 찾기
                // client.findGasStation();
            }
        }catch(Exception e){
           e.printStackTrace();
        }
    }
}