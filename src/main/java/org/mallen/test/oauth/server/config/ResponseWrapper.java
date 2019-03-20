package org.mallen.test.oauth.server.config;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 * 封装Response，为了解决某些（如：tomcat）web容器中只能获取一次response的outputStream或者writer的问题
 * （对于tomcat，要么只能获取一次outputStream，要么只能获取一次writer，如果出现获取了outputStream/writer，再去获取writer/outputStream的情况，则会抛出异常：
 * java.lang.IllegalStateException: getWriter() has already been called for this response或者
 * java.lang.IllegalStateException: getOutputStream() has already been called for this response）。
 * <p>
 * 使用该类后，即可随意获取多次outputStream或者writer，以便应用内可以在多个地方操作response
 * <p>
 * Created by mallen on 4/27/17.
 */
public class ResponseWrapper extends HttpServletResponseWrapper {
    private OutputStreamWrapper outputStream;
    private PrintWriterWrapper printWriter;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response
     * @throws IllegalArgumentException if the response is null
     */
    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        try {
            OutputStream originOutputStream = response.getOutputStream();
            // tomcat底层会强制应用要么获取一次writer，要么获取一次outputStream，所以，此处新建一个PrintWriter
            PrintWriter originWriter = new PrintWriter(originOutputStream);
            outputStream = new OutputStreamWrapper(originOutputStream);
            printWriter = new PrintWriterWrapper(originWriter, outputStream.getBranch());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public PrintWriter getWriter() throws IOException {
        return this.printWriter;
    }

    public ServletOutputStream getOutputStream() {
        return this.outputStream;
    }


    public byte[] toByteArray() {
        return outputStream.toByteArray();
    }

    @Override
    public ServletResponse getResponse() {
        return this;
    }

    /**
     * 封装outputStream，在往标准的response的outputStream中写入数据的同时，写入自定义的outputStream中。
     * 其他类在使用时只提供自定义的outputStream，该outputStream提供toByteArray方法，方便查看response的内容
     */
    private class OutputStreamWrapper extends ServletOutputStream {
        private OutputStream origin;
        private ByteArrayOutputStream branch;

        public OutputStreamWrapper(OutputStream origin) {
            this.origin = origin;
            branch = new ByteArrayOutputStream();
        }

        @Override
        public synchronized void write(int b) throws IOException {
            origin.write(b);
            branch.write(b);
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            branch.flush();
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                this.branch.close();
            }
        }

        public ByteArrayOutputStream getBranch() {
            return branch;
        }

        public byte[] toByteArray() {
            return branch.toByteArray();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {

        }
    }

    /**
     * 封装PrintWriter，与OutputStreamWrapper类似，该类会同时往response的标准输出流和自定义输出流写入数据，便于应用中查看response的内容
     */
    private class PrintWriterWrapper extends PrintWriter {
        private PrintWriter branch;

        public PrintWriterWrapper(Writer out, OutputStream outputStreamBranch) {
            super(out);
            this.branch = new PrintWriter(outputStreamBranch);
        }

        public void write(char buf[], int off, int len) {
            super.write(buf, off, len);
            super.flush();
            branch.write(buf, off, len);
            branch.flush();
        }

        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
            branch.write(s, off, len);
            branch.flush();
        }

        public void write(int c) {
            super.write(c);
            super.flush();
            branch.write(c);
            branch.flush();
        }

        public void flush() {
            super.flush();
            branch.flush();
        }
    }
}
