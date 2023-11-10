package com.rothsCode.litehdfs.common.netty.request;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * @author rothsCode
 * @Description:
 * @date 2021/11/410:36
 */
@Data
@Builder
public class NettyPacket {

    private byte[] body;
    private Map<String, Object> headers = new HashMap<>();

    public static NettyPacket buildPacket(byte[] body, Object msgType) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("msgType", msgType);
        headers.put("msgType", msgType);
        NettyPacket nettyPacket = NettyPacket.builder().body(body)
            .headers(headers).build();
        return nettyPacket;
    }

    //解包
    public static NettyPacket decodePacket(ByteBuf byteBuf)
        throws IOException, ClassNotFoundException {
        int headerLength = byteBuf.readInt();
        byte[] headByte = new byte[headerLength];
        byteBuf.readBytes(headByte);
        Map headMap = JSONObject.parseObject(new String(headByte), Map.class);
        int bodyLength = byteBuf.readInt();
        byte[] bodyBytes = new byte[bodyLength];
        byteBuf.readBytes(bodyBytes);
        return NettyPacket.builder()
            .headers(headMap)
            .body(bodyBytes)
            .build();

    }

    public String getSequence() {
        return (String) headers.get("sequence");
    }

    public void setSequence(String sequence) {
        headers.put("sequence", sequence);
    }

    public int getPackageType() {
        return (int) headers.get("msgType");
    }

    public void writeBuffer(ByteBuf byteBuf) {
        byte[] headByte = JSONObject.toJSONString(headers).getBytes();
        byteBuf.writeInt(headByte.length);
        byteBuf.writeBytes(headByte);
        byteBuf.writeInt(body.length);
        byteBuf.writeBytes(body);

    }


}
