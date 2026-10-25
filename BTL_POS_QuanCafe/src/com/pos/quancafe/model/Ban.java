package com.pos.quancafe.model;

public class Ban {
    private String maBan;             // "B01"
    private String viTri;             // "2-1"
    private TrangThaiBan trangThai;
    private String maHoaDon;          // null khi bĂ n trá»‘ng

    public Ban () {
        this.maHoaDon=null;
        this.trangThai=TrangThaiBan.TRONG;
    }

    public Ban (String maBan, String viTri, TrangThaiBan trangThai, String maHoaDon) {
        this.maBan=maBan;
        this.viTri=viTri;
        this.trangThai=trangThai;
        this.maHoaDon=maHoaDon;
    }

    public void setMaBan (String maBan) {this.maBan=maBan;}
    public String getMaBan () {return maBan;}
    public void setViTri (String viTri) {this.viTri=viTri;}
    public String getViTri () {return viTri;}
    public void setTrangThai (TrangThaiBan trangThai) {this.trangThai=trangThai;}
    public TrangThaiBan getTrangThai () {return trangThai;}
    public void setMaHoaDon (String maHoaDon) {this.maHoaDon=maHoaDon;}
    public String getMaHoaDon () {return maHoaDon;}


    public boolean isTrong () {
        if (maHoaDon == null || trangThai==TrangThaiBan.TRONG) {
            return true;

        } else {
            return false;
        }
    }

    public void moBan (String maHD) {
        maHoaDon=maHD;
        trangThai=TrangThaiBan.CO_KHACH;

    }

    public void traBan () {
        maHoaDon=null;
        trangThai=TrangThaiBan.TRONG;
    }


    @Override
    public String toString () {
        return maBan + " " + viTri + " " + trangThai;
    }





}
