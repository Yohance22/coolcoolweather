package com.example.lenovo.coolcoolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.coolcoolweather.db.City;
import com.example.lenovo.coolcoolweather.db.Country;
import com.example.lenovo.coolcoolweather.db.Province;
import com.example.lenovo.coolcoolweather.util.HttpUtil;
import com.example.lenovo.coolcoolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY =2;
    private ProgressDialog progressDialog;
    private TextView title_text;
    private Button back_button;
    private ListView list_view;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province>provinceList;
    private List<City>cityList;
    private List<Country>countryList;
    private City selectedCity;
    private Province selectedProvince;
    //被选中的级别
    private int currentLevel;
//获取控件的实例，初始化ArrayAdapter  并将它设置listview的适配器
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        title_text = view.findViewById(R.id.title_text);
        back_button = view.findViewById(R.id.back_button);
        list_view = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        list_view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCites();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countryList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });



        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCites();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        title_text.setText("中国");
        //将返回按钮隐藏起来，因为省级列表不能返回了
        back_button.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(com.example.lenovo.coolcoolweather.db.Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel = LEVEL_PROVINCE;

        }else {
            String address = "http://guolin.tech/api/china/";
            queryFromServer(address,"province");
        }
    }
    private void queryCites() {
        title_text.setText(selectedProvince.getProvinceName());
        back_button.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(com.example.lenovo.coolcoolweather.db.City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city :cityList){
                dataList.add(city.getCityName());

            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" +provinceCode;
            queryFromServer(address,"city");
        }

    }



    private void queryCounties() {
        title_text.setText(selectedCity.getCityName());
        back_button.setVisibility(View.VISIBLE);
        countryList = DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(com.example.lenovo.coolcoolweather.db.Country.class);
        if (countryList.size()>0){
            dataList.clear();
            for (Country country :countryList){
                dataList.add(country.getCountryName());

            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel = LEVEL_COUNTY;


        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");

        }
    }


    private void queryFromServer(String address,final String type) {
        showProgressDialog();
        //向服务器发送请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialpg();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }
//调用handleProvinceResponse（）方法来解析和处理服务器返回的数据，并存储到数据库里中
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String reponseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(reponseText);

                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(reponseText,selectedProvince.getId());

                }else if ("county".equals(type)){
                    result = Utility.handleCountryResponse(reponseText,selectedCity.getId());

                }
                //runOnUiThread（）方法从子线程切换到主线程
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialpg();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCites();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }

                        }
                    });
                }

            }

        });

    }

    private void closeProgressDialpg() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void showProgressDialog() {
        if (progressDialog !=null){
            progressDialog.dismiss();
        }
    }
}
