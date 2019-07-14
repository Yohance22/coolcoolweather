package com.example.lenovo.coolcoolweather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {
    private int id;
    //记录省的名字
    private String provinceName;
    //记录省的代号
    private int provinceCode;
    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}
