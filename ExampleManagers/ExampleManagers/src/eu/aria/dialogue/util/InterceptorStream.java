/**
 * Created by Alexandru G on 24/09/2015.
 *
 */

package eu.aria.dialogue.util;

import com.sun.istack.internal.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;


public class InterceptorStream extends PrintStream {

    private Listener listener;

    public InterceptorStream(OutputStream out) {
        super(out);
    }

    public InterceptorStream(OutputStream out, Listener listener) {
        super(out);
        this.listener = listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public PrintStream append(char c) {
        printInternal(c);
        return super.append(c);
    }

    @Override
    public PrintStream append(CharSequence csq) {
        printInternal(csq);
        return super.append(csq);
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        printInternal(cs.subSequence(start, end).toString());
        return super.append(csq, start, end);
    }

    @Override
    public void print(boolean b) {
        super.print(b);
        printInternal(b);
    }

    @Override
    public void print(char c) {
        super.print(c);
        printInternal(c);
    }

    @Override
    public void print(double d) {
        super.print(d);
        printInternal(d);
    }

    @Override
    public void print(float f) {
        super.print(f);
        printInternal(f);
    }

    @Override
    public void print(int i) {
        super.print(i);
        printInternal(i);
    }

    @Override
    public void print(long l) {
        super.print(l);
        printInternal(l);
    }

    @Override
    public void print(Object obj) {
        super.print(obj);
        printInternal(obj);
    }

    @Override
    public void print(@NotNull char[] s) {
        super.print(s);
        printInternal(s);
    }

    @Override
    public void print(String s) {
        super.print(s);
        printInternal(s);
    }

    @Override
    public synchronized void println() {
        super.println();
        newLine();
    }

    @Override
    public synchronized void println(boolean x) {
        super.println(x);
        newLine();
    }

    @Override
    public synchronized void println(char x) {
        super.println(x);
        newLine();
    }

    @Override
    public synchronized void println(float x) {
        super.println(x);
        newLine();
    }

    @Override
    public synchronized void println(double x) {
        super.println(x);
        newLine();
    }

    @Override
    public synchronized void println(char[] x) {
        super.println(x);
        newLine();
    }

    @Override
    public synchronized void println(String x) {
        super.println(x);
        newLine();
    }

    @Override
    public synchronized void println(Object x) {
        super.println(x);
        newLine();
    }

    @Override
    public synchronized void println(int x) {
        super.println(x);
        newLine();
    }

    @Override
    public synchronized void println(long x) {
        super.println(x);
        newLine();
    }

    private synchronized void printInternal(Object o) {
        if (listener != null) {
            listener.onData(String.valueOf(o));
        }
    }

    private void newLine() {
        if (listener != null) {
            listener.onData(System.lineSeparator());
        }
    }

    public interface Listener {
        void onData(String value);
    }
}
