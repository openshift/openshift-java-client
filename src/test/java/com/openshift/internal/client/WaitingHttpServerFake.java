package com.openshift.internal.client;

import com.openshift.client.fakes.HttpServerFake;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Nicolas
 * Date: 05/06/13
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public class WaitingHttpServerFake extends HttpServerFake {

    private long delay;

    @Override
    protected void write(byte[] text, OutputStream outputStream) throws IOException {

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            //Intentional ignore
            return ;
        }
    }

    public WaitingHttpServerFake(long delay){
        this.delay = delay;
    }
}
