package com.example.duolaweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duolaweather.db.City;
import com.example.duolaweather.db.District;
import com.example.duolaweather.db.Province;
import com.example.duolaweather.util.HttpUtil;
import com.example.duolaweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/2/5.
 */

public class ChooseAreaFragment extends Fragment {
    private static final String TAG="dbg";
    public static final int E_PROVINCE=0;
    public static final int E_CITY=1;
    public static final int E_DISTRICT=2;
    private ProgressDialog progressDialog;
    private TextView textTitle;
    private ListView listViewProvince;
    private ListView listViewCity;
    private ListView listViewDistrict;
    private ArrayAdapter<String> adapterProvince;
    private ArrayAdapter<String> adapterCity;
    private ArrayAdapter<String> adapterDistrict;
    private List<String> listDataProvince=new ArrayList<>();
    private List<String> listDataCity=new ArrayList<>();
    private List<String> listDataDistrict=new ArrayList<>();
    //省市县列表
    private List<Province> listProvince;
    private List<City> listCity;
    private List<District> listDistrict;
    //选中的省市县
    private Province provinceSelected;
    private City citySelected;
    private District districtSelected;
    //选中级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        textTitle=(TextView)view.findViewById(R.id.text_title);
        listViewProvince=(ListView)view.findViewById(R.id.list_view_province);
        listViewCity=(ListView)view.findViewById(R.id.list_view_city);
        listViewDistrict=(ListView)view.findViewById(R.id.list_view_district);
        adapterProvince = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,listDataProvince);
        adapterCity = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,listDataCity);
        adapterDistrict=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,listDataDistrict);
        listViewProvince.setAdapter(adapterProvince);
        listViewCity.setAdapter(adapterCity);
        listViewDistrict.setAdapter(adapterDistrict);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listViewProvince.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    provinceSelected=listProvince.get(position);
                    queryCities();
                    if(listCity.size()>0){
                        citySelected=listCity.get(0);
                        queryDistricts();
                        Log.d(TAG, "onItemClick: query districts");
                    }else {
                        listDataDistrict.clear();
                        adapterDistrict.notifyDataSetChanged();
                    }
            }
        });
        listViewCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                citySelected=listCity.get(position);
                queryDistricts();
            }
        });
        queryProvinces();
        if(listProvince.size()>0){
            provinceSelected=listProvince.get(0);
            queryCities();
            if (listCity.size()>0){
                citySelected=listCity.get(0);
                queryDistricts();
            }
        }
    }

    /*查询省*/
    private void queryProvinces() {
        listProvince= DataSupport.findAll(Province.class);
        Log.d(TAG, "queryProvinces: "+listProvince.size());
        if(listProvince.size()>0){
            listDataProvince.clear();
            for(Province province : listProvince){
                listDataProvince.add(province.getName());
            }
            adapterProvince.notifyDataSetChanged();
            listViewProvince.setSelection(0);
            currentLevel=E_PROVINCE;
        }else {
            String url="http://guolin.tech/api/china";
            queryFromServer(url,E_PROVINCE);
        }
    }

    /*查询市*/
    private void queryCities() {
        textTitle.setText(provinceSelected.getName());
        listCity=DataSupport.where("provinceId = ?",String.valueOf(provinceSelected.getId())).find(City.class);
        if(listCity.size()>0){
            listDataCity.clear();
            for(City city : listCity){
                listDataCity.add(city.getName());
            }
            adapterCity.notifyDataSetChanged();
            listViewCity.setSelection(0);
            currentLevel=E_CITY;
        }else {
            int provinceCode=provinceSelected.getCode();
            String url="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(url,E_CITY);
        }
    }
    private void queryDistricts() {
        textTitle.setText(provinceSelected.getName() + '-'  + citySelected.getName());
        listDistrict=DataSupport.where("cityId = ?",String.valueOf(citySelected.getId())).find(District.class);
        if(listDistrict.size()>0){
            listDataDistrict.clear();
            for(District district : listDistrict){
                listDataDistrict.add(district.getName());
            }
            adapterDistrict.notifyDataSetChanged();
            listViewDistrict.setSelection(0);
            currentLevel=E_DISTRICT;
        }else {
            int provinceCode=provinceSelected.getCode();
            int cityCode=citySelected.getCode();
            String url="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(url,E_DISTRICT);
        }
    }

    /*
    * 根据传入地址和类型从服务器查询省市县数据
     */
    private void queryFromServer(String url,final int type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),R.string.load_fail,Toast.LENGTH_SHORT).show();
                    }
                });//runOnUiThread
            }//onFailure

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strResponse=response.body().string();
                boolean result=false;
                if(type==E_PROVINCE){
                    result= Utility.processProvinceJson(strResponse);
                }else if(type==E_CITY){
                    result=Utility.processCityJson(strResponse,provinceSelected.getId());
                }else if(type==E_DISTRICT){
                    result=Utility.processDistrictJson(strResponse,citySelected.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type){
                                case E_PROVINCE:{
                                    queryProvinces();
                                    if(listProvince.size()>0){
                                        provinceSelected=listProvince.get(0);
                                        queryCities();
                                    }
                                    break;
                                }
                                case E_CITY:{
                                    queryCities();
                                    if(listCity.size()>0){
                                        citySelected=listCity.get(0);
                                        Log.d(TAG, "run: query districts");
                                        queryDistricts();
                                    }
                                    break;
                                }
                                case E_DISTRICT:{
                                    queryDistricts();
                                    break;
                                }
                            }//switch
                        }//run
                    });//runOnUiThread
                }//if
            }//onResponse
        });
    }

    private void closeProgressDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    /*
    * 显示加载进度
     */
    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
