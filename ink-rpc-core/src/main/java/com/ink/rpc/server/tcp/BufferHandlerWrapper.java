package com.ink.rpc.server.tcp;

import com.ink.rpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 能力增强的 BufferHandler （处理半包，粘包）
 */
public class BufferHandlerWrapper implements Handler<Buffer> {

    private final RecordParser recordParser;

    public BufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecordParser(bufferHandler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<>() {
            //size 值用于标识请求体长度，及是否已读取消息头
            int size = -1;
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (size == -1) {
                    //更新 size 值
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    //读取消息头
                    resultBuffer.appendBuffer(buffer);
                } else {
                    //读取消息体
                    resultBuffer.appendBuffer(buffer);
                    //Buffer 对象已拼接完整，执行处理
                    bufferHandler.handle(resultBuffer);
                    //为下一次读取重置
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);//重置读取长度
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        return parser;
    }

}
