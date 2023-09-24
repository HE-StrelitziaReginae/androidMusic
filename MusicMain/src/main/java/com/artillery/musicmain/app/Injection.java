package com.artillery.musicmain.app;

import com.artillery.musicmain.data.Repository;
import com.artillery.musicmain.data.source.HttpDataSource;
import com.artillery.musicmain.data.source.LocalDataSource;
import com.artillery.musicmain.data.source.http.HttpDataSourceImpl;
import com.artillery.musicmain.data.source.http.service.ApiService;
import com.artillery.musicmain.data.source.local.LocalDataSourceImpl;
import com.artillery.musicmain.net.RetrofitClient;

/**
 * 1
 * 注入全局的数据仓库，可以考虑使用Dagger2。（根据项目实际情况搭建，千万不要为了架构而架构）
 *
 * @author ArtilleryOrchid
 */
public class Injection {
    public static Repository provideRepository() {
        //网络API服务
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        //网络数据源
        HttpDataSource httpDataSource = HttpDataSourceImpl.getInstance(apiService);
        //本地数据源
        LocalDataSource localDataSource = LocalDataSourceImpl.getInstance();
        //两条分支组成一个数据仓库
        return Repository.getInstance(httpDataSource, localDataSource);
    }
}
