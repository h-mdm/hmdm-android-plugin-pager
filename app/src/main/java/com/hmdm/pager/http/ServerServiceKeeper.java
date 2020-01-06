package com.hmdm.pager.http;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdm.pager.Const;
import com.hmdm.pager.SettingsHelper;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ServerServiceKeeper {
    private static ServerService serverServiceInstance;
    private static ServerService secondaryServerServiceInstance;

    public static ServerService getServerServiceInstance(Context context) {
        SettingsHelper settings = SettingsHelper.getInstance(context);
        String baseUrl = settings.getString(SettingsHelper.KEY_SERVER_HOST);
        if (baseUrl == null) {
            return null;
        }
        if ( serverServiceInstance == null ) {
            serverServiceInstance = createServerService(baseUrl);
        }

        return serverServiceInstance;
    }

    public static ServerService getSecondaryServerServiceInstance(Context context) {
        SettingsHelper settings = SettingsHelper.getInstance(context);
        String baseUrl = settings.getString(SettingsHelper.KEY_SECONDARY_SERVER_HOST);
        if (baseUrl == null) {
            return null;
        }
        if ( secondaryServerServiceInstance == null ) {
            secondaryServerServiceInstance = createServerService(baseUrl);
        }

        return secondaryServerServiceInstance;
    }

    private static ServerService createServerService( String baseUrl ) {
        return createBuilder( baseUrl ).build().create( ServerService.class );
    }

    private static Retrofit.Builder createBuilder(String baseUrl ) {
        Retrofit.Builder builder = new Retrofit.Builder();
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().
                connectTimeout( Const.CONNECTION_TIMEOUT, TimeUnit.SECONDS ).
                readTimeout( Const.CONNECTION_TIMEOUT, TimeUnit.SECONDS ).
                writeTimeout( Const.CONNECTION_TIMEOUT, TimeUnit.SECONDS );

        builder.client( clientBuilder.build() );

        builder.baseUrl( baseUrl )
                .addConverterFactory( JacksonConverterFactory.create( new ObjectMapper()) );

        return builder;
    }

}
