package com.rothsCode.litehdfs.common.netty.listener;

import com.rothsCode.litehdfs.common.netty.request.RequestWrapper;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/415:25
 */
public interface NettyPacketListener {


  /**
   * 回调处理
   */
  void callBack(RequestWrapper requestWrapper);
}
