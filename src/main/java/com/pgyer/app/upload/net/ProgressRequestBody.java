package com.pgyer.app.upload.net;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.*;

import java.io.IOException;

public class ProgressRequestBody extends RequestBody {

    protected Listener listener;
    protected RequestBody delegate;
    protected CountingSink countingSink;

    public ProgressRequestBody(RequestBody delegate, Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return delegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);

            bytesWritten += byteCount;
            if(listener != null){
                listener.onRequestProgress(bytesWritten, contentLength());
            }
        }
    }

    public interface Listener {
        void onRequestProgress(long bytesWritten, long contentLength);
    }
}