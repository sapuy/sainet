package xyz.leosap.multiplace.common;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by LeoSap on 28/11/2016.
 */

public class Constants {
    //Debug
    public static boolean DebugHTTP=true;

    //WS
    public static String WS_user="rest_api";
    public static String WS_pass="123456789";

    public static String default_token ="dRWCdGhOWj8-u24sPx2NTLilvqUX980jOCIUuuPKBzw";

    public static String URL_loginWS ="http://108.179.199.76/~pruebadesarrollo/restws/session/token";
    public static String URL_user ="http://108.179.199.76/~pruebadesarrollo/user";
    public static String URL_user_login ="http://108.179.199.76/~pruebadesarrollo/app_login";
    public static String URL_place ="http://108.179.199.76/~pruebadesarrollo/node";


    public static final int image_width = 500;
    public static final int image_height = 500;

    //map
    public static final LatLng latLngColombia = new LatLng(4.580689, -74.078302);
    public static final int zoom = 18;

}
