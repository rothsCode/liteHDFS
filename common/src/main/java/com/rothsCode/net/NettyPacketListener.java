package com.rothsCode.net;

import com.rothsCode.net.request.RequestWrapper;

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
