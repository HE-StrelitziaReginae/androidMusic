package com.artillery.musicmain.app;

import com.artillery.musicmain.data.MusicRepository;
import com.artillery.musicmain.data.source.MusicHttpSource;
import com.artillery.musicmain.data.source.MusicLocalSource;
import com.artillery.musicmain.data.source.MusicPlaySource;
import com.artillery.musicmain.data.source.contract.MusicPlayContractImpl;
import com.artillery.musicmain.data.source.contract.view.MusicPlayView;
import com.artillery.musicmain.data.source.http.MusicHttpSourceImpl;
import com.artillery.musicmain.data.source.http.service.ApiService;
import com.artillery.musicmain.data.source.local.MusicLocalSourceImpl;
import com.artillery.musicmain.net.RetrofitClient;

/**
 * 1
 * 注入全局的数据仓库，可以考虑使用Dagger2。（根据项目实际情况搭建，千万不要为了架构而架构）
 *
 * @author ArtilleryOrchid
 */
public class Injection {
    public static MusicRepository provideRepository() {
//        //网络API服务
//        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
//        //网络数据源
//        MusicHttpSource httpDataSource = MusicHttpSourceImpl.getInstance(apiService);
        //本地数据源
        MusicLocalSource localDataSource = MusicLocalSourceImpl.getInstance();
        //服务注册
        MusicPlaySource musicPlaySource = MusicPlayContractImpl.getInstance();
        //两条分支组成一个数据仓库
        return MusicRepository.getInstance(localDataSource, musicPlaySource);
    }
}
